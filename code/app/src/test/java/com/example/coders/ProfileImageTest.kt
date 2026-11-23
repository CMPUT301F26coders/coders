/**
 * Tests the pure profile-image record helpers and Base64 data-URL conversion functions.
 *
 * Outstanding issues: Android bitmap scaling is intentionally covered by instrumented behavior
 * rather than this JVM-only test class.
 */

package com.example.coders

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// The following test was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
// and remove profile images stored as Firestore data URLs", 2026-11-22.
/** Tests deterministic profile-image records and pure data-URL conversion. */
class ProfileImageTest {

    // The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
    // and remove profile images stored as Firestore data URLs", 2026-11-22.
    /** Verifies profile image IDs are deterministic and use the reserved prefix. */
    @Test
    fun profileImageId_isDeterministicAndPrefixed() {
        val profileId = "entrant-123"

        assertEquals("profile_entrant-123", ProfileImageRepo.profileImageId(profileId))
        assertEquals(
            ProfileImageRepo.profileImageId(profileId),
            ProfileImageRepo.profileImageId(profileId)
        )
        assertTrue(
            ProfileImageRepo.profileImageId(profileId)
                .startsWith(ProfileImageRepo.PROFILE_IMAGE_PREFIX)
        )
    }

    // The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
    // and remove profile images stored as Firestore data URLs", 2026-11-22.
    /** Verifies the canonical record has all profile-image fields required by Firestore. */
    @Test
    fun profileImageRecord_setsCanonicalFields() {
        val record = ProfileImageRepo.profileImageRecord(
            profileId = "entrant-123",
            dataUrl = "data:image/jpeg;base64,AQID"
        )

        assertEquals("profile_entrant-123", record.id)
        assertEquals("entrant-123", record.uploaderId)
        assertEquals("", record.campsiteId)
        assertEquals("Profile picture", record.description)
        assertEquals("data:image/jpeg;base64,AQID", record.url)
    }

    // The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
    // and remove profile images stored as Firestore data URLs", 2026-11-22.
    /** Verifies profile-image detection excludes campsite images. */
    @Test
    fun isProfileImage_distinguishesProfileAndCampsiteRecords() {
        val profileImage = ProfileImageRepo.profileImageRecord(
            profileId = "entrant-123",
            dataUrl = "data:image/jpeg;base64,AQID"
        )
        val campsiteImage = Image(
            id = "campsite-image-123",
            campsiteId = "campsite-123",
            uploaderId = "entrant-123"
        )

        assertTrue(ProfileImageRepo.isProfileImage(profileImage))
        assertFalse(ProfileImageRepo.isProfileImage(campsiteImage))
    }

    // The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
    // and remove profile images stored as Firestore data URLs", 2026-11-22.
    /** Verifies Base64 data URLs preserve each source byte exactly. */
    @Test
    fun jpegDataUrl_roundTripsBytes() {
        val bytes = byteArrayOf(0, 1, 2, 3, 127, -1, -128)

        assertArrayEquals(bytes, dataUrlToBytes(jpegBytesToDataUrl(bytes)))
    }

    // The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
    // and remove profile images stored as Firestore data URLs", 2026-11-22.
    /** Verifies non-data URLs, empty strings, and invalid Base64 are rejected. */
    @Test
    fun dataUrlToBytes_rejectsInvalidValues() {
        assertNull(dataUrlToBytes("https://example.com/photo.jpg"))
        assertNull(dataUrlToBytes(""))
        assertNull(dataUrlToBytes("data:image/jpeg;base64,not_base64!"))
    }
}
