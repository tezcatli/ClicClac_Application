package com.example.myapplication

//import android.util.Log
//import android.os.Build

import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import android.util.Log
import at.favre.lib.hkdf.HKDF
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.math.BigInteger
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.*
import java.security.KeyStore.SecretKeyEntry
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.ECKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.*
import javax.crypto.*
import javax.crypto.spec.SecretKeySpec


val certificate = """
-----BEGIN CERTIFICATE-----
MIIC9zCCAlqgAwIBAgIUfpIgqEPo9nIt8KHGrS0n4KULDDgwCgYIKoZIzj0EAwIw
gY0xCzAJBgNVBAYTAkZSMRYwFAYDVQQIDA1JbGUgRGUgRnJhbmNlMQ4wDAYDVQQH
DAVQYXJpczEOMAwGA1UECgwFQmxvcmcxEzARBgNVBAsMClBsb3JnIFVuaXQxEjAQ
BgNVBAMMCUJsb3JnIENFTzEdMBsGCSqGSIb3DQEJARYOYmxvcmdAYmxvcmcuZnIw
HhcNMjMwMzIwMDczNTA1WhcNMjQwMzE5MDczNTA1WjCBjTELMAkGA1UEBhMCRlIx
FjAUBgNVBAgMDUlsZSBEZSBGcmFuY2UxDjAMBgNVBAcMBVBhcmlzMQ4wDAYDVQQK
DAVCbG9yZzETMBEGA1UECwwKUGxvcmcgVW5pdDESMBAGA1UEAwwJQmxvcmcgQ0VP
MR0wGwYJKoZIhvcNAQkBFg5ibG9yZ0BibG9yZy5mcjCBmzAQBgcqhkjOPQIBBgUr
gQQAIwOBhgAEAKl1gMB+Z8x+kX4Xc7o6+wOw0y4aItvQzc/LUYuDV6ns68LP6s+j
Ovi/LZPqjGGkWmvVP47MYg1QhyPxbGQC4PMrAFVUxA8iEOEImxpe6guVenGEZesb
uP0a2vawSA3tUzHjSB0Q4DWfEyuOtMQX8rhNeg4oUW3dWkfxttRU+J61rYNuo1Mw
UTAdBgNVHQ4EFgQUJ2d5BNtxF5jJGt1OmvR/mr+vviYwHwYDVR0jBBgwFoAUJ2d5
BNtxF5jJGt1OmvR/mr+vviYwDwYDVR0TAQH/BAUwAwEB/zAKBggqhkjOPQQDAgOB
igAwgYYCQRU2/3/6LtZvFI86x9Rvkuu7GbcCxGwkOyV6nO6cY3i/KdmX3l8DAYE+
a1Ydexdod5mSAQsCXRXnWLsmY6MZOkP7AkE+zYs2EDE0c6+7OiMvVRpDlL3iWD/e
DVQo+PBC5J6QwP01pVAg4Lh+uwzkS2Zb6P1fM2fEhZ7tmVM88qq0MkYv9w==
-----END CERTIFICATE-----
""".trimIndent()

/*

 x = server private key (stored on server)
 X = server public key (g^x, stored on client)
 y = client private key
 Y = client public key (g^y, stored on client)

 shared_key = KDF(g^(x*y))


 s_key = session key, generated from random
 w_key = wrapping key, KDF(shared_key,"wrapping-key")

 deadline = minimum time to reach



 escrow_params = {time, Y, AES_GCM(w_key, s_key)}, HMAC(KDF(shared_key,"mac-key"), {time, Y, AES_GCM(w_key, s_key)})
 unescrow_request = {time, Y, AES_GCM(w_key, s_key)}, HMAC(KDF(shared_key,"mac-key"), {time, Y}, AES_GCM(w_key, s_key))
 unescrow_response = w_key
 recover_token = AES_GCM(r_key, s_key)

 */

object ZonedDateTimeSerializer : KSerializer<ZonedDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("DateTime", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: ZonedDateTime) = encoder.encodeString(value.format(ISO_OFFSET_DATE_TIME))
    override fun deserialize(decoder: Decoder): ZonedDateTime = ZonedDateTime.parse(decoder.decodeString())
}


object PublicKeySerializer : KSerializer<PublicKey> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("PublicKey", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: PublicKey) = encoder.encodeString(Base64.getEncoder().encodeToString(value.encoded))
    override fun deserialize(decoder: Decoder): PublicKey {
        val byteKey: ByteArray = Base64.getDecoder().decode(decoder.decodeString())
        val publicKey = X509EncodedKeySpec(byteKey)
        val kf = KeyFactory.getInstance("EC")
        return kf.generatePublic(publicKey)
    }
}


object SecretKeySerializer : KSerializer<SecretKey> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SecretKey", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: SecretKey) = encoder.encodeString(Base64.getEncoder().encodeToString(value.encoded))
    override fun deserialize(decoder: Decoder): SecretKey {
        val byteKey: ByteArray = Base64.getDecoder().decode(decoder.decodeString())
        return SecretKeySpec(byteKey, "AES_256")
    }
}

object ByteArraySerializer : KSerializer<ByteArray> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ByteArray2", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: ByteArray) = encoder.encodeString(Base64.getEncoder().encodeToString(value))
    override fun deserialize(decoder: Decoder): ByteArray {
        return Base64.getDecoder().decode(decoder.decodeString())
    }
}

object BigIntegerSerializer : KSerializer<BigInteger> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BigInteger", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: BigInteger) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): BigInteger {
        return BigInteger(decoder.decodeString())
    }
}

@Serializable
data class TokenContent(
    @Serializable(with = ZonedDateTimeSerializer::class)
    val deadline : ZonedDateTime,
    @Serializable(with = ByteArraySerializer::class)
    val signature : ByteArray,
    @Serializable(with = PublicKeySerializer::class)
    val escrow: PublicKey,
    //@Serializable(with = ByteArraySerializer::class)
    //val escrow2: ByteArray
    )

object KeySerializer : KSerializer<ByteArray> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("MacKey", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: ByteArray) = encoder.encodeString(Base64.getEncoder().encodeToString(value))
    override fun deserialize(decoder: Decoder): ByteArray = Base64.getDecoder().decode(decoder.decodeString())
}

// @Serializable(with = MacKeySerializer::class)
@Serializable
data class Token(
    @Serializable(with = KeySerializer::class)
    val mac : ByteArray,
    val escrowJson : String
)

data class TokenList(
    val tokenList : List<Token>
)

@Serializable
data class WithdrawResult(
    val status : String,
    val error: String? = null,
    @Serializable(with = KeySerializer::class)
    val wKey: ByteArray? = null
)

@Serializable
data class WithdrawResultList(
    val status: String,
    val withdrawResultList: List<WithdrawResult>
)

data class Escrow (
    val token : String,
    val wrappedKey: ByteArray
)


class EscrowCipher(private val externalScope : CoroutineScope) {

    private lateinit var x509certificate : X509Certificate
    private lateinit var androidKS : KeyStore

    public suspend fun init(url: URL) {
        androidKS = KeyStore.getInstance("AndroidKeyStore")
        androidKS.load(null, null)

        Log.e("TEST2", url.toString())
        externalScope.launch {
            var client : OkHttpClient = OkHttpClient();


            var request = Request.Builder()
                .url(url)
                .build();

            withContext(Dispatchers.IO) {

                Log.e("TEST2", url.toString())
                var response: Response = client.newCall(request).execute()
                //Log.e("HTTP2", response.body!!.string())

                if (!response.isSuccessful)
                    throw Exception("Error")

                val cf: CertificateFactory = CertificateFactory.getInstance("X.509")

                x509certificate = cf.generateCertificate(response.body!!.byteStream()) as X509Certificate

                //x509certificate.serialNumber
            }
        }.join()
    }

    public suspend fun withdraw(url: URL, escrowList : List<Escrow>) : List<SecretKey?> {

        lateinit var sKeyList : List<SecretKey?>
        externalScope.launch {


            withContext(Dispatchers.IO) {
                var client : OkHttpClient = OkHttpClient();


                val tokenList = escrowList.map {Json.decodeFromString<Token>(it.token) }

                var request = Request.Builder()
                    .url(url)
                    .post(Json.encodeToString(tokenList).toRequestBody("application/json; charset=utf-8".toMediaType()))
                    .build();

                Log.e("TEST2---->", Json.encodeToString(tokenList))

                var response: Response = client.newCall(request).execute()

                if (!response.isSuccessful)
                    throw Exception("Error")

                val result = Json.decodeFromStream<WithdrawResultList>(response.body!!.byteStream())

                val cipher = Cipher.getInstance("AES_256/ECB/NoPadding")

                sKeyList = result.withdrawResultList.mapIndexed() { idx, value ->
                    if (value.status == "ok") {
                        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(value.wKey, "AES"))
                        SecretKeySpec(cipher.doFinal(escrowList[idx].wrappedKey), "AES")
                    } else {
                        null
                    }
                }

            }
        }.join()

        return sKeyList
    }

    public fun escrow(deadline: ZonedDateTime, uuid : String) : Escrow {

        val sKeyName = "sKey-$uuid"


        val sKey = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES).run {
            init(256)
            generateKey()
        }

        val pubKey: PublicKey = x509certificate.publicKey

        if (pubKey.algorithm != "EC")
            throw Exception("Wrong certificate type")

        val params: AlgorithmParameters = AlgorithmParameters.getInstance("EC")

        params.init((pubKey as ECKey).params)

        val kpg: KeyPairGenerator = KeyPairGenerator.getInstance("EC")

        kpg.initialize(params.getParameterSpec(ECGenParameterSpec::class.java));

        val kp: KeyPair = kpg.genKeyPair()

        val ecdh: KeyAgreement = KeyAgreement.getInstance("ECDH")

        ecdh.init(kp.private)
        ecdh.doPhase(pubKey, true)

        val sharedKey = ecdh.generateSecret()!!
        Log.i("TEST", "Shared Key: "+ BigInteger(1, sharedKey).toString(16)
            .uppercase(Locale.getDefault()))

        var hkdf = HKDF.fromHmacSha256()

        var pseudoRandomKey = hkdf.extract(null as ByteArray?, sharedKey)

        var macKey = hkdf.expand(pseudoRandomKey, "mac-key".toByteArray(StandardCharsets.UTF_8), 32)


        hkdf = HKDF.fromHmacSha256()

        pseudoRandomKey = hkdf.extract(null as ByteArray?, sharedKey)

        val wrapKey = hkdf.expand(pseudoRandomKey, "wrap-key".toByteArray(StandardCharsets.UTF_8), 32)


        val cipher = Cipher.getInstance("AES_256/ECB/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(wrapKey, "AES"))

        Log.i("TEST","Wrap Key: " + Base64.getEncoder().encodeToString(wrapKey))

        val wrappedKey: ByteArray = cipher.doFinal(sKey.encoded)

        val tokenContentValue = Json.encodeToString(TokenContent(deadline, x509certificate.signature, kp.public))

        val mac: Mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(macKey, "HmacSHA256"))

        mac.update(tokenContentValue.toByteArray())

        Log.i("TEST", "Bytes to mac: " + BigInteger(1, tokenContentValue.toByteArray()).toString(16)
            .uppercase(Locale.getDefault()))

        val macResult = Token(mac.doFinal(), tokenContentValue)

        Log.i("TEST", "macResult = " + Json.encodeToString(macResult))


        androidKS.setEntry(
            "$sKeyName-enc",SecretKeyEntry(sKey),
            KeyProtection.Builder(KeyProperties.PURPOSE_ENCRYPT).run {
                setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                setRandomizedEncryptionRequired(true)
                build()
            })


        androidKS.setEntry(
            "$sKeyName-dec",SecretKeyEntry(sKey),
            KeyProtection.Builder(KeyProperties.PURPOSE_DECRYPT).run {
                setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                setRandomizedEncryptionRequired(true)
                setKeyValidityStart(Date.from(deadline.toInstant()))
                build()
            })

        // (androidKS.getEntry("$sKeyName-enc", null) as SecretKeyEntry).secretKey

        return Escrow(Json.encodeToString(macResult), wrappedKey)
    }

    public fun getsKeyEnc(uuid : String) : SecretKey {
        return (androidKS.getEntry("sKey-$uuid-enc", null) as SecretKeyEntry).secretKey
    }

    public fun getsKeyDec(uuid : String) : SecretKey {
        return (androidKS.getEntry("sKey-$uuid-dec", null) as SecretKeyEntry).secretKey
    }
}
