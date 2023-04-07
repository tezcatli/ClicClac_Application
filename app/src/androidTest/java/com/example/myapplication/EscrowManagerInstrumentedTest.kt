package com.example.myapplication


import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class EscrowManagerInstrumentedTest {

    @Test
    @kotlinx.coroutines.ExperimentalCoroutinesApi
    fun escrowManagerTest() = runTest {

        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val escrowManager = EscrowManager(appContext, this)

        escrowManager.init()



    }
}