package com.example.coders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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
    fun image_ids_autoIncrementOnCreation() {
        val img1 = Image("https://example.com/1.jpg", "Img 1")
        val img2 = Image("https://example.com/2.jpg", "Img 2")

        assertNotEquals(img1.id, img2.id)
        assertEquals(img1.id + 1, img2.id)
    }
}