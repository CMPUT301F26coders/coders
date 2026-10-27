package com.example.coders

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.util.concurrent.atomic.AtomicInteger



/**
 * Common interface for all profiles.
 *
 * @property name The name of the profile.
 * @property email The email of the profile.
 * @property phone The phone number of the profile.
 */
interface Profile {
    val name: String
    val email: String
    val phone: String
}

/**
 * Represents a user who is an Entrant.
 *
 * @property bookings A list of bookings made by the entrant.
 * @property waitlists A list of waitlists made by the entrant.
 */
data class Entrant(
    override val name: String,
    override val email: String,
    override val phone: String,
    val bookings: List<Reservation>,
    val waitlists: List<Waitlist>
) : Profile {

}

/**
 * Represents a user who is a CampOwner.
 *
 * @property campsites A list of campsites owned by the camp owner.
 * @property lotteries A list of lotteries managed by the camp owner.
 * @property reservations A list of reservations made by the camp owner.
 * @property notifications A list of notifications sent to the camp owner.
 */
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

/**
 * Represents a user who is an Admin.
 *
 * @property waitlists A list of waitlists managed by the admin.
 * @property campsites A list of campsites managed by the admin.
 * @property lotteries A list of lotteries managed by the admin.
 * @property reservations A list of reservations managed by the admin.
 * @property notificationLogs A list of notification logs managed by the admin.
 * @property messageLogs A list of message logs managed by the admin.
 */
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

/**
 * Represents a waitlist for a campsite.
 *
 * @property campsite The campsite being waited on.
 * @property entrantList The list of entrants waiting for the campsite.
 */
data class Waitlist (
    val campsite : Campsite,
    val entrantList : List<Entrant>
)

/**
 * Represents a campsite.
 *
 * @property name The name of the campsite. Necessary to make a campsite instance.
 * @property location The location of the campsite. Necessary to make a campsite instance
 * @property description A description of the campsite. Necessary to make a campsite instance
 * @property capacity The capacity of the campsite. Necessary to make a campsite instance
 * @property mapLocation The visual map location of the campsite. Necessary to make a campsite instance
 * @property campOwner The camp owner of the campsite. Necessary to make a campsite instance
 * @property rating The rating of the campsite. UnNecessary to make a campsite instance
 * @property images A list of images associated with the campsite. UnNecessary to make a campsite instance
 * @property tags A list of tags associated with the campsite. UnNecessary to make a campsite instance
 * @property waitlist A list of waitlists for the campsite. UnNecessary to make a campsite instance
 * @property reservations A list of reservations for the campsite. UnNecessary to make a campsite instance
 * @property notifications A list of notifications for the campsite. UnNecessary to make a campsite instance
 * @property qr The QR code associated with the campsite. UnNecessary to make a campsite instance
 * @property id The id of the campsite. UnNecessary to make a campsite instance
 */
data class Campsite (
    val name : String,
    val location : String,
    val description : String,
    val capacity : Int,
    val mapLocation : String,

    val campOwner: CampOwner,
    val rating: Double? = null,

    val images: List<Image> = emptyList(),
    val tags: List<String> = emptyList(),

    val waitlist : List<Waitlist> = emptyList(),
    val reservations : List<Reservation> = emptyList(),
    val notifications : List<Notification> = emptyList(),
    val qr : String = "",
    val id : Int = nextId.getAndIncrement()
) {
    companion object {
        private val nextId = AtomicInteger(1)
    }
}

/**
 * Represents the status of a campsite's lottery from a CampOwner perspective.
 *
 * @property Active The campsite's lottery is currently active.
 * @property BookingPeriod The campsite's lottery is currently in the booking period.
 * @property Drawn The campsite's lottery has been drawn.
 */
enum class CampOwnerLotteryStatus {
    Active,
    BookingPeriod,
    Drawn
}

/**
 * Represents the status of a campsite's lottery from an Entrant perspective.
 *
 * @property Waitlisted The campsite's lottery has been waitlisted.
 * @property Won The campsite's lottery has been won.
 * @property Lost The campsite's lottery has been lost.
 * @property Declined The campsite's lottery has been declined.
 */
enum class EntrantLotteryStatus {
    Waitlisted,
    Won,
    Lost,
    Declined
}

/**
 * Represents the status of a reservation.
 *
 * @property Active The reservation is currently active.
 * @property Past The reservation has already passed.
 * @property Cancelled The reservation has been cancelled.
 * @property Rejected The reservation has been rejected.
 */
enum class ReservationStatus {
    Active,
    Past,
    Cancelled,
    Rejected
}

/**
 * Represents a lottery for a campsite.
 *
 * @property campsites A list of campsites in the lottery.
 * @property date The date of the lottery.
 * @property status The status of the lottery.
 */
data class Lottery (
    val campsites : List<Campsite>,
    val date : String,
    val status : Enum<CampOwnerLotteryStatus>
)

/**
 * Represents a reservation for a campsite.
 *
 * @property campsite The campsite being reserved.
 * @property date The date of the reservation.
 * @property guestNumber The number of guests for the reservation.
 * @property status The status of the reservation.
 */
data class Reservation (
    val campsite : Campsite,
    val date : String,
    val guestNumber : Int,
    val status : Enum<ReservationStatus>
)

/**
 * Represents a notification.
 *
 * @property title The title of the notification.
 * @property content The content of the notification.
 * @property date The date of the notification.
 */
data class Notification (
    val title : String,
    val content : String,
    val date : String,
)

/**
 * Represents a message.
 *
 * @property sender The sender of the message.
 * @property content The content of the message.
 * @property date The date of the message.
 */
data class Message (
    val sender : Profile,
    val content : String,
    val date : String,
)

/**
 * Represents a chat between two profiles.
 *
 * @property messages A list of messages in the chat.
 * @property campOwner The camp owner of the chat.
 * @property entrant The entrant of the chat.
 */
data class MessageChat (
    val messages : List<Message>,
    val campOwner : CampOwner,
    val entrant : Entrant,
)

/**
 * Represents a repository of campsites.
 *
 * @property _campsites A list of campsites.
 * @property campsiteRef The reference to the campsites collection in the database.
 * @property db The database.
 */
class CampsiteRepo {
    private val db = Firebase.firestore
    private val campsiteRef = db.collection("campsites")

    private val _campsites = mutableStateListOf<Campsite>()

    /**
     * Returns all campsites currently loaded from Firestore.
     *
     * @return A list of campsites.
     */
    fun getAllCampsites(): List<Campsite> {
        return _campsites.toList()
    }

    /**
     *Gets all the camp sites for a particular owner.
     *
     * @param owner Owner of the campsite.
     * @returns a list of all campsites owned by the given owner.
     */
    fun getCampsitesByOwner(owner: CampOwner): List<Campsite> {
        return _campsites.filter {
            it.campOwner == owner
        }
    }

    /**
     * Adds a campsite to the repository.
     *
     * @param campsite The campsite to add.
     */
    fun addCampsite(campsite: Campsite) {
        campsiteRef.document(campsite.name).set(campsite)
    }

    /**
     * Updates a campsite in the repository.
     *
     * @param oldCampsite The old campsite.
     * @param updatedCampsite The updated campsite.
     */
    fun updateCampsite(oldCampsite: Campsite, updatedCampsite: Campsite) {
        if (oldCampsite.name != updatedCampsite.name) {
            campsiteRef.document(oldCampsite.name).delete()
        }
        campsiteRef.document(updatedCampsite.name).set(updatedCampsite)
    }

    /**
     * Deletes a campsite from the repository.
     *
     * @param campsite The campsite to delete.
     */
    fun deleteCampsite(campsite: Campsite) {
        campsiteRef.document(campsite.name).delete()
    }

    /**
     * Initializes the repository.
     *
     * @param campsiteRef The reference to the campsites collection in the database.
     * @param _campsites A list of campsites.
     */
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

/**
 * Represents a repository of profiles.
 *
 * @property _profiles A list of profiles.
 * @property profileRef The reference to the profiles collection in the database.
 * @property db The database.
 */
class ProfileRepo {
    private val db = Firebase.firestore
    private val profileRef = db.collection("profiles")

    private val _profiles = mutableStateListOf<Profile>()

    /**
     * Adds a profile to the repository.
     *
     * @param profile The profile to add.
     */
    fun addProfile(profile: Profile) {
        profileRef.document(profile.name).set(profile)
    }

    /**
     * Updates a profile in the repository.
     *
     * @param oldProfile The old profile.
     * @param updatedProfile The updated profile.
     */
    fun updateProfile(oldProfile: Profile, updatedProfile: Profile) {
        if (oldProfile.name != updatedProfile.name) {
            profileRef.document(oldProfile.name).delete()
        }
        profileRef.document(updatedProfile.name).set(updatedProfile)
    }

    /**
     * Deletes a profile from the repository.
     *
     * @param profile The profile to delete.
     */
    fun deleteProfile(profile: Profile) {
        profileRef.document(profile.name).delete()
    }

    /**
     * Initializes the repository.
     *
     * @param profileRef The reference to the profiles collection in the database.
     * @param _profiles A list of profiles.
     */
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

data class Image(
    val url: String,
    val description: String,
    val id : Int = nextId.getAndIncrement()
) {
    companion object {
        private val nextId = AtomicInteger(1)
    }
}

/**
 * Represents a repository of image.
 *
 * @property _image A list of image.
 * @property imageRef The reference to the image collection in the database.
 * @property db The database.
 */
class ImageRepo {
    private val db = Firebase.firestore
    private val imageRef = db.collection("image")

    private val _image = mutableStateListOf<Image>()

    /**
     * Adds a image to the repository.
     *
     * @param image The image to add.
     */
    fun addImage(image: Image) {
        imageRef.document(image.id.toString()).set(image)
    }

    /**
     * Deletes a image from the repository.
     *
     * @param image The image to delete.
     */
    fun deleteImage(image: Image) {
        imageRef.document(image.id.toString()).delete()
    }

    /**
     * Initializes the repository.
     *
     * @param imageRef The reference to the image collection in the database.
     * @param _image A list of profiles.
     */
    init{
        imageRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _image.clear()

            snapshot?.documents?.forEach { document ->
                val image = document.toObject(Image::class.java)
                if (image != null) {
                    _image.add(image)
                }
            }

        }
    }
}