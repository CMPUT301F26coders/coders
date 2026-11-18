
package com.example.coders

import org.junit.Assert.*
import org.junit.Test

class LotteryUserStoriesTest {

    @Test
    fun multipleOwnersCanShareLottery() {
        val lottery = Lottery(
            campOwnerId = "owner1",
            campsiteIds = listOf("camp1", "camp2", "camp3"),
            participatingOwnerIds = listOf("owner1", "owner2"),
            allowOtherOwners = true
        )

        assertEquals(3, lottery.campsiteIds.size)
        assertEquals(2, lottery.participatingOwnerIds.size)
        assertTrue(lottery.allowOtherOwners)
    }
}
