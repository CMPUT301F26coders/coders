package com.example.coders

import androidx.compose.runtime.mutableStateListOf
import com.example.coders.Campsite.Companion.nextId
import com.example.coders.Profile.Companion.nextProfileId
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
    val id: Int
        get() = nextProfileId.getAndIncrement()

    companion object {
        val nextProfileId = AtomicInteger(1)
    }
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
    val bookings: List<Reservation> = emptyList(),
    val waitlists: List<Waitlist> = emptyList()
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
    val campsites : List<Campsite> = emptyList(),
    val lotteries : List<Lottery> = emptyList(),
    val reservations : List<Reservation> = emptyList(),
    val notifications : List<Notification> = emptyList()
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
    val waitlists: List<Waitlist> = emptyList(),
    val campsites : List<Campsite> = emptyList(),
    val lotteries : List<Lottery> = emptyList(),
    val reservations : List<Reservation> = emptyList(),
    val notificationLogs : List<Notification> = emptyList(),
    val messageLogs : List<Message> = emptyList()
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
 * Necessary properties to make a campsite instance:
 * @property name The name of the campsite. Necessary to make a campsite instance.
 * @property location The location of the campsite. Necessary to make a campsite instance
 * @property description A description of the campsite. Necessary to make a campsite instance
 * @property capacity The capacity of the campsite. Necessary to make a campsite instance
 * @property mapLocation The visual map location of the campsite. Necessary to make a campsite instance
 * @property campOwner The camp owner of the campsite. Necessary to make a campsite instance
 *
 * Unnecessary properties to make a campsite instance:
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
    val id : Int = nextId.getAndIncrement()

) {
    companion object {
        private val nextId = AtomicInteger(1)
    }
}

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
data class Conversation (
    val messages : List<Message>,
    val campOwner : CampOwner,
    val entrant : Entrant,
    val id : Int = nextId.getAndIncrement()
) {
    companion object {
        private val nextId = AtomicInteger(1)
    }
}

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
     * Adds a campsite to the repository.
     *
     * @param campsite The campsite to add.
     */
    fun addCampsite(campsite: Campsite) {
        campsiteRef.document(campsite.id.toString()).set(campsite)
    }

    /**
     * Updates a campsite in the repository.
     *
     * @param oldCampsite The old campsite.
     * @param updatedCampsite The updated campsite.
     */
    fun updateCampsite(oldCampsite: Campsite, updatedCampsite: Campsite) {
        if (oldCampsite.id.toString() != updatedCampsite.id.toString()) {
            campsiteRef.document(oldCampsite.id.toString()).delete()
        }
        campsiteRef.document(updatedCampsite.id.toString()).set(updatedCampsite)
    }

    /**
     * Deletes a campsite from the repository.
     *
     * @param campsite The campsite to delete.
     */
    fun deleteCampsite(campsite: Campsite) {
        campsiteRef.document(campsite.id.toString()).delete()
    }

    /**
     * Initializes the repository.
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
        profileRef.document(profile.id.toString()).set(profile)
    }

    /**
     * Updates a profile in the repository.
     *
     * @param oldProfile The old profile.
     * @param updatedProfile The updated profile.
     */
    fun updateProfile(oldProfile: Profile, updatedProfile: Profile) {
        if (oldProfile.id.toString() != updatedProfile.id.toString()) {
            profileRef.document(oldProfile.id.toString()).delete()
        }
        profileRef.document(updatedProfile.id.toString()).set(updatedProfile)
    }

    /**
     * Deletes a profile from the repository.
     *
     * @param profile The profile to delete.
     */
    fun deleteProfile(profile: Profile) {
        profileRef.document(profile.id.toString()).delete()
    }

    /**
     * Initializes the repository.
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
 * @property _images A list of image.
 * @property imageRef The reference to the image collection in the database.
 * @property db The database.
 */
class ImageRepo {
    private val db = Firebase.firestore
    private val imageRef = db.collection("image")

    private val _images = mutableStateListOf<Image>()

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
     */
    init{
        imageRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _images.clear()

            snapshot?.documents?.forEach { document ->
                val image = document.toObject(Image::class.java)
                if (image != null) {
                    _images.add(image)
                }
            }

        }
    }
}

/**
 * Represents a repository of notification.
 *
 * @property _notifications A list of notification.
 * @property notificationRef The reference to the notification collection in the database.
 * @property db The database.
 */
class NotificationRepo{
    private val db = Firebase.firestore
    private val notificationRef = db.collection("notification")

    private val _notifications = mutableStateListOf<Notification>()

    /**
     * Adds a notification to the repository.
     *
     * @param notification The notification to add.
     */
    fun addNotification(notification: Notification) {
        notificationRef.document(notification.id.toString()).set(notification)
    }

    /**
     * Initializes the repository.
     */
    init{
        notificationRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _notifications.clear()

            snapshot?.documents?.forEach { document ->
                val notification = document.toObject(Notification::class.java)
                if (notification != null) {
                    _notifications.add(notification)
                }
            }

        }
    }
}

/**
 * Represents a repository of conversations.
 *
 * @property _conversations A list of conversation.
 * @property conversationRef The reference to the conversation collection in the database.
 * @property db The database.
 */
class ConversationRepo{
    private val db = Firebase.firestore
    private val conversationRef = db.collection("conversation")

    private val _conversations = mutableStateListOf<Conversation>()

    /**
     * Adds a conversation to the repository.
     *
     * @param conversation The conversation to add.
     */
    fun addConversation(conversation: Conversation) {
        conversationRef.document(conversation.id.toString()).set(conversation)
    }

    /**
     * Initializes the repository.
     *
     */
    init{
        conversationRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _conversations.clear()

            snapshot?.documents?.forEach { document ->
                val conversation = document.toObject(Conversation::class.java)
                if (conversation != null) {
                    _conversations.add(conversation)
                }
            }

        }
    }
}