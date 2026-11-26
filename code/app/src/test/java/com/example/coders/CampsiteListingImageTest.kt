/**
 * Tests the deterministic campsite-listing image record helpers.
 *
 * Outstanding issues: Firestore listeners and the system photo picker are covered by the Android
 * UI flow rather than this JVM-only test class.
 */

package com.example.coders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

// The following test was added using OpenAI, Codex, "Add campsite image uploads to owner listings", 2026-11-26.
/** Verifies one campsite listing image maps to one stable Firestore document. */
class CampsiteListingImageTest {

    // The following function was added using OpenAI, Codex, "Add campsite image uploads to owner listings", 2026-11-26.
    /** Verifies a campsite image record stores its required Firestore fields. */
    @Test
    fun campsiteImageRecord_usesCampsiteAndOwnerIds() {
        val record = CampsiteListingImageRepo.campsiteImageRecord(
            campsiteId = "site-123",
            uploaderId = "owner-123",
            dataUrl = "data:image/jpeg;base64,AQID"
        )

        assertEquals("campsite_site-123", record.id)
        assertEquals("site-123", record.campsiteId)
        assertEquals("owner-123", record.uploaderId)
        assertEquals("Campsite listing image", record.description)
        assertEquals("data:image/jpeg;base64,AQID", record.url)
        assertFalse(ProfileImageRepo.isProfileImage(record))
    }
}
