package com.example.coders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageTest {

    @Test
    fun image_creation_storesPropertiesCorrectly() {
        val image = Image(
            url = "https://example.com/campsite.jpg",
            description = "Front view of campsite"
        )

        assertEquals("https://example.com/campsite.jpg", image.url)
        assertEquals("Front view of campsite", image.description)
    }

    @Test
    fun image_idsAreUniqueAndNonEmpty() {
        val img1 = Image("https://example.com/1.jpg", "Img 1")
        val img2 = Image("https://example.com/2.jpg", "Img 2")

        assertNotEquals(img1.id, img2.id)
        assertTrue(img1.id.isNotBlank())
        assertTrue(img2.id.isNotBlank())
    }
}
