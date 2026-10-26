package com.example.coders

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

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

enum class EntrantLotteryStatus {
    Active,
    BookingPeriod,
    Drawn
}

enum class ReservationStatus {
    Active,
    Past,
    Cancelled,
    Rejected
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
    val status : Enum<ReservationStatus>
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


class CampsiteRepo {
    private val db = Firebase.firestore
    private val campsiteRef = db.collection("campsites")

    private val _campsites = mutableStateListOf<Campsite>()

    fun addCampsite(campsite: Campsite) {
        campsiteRef.document(campsite.name).set(campsite)
    }

    fun updateCampsite(oldCampsite: Campsite, updatedCampsite: Campsite) {
        if (oldCampsite.name != updatedCampsite.name) {
            campsiteRef.document(oldCampsite.name).delete()
        }
        campsiteRef.document(updatedCampsite.name).set(updatedCampsite)
    }

    fun deleteCampsite(campsite: Campsite) {
        campsiteRef.document(campsite.name).delete()
    }

    init{
        campsiteRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _campsites.clear()

            snapshot?.documents?.forEach { document ->
                val campsite = document.toObject(Campsite::class.java)
                if (campsite != null) {
                    _campsites.add(campsite)
                }
            }

        }
    }
}

class ProfileRepo {
    private val db = Firebase.firestore
    private val profileRef = db.collection("profiles")

    private val _profiles = mutableStateListOf<Profile>()

    fun addProfile(profile: Profile) {
        profileRef.document(profile.name).set(profile)
    }

    fun updateProfile(oldProfile: Profile, updatedProfile: Profile) {
        if (oldProfile.name != updatedProfile.name) {
            profileRef.document(oldProfile.name).delete()
        }
        profileRef.document(updatedProfile.name).set(updatedProfile)
    }

    fun deleteProfile(profile: Profile) {
        profileRef.document(profile.name).delete()
    }

    init{
        profileRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _profiles.clear()

            snapshot?.documents?.forEach { document ->
                val profile = document.toObject(Profile::class.java)
                if (profile != null) {
                    _profiles.add(profile)
                }
            }

        }
    }
}