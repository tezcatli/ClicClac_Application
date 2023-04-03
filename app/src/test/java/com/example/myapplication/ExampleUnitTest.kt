package com.example.myapplication

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.*
import org.junit.Test
import java.net.URL
import java.time.ZonedDateTime

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

/*
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
""
*/

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() = runTest {





        val cipherEscrow = com.example.myapplication.CipherEscrow(this)
        cipherEscrow.init(URL("http://localhost:5000/certificate"))

        cipherEscrow.escrow(ZonedDateTime.parse("2023-12-03T10:15:30+01:00[Europe/Paris]"))

        assertEquals(4, 2 + 2)
    }
}

