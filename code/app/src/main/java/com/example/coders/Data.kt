package com.example.coders

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import java.util.UUID
import com.google.android.gms.tasks.Tasks
import android.util.Log

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
    val id: String

    val type: String
}

/**
 * Represents a user who is an Entrant.
 *
 * @property reservations A list of bookings made by the entrant.
 * @property waitlists A list of waitlists made by the entrant.
 * @property notifications A list of notifications made by the entrant.
 * @property conversations Conversations associated with this entrant.
 */
data class Entrant(
    override val name: String = "",
    override val email: String = "",
    override val phone: String = "",
    val reservations: List<Reservation> = emptyList(),
    val waitlists: List<Waitlist> = emptyList(),
    val notifications: List<Notification> = emptyList(),
    override val type: String = "Entrant",
    override val id: String = UUID.randomUUID().toString(),
    val conversations: MutableList<Conversation> = mutableListOf()
) : Profile {

}

/**
 * Represents a user who is a CampOwner.
 *
 * @property campsites A list of campsites owned by the camp owner.
 * @property lotteries A list of lotteries managed by the camp owner.
 * @property reservations A list of reservations made by the camp owner.
 * @property notificationsSent A list of notifications sent by the camp owner.
 * @property notificationsReceived A list of notifications received by the camp owner.
 * @property conversations Conversations associated with this camp owner.
 */
data class CampOwner(
    override val name: String = "",
    override val email: String = "",
    override val phone: String = "",
    val campsites : List<Campsite> = emptyList(),
    val lotteries : List<Lottery> = emptyList(),
    val reservations : List<Reservation> = emptyList(),
    val notificationsSent : List<Notification> = emptyList(),
    val notificationsReceived : List<Notification> = emptyList(),
    val conversations: MutableList<Conversation> = mutableListOf(),
    override val type: String = "CampOwner",
    override val id: String = UUID.randomUUID().toString()
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
    override val name: String = "",
    override val email: String = "",
    override val phone: String = "",
    val waitlists: List<Waitlist> = emptyList(),
    val campsites : List<Campsite> = emptyList(),
    val lotteries : List<Lottery> = emptyList(),
    val reservations : List<Reservation> = emptyList(),
    val notificationLogs : List<Notification> = emptyList(),
    val messageLogs : List<Message> = emptyList(),
    override val type: String = "Admin",
    override val id: String = UUID.randomUUID().toString()
) : Profile {

}

/**
 * Represents a waitlist for a campsite.
 *
 * @property campsite The campsite being waited on.
 * @property entrantList The list of entrants waiting for the campsite.
 * @property id Stable waitlist document ID.
 * @property lotteryId Stable lottery document ID.
 * @property campsiteId Campsite document ID.
 * @property entrantIds Entrant profile document IDs.
 */
data class Waitlist (
    val campsite : Campsite = Campsite(),
    val entrantList : List<Entrant> = emptyList(),
    val id: String = UUID.randomUUID().toString(),
    val lotteryId: String = "",
    val campsiteId: String = "",
    val entrantIds: List<String> = emptyList()
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
 * @property campOwnerId The id of the camp owner of the campsite. Necessary to make a campsite instance
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

data class Campsite(
    var name: String = "",
    var location: String = "",
    var description: String = "",
    var capacity: Int = 0,
    var mapLocation: String = "",
    val campOwner: CampOwner = CampOwner(),
    var rating: Double? = null,

    var images: List<Image> = emptyList(),
    var tags: List<String> = emptyList(),

    val waitlist: List<Waitlist> = emptyList(),
    val reservations: List<Reservation> = emptyList(),
    val notifications: List<Notification> = emptyList(),
    val qr: String = "",
    val id: String = UUID.randomUUID().toString(),
    val campOwnerId: String = ""
)

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
 * @property campsites Campsite details associated with the lottery.
 * @property date Legacy display date for the lottery.
 * @property status Current owner-facing lottery status.
 * @property id Stable lottery document ID.
 * @property campOwnerId Owner profile document ID.
 * @property campsiteIds Campsite document IDs associated with the lottery.
 * @property registrationStartDate Registration period start.
 * @property registrationEndDate Registration period end.
 * @property bookingStartDate Winner booking period start.
 * @property bookingEndDate Winner booking period end.
 * @property winnerCount Number of winners to draw.
 * @property waitlistCapacity Optional maximum number of entrants.
 * @property waitlistEntrantIds Entrant profile IDs on the waitlist.
 */
data class Lottery (
    val campsites : List<Campsite> = emptyList(),
    val date : String = "",
    val status : CampOwnerLotteryStatus = CampOwnerLotteryStatus.Active,
    val id: String = UUID.randomUUID().toString(),
    val campOwnerId: String = "",
    val campsiteIds: List<String> = emptyList(),
    val registrationStartDate: String = "",
    val registrationEndDate: String = "",
    val bookingStartDate: String = "",
    val bookingEndDate: String = "",
    val winnerCount: Int = 1,
    val waitlistCapacity: Int? = null,
    val waitlistEntrantIds: List<String> = emptyList()
)

/**
 * Represents a reservation for a campsite.
 *
 * @property campsite The campsite details captured with the reservation.
 * @property dates The dates of the reservation.
 * @property guestNumber The number of guests for the reservation.
 * @property status The status of the reservation.
 * @property campOwner The camp owner of the reservation.
 * @property entrants The entrants for the reservation.
 * @property id The id of the reservation.
 * @property campsiteId The id of the campsite being reserved.
 * @property campOwnerId The id of the camp owner of the reservation.
 * @property entrantIds The ids of the entrants for the reservation.
 */
data class Reservation (
    val campsite : Campsite = Campsite(),
    val dates : String = "",
    val guestNumber : Int = 0,
    val status : ReservationStatus = ReservationStatus.Active,
    val campOwner : CampOwner = CampOwner(),
    val entrants : List<Entrant> = emptyList(),
    val id: String = UUID.randomUUID().toString(),
    val campsiteId: String = "",
    val campOwnerId: String = "",
    val entrantIds: List<String> = emptyList()
)

/**
 * Represents a notification.
 *
 * @property title The title of the notification.
 * @property content The content of the notification.
 * @property date The date of the notification.
 * @property id Stable notification document ID.
 * @property recipientId Recipient profile document ID.
 * @property senderId Sender profile document ID.
 */
data class Notification (
    val title : String = "",
    val content : String = "",
    val date : String = "",
    val id: String = UUID.randomUUID().toString(),
    val recipientId: String = "",
    val senderId: String = ""
)

/**
 * Represents a message.
 *
 * @property senderType Sender profile type stored as a Firestore-safe string.
 * @property senderId Sender profile document ID.
 * @property senderName Sender display name.
 * @property senderEmail Sender email.
 * @property senderPhone Sender phone number.
 * @property sender Reconstructed sender profile for app display.
 * @property content The content of the message.
 * @property date The date of the message.
 */
data class Message (
    val senderType: String = "Entrant",
    val senderId: String = "",
    val senderName: String = "",
    val senderEmail: String = "",
    val senderPhone: String = "",
    val content : String = "",
    val date : String = ""
) {
    /**
     * Creates a message from an app profile while storing sender fields in Firestore-safe form.
     *
     * @param sender The sender profile.
     * @param content The content of the message.
     * @param date The date of the message.
     */
    constructor(sender: Profile, content: String, date: String) : this(
        senderType = sender.type,
        senderId = sender.id,
        senderName = sender.name,
        senderEmail = sender.email,
        senderPhone = sender.phone,
        content = content,
        date = date
    )

    /**
     * Reconstructs the sender profile from the scalar fields stored with the message.
     *
     * @return The sender profile.
     */
    @get:Exclude
    val sender: Profile
        get() = when (senderType) {
            "CampOwner" -> CampOwner(name = senderName, email = senderEmail, phone = senderPhone, id = senderId)
            "Admin" -> Admin(name = senderName, email = senderEmail, phone = senderPhone, id = senderId)
            else -> Entrant(name = senderName, email = senderEmail, phone = senderPhone, id = senderId)
        }
}

/**
 * Represents a chat between two profiles.
 *
 * @property messages A list of messages in the chat.
 * @property campOwner The camp owner of the chat.
 * @property entrant The entrant of the chat.
 * @property campsite The campsite details captured with the chat.
 * @property campOwnerId Camp owner profile document ID.
 * @property entrantId Entrant profile document ID.
 * @property campsiteId Campsite document ID.
 * @property participantIds Profile document IDs participating in the chat.
 */
data class Conversation (
    val messages : List<Message> = emptyList(),
    val campOwner : CampOwner = CampOwner(),
    val entrant : Entrant = Entrant(),
    val id: String = UUID.randomUUID().toString(),
    val campsite: Campsite = Campsite(),
    val campOwnerId: String = "",
    val entrantId: String = "",
    val campsiteId: String = "",
    val participantIds: List<String> = emptyList()
)


/**
 * Decodes documents from a Firestore listener and forwards results or errors.
 *
 * @param decode Decodes a Firestore document into the requested value type.
 * @param onUpdate Called with the current data after it loads or changes.
 * @param onError Called when the Firestore listener reports an error.
 * @return The listener registration, which can be removed to stop listening.
 */
private fun <T> Query.listenForDecodedDocuments(
    decode: (DocumentSnapshot) -> T?,
    onUpdate: (List<T>) -> Unit,
    onError: (Exception) -> Unit = {}
): ListenerRegistration = addSnapshotListener { snapshot, error ->
    if (error != null) {
        onError(error)
    } else {
        onUpdate(snapshot?.documents?.mapNotNull(decode).orEmpty())
    }
}

/**
 * Checks whether a profile contains identity fields rather than placeholder defaults
 *
 * @return Whether the profile contains identity fields.
 */
private fun Profile.hasStoredIdentity(): Boolean =
    name.isNotBlank() || email.isNotBlank() || phone.isNotBlank()

/**
 * Provides campsite storage, search, and owner-scoped live queries.
 *
 * @property _campsites A list of campsites.
 * @property campsiteRef The reference to the campsites collection in the database.
 * @property db The database.
 */
class CampsiteRepo : AutoCloseable {
    private val db = Firebase.firestore
    private val campsiteRef = db.collection("campsites")

    private val _campsites = mutableStateListOf<Campsite>()

    /**
     * Filters campsites currently loaded in the compatibility cache.
     *
     * @param location The campsite location to match.
     * @param date The date to match.
     * @param guests The guest capacity to match.
     * @return The matching campsites.
     */
    fun searchCampsites(
        location: String = "",
        date: String = "",
        guests: Int = 0
    ): List<Campsite> {
        return filterCampsites(_campsites, location, date, guests)
    }

    /**
     * Returns all campsites currently loaded from Firestore.
     *
     * @return All campsites currently loaded by the repository.
     */
    fun getAllCampsites(): List<Campsite> {
        return _campsites.toList()
    }

    /**
     * Fetches the current campsite collection once.
     *
     * @param onComplete Called with the result and no error, or `null` and the error.
     */
    fun getAllCampsites(onComplete: (List<Campsite>?, Exception?) -> Unit) {
        campsiteRef.get()
            .addOnSuccessListener { snapshot ->
                onComplete(snapshot.documents.mapNotNull(::campsiteFromDocument), null)
            }
            .addOnFailureListener { error -> onComplete(null, error) }
    }

    /**
     * Fetches one campsite by its stable document ID.
     *
     * @param id The stable document ID.
     * @param onComplete Called with the result and no error, or `null` and the error.
     */
    fun getCampsiteById(id: String, onComplete: (Campsite?, Exception?) -> Unit) {
        campsiteRef.document(id).get()
            .addOnSuccessListener { document -> onComplete(campsiteFromDocument(document), null) }
            .addOnFailureListener { error -> onComplete(null, error) }
    }

    /**
     * Observes campsite changes. The returned handle should be removed when no longer needed.
     *
     * @param onUpdate Called with the current data after it loads or changes.
     * @param onError Called when the Firestore listener reports an error.
     * @return The listener registration, which can be removed to stop listening.
     */
    fun listenForCampsites(
        onUpdate: (List<Campsite>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration = campsiteRef.listenForDecodedDocuments(::campsiteFromDocument, onUpdate, onError)

    /**
     * Observes campsites registered to one owner.
     *
     * @param ownerId The owner profile ID.
     * @param onUpdate Called with the current data after it loads or changes.
     * @param onError Called when the Firestore listener reports an error.
     * @return The listener registration, which can be removed to stop listening.
     */
    fun listenForOwnerCampsites(
        ownerId: String,
        onUpdate: (List<Campsite>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration = campsiteRef.whereEqualTo("campOwnerId", ownerId)
        .listenForDecodedDocuments(::campsiteFromDocument, onUpdate, onError)

    /**
     * Gets all the camp sites for a particular owner.
     *
     * @param owner The campsite owner whose campsites should be returned.
     * @return The matching campsites.
     */
    fun getCampsitesByOwner(owner: CampOwner): List<Campsite> {
        return _campsites.filter {
            (it.campOwnerId.ifBlank { if (it.campOwner.hasStoredIdentity()) it.campOwner.id else "" }) == owner.id
        }
    }

    /**
     * Fetches an owner's campsites once using the stable owner profile ID.
     *
     * @param ownerId The owner profile ID.
     * @param onComplete Called with the result and no error, or `null` and the error.
     */
    fun getCampsitesByOwner(
        ownerId: String,
        onComplete: (List<Campsite>?, Exception?) -> Unit
    ) {
        campsiteRef.whereEqualTo("campOwnerId", ownerId).get()
            .addOnSuccessListener { snapshot ->
                onComplete(snapshot.documents.mapNotNull(::campsiteFromDocument), null)
            }
            .addOnFailureListener { error -> onComplete(null, error) }
    }

    /**
     * Adds a campsite to the repository.
     *
     * @param campsite The campsite record to save or add.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun addCampsite(campsite: Campsite, onComplete: (Exception?) -> Unit = {}) {
        val record = campsite.copy(campOwnerId = campsiteOwnerId(campsite))
        campsiteRef.document(record.id).set(record)
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    /**
     * Updates a campsite in the repository.
     *
     * @param oldCampsite The existing campsite, used to preserve its ID.
     * @param updatedCampsite The updated campsite data to save.
     */
    fun updateCampsite(oldCampsite: Campsite, updatedCampsite: Campsite) {
        updateCampsite(updatedCampsite.copy(id = oldCampsite.id))
    }

    /**
     * Updates a campsite without changing its document ID.
     *
     * @param campsite The campsite record to update.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun updateCampsite(campsite: Campsite, onComplete: (Exception?) -> Unit = {}) {
        val record = campsite.copy(
            campOwnerId = campsiteOwnerId(campsite)
        )
        campsiteRef.document(record.id).set(record)
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    /**
     * Deletes a campsite from the repository.
     *
     * @param campsite The campsite record to delete.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun deleteCampsite(campsite: Campsite, onComplete: (Exception?) -> Unit = {}) {
        deleteCampsite(campsite.id, onComplete)
    }

    /**
     * Deletes a campsite by its document ID.
     *
     * @param id The stable document ID.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun deleteCampsite(id: String, onComplete: (Exception?) -> Unit = {}) {
        campsiteRef.document(id).delete()
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    /**
     * Converts a Firestore document into a campsite using its document ID.
     *
     * @param document The Firestore document to convert.
     * @return The decoded campsite, or `null` if conversion fails.
     */
    private fun campsiteFromDocument(document: DocumentSnapshot): Campsite? =
        document.toObject(Campsite::class.java)?.let { campsite ->
            campsite.copy(
                id = document.id,
                campOwnerId = campsiteOwnerId(campsite)
            )
        }

    /**
     * Returns the campsite owner ID without using placeholder profile IDs.
     *
     * @param campsite The campsite whose owner ID should be returned.
     * @return The camp owner profile ID, or an empty string when unavailable.
     */
    private fun campsiteOwnerId(campsite: Campsite): String =
        campsite.campOwnerId.ifBlank {
            if (campsite.campOwner.hasStoredIdentity()) campsite.campOwner.id else ""
        }

    private val cacheListener = campsiteRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _campsites.clear()

            snapshot?.documents?.forEach { document ->
                val campsite = campsiteFromDocument(document)
                if (campsite != null) {
                    _campsites.add(campsite)
                }
            }

    }

    /**
     * Removes the repository's compatibility cache listener.
     */
    override fun close() = cacheListener.remove()
}

/**
 * Represents a repository of profiles.
 *
 * @property _profiles A list of profiles.
 * @property profileRef The reference to the profiles collection in the database.
 * @property db The database.
 */
class ProfileRepo : AutoCloseable {
    private val db = Firebase.firestore
    private val profileRef = db.collection("profiles")

    private val _profiles = mutableStateListOf<Profile>()


    /**
     * Filters profiles currently loaded in the compatibility cache.
     *
     * @param name The name to match.
     * @param type The profile role to match, or an empty string for all roles.
     * @return The profiles matching the supplied name and role.
     */
    fun searchProfiles(
        name: String = "",
        type: String = ""
    ): List<Profile> {
        return filterProfiles(_profiles, name, type)
    }

    /**
     * Adds a profile to the repository.
     *
     * @param profile The profile record to save or add.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun addProfile(profile: Profile, onComplete: (Exception?) -> Unit) {
        profileRef.document(profile.id)
            .set(profile)
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    /**
     * Updates a profile in the repository.
     *
     * @param oldProfile The existing profile, used to preserve its ID.
     * @param updatedProfile The updated profile data to save.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun updateProfile(oldProfile: Profile, updatedProfile: Profile, onComplete: (Exception?) -> Unit = {}) {
        val record = withProfileId(updatedProfile, oldProfile.id)
        profileRef.document(oldProfile.id).set(record)
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    /**
     * Deletes a profile from the repository.
     *
     * @param profile The profile record to delete.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun deleteProfile(profile: Profile, onComplete: (Exception?) -> Unit = {}) {
        profileRef.document(profile.id).delete()
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    /**
     * Loads a profile by its saved Firestore document ID.
     *
     * @param id The stable document ID.
     * @param onComplete Called with the result and no error, or `null` and the error.
     */
    fun getProfileById(id: String, onComplete: (Profile?, Exception?) -> Unit) {
        profileRef.document(id).get()
            .addOnSuccessListener { document ->
                onComplete(if (document.exists()) profileFromDocument(document) else null, null)
            }
            .addOnFailureListener { error -> onComplete(null, error) }
    }

    /**
     * Fetches all profiles once, for administrative browsing.
     *
     * @param onComplete Called with the result and no error, or `null` and the error.
     */
    fun getAllProfiles(onComplete: (List<Profile>?, Exception?) -> Unit) {
        profileRef.get()
            .addOnSuccessListener { snapshot ->
                onComplete(snapshot.documents.mapNotNull(::profileFromDocument), null)
            }
            .addOnFailureListener { error -> onComplete(null, error) }
    }

    /**
     * Observes profiles, optionally filtered by role.
     *
     * @param onUpdate Called with the current data after it loads or changes.
     * @param type The profile role to match, or `null` for all roles.
     * @param onError Called when the Firestore listener reports an error.
     * @return The listener registration, which can be removed to stop listening.
     */
    fun listenForProfiles(
        onUpdate: (List<Profile>) -> Unit,
        type: String? = null,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration {
        val query = if (type == null) profileRef else profileRef.whereEqualTo("type", type)
        return query.listenForDecodedDocuments(::profileFromDocument, onUpdate, onError)
    }

    /**
     * Converts a Firestore document to a profile.
     *
     * @param document The Firestore document to convert.
     * @return The decoded profile, or `null` if the document is missing or has an unknown type.
     */
    private fun profileFromDocument(document: DocumentSnapshot): Profile? {
        val profile = when (document.getString("type")) {
            "Entrant" -> document.toObject(Entrant::class.java)
            "CampOwner" -> document.toObject(CampOwner::class.java)
            "Admin" -> document.toObject(Admin::class.java)
            else -> null
        }
        return profile?.let { withProfileId(it, document.id) }
    }

    /**
     * Copies a profile with the Firestore document ID as its stable ID.
     *
     * @param profile The profile record to save or add.
     * @param id The stable document ID.
     * @return The profile with its Firestore document ID.
     */
    private fun withProfileId(profile: Profile, id: String): Profile = when (profile) {
        is Entrant -> profile.copy(id = id)
        is CampOwner -> profile.copy(id = id)
        is Admin -> profile.copy(id = id)
        else -> profile
    }

    private val cacheListener = profileRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _profiles.clear()

            snapshot?.documents?.forEach { document ->
                val profile = profileFromDocument(document)
                if (profile != null) {
                    _profiles.add(profile)
                }
            }

    }

    /**
     * Removes the repository's compatibility cache listener.
     */
    override fun close() = cacheListener.remove()
}

/**
 * Represents an image associated with a campsite.
 *
 */
data class Image(
    val url: String = "",
    val description: String = "",
    val id: String = UUID.randomUUID().toString(),
    val campsiteId: String = "",
    val uploaderId: String = ""
)

/**
 * Represents a repository of image.
 *
 * @property _images A list of image.
 * @property imageRef The reference to the image collection in the database.
 * @property db The database.
 */
class ImageRepo : AutoCloseable {
    private val db = Firebase.firestore
    private val imageRef = db.collection("image")

    private val _images = mutableStateListOf<Image>()

    /**
     * Adds a image to the repository.
     *
     * @param image The image record to save or add.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun addImage(image: Image, onComplete: (Exception?) -> Unit = {}) {
        imageRef.document(image.id).set(image)
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    /**
     * Updates an image record by its stable ID.
     *
     * @param image The image record to update.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun updateImage(image: Image, onComplete: (Exception?) -> Unit = {}) {
        imageRef.document(image.id).set(image)
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    /**
     * Deletes a image from the repository.
     *
     * @param image The image record to delete.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun deleteImage(image: Image, onComplete: (Exception?) -> Unit = {}) {
        imageRef.document(image.id).delete()
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    /**
     * Returns the images currently loaded by the compatibility listener.
     *
     * @return The images currently loaded by the repository.
     */
    fun getAllImages(): List<Image> = _images.toList()

    /**
     * Fetches one image by document ID.
     *
     * @param id The stable document ID.
     * @param onComplete Called with the result and no error, or `null` and the error.
     */
    fun getImageById(id: String, onComplete: (Image?, Exception?) -> Unit) {
        imageRef.document(id).get()
            .addOnSuccessListener { document -> onComplete(imageFromDocument(document), null) }
            .addOnFailureListener { error -> onComplete(null, error) }
    }

    /**
     * Loads images associated with a campsite once.
     *
     * @param campsiteId The campsite document ID.
     * @param onComplete Called with the result and no error, or `null` and the error.
     */
    fun getImagesForCampsite(
        campsiteId: String,
        onComplete: (List<Image>?, Exception?) -> Unit
    ) {
        imageRef.whereEqualTo("campsiteId", campsiteId).get()
            .addOnSuccessListener { snapshot ->
                onComplete(snapshot.documents.mapNotNull(::imageFromDocument), null)
            }
            .addOnFailureListener { error -> onComplete(null, error) }
    }

    /**
     * Observes all image records, for administrative browsing.
     *
     * @param onUpdate Called with the current data after it loads or changes.
     * @param onError Called when the Firestore listener reports an error.
     * @return The listener registration, which can be removed to stop listening.
     */
    fun listenForImages(
        onUpdate: (List<Image>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration = imageRef.listenForDecodedDocuments(::imageFromDocument, onUpdate, onError)

    /**
     * Observes images belonging to a campsite.
     *
     * @param campsiteId The campsite document ID.
     * @param onUpdate Called with the current data after it loads or changes.
     * @param onError Called when the Firestore listener reports an error.
     * @return The listener registration, which can be removed to stop listening.
     */
    fun listenForCampsiteImages(
        campsiteId: String,
        onUpdate: (List<Image>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration = imageRef.whereEqualTo("campsiteId", campsiteId)
        .listenForDecodedDocuments(::imageFromDocument, onUpdate, onError)

    /**
     * Converts a Firestore document into an image using its document ID.
     *
     * @param document The Firestore document to convert.
     * @return The decoded image, or `null` if conversion fails.
     */
    private fun imageFromDocument(document: DocumentSnapshot): Image? =
        document.toObject(Image::class.java)?.copy(id = document.id)

    private val cacheListener = imageRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _images.clear()

            snapshot?.documents?.forEach { document ->
                val image = imageFromDocument(document)
                if (image != null) {
                    _images.add(image)
                }
            }

    }

    /**
     * Removes the repository's compatibility cache listener.
     */
    override fun close() = cacheListener.remove()
}

/**
 * Represents a repository of notification.
 *
 * @property _notifications A list of notification.
 * @property notificationRef The reference to the notification collection in the database.
 * @property db The database.
 */
class NotificationRepo : AutoCloseable {
    private val db = Firebase.firestore
    private val notificationRef = db.collection("notification")

    private val _notifications = mutableStateListOf<Notification>()

    /**
     * Adds a notification to the repository.
     *
     * @param notification The notification record to save or add.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun addNotification(notification: Notification, onComplete: (Exception?) -> Unit = {}) {
        notificationRef.document(notification.id).set(notification)
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    /**
     * Updates a notification record by its stable ID.
     *
     * @param notification The notification record to update.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun updateNotification(notification: Notification, onComplete: (Exception?) -> Unit = {}) {
        notificationRef.document(notification.id).set(notification)
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    /**
     * Returns notifications currently loaded by the compatibility listener.
     *
     * @return The notifications currently loaded by the repository.
     */
    fun getAllNotifications(): List<Notification> = _notifications.toList()

    /**
     * Loads notifications addressed to one profile.
     *
     * @param userId The profile ID whose notifications should be returned.
     * @param onComplete Called with the result and no error, or `null` and the error.
     */
    fun getNotificationsForUser(userId: String, onComplete: (List<Notification>?, Exception?) -> Unit) {
        notificationRef.whereEqualTo("recipientId", userId).get()
            .addOnSuccessListener { snapshot ->
                onComplete(snapshot.documents.mapNotNull(::notificationFromDocument), null)
            }
            .addOnFailureListener { error -> onComplete(null, error) }
    }

    /**
     * Observes notifications addressed to one profile.
     *
     * @param userId The profile ID whose notifications should be returned.
     * @param onUpdate Called with the current data after it loads or changes.
     * @param onError Called when the Firestore listener reports an error.
     * @return The listener registration, which can be removed to stop listening.
     */
    fun listenForUserNotifications(
        userId: String,
        onUpdate: (List<Notification>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration = notificationRef.whereEqualTo("recipientId", userId)
        .listenForDecodedDocuments(::notificationFromDocument, onUpdate, onError)

    /**
     * Observes every notification for the administrator's notification log.
     *
     * @param onUpdate Called with the current data after it loads or changes.
     * @param onError Called when the Firestore listener reports an error.
     * @return The listener registration, which can be removed to stop listening.
     */
    fun listenForAllNotifications(
        onUpdate: (List<Notification>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration = notificationRef.listenForDecodedDocuments(::notificationFromDocument, onUpdate, onError)

    /**
     * Observes notifications sent by one profile, for the owner's sent log.
     *
     * @param senderId The sender profile ID.
     * @param onUpdate Called with the current data after it loads or changes.
     * @param onError Called when the Firestore listener reports an error.
     * @return The listener registration, which can be removed to stop listening.
     */
    fun listenForSentNotifications(
        senderId: String,
        onUpdate: (List<Notification>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration = notificationRef.whereEqualTo("senderId", senderId)
        .listenForDecodedDocuments(::notificationFromDocument, onUpdate, onError)

    /**
     * Deletes a notification by its stable ID.
     *
     * @param id The stable document ID.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun deleteNotification(id: String, onComplete: (Exception?) -> Unit = {}) {
        notificationRef.document(id).delete()
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    /**
     * Converts a Firestore document into a notification using its document ID.
     *
     * @param document The Firestore document to convert.
     * @return The decoded notification, or `null` if conversion fails.
     */
    private fun notificationFromDocument(document: DocumentSnapshot): Notification? =
        document.toObject(Notification::class.java)?.copy(id = document.id)

    private val cacheListener = notificationRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _notifications.clear()

            snapshot?.documents?.forEach { document ->
                val notification = notificationFromDocument(document)
                if (notification != null) {
                    _notifications.add(notification)
                }
            }

    }

    /**
     * Removes the repository's compatibility cache listener.
     */
    override fun close() = cacheListener.remove()
}

/**
 * Represents a repository of conversations.
 *
 * @property conversationRef The reference to the conversation collection in the database.
 * @property db The database.
 */
class ConversationRepo {
    private val db = Firebase.firestore
    private val conversationRef = db.collection("conversation")

    /**
     * Adds a conversation to the repository.
     *
     * @param conversation The conversation record to save or add.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun addConversation(conversation: Conversation, onComplete: (Exception?) -> Unit = {}) {
        val record = withParticipantIds(conversation)
        conversationRef.document(record.id).set(record)
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    /**
     * Replaces a conversation while preserving its stable ID.
     *
     * @param conversation The conversation record to update.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun updateConversation(conversation: Conversation, onComplete: (Exception?) -> Unit = {}) {
        val record = withParticipantIds(conversation)
        conversationRef.document(record.id).set(record)
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    /**
     * Appends a message to the conversation's message array.
     *
     * @param conversationId The conversation document ID.
     * @param message The message record to save or add.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun addMessage(
        conversationId: String,
        message: Message,
        onComplete: (Exception?) -> Unit = {}
    ) {
        conversationRef.document(conversationId)
            .update("messages", FieldValue.arrayUnion(message))
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    /**
     * Fetches one conversation by its stable document ID.
     *
     * @param id The stable document ID.
     * @param onComplete Called with the result and no error, or `null` and the error.
     */
    fun getConversationById(id: String, onComplete: (Conversation?, Exception?) -> Unit) {
        conversationRef.document(id).get()
            .addOnSuccessListener { document -> onComplete(conversationFromDocument(document), null) }
            .addOnFailureListener { error -> onComplete(null, error) }
    }

    /**
     * Fetches all conversations once, for administrative message-log review.
     *
     * @param onComplete Called with the result and no error, or `null` and the error.
     */
    fun getAllConversations(onComplete: (List<Conversation>?, Exception?) -> Unit) {
        conversationRef.get()
            .addOnSuccessListener { snapshot ->
                onComplete(snapshot.documents.mapNotNull(::conversationFromDocument), null)
            }
            .addOnFailureListener { error -> onComplete(null, error) }
    }

    /**
     * Observes conversations for a participant; the caller should remove the returned handle.
     *
     * @param userId The profile ID whose notifications or conversations should be returned.
     * @param onUpdate Called with the current data after it loads or changes.
     * @param onError Called when the Firestore listener reports an error.
     * @return The listener registration, which can be removed to stop listening.
     */
    fun listenForUserConversations(
        userId: String,
        onUpdate: (List<Conversation>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration = conversationRef.whereArrayContains("participantIds", userId)
        .listenForDecodedDocuments(::conversationFromDocument, onUpdate, onError)

    /**
     * Observes all conversations for the administrator's message log.
     *
     * @param onUpdate Called with the current data after it loads or changes.
     * @param onError Called when the Firestore listener reports an error.
     * @return The listener registration, which can be removed to stop listening.
     */
    fun listenForAllConversations(
        onUpdate: (List<Conversation>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration = conversationRef.listenForDecodedDocuments(::conversationFromDocument, onUpdate, onError)

    /**
     * Observes a single conversation for real-time message updates.
     *
     * @param conversationId The conversation document ID.
     * @param onUpdate Called with the current data after it loads or changes.
     * @param onError Called when the Firestore listener reports an error.
     * @return The listener registration, which can be removed to stop listening.
     */
    fun listenForConversation(
        conversationId: String,
        onUpdate: (Conversation?) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration = conversationRef.document(conversationId).addSnapshotListener { document, error ->
        if (error != null) onError(error) else onUpdate(document?.let(::conversationFromDocument))
    }

    /**
     * Deletes a conversation by document ID.
     *
     * @param id The stable document ID.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun deleteConversation(id: String, onComplete: (Exception?) -> Unit = {}) {
        conversationRef.document(id).delete()
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    /**
     * Fills missing relationship IDs from the conversation's participant details.
     *
     * @param conversation The conversation record to save or add.
     * @return The conversation with its relationship IDs filled.
     */
    private fun withParticipantIds(conversation: Conversation): Conversation = conversation.copy(
        campOwnerId = conversation.campOwnerId.ifBlank {
            if (conversation.campOwner.hasStoredIdentity()) conversation.campOwner.id else ""
        },
        entrantId = conversation.entrantId.ifBlank {
            if (conversation.entrant.hasStoredIdentity()) conversation.entrant.id else ""
        },
        campsiteId = conversation.campsiteId.ifBlank {
            if (conversation.campsite.name.isNotBlank()) conversation.campsite.id else ""
        },
        participantIds = (conversation.participantIds + conversation.campOwnerId + conversation.entrantId +
            (if (conversation.campOwner.hasStoredIdentity()) conversation.campOwner.id else "") +
            (if (conversation.entrant.hasStoredIdentity()) conversation.entrant.id else ""))
            .filter(String::isNotBlank)
            .distinct()
    )

    /**
     * Converts a Firestore document into a conversation using its document ID.
     *
     * @param document The Firestore document to convert.
     * @return The decoded conversation, or `null` if conversion fails.
     */
    private fun conversationFromDocument(document: DocumentSnapshot): Conversation? =
        document.toObject(Conversation::class.java)?.copy(id = document.id)


    /**
     * @param reservation The reservation to create a conversation for
     */
    fun createConversationsForReservation(
        reservation: Reservation,
    ) {
        val owner = reservation.campOwner
        val campsite = reservation.campsite

        // Use actual participants, not placeholder profiles.
        if (reservation.campOwnerId.isBlank() ||
            reservation.campsiteId.isBlank() ||
            reservation.entrants.isEmpty()
        ) {
            return
        }

        val tasks = reservation.entrants.distinctBy { it.id }.map { entrant ->

            // Stable ID prevents duplicate conversations if retried.
            val conversationId = "${reservation.id}_${entrant.id}"

            val conversation = Conversation(
                id = conversationId,
                campOwner = owner,
                entrant = entrant,
                campsite = campsite,
                messages = emptyList(),
                campOwnerId = reservation.campOwnerId,
                entrantId = entrant.id,
                campsiteId = reservation.campsiteId,
                participantIds = listOf(
                    reservation.campOwnerId,
                    entrant.id
                )
            )

            val document = conversationRef.document(conversationId)

            // Create only if it doesn't already exist.
            db.runTransaction { transaction ->
                val existing = transaction.get(document)

                if (!existing.exists()) {
                    transaction.set(document, conversation)
                }
            }
        }

        Tasks.whenAll(tasks)
            .addOnFailureListener { error ->
                Log.e(
                    "ConversationRepo",
                    "Failed to create conversations",
                    error
                )
            }
    }

}

/**
 * Provides Firebase operations for lotteries and lottery waitlists.
 *
 * @property lotteryRef The reference to the lottery collection in the database.
 * @property db The database.
 */
class LotteryRepo {
    private val db = Firebase.firestore
    private val lotteryRef = db.collection("lottery")

    /**
     * Saves a new lottery using its stable document ID.
     *
     * @param lottery The lottery record to save or add.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun addLottery(lottery: Lottery, onComplete: (Exception?) -> Unit = {}) {
        val record = withRelationshipIds(lottery)
        lotteryRef.document(record.id).set(record)
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    /**
     * Updates a lottery without changing its ID.
     *
     * @param lottery The lottery record to update.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun updateLottery(lottery: Lottery, onComplete: (Exception?) -> Unit = {}) {
        val record = withRelationshipIds(lottery)
        lotteryRef.document(record.id).set(record)
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    /**
     * Fetches one lottery by ID.
     *
     * @param id The stable document ID.
     * @param onComplete Called with the result and no error, or `null` and the error.
     */
    fun getLotteryById(id: String, onComplete: (Lottery?, Exception?) -> Unit) {
        lotteryRef.document(id).get()
            .addOnSuccessListener { document -> onComplete(lotteryFromDocument(document), null) }
            .addOnFailureListener { error -> onComplete(null, error) }
    }

    /**
     * Observes lotteries owned by one profile.
     *
     * @param ownerId The owner profile ID.
     * @param onUpdate Called with the current data after it loads or changes.
     * @param onError Called when the Firestore listener reports an error.
     * @return The listener registration, which can be removed to stop listening.
     */
    fun listenForOwnerLotteries(
        ownerId: String,
        onUpdate: (List<Lottery>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration = lotteryRef.whereEqualTo("campOwnerId", ownerId)
        .listenForDecodedDocuments(::lotteryFromDocument, onUpdate, onError)

    /**
     * Observes lotteries an entrant has joined.
     *
     * @param entrantId The entrant profile ID.
     * @param onUpdate Called with the current data after it loads or changes.
     * @param onError Called when the Firestore listener reports an error.
     * @return The listener registration, which can be removed to stop listening.
     */
    fun listenForEntrantLotteries(
        entrantId: String,
        onUpdate: (List<Lottery>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration = lotteryRef.whereArrayContains("waitlistEntrantIds", entrantId)
        .listenForDecodedDocuments(::lotteryFromDocument, onUpdate, onError)

    /**
     * Observes all lotteries, for administrative browsing.
     *
     * @param onUpdate Called with the current data after it loads or changes.
     * @param onError Called when the Firestore listener reports an error.
     * @return The listener registration, which can be removed to stop listening.
     */
    fun listenForAllLotteries(
        onUpdate: (List<Lottery>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration = lotteryRef.listenForDecodedDocuments(::lotteryFromDocument, onUpdate, onError)

    /**
     * Observes lotteries that include the campsite.
     *
     * @param campsiteId The campsite document ID.
     * @param onUpdate Called with the current data after it loads or changes.
     * @param onError Called when the Firestore listener reports an error.
     * @return The listener registration, which can be removed to stop listening.
     */
    fun listenForCampsiteLotteries(
        campsiteId: String,
        onUpdate: (List<Lottery>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration = lotteryRef.whereArrayContains("campsiteIds", campsiteId)
        .listenForDecodedDocuments(::lotteryFromDocument, onUpdate, onError)

    /**
     * Adds an entrant to a lottery waitlist, enforcing a configured capacity.
     *
     * @param lotteryId The lottery document ID.
     * @param entrantId The entrant profile ID.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun joinWaitlist(
        lotteryId: String,
        entrantId: String,
        onComplete: (Exception?) -> Unit = {}
    ) {
        val lotteryDocument = lotteryRef.document(lotteryId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(lotteryDocument)
            if (!snapshot.exists()) throw IllegalArgumentException("Lottery not found.")
            val entrants = snapshot.get("waitlistEntrantIds") as? List<*> ?: emptyList<Any>()
            val capacity = snapshot.getLong("waitlistCapacity")?.toInt()
            if (entrantId !in entrants && capacity != null && entrants.size >= capacity) {
                throw IllegalStateException("The lottery waitlist is full.")
            }
            transaction.update(lotteryDocument, "waitlistEntrantIds", FieldValue.arrayUnion(entrantId))
            true
        }.addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    /**
     * Removes an entrant from a lottery waitlist.
     *
     * @param lotteryId The lottery document ID.
     * @param entrantId The entrant profile ID.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun leaveWaitlist(lotteryId: String, entrantId: String, onComplete: (Exception?) -> Unit = {}) {
        lotteryRef.document(lotteryId)
            .update("waitlistEntrantIds", FieldValue.arrayRemove(entrantId))
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    /**
     * Deletes a lottery by ID.
     *
     * @param id The stable document ID.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun deleteLottery(id: String, onComplete: (Exception?) -> Unit = {}) {
        lotteryRef.document(id).delete()
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    /**
     * Fills missing campsite relationship IDs from embedded campsite details.
     *
     * @param lottery The lottery record to save or add.
     * @return The lottery with its campsite IDs filled.
     */
    private fun withRelationshipIds(lottery: Lottery): Lottery = lottery.copy(
        campsiteIds = (lottery.campsiteIds + lottery.campsites.map { it.id })
            .filter(String::isNotBlank)
            .distinct()
    )

    /**
     * Converts a Firestore document into a lottery using its document ID.
     *
     * @param document The Firestore document to convert.
     * @return The decoded lottery, or `null` if conversion fails.
     */
    private fun lotteryFromDocument(document: DocumentSnapshot): Lottery? =
        document.toObject(Lottery::class.java)?.copy(id = document.id)

}

/**
 * Represents a repository of reservation. Provides Firebase operations for reservations, including scoped live queries.
 *
 * @property reservationRef The reference to the reservation collection in the database.
 * @property db The database.
 */
class ReservationRepo {
    private val db = Firebase.firestore
    private val reservationRef = db.collection("reservation")

    /**
     * Saves a reservation, filling its relationship IDs from the embedded objects
     * when they were not provided.
     *
     * @param reservation The reservation record to save or add.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun addReservation(
        reservation: Reservation,
        onComplete: (Exception?) -> Unit = {}
    ) {
        val record = withRelationshipIds(reservation)

        reservationRef.document(record.id)
            .set(record)
            .addOnSuccessListener {
                ConversationRepo().createConversationsForReservation(record)
                onComplete(null)
            }
            .addOnFailureListener { error ->
                onComplete(error)
            }
    }

    /**
     * Replaces the saved reservation with the supplied version.
     *
     * @param reservation The reservation record to update.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun updateReservation(reservation: Reservation, onComplete: (Exception?) -> Unit = {}) {
        val record = withRelationshipIds(reservation)
        reservationRef.document(record.id)
            .set(record)
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    /**
     * Updates only the status field of an existing reservation.
     *
     * @param reservationId The reservation document ID.
     * @param status The new reservation status.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun updateReservationStatus(
        reservationId: String,
        status: ReservationStatus,
        onComplete: (Exception?) -> Unit = {}
    ) {
        reservationRef.document(reservationId)
            .update("status", status.name)
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    /**
     * Deletes a reservation by its stable document ID.
     *
     * @param reservationId The reservation document ID.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun deleteReservation(reservationId: String, onComplete: (Exception?) -> Unit = {}) {
        reservationRef.document(reservationId)
            .delete()
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    /**
     * Loads one reservation. A missing document returns `null` without an error.
     *
     * @param reservationId The reservation document ID.
     * @param onComplete Called with the result and no error, or `null` and the error.
     */
    fun getReservationById(
        reservationId: String,
        onComplete: (Reservation?, Exception?) -> Unit
    ) {
        reservationRef.document(reservationId)
            .get()
            .addOnSuccessListener { document ->
                onComplete(reservationFromDocument(document), null)
            }
            .addOnFailureListener { error -> onComplete(null, error) }
    }

    /**
     * Fetches every reservation once, for administrative browsing.
     *
     * @param onComplete Called with the result and no error, or `null` and the error.
     */
    fun getAllReservations(onComplete: (List<Reservation>?, Exception?) -> Unit) {
        reservationRef.get()
            .addOnSuccessListener { snapshot ->
                onComplete(snapshot.documents.mapNotNull(::reservationFromDocument), null)
            }
            .addOnFailureListener { error -> onComplete(null, error) }
    }

    /**
     * Observes all reservations, for administrative browsing.
     *
     * @param onUpdate Called with the current data after it loads or changes.
     * @param onError Called when the Firestore listener reports an error.
     * @return The listener registration, which can be removed to stop listening.
     */
    fun listenForAllReservations(
        onUpdate: (List<Reservation>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration = reservationRef.listenForDecodedDocuments(::reservationFromDocument, onUpdate, onError)

    /**
     * Listens only to reservations associated with the given entrant.
     *
     * @param entrantId The entrant profile ID.
     * @param onUpdate Called with the current data after it loads or changes.
     * @param onError Called when the Firestore listener reports an error.
     * @return The listener registration, which can be removed to stop listening.
     */
    fun listenForEntrantReservations(
        entrantId: String,
        onUpdate: (List<Reservation>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration = reservationRef.whereArrayContains("entrantIds", entrantId)
        .listenForDecodedDocuments(::reservationFromDocument, onUpdate, onError)

    /**
     * Listens only to reservations associated with the given camp owner.
     *
     * @param campOwnerId The camp owner profile ID.
     * @param onUpdate Called with the current data after it loads or changes.
     * @param onError Called when the Firestore listener reports an error.
     * @return The listener registration, which can be removed to stop listening.
     */
    fun listenForOwnerReservations(
        campOwnerId: String,
        onUpdate: (List<Reservation>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration = reservationRef.whereEqualTo("campOwnerId", campOwnerId)
        .listenForDecodedDocuments(::reservationFromDocument, onUpdate, onError)

    /**
     * Listens only to reservations associated with the given campsite.
     *
     * @param campsiteId The campsite document ID.
     * @param onUpdate Called with the current data after it loads or changes.
     * @param onError Called when the Firestore listener reports an error.
     * @return The listener registration, which can be removed to stop listening.
     */
    fun listenForCampsiteReservations(
        campsiteId: String,
        onUpdate: (List<Reservation>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration = reservationRef.whereEqualTo("campsiteId", campsiteId)
        .listenForDecodedDocuments(::reservationFromDocument, onUpdate, onError)

    /**
     * Fills missing relationship IDs from the reservation's associated records.
     *
     * @param reservation The reservation record to save or add.
     * @return The reservation with its relationship IDs filled.
     */
    private fun withRelationshipIds(reservation: Reservation): Reservation = reservation.copy(
        campsiteId = reservation.campsiteId.ifBlank {
            if (reservation.campsite.name.isNotBlank()) reservation.campsite.id else ""
        },
        campOwnerId = reservation.campOwnerId.ifBlank {
            when {
                reservation.campOwner.hasStoredIdentity() -> reservation.campOwner.id
                reservation.campsite.campOwner.hasStoredIdentity() -> reservation.campsite.campOwner.id
                else -> ""
            }
        },
        entrantIds = (reservation.entrantIds + reservation.entrants.map { it.id })
            .filter(String::isNotBlank)
            .distinct()
    )

    /**
     * Converts a Firestore document into a reservation using its document ID.
     *
     * @param document The Firestore document to convert.
     * @return The decoded reservation, or `null` if conversion fails.
     */
    private fun reservationFromDocument(document: DocumentSnapshot): Reservation? =
        document.toObject(Reservation::class.java)?.copy(id = document.id)
}
