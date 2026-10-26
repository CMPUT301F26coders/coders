package com.example.coders

import android.icu.number.Scale.none
import androidx.compose.animation.EnterTransition.Companion.None

//
interface Profile {
    val name: String
    val email: String
    val phone: String
}

data class Entrant(
    override val name: String,
    override val email: String,
    override val phone: String,
    val bookings: List<Reservation>,
    val waitlists: List<Waitlist>
) : Profile {

}

data class CampOwner(
    override val name: String,
    override val email: String,
    override val phone: String,
    val campsites : List<Campsite>,
    val lotteries : List<Lottery>,
    val reservations : List<Reservation>,
    val notifications : List<Notification>
) : Profile {

}

data class Admin (
    override val name: String,
    override val email: String,
    override val phone: String,
    val waitlists: List<Waitlist>,
    val campsites : List<Campsite>,
    val lotteries : List<Lottery>,
    val reservations : List<Reservation>,
    val notificationLogs : List<Notification>,
    val messageLogs : List<Message>
) : Profile {

}

data class Waitlist (
    val campsite : Campsite,
    val entrant_list : List<Entrant>
)

data class Campsite (
    val name : String,
    val location : String,
    val description : String,
    val capacity : Int,
    val mapLocation : String,

    val campOwner: CampOwner,
    val rating: Double,

    val images: List<String> = emptyList(),
    val tags: List<String> = emptyList(),

    val waitlist : List<Waitlist> = emptyList(),
    val reservations : List<Reservation> = emptyList(),
    val notifications : List<Notification> = emptyList(),
    val qr : String = "",

)

enum class CampOwnerLotteryStatus {
    Active,
    BookingPeriod,
    Drawn
}
data class Lottery (
    val campsites : List<Campsite>,
    val date : String,
    val status : Enum<CampOwnerLotteryStatus>
)
data class Reservation (
    val campsite : Campsite,
    val date : String,
    val guestNumber : Int,
    val status : String
)
data class Notification (
    val title : String,
    val content : String,
    val date : String,
)
data class Message (
    val sender : Profile,
    val content : String,
    val date : String
)

data class MessageChat (
    val messages : List<Message>,
    val campOwner : CampOwner,
    val entrant : Entrant,
)