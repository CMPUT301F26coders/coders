package com.example.coders

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule

/** Wraps an activity rule so each UI test starts and ends without a saved profile ID. */
fun freshSessionRule(activityRule: TestRule): TestRule {
    val resetSession = object : ExternalResource() {
        private fun clearSavedId() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            runBlocking { AuthManager(context).clearUserId() }
        }

        override fun before() = clearSavedId()

        override fun after() = clearSavedId()
    }

    return RuleChain.outerRule(resetSession).around(activityRule)
}
