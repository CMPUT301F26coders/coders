/**
 * Contains unit tests for the application's Image model.
 *
 * Tests that images correctly store their URL and description
 * when created. Also verifies that each image receives a
 * unique and non-empty ID.
 *
 * Outstanding issues: None known.
 */

package com.example.coders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contains unit tests for the Image class.
 *
 * These tests verify that:
 * - Images store their URL and description correctly.
 * - Each image receives a unique and non-empty ID.
 */
class ImageTest {

    /**
     * Tests that an image stores its properties correctly.
     *
     * Creates an image with a URL and description.
     * Verifies that:
     * - The image URL matches the provided URL.
     * - The image description matches the provided description.
     */
    @Test
    fun image_creation_storesPropertiesCorrectly() {
        val image = Image(
            url = "https://example.com/campsite.jpg",
            description = "Front view of campsite"
        )

        assertEquals("https://example.com/campsite.jpg", image.url)
        assertEquals("Front view of campsite", image.description)
    }

    /**
     * Tests that different images receive unique and non-empty IDs.
     *
     * Creates two images and verifies that:
     * - Their generated IDs are different.
     * - Neither image has a blank ID.
     *
     * This helps ensure each image can be identified individually.
     */
    @Test
    fun image_idsAreUniqueAndNonEmpty() {
        val img1 = Image("https://example.com/1.jpg", "Img 1")
        val img2 = Image("https://example.com/2.jpg", "Img 2")

        assertNotEquals(img1.id, img2.id)
        assertTrue(img1.id.isNotBlank())
        assertTrue(img2.id.isNotBlank())
    }
}
