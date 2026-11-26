/**
 * Tests pure campsite-registration validation, tag, image-record, and model-building helpers.
 *
 * Outstanding issues: Firestore batch behavior is verified through the production repository
 * implementation and connected instrumentation tests rather than JVM-only tests.
 */

package com.example.coders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// The following test was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
/** Tests the deterministic, platform-independent campsite-registration helpers. */
class CampsiteRegistrationTest {

    private val owner = Profile(name = "Owner", id = "owner-123")

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Verifies that no saved account is rejected without a saved-account flag. */
    @Test
    fun registrationAccess_withoutSavedId_isRejectedWithoutAccount() {
        assertEquals(
            CampsiteRegistrationAccess.Rejected(false),
            campsiteRegistrationAccess(null, owner)
        )
        assertEquals(
            CampsiteRegistrationAccess.Rejected(false),
            campsiteRegistrationAccess("   ", owner)
        )
    }

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Verifies saved IDs without a campsite-owner profile are rejected. */
    @Test
    fun registrationAccess_withoutCampOwner_isRejectedWithAccount() {
        assertEquals(
            CampsiteRegistrationAccess.Rejected(true),
            campsiteRegistrationAccess("saved", null)
        )
        assertEquals(
            CampsiteRegistrationAccess.Rejected(true),
            campsiteRegistrationAccess("saved", Profile(id = "entrant-123"))
        )
        assertEquals(
            CampsiteRegistrationAccess.Rejected(true),
            campsiteRegistrationAccess("saved", Profile(id = "admin-123"))
        )
    }

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Verifies a saved campsite owner receives the exact allowed owner. */
    @Test
    fun registrationAccess_withCampOwner_isAllowed() {
        assertEquals(
            CampsiteRegistrationAccess.Allowed(owner),
            campsiteRegistrationAccess("saved", owner)
        )
    }

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Verifies all complete, valid required fields pass validation. */
    @Test
    fun missingFields_withValidInput_isEmpty() {
        assertTrue(
            missingCampsiteFields("Name", "Location", "Amenities", " 4 ", 53.5, -113.5)
                .isEmpty()
        )
    }

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Verifies each individual required field is reported with whitespace treated as missing. */
    @Test
    fun missingFields_reportsEachFieldIndividually() {
        val valid = arrayOf("Name", "Location", "Amenities", "4")
        CampsiteRequiredField.entries.forEachIndexed { index, expected ->
            val values = valid.copyOf()
            val latitude = if (expected == CampsiteRequiredField.MAP_LOCATION) null else 53.5
            if (index < values.size) values[index] = "   "
            assertEquals(
                listOf(expected),
                missingCampsiteFields(
                    values[0], values[1], values[2], values[3], latitude, -113.5
                )
            )
        }
        assertEquals(
            listOf(CampsiteRequiredField.MAP_LOCATION),
            missingCampsiteFields("Name", "Location", "Amenities", "4", 53.5, null)
        )
    }

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Verifies invalid occupancy values are consistently reported as missing. */
    @Test
    fun missingFields_invalidOccupancy_isReported() {
        listOf("abc", "0", "-2", "2.5", "").forEach { occupancy ->
            assertEquals(
                listOf(CampsiteRequiredField.OCCUPANCY),
                missingCampsiteFields("Name", "Location", "Amenities", occupancy, 53.5, -113.5)
            )
        }
        assertEquals(4, parseCampsiteOccupancy(" 4 "))
    }

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Verifies all missing data is listed in required-field enum order. */
    @Test
    fun missingFields_allMissing_areInEnumOrder() {
        assertEquals(
            CampsiteRequiredField.entries.toList(),
            missingCampsiteFields(" ", " ", " ", " ", null, null)
        )
    }

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Verifies tags are normalized, bounded, distinct, and inserted in order. */
    @Test
    fun campsiteTags_normalizeAndValidate() {
        assertEquals("lake front", normalizeCampsiteTag("  lake   front  "))
        assertEquals(CampsiteTagResult.Blank, addCampsiteTag(emptyList(), " \t "))
        assertEquals(
            CampsiteTagResult.TooLong,
            addCampsiteTag(emptyList(), "a".repeat(MAX_CAMPSITE_TAG_LENGTH + 1))
        )
        assertEquals(
            CampsiteTagResult.Added(listOf("a".repeat(MAX_CAMPSITE_TAG_LENGTH))),
            addCampsiteTag(emptyList(), "a".repeat(MAX_CAMPSITE_TAG_LENGTH))
        )
        assertEquals(
            CampsiteTagResult.Duplicate,
            addCampsiteTag(listOf("Lakefront"), " lakefront ")
        )
        val tenTags = (1..MAX_CAMPSITE_TAGS).map { "tag$it" }
        assertEquals(CampsiteTagResult.LimitReached, addCampsiteTag(tenTags, "tag11"))
        assertEquals(
            CampsiteTagResult.Added(listOf("first", "second")),
            addCampsiteTag(listOf("first"), "second")
        )
    }

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Verifies campsite image records use a distinct deterministic ID and complete metadata. */
    @Test
    fun campsiteImageRecord_setsCanonicalFieldsAndIsNotProfileImage() {
        val record = campsiteImageRecord("site-123", "owner-123", "data:image/jpeg;base64,AQID")

        assertEquals("campsite_site-123", record.id)
        assertEquals("site-123", record.campsiteId)
        assertEquals("owner-123", record.uploaderId)
        assertEquals("data:image/jpeg;base64,AQID", record.url)
        assertEquals("Campsite image", record.description)
        assertFalse(ProfileImageRepo.isProfileImage(record))
    }

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Verifies registration model construction trims fields and appends non-blank rules only. */
    @Test
    fun buildCampsiteForRegistration_normalizesFieldsAndRules() {
        val withRules = buildCampsiteForRegistration(
            owner = owner,
            name = " Site ",
            location = " Lake ",
            description = " Amenities ",
            rules = " No fires ",
            capacity = 4,
            latitude = 53.5,
            longitude = -113.5,
            tags = listOf("lakefront")
        )
        val withoutRules = buildCampsiteForRegistration(
            owner, "Site", "Lake", "Amenities", "  ", 4, 53.5, -113.5, emptyList()
        )

        assertEquals("Site", withRules.name)
        assertEquals("Lake", withRules.location)
        assertEquals("Amenities\n\nRules: No fires", withRules.description)
        assertEquals("53.5,-113.5", withRules.mapLocation)
        assertEquals(owner, withRules.campOwner)
        assertEquals(owner.id, withRules.campOwnerId)
        assertEquals(listOf("lakefront"), withRules.tags)
        assertTrue(withRules.images.isEmpty())
        assertEquals("Amenities", withoutRules.description)
    }
}
