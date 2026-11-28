/**
 * Defines the application's data models and Firebase Firestore repositories.
 *
 * Contains models for profiles, campsites, reservations, lotteries,
 * waitlists, notifications, messages, and conversations.
 *
 * Implements the repository pattern to manage Firestore operations,
 * including creating, retrieving, updating, deleting, and observing records.
 *
 * Outstanding issues: None known.
 */

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
import java.time.LocalDate

data class Profile(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val reservations: List<Reservation> = emptyList(),
    val waitlists: List<Waitlist> = emptyList(),
    val campsites: List<Campsite> = emptyList(),
    val lotteries: List<Lottery> = emptyList(),
    val notifications: List<Notification> = emptyList(),
    val notificationsSent: List<Notification> = emptyList(),
    val conversations: MutableList<Conversation> = mutableListOf(),
    val notificationLogs: List<Notification> = emptyList(),
    val messageLogs: List<Message> = emptyList(),
    val id: String = UUID.randomUUID().toString(),
    val type: String = "User"
) {
    @get:Exclude
    val role: UserRole //basically makes it so the pre-existing profiles that used "Entrant" or "Campsite Owner" will still function. This automatically makes them NORMAL users
        get() = if (type.equals("Admin", ignoreCase = true)) {
            UserRole.ADMIN
        } else {
            UserRole.NORMAL
        }
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
    val entrantList : List<Profile> = emptyList(),
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
    val campOwner: Profile = Profile(),
    var rating: Double? = null,

    var images: List<Image> = emptyList(),
    var tags: List<String> = emptyList(),

    val waitlist: List<Waitlist> = emptyList(),
    val reservations: List<Reservation> = emptyList(),
    val notifications: List<Notification> = emptyList(),
    val qr: String = "",
    val id: String = UUID.randomUUID().toString(),
    val campOwnerId: String = "",
    /** Sum of all reservation ratings, used to calculate [rating] without reading every reservation. */
    val ratingTotal: Int = 0,
    /** Number of reservations that have submitted a rating. */
    val ratingCount: Int = 0
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
 * @property Pending The reservation is a lottery offer awaiting the entrant's decision.
 * @property Active The reservation is currently active.
 * @property Past The reservation has already passed.
 * @property Cancelled The reservation has been cancelled.
 * @property Rejected The reservation has been rejected.
 */
enum class ReservationStatus {
    Active,
    Past,
    Cancelled,
    Rejected,
    Pending
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
    val waitlistEntrantIds: List<String> = emptyList(),
    val participatingOwnerIds: List<String> = emptyList(),
    val invitedOwnerIds: List<String> = emptyList(),
    val allowOtherOwners: Boolean = false,
    val availabilityStartDate: String = "",
    val availabilityEndDate: String = "",

    // Stores the IDs of entrants selected as winners.
    val winnerEntrantIds: List<String> = emptyList(),

    // True once the lottery draw is completed.
    val drawCompleted: Boolean = false,

    // Stores the date each entrant joined the waitlist.
    val waitlistJoinedDates: Map<String, String> = emptyMap()

)

// The following function was added using OpenAI, Codex, "Implement US 01.01.06: allow an
// entrant to join an active lottery from campsite details", 2026-11-27.
/**
 * Returns whether entrants can currently register for this lottery.
 *
 * A lottery is open only while it has [CampOwnerLotteryStatus.Active] status, has not been
 * drawn, and the supplied date is within its inclusive registration period. Invalid or missing
 * dates are treated as closed so older incomplete records cannot accept entries.
 */
fun Lottery.isRegistrationOpen(today: LocalDate = LocalDate.now()): Boolean {
    if (status != CampOwnerLotteryStatus.Active || drawCompleted) {
        return false
    }

    val registrationStart = runCatching {
        LocalDate.parse(registrationStartDate)
    }.getOrNull() ?: return false
    val registrationEnd = runCatching {
        LocalDate.parse(registrationEndDate)
    }.getOrNull() ?: return false

    return !today.isBefore(registrationStart) && !today.isAfter(registrationEnd)
}

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
    val campOwner : Profile = Profile(),
    val entrants : List<Profile> = emptyList(),
    val id: String = UUID.randomUUID().toString(),
    val campsiteId: String = "",
    val campOwnerId: String = "",
    val entrantIds: List<String> = emptyList(),
    val lotteryId: String = "",
    val startDate: String = "",
    val endDate: String = ""

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
    val senderId: String = "",
    val sendGroupId: String = ""
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
    val senderType: String = "User",
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
        get() = Profile(
            name = senderName,
            email = senderEmail,
            phone = senderPhone,
            id = senderId,
            type = senderType
        )
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
 * @property id The unique identifier of the conversation.
 */
data class Conversation (
    val messages : List<Message> = emptyList(),
    val campOwner : Profile = Profile(),
    val entrant : Profile = Profile(),
    val id: String = UUID.randomUUID().toString(),
    val campsite: Campsite = Campsite(),
    val campOwnerId: String = "",
    val entrantId: String = "",
    val campsiteId: String = "",
    val participantIds: List<String> = emptyList()
)

/**
 * Represents a rating submitted for a past reservation.
 *
 * @property reservationId ID of the associated reservation.
 * @property entrantId ID of the entrant who submitted the rating.
 * @property campsiteId ID of the campsite being rated.
 * @property score The score the entrant gave the reservation.
 */
data class ReservationRating(
    val reservationId: String = "",
    val entrantId: String = "",
    val campsiteId: String = "",
    val score: Int = 0
)

/**
 * Represents a written review submitted for a past reservation.
 *
 * @property reservationId ID of the associated reservation.
 * @property entrantId ID of the entrant who submitted the review.
 * @property campsiteId ID of the campsite being reviewed.
 * @property content The written review.
 */
data class ReservationReview(
    val reservationId: String = "",
    val entrantId: String = "",
    val campsiteId: String = "",
    val content: String = ""
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
    fun getCampsitesByOwner(owner: Profile): List<Campsite> {
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

        val record = campsite.copy(
            campOwnerId = campsiteOwnerId(campsite),
            qr = campsite.qr.ifBlank { "coders://campsite/${campsite.id}" }
        )
        campsiteRef.document(record.id).set(record)
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    // The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /**
     * Atomically registers a campsite and, when supplied, its one optional image record.
     *
     * Keeping encoded image data in the `images` collection prevents it from approaching the
     * Firestore document-size limit for the campsite itself.
     *
     * @param campsite The campsite record to register.
     * @param imageDataUrl The optional bounded image data URL selected by the owner.
     * @param onComplete Called once with `null` on success or the write error on failure.
     */
    fun registerCampsite(
        campsite: Campsite,
        imageDataUrl: String? = null,
        onComplete: (Exception?) -> Unit = {}
    ) {
        val record = campsite.copy(
            campOwnerId = campsiteOwnerId(campsite),
            images = emptyList()
        )
        val batch = db.batch()
        batch.set(campsiteRef.document(record.id), record)
        if (imageDataUrl != null) {
            val image = campsiteImageRecord(record.id, record.campOwnerId, imageDataUrl)
            batch.set(db.collection("images").document(image.id), image)
        }
        batch.commit()
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
        campsiteRef.document(campsite.id).update(
            mapOf(
                "name" to campsite.name,
                "location" to campsite.location,
                "description" to campsite.description,
                "capacity" to campsite.capacity,
                "mapLocation" to campsite.mapLocation,
                "tags" to campsite.tags
            )
        )
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
    fun campsiteOwnerId(campsite: Campsite): String =
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
    private val reservationRef = db.collection("reservation")

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

    // The following function was added using OpenAI, Codex, "Implement US 01.04.03: create a
    // pending reservation offer in a lottery winner's profile", 2026-11-06.
    /**
     * Creates a lottery offer for the winner. The supplied reservation is stored in both the
     * winner's profile history and the reservation collection as [ReservationStatus.Pending],
     * because a lottery win is not an accepted reservation yet. Both writes occur in one
     * transaction, and relationships are represented by their document IDs.
     *
     * @param winnerId The entrant profile document ID selected by the lottery draw.
     * @param reservation The reservation details offered to that entrant.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun addPendingLotteryOffer(
        winnerId: String,
        reservation: Reservation,
        onComplete: (Exception?) -> Unit = {}
    ) {
        require(winnerId.isNotBlank()) { "Lottery winner ID is required." }
        require(reservation.id.isNotBlank()) { "Reservation ID is required." }
        require(reservation.campsiteId.isNotBlank()) { "Campsite ID is required." }
        require(reservation.campOwnerId.isNotBlank()) { "Camp owner ID is required." }

        val winnerDocument = profileRef.document(winnerId)
        val reservationDocument = reservationRef.document(reservation.id)
        val notification = Notification(
            title = "Lottery Won!",
            content = "You won the campsite lottery. Review your pending reservation offer.",
            date = java.time.LocalDate.now().toString(),
            id = reservation.lotteryId.takeIf(String::isNotBlank)?.let { lotteryId ->
                UUID.nameUUIDFromBytes("lottery-result:$lotteryId:$winnerId".toByteArray()).toString()
            } ?: "lottery-win-${reservation.id}",
            recipientId = winnerId,
            senderId = reservation.campOwnerId
        )
        val notificationDocument = db.collection("notification").document(notification.id)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(winnerDocument)
            val winner = profileFromDocument(snapshot)
                ?: throw IllegalArgumentException("Lottery winner profile was not found.")

            val pendingOffer = reservation.copy(
                status = ReservationStatus.Pending,
                entrantIds = (reservation.entrantIds + winner.id)
                    .filter(String::isNotBlank)
                    .distinct()
            )
            val updatedWinner = winner.copy(
                reservations = winner.reservations
                    .filterNot { it.id == pendingOffer.id } + pendingOffer
            )

            transaction.update(
                winnerDocument,
                "reservations",
                updatedWinner.reservations.map(::reservationStorageRecord)
            )
            transaction.set(reservationDocument, reservationStorageRecord(pendingOffer))
            transaction.set(notificationDocument, notification)
        }.addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    /** Serializes reservation relationships by ID rather than embedded Firestore objects. */
    private fun reservationStorageRecord(reservation: Reservation): Map<String, Any> = buildMap {
        put("id", reservation.id)
        put("campsiteId", reservation.campsiteId)
        put("campOwnerId", reservation.campOwnerId)
        put("entrantIds", reservation.entrantIds)
        put("dates", reservation.dates)
        put("guestNumber", reservation.guestNumber)
        put("status", reservation.status.name)
    }


    /**
     * Deletes a profile from the repository.
     *
     * @param profile The profile record to delete.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun deleteProfile(profile: Profile, onComplete: (Exception?) -> Unit = {}) {
        deleteProfile(profile.id, onComplete)
    }

    /**
     * Deletes a profile by its document ID.
     *
     * @param id The stable document ID.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun deleteProfile(id: String, onComplete: (Exception?) -> Unit = {}) {
        profileRef.document(id).delete()
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
        val query = when (type?.trim()?.uppercase()) {
            null, "", "USER" -> profileRef

            "NORMAL" -> profileRef.whereIn(
                "type", listOf("Entrant", "CampOwner", "User")
            )

            "ADMIN" -> profileRef.whereEqualTo(
                "type", "Admin"
            )

            else -> throw IllegalArgumentException("Unknown user role")
        }

        return query.listenForDecodedDocuments(
            ::profileFromDocument,
            onUpdate,
            onError
        )
    }

    /**
     * Converts a Firestore document to a user.
     *
     * @param document The Firestore document to convert.
     * @return The decoded user, or null if the document is missing.
     */
    private fun profileFromDocument(document: DocumentSnapshot): Profile? {
        return document.toObject(Profile::class.java)?.copy(id = document.id)
    }

    /**
     * Copies a profile with the Firestore document ID as its stable ID.
     *
     * @param profile The profile record to save or add.
     * @param id The stable document ID.
     * @return The profile with its Firestore document ID.
     */
    private fun withProfileId(profile: Profile, id: String): Profile = profile.copy(id = id)

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



// The following repository was added using OpenAI, Codex, "Implement US 01.13.01: rate a past
// campsite reservation with an ID-based campsite lookup and atomic average update", 2026-11-06.
/**
 * Manages the submission, retrieval and deletion of reservation ratings.
 *
 * Maintains the aggregate rating of each campsite and ensures
 * that reservations cannot have multiple ratings simultaneously.
 *
 * Outstanding issues: None known.
 */
class ReservationRatingRepo {

    private val db = Firebase.firestore
    private val reservationRef = db.collection("reservation")
    private val campsiteRef = db.collection("campsites")
    private val ratingRef = db.collection("reservationRatings")

    /**
     * Submits a rating for a past reservation.
     *
     * @param entrantId ID of the entrant submitting the rating.
     * @param reservationId ID of the reservation being rated.
     * @param score Rating between 1 and 5.
     * @param onComplete Called with null on success or an exception on failure.
     */
    fun submitRating(
        entrantId: String,
        reservationId: String,
        score: Int,
        onComplete: (Exception?) -> Unit = {}
    ) {
        if (score !in 1..5) {
            onComplete(
                IllegalArgumentException("Rating must be between 1 and 5.")
            )
            return
        }

        if (entrantId.isBlank() || reservationId.isBlank()) {
            onComplete(
                IllegalArgumentException("Entrant and reservation IDs are required.")
            )
            return
        }

        val reservationDocument = reservationRef.document(reservationId)
        val ratingDocument = ratingRef.document(reservationId)

        db.runTransaction { transaction ->

            val reservation = transaction.get(reservationDocument)
                .toObject(Reservation::class.java)
                ?: error("Reservation was not found.")

            check(entrantId in reservation.entrantIds) {
                "Entrant is not associated with this reservation."
            }

            check(reservation.status == ReservationStatus.Past) {
                "Only past reservations can be rated."
            }

            check(reservation.campsiteId.isNotBlank()) {
                "Reservation does not have a campsite ID."
            }


            val campsiteDocument =
                campsiteRef.document(reservation.campsiteId)

            val campsite = transaction.get(campsiteDocument)
                .toObject(Campsite::class.java)
                ?: error("Campsite was not found.")


            check(!transaction.get(ratingDocument).exists()) {
                "Reservation has already been rated."
            }

            val rating = ReservationRating(
                reservationId = reservationId,
                entrantId = entrantId,
                campsiteId = reservation.campsiteId,
                score = score
            )

            transaction.set(ratingDocument, rating)


            transaction.update(
                campsiteDocument,
                calculateRating(
                    campsite.ratingTotal + score,
                    campsite.ratingCount + 1
                )
            )

        }.addOnSuccessListener {
            onComplete(null)
        }.addOnFailureListener { error ->
            onComplete(error)
        }
    }

    /**
     * Retrieves all individual ratings for a campsite.
     *
     * @param campsiteId ID of the campsite.
     * @param onComplete Returns the ratings or an exception.
     */
    fun getRatingsByCampsite(
        campsiteId: String,
        onComplete: (List<ReservationRating>?, Exception?) -> Unit
    ) {
        ratingRef
            .whereEqualTo("campsiteId", campsiteId)
            .get()
            .addOnSuccessListener { snapshot ->
                onComplete(
                    snapshot.toObjects(ReservationRating::class.java),
                    null
                )
            }
            .addOnFailureListener { error ->
                onComplete(null, error)
            }
    }

    /**
     * Deletes a rating and recalculates the campsite's aggregate rating.
     *
     * @param reservationId ID of the reservation associated with the rating.
     * @param onComplete Called with null on success or an exception on failure.
     */
    fun deleteRating(
        reservationId: String,
        onComplete: (Exception?) -> Unit = {}
    ) {

        val ratingDocument = ratingRef.document(reservationId)

        db.runTransaction { transaction ->

            val rating = transaction.get(ratingDocument)
                .toObject(ReservationRating::class.java)
                ?: error("Rating was not found.")

            val campsiteDocument =
                campsiteRef.document(rating.campsiteId)

            val campsite = transaction.get(campsiteDocument)
                .toObject(Campsite::class.java)
                ?: error("Campsite was not found.")

            /*check(campsite.ratingCount > 0) {
                "Campsite rating count is invalid."
            }

            check(campsite.ratingTotal >= rating.score) {
                "Campsite rating total is invalid."
            }*/

            transaction.delete(ratingDocument)

            transaction.update(
                campsiteDocument,
                calculateRating(
                    (campsite.ratingTotal - rating.score).coerceAtLeast(0), //cant be negative
                    (campsite.ratingCount - 1).coerceAtLeast(0) //cant be negative
                )
            )

        }.addOnSuccessListener {
            onComplete(null)
        }.addOnFailureListener { error ->
            onComplete(error)
        }
    }

    /**
     * Calculates the campsite's aggregate rating.
     */
    private fun calculateRating(
        total: Int,
        count: Int
    ): Map<String, Any?> {
        val average = if (count > 0) {
            total.toDouble() / count
        } else {
            null
        }

        return mapOf(
            "ratingTotal" to total,
            "ratingCount" to count,
            "rating" to average
        )
    }

    /**
     * Retrieves the rating associated with a reservation.
     *
     * @param reservationId ID of the reservation.
     * @param onComplete Returns the rating or an exception.
     */
    fun getRatingByReservation(
        reservationId: String,
        onComplete: (ReservationRating?, Exception?) -> Unit
    ) {
        ratingRef.document(reservationId)
            .get()
            .addOnSuccessListener { snapshot ->
                onComplete(
                    snapshot.toObject(ReservationRating::class.java),
                    null
                )
            }
            .addOnFailureListener { error ->
                onComplete(null, error)
            }
    }
}

/**
 * Manages the submission, retrieval and deletion of reservation reviews.
 *
 * Ensures that only past reservations can be reviewed and that
 * each reservation can have at most one review.
 */
class ReservationReviewRepo {

    private val db = Firebase.firestore
    private val reservationRef = db.collection("reservation")
    private val reviewRef = db.collection("reservationReviews")

    /**
     * Submits a written review for a past reservation.
     *
     * @param entrantId ID of the entrant submitting the review.
     * @param reservationId ID of the reservation being reviewed.
     * @param content The written review.
     * @param onComplete Called with null on success or an exception on failure.
     */
    fun submitReview(
        entrantId: String,
        reservationId: String,
        content: String,
        onComplete: (Exception?) -> Unit = {}
    ) {
        if (content.isBlank()) {
            onComplete(
                IllegalArgumentException("Review cannot be empty.")
            )
            return
        }

        if (entrantId.isBlank() || reservationId.isBlank()) {
            onComplete(
                IllegalArgumentException(
                    "Entrant and reservation IDs are required."
                )
            )
            return
        }

        val reservationDocument = reservationRef.document(reservationId)
        val reviewDocument = reviewRef.document(reservationId)

        db.runTransaction { transaction ->

            val reservation = transaction.get(reservationDocument)
                .toObject(Reservation::class.java)
                ?: error("Reservation was not found.")

            check(entrantId in reservation.entrantIds) {
                "Entrant is not associated with this reservation."
            }

            check(reservation.status == ReservationStatus.Past) {
                "Only past reservations can be reviewed."
            }

            check(reservation.campsiteId.isNotBlank()) {
                "Reservation does not have a campsite ID."
            }

            check(!transaction.get(reviewDocument).exists()) {
                "Reservation has already been reviewed."
            }

            val review = ReservationReview(
                reservationId = reservationId,
                entrantId = entrantId,
                campsiteId = reservation.campsiteId,
                content = content.trim()
            )

            transaction.set(reviewDocument, review)

        }.addOnSuccessListener {
            onComplete(null)
        }.addOnFailureListener { error ->
            onComplete(error)
        }
    }

    /**
     * Retrieves all written reviews for a campsite.
     *
     * @param campsiteId ID of the campsite.
     * @param onComplete Returns the reviews or an exception.
     */
    fun getReviewsByCampsite(
        campsiteId: String,
        onComplete: (List<ReservationReview>?, Exception?) -> Unit
    ) {
        reviewRef
            .whereEqualTo("campsiteId", campsiteId)
            .get()
            .addOnSuccessListener { snapshot ->
                onComplete(
                    snapshot.toObjects(ReservationReview::class.java),
                    null
                )
            }
            .addOnFailureListener { error ->
                onComplete(null, error)
            }
    }

    /**
     * Retrieves the review associated with a reservation.
     *
     * @param reservationId ID of the reservation.
     * @param onComplete Returns the review or an exception.
     */
    fun getReviewByReservation(
        reservationId: String,
        onComplete: (ReservationReview?, Exception?) -> Unit
    ) {
        reviewRef.document(reservationId)
            .get()
            .addOnSuccessListener { snapshot ->
                onComplete(
                    snapshot.toObject(ReservationReview::class.java),
                    null
                )
            }
            .addOnFailureListener { error ->
                onComplete(null, error)
            }
    }

    /**
     * Deletes a review using its associated reservation ID.
     *
     * @param reservationId ID of the reservation associated with the review.
     * @param onComplete Called with null on success or an exception on failure.
     */
    fun deleteReview(
        reservationId: String,
        onComplete: (Exception?) -> Unit = {}
    ) {
        reviewRef.document(reservationId)
            .delete()
            .addOnSuccessListener {
                onComplete(null)
            }
            .addOnFailureListener { error ->
                onComplete(error)
            }
    }
}

/**
 * Represents an image associated with a campsite.
 *
 * @property url The URL of the image.
 * @property description A description of the image.
 * @property id The unique identifier of the image.
 * @property campsiteId The ID of the campsite associated with the image.
 * @property uploaderId The ID of the profile that uploaded the image.
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
    private val imageRef = db.collection("images")

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
        deleteImage(image.id, onComplete)
    }

    /**
     * Deletes an image by its document ID.
     *
     * @param id The stable document ID.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun deleteImage(id: String, onComplete: (Exception?) -> Unit = {}) {
        imageRef.document(id).delete()
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

// The following class was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
// and remove profile images stored as Firestore data URLs", 2026-11-22.
/**
 * Saves and observes the one image record associated with a profile.
 *
 * This repository intentionally observes a single document rather than maintaining an image
 * collection cache.
 *
 * @property imageRef The Firestore collection containing all image records.
 */
class ProfileImageRepo {
    private val imageRef = Firebase.firestore.collection("images")

    // The following block was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
    // and remove profile images stored as Firestore data URLs", 2026-11-22.
    /** Utilities for constructing and recognizing profile image records. */
    companion object {
        /** Prefix used for the deterministic Firestore document ID of a profile image. */
        const val PROFILE_IMAGE_PREFIX = "profile_"

        // The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
        // and remove profile images stored as Firestore data URLs", 2026-11-22.
        /**
         * Returns the deterministic image document ID for a profile.
         *
         * @param profileId The profile document ID.
         * @return The matching profile image document ID.
         */
        fun profileImageId(profileId: String): String = "$PROFILE_IMAGE_PREFIX$profileId"

        // The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
        // and remove profile images stored as Firestore data URLs", 2026-11-22.
        /**
         * Returns whether an image record follows the profile-image convention.
         *
         * @param image The image record to inspect.
         * @return `true` when the record has a profile ID and no campsite association.
         */
        fun isProfileImage(image: Image): Boolean =
            image.id.startsWith(PROFILE_IMAGE_PREFIX) && image.campsiteId.isBlank()

        // The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
        // and remove profile images stored as Firestore data URLs", 2026-11-22.
        /**
         * Creates the canonical image record stored for a profile.
         *
         * @param profileId The profile document ID that owns the image.
         * @param dataUrl The JPEG image encoded as a data URL.
         * @return The Firestore-ready profile image record.
         */
        fun profileImageRecord(profileId: String, dataUrl: String): Image = Image(
            id = profileImageId(profileId),
            uploaderId = profileId,
            campsiteId = "",
            description = "Profile picture",
            url = dataUrl
        )
    }

    // The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
    // and remove profile images stored as Firestore data URLs", 2026-11-22.
    /**
     * Creates or replaces a profile's image record.
     *
     * @param profileId The profile document ID that owns the image.
     * @param dataUrl The JPEG image encoded as a data URL.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun setProfileImage(
        profileId: String,
        dataUrl: String,
        onComplete: (Exception?) -> Unit
    ) {
        val image = profileImageRecord(profileId, dataUrl)
        imageRef.document(image.id).set(image)
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    // The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
    // and remove profile images stored as Firestore data URLs", 2026-11-22.
    /**
     * Deletes a profile's image record, including when that record does not exist.
     *
     * @param profileId The profile document ID that owns the image.
     * @param onComplete Called with `null` on success or the error on failure.
     */
    fun removeProfileImage(profileId: String, onComplete: (Exception?) -> Unit) {
        imageRef.document(profileImageId(profileId)).delete()
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    // The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
    // and remove profile images stored as Firestore data URLs", 2026-11-22.
    /**
     * Observes one profile image document.
     *
     * @param profileId The profile document ID whose image should be observed.
     * @param onUpdate Called with the decoded image, or `null` when the document is absent.
     * @param onError Called when the Firestore listener reports an error.
     * @return The listener registration, which can be removed to stop listening.
     */
    fun listenForProfileImage(
        profileId: String,
        onUpdate: (Image?) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration = imageRef.document(profileImageId(profileId))
        .addSnapshotListener { document, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }

            val image = document?.let { snapshot ->
                snapshot.takeIf { it.exists() }
                    ?.toObject(Image::class.java)
                    ?.copy(id = snapshot.id)
            }
            onUpdate(image)
        }

    // The following function was added using OpenAI, Codex, "Implement US 01.08.02: add, update,
    // and remove profile images stored as Firestore data URLs", 2026-11-22.
    /**
     * Retrieves the image associated with a profile from Firestore.
     *
     * @param profileId The ID of the profile whose image should be retrieved.
     * @param onComplete Called with the Image and null on success,
     * or null and an Exception on failure.
     */
    fun getProfileImage(
        profileId: String,
        onComplete: (Image?, Exception?) -> Unit
    ) {
        imageRef.document(profileImageId(profileId)).get()
            .addOnSuccessListener { document ->
                val image = if (document.exists()) {
                    document.toObject(Image::class.java)
                        ?.copy(id = document.id)
                } else {
                    null
                }
                onComplete(image, null)
            }
            .addOnFailureListener { error ->
                onComplete(null, error)
            }
    }
}

// The following class was added using OpenAI, Codex, "Add campsite image uploads to owner listings", 2026-11-26.
/**
 * Saves and observes the primary image displayed for a campsite listing.
 *
 * This repository observes a single deterministic image document so owner-list cards do not
 * create a collection-wide image listener.
 */
class CampsiteListingImageRepo {
    private val imageRef = Firebase.firestore.collection("images")

    // The following block was added using OpenAI, Codex, "Add campsite image uploads to owner listings", 2026-11-26.
    /** Utilities for the one primary image shown on an owner campsite card. */
    companion object {
        /** Prefix used for the deterministic Firestore document ID of a campsite listing image. */
        const val CAMPSITE_LISTING_IMAGE_PREFIX = "campsite_"

        /** Description stored with a campsite listing image. */
        const val CAMPSITE_LISTING_IMAGE_DESCRIPTION = "Campsite listing image"

        // The following function was added using OpenAI, Codex, "Add campsite image uploads to owner listings", 2026-11-26.
        /**
         * Returns the deterministic image document ID for a campsite.
         *
         * @param campsiteId The campsite document ID.
         * @return The matching primary-image document ID.
         */
        fun campsiteImageId(campsiteId: String): String = "$CAMPSITE_LISTING_IMAGE_PREFIX$campsiteId"

        // The following function was added using OpenAI, Codex, "Add campsite image uploads to owner listings", 2026-11-26.
        /**
         * Creates the Firestore record for a campsite's primary listing image.
         *
         * @param campsiteId The campsite document ID.
         * @param uploaderId The owner profile document ID.
         * @param dataUrl The encoded JPEG data URL.
         * @return The Firestore-ready image record.
         */
        fun campsiteImageRecord(campsiteId: String, uploaderId: String, dataUrl: String): Image =
            Image(
                id = campsiteImageId(campsiteId),
                campsiteId = campsiteId,
                uploaderId = uploaderId,
                description = CAMPSITE_LISTING_IMAGE_DESCRIPTION,
                url = dataUrl
            )
    }

    // The following function was added using OpenAI, Codex, "Add campsite image uploads to owner listings", 2026-11-26.
    /**
     * Creates or replaces a campsite listing image.
     *
     * @param campsiteId The campsite document ID.
     * @param uploaderId The owner profile document ID.
     * @param dataUrl The bounded encoded JPEG data URL.
     * @param onComplete Called with `null` on success or the save error on failure.
     */
    fun setCampsiteImage(
        campsiteId: String,
        uploaderId: String,
        dataUrl: String,
        onComplete: (Exception?) -> Unit
    ) {
        val image = campsiteImageRecord(campsiteId, uploaderId, dataUrl)
        imageRef.document(image.id).set(image)
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }

    // The following function was added using OpenAI, Codex, "Add campsite image uploads to owner listings", 2026-11-26.
    /**
     * Observes the one primary listing image for a campsite.
     *
     * @param campsiteId The campsite document ID.
     * @param onUpdate Receives the image or `null` if one is not stored.
     * @param onError Receives Firestore listener errors.
     * @return The listener registration to remove when the card leaves composition.
     */
    fun listenForCampsiteImage(
        campsiteId: String,
        onUpdate: (Image?) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration = imageRef.document(campsiteImageId(campsiteId))
        .addSnapshotListener { document, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }

            val image = document?.takeIf { it.exists() }
                ?.toObject(Image::class.java)
                ?.copy(id = document.id)
            onUpdate(image)
        }
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
     * Creates a conversation between the campsite owner and each entrant
     * associated with a reservation.
     *
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
     * Checks that lottery settings are valid.
     *
     * @param lottery The lottery to check.
     * @return True if the settings are valid.
     */
    fun validateLottery(lottery: Lottery): Boolean {
        if (lottery.winnerCount <= 0) {
            return false
        }

        if (lottery.waitlistCapacity != null &&
            lottery.waitlistCapacity <= 0) {
            return false
        }

        return true
    }


    /**
     * Saves a new lottery using its stable document ID.
     *
     * @param lottery The lottery record to save or add.
     * @param onComplete Called with `null` on success or the error on failure.
     */

    fun addLottery(lottery: Lottery, onComplete: (Exception?) -> Unit = {}) {
        if (!validateLottery(lottery)) {
            onComplete(IllegalArgumentException("Invalid lottery settings."))
            return
        }

        val record = withRelationshipIds(lottery)
        lotteryRef.document(record.id).set(record)
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { error -> onComplete(error) }
    }


    fun joinOwnerLottery(
        lotteryId: String,
        ownerId: String,
        campsiteIds: List<String>,
        onComplete: (Exception?) -> Unit = {}
    ) {
        val lotteryDocument = lotteryRef.document(lotteryId)

        db.runTransaction { transaction ->
            val lotterySnapshot = transaction.get(lotteryDocument)

            if (!lotterySnapshot.exists()) {
                throw IllegalArgumentException("Lottery not found.")
            }

            val lottery = lotterySnapshot.toObject(Lottery::class.java)
                ?: throw IllegalArgumentException("Invalid lottery.")

            if (!lottery.allowOtherOwners) {
                throw IllegalStateException("This lottery does not allow other owners.")
            }

            if (lottery.campOwnerId == ownerId) {
                throw IllegalStateException("You cannot join your own lottery.")
            }

            if (ownerId in lottery.participatingOwnerIds) {
                throw IllegalStateException("You already joined this lottery.")
            }

            if (campsiteIds.isEmpty()) {
                throw IllegalArgumentException("Select at least one campsite.")
            }

            val campsiteSnapshots = campsiteIds.distinct().map { id ->
                transaction.get(db.collection("campsites").document(id))
            }

            for (snapshot in campsiteSnapshots) {
                if (!snapshot.exists() ||
                    snapshot.getString("campOwnerId") != ownerId
                ) {
                    throw IllegalArgumentException(
                        "You can only add campsites that you own."
                    )
                }
            }

            transaction.update(
                lotteryDocument,
                mapOf(
                    "campsiteIds" to
                            (lottery.campsiteIds + campsiteIds).distinct(),
                    "participatingOwnerIds" to
                            (lottery.participatingOwnerIds +
                                    lottery.campOwnerId + ownerId)
                                .filter { it.isNotBlank() }
                                .distinct()
                )
            )

            true
        }.addOnSuccessListener {
            onComplete(null)
        }.addOnFailureListener { error ->
            onComplete(error)
        }
    }


    /**
     * Updates a lottery only before registration closes.
     *
     * @param lottery The lottery with updated settings.
     * @param onComplete Called with null on success or an error on failure.
     */
    fun updateLottery(lottery: Lottery, onComplete: (Exception?) -> Unit = {}) {
        if (!validateLottery(lottery)) {
            onComplete(IllegalArgumentException("Invalid lottery settings."))
            return
        }

        val record = withRelationshipIds(lottery)
        val lotteryDocument = lotteryRef.document(record.id)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(lotteryDocument)

            if (!snapshot.exists()) {
                throw IllegalArgumentException("Lottery not found.")
            }

            val existing = snapshot.toObject(Lottery::class.java)
                ?: throw IllegalArgumentException("Invalid lottery.")

            if (existing.drawCompleted) {
                throw IllegalStateException("Cannot edit a completed lottery.")
            }

            val registrationEndDate = snapshot.getString("registrationEndDate")
                ?: throw IllegalStateException("Registration end date is missing.")

            val closingDate = LocalDate.parse(registrationEndDate)
            val today = LocalDate.now()

            if (!today.isBefore(closingDate)) {
                throw IllegalStateException("Registration has already ended.")
            }

            transaction.update(
                lotteryDocument,
                mapOf(
                    "campsiteIds" to record.campsiteIds,
                    "registrationStartDate" to record.registrationStartDate,
                    "registrationEndDate" to record.registrationEndDate,
                    "bookingStartDate" to record.bookingStartDate,
                    "bookingEndDate" to record.bookingEndDate,
                    "winnerCount" to record.winnerCount,
                    "waitlistCapacity" to record.waitlistCapacity,
                    "availabilityStartDate" to record.availabilityStartDate,
                    "availabilityEndDate" to record.availabilityEndDate,
                    "allowOtherOwners" to record.allowOtherOwners
                )
            )

            true
        }.addOnSuccessListener {
            onComplete(null)
        }.addOnFailureListener { error ->
            onComplete(error)
        }
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

    // The following function was modified using OpenAI, Codex, "Implement US 01.01.06: allow an
    // entrant to join an active lottery from campsite details", 2026-11-27.
    fun joinWaitlist(
        lotteryId: String,
        entrantId: String,
        onComplete: (Exception?) -> Unit = {}
    ) {
        if (lotteryId.isBlank() || entrantId.isBlank()) {
            onComplete(IllegalArgumentException("Lottery and entrant IDs are required."))
            return
        }

        val lotteryDocument = lotteryRef.document(lotteryId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(lotteryDocument)

            if (!snapshot.exists()) {
                throw IllegalArgumentException("Lottery not found.")
            }

            val lottery = snapshot.toObject(Lottery::class.java)
                ?: throw IllegalArgumentException("Invalid lottery.")

            val today = LocalDate.now()
            if (!lottery.isRegistrationOpen(today)) {
                throw IllegalStateException(
                    "This lottery is not active or registration is closed."
                )
            }

            val entrants = lottery.waitlistEntrantIds
            val capacity = lottery.waitlistCapacity

            // Prevent duplicate entries.
            if (entrantId in entrants) {
                throw IllegalStateException("You already joined this lottery.")
            }

            // Check maximum capacity.
            if (capacity != null && entrants.size >= capacity) {
                throw IllegalStateException("The lottery waitlist is full.")
            }

            // Add entrant and save joining date together.
            transaction.update(
                lotteryDocument,
                mapOf(
                    "waitlistEntrantIds" to FieldValue.arrayUnion(entrantId),
                    "waitlistJoinedDates" to
                            (lottery.waitlistJoinedDates + (entrantId to today.toString()))
                )
            )

            true
        }.addOnSuccessListener {
            onComplete(null)
        }.addOnFailureListener { error ->
            onComplete(error)
        }
    }


    /**
     * Removes an entrant from a lottery waitlist.
     *
     * @param lotteryId The lottery document ID.
     * @param entrantId The entrant profile ID.
     * @param onComplete Called with `null` on success or the error on failure.
     */

    fun leaveWaitlist(
        lotteryId: String,
        entrantId: String,
        onComplete: (Exception?) -> Unit = {}
    ) {
        val lotteryDocument = lotteryRef.document(lotteryId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(lotteryDocument)

            if (!snapshot.exists()) {
                throw IllegalArgumentException("Lottery not found.")
            }

            val lottery = snapshot.toObject(Lottery::class.java)
                ?: throw IllegalArgumentException("Invalid lottery.")

            if (lottery.drawCompleted) {
                throw IllegalStateException("Cannot leave after winners are drawn.")
            }

            transaction.update(
                lotteryDocument,
                mapOf(
                    "waitlistEntrantIds" to FieldValue.arrayRemove(entrantId),
                    "waitlistJoinedDates" to
                            (lottery.waitlistJoinedDates - entrantId)
                )
            )

            true
        }.addOnSuccessListener {
            onComplete(null)
        }.addOnFailureListener { error ->
            onComplete(error)
        }
    }



    /**
     * Randomly selects winners after lottery registration closes.
     */
    fun drawWinners(
        lotteryId: String,
        ownerId: String,
        onComplete: (Exception?) -> Unit = {}
    ) {
        val lotteryDocument = lotteryRef.document(lotteryId)

        db.runTransaction { transaction ->

            // 1. Get the lottery from Firebase.
            val snapshot = transaction.get(lotteryDocument)

            if (!snapshot.exists()) {
                throw IllegalArgumentException("Lottery not found.")
            }

            val lottery = snapshot.toObject(Lottery::class.java)
                ?: throw IllegalArgumentException("Invalid lottery.")

            // 2. Make sure the correct owner is drawing winners.
            if (lottery.campOwnerId != ownerId) {
                throw IllegalStateException("Only the lottery owner can draw winners.")
            }

            // 3. Prevent drawing winners twice.
            if (lottery.drawCompleted) {
                throw IllegalStateException("Winners have already been drawn.")
            }

            // 4. Make sure registration has ended.
            val endDate = LocalDate.parse(lottery.registrationEndDate)

            if (!LocalDate.now().isAfter(endDate)) {
                throw IllegalStateException("Registration has not ended yet.")
            }

            // 5. Get all entrants.
            val entrants = lottery.waitlistEntrantIds
                .filter(String::isNotBlank)
                .distinct()

            if (entrants.isEmpty()) {
                throw IllegalStateException("No entrants are on the waitlist.")
            }

            if (lottery.winnerCount <= 0) {
                throw IllegalStateException("Winner count must be positive.")
            }

            // 6. Randomly select winners.
            val winners = entrants
                .shuffled()
                .take(lottery.winnerCount)
            if (entrants.size >= 500) {
                throw IllegalStateException("This waitlist is too large to finalize in one draw.")
            }
            val winnerIds = winners.toSet()
            val notifications = db.collection("notification")

            // 7. Save the results to Firebase.
            transaction.update(
                lotteryDocument,
                mapOf(
                    "winnerEntrantIds" to winners,
                    "drawCompleted" to true,
                    "status" to CampOwnerLotteryStatus.Drawn.name
                )
            )

            // Send each entrant their result as part of the same draw transaction.
            entrants.forEach { entrantId ->
                val won = entrantId in winnerIds
                val notificationId = UUID.nameUUIDFromBytes(
                    "lottery-result:$lotteryId:$entrantId".toByteArray()
                ).toString()
                transaction.set(
                    notifications.document(notificationId),
                    Notification(
                        title = if (won) "Lottery Won!" else "Lottery Result",
                        content = if (won) {
                            "You were selected in the campsite lottery. Check your reservations for the pending offer."
                        } else {
                            "You were not selected in the campsite lottery. Better luck next time!"
                        },
                        date = LocalDate.now().toString(),
                        id = notificationId,
                        recipientId = entrantId,
                        senderId = ownerId
                    )
                )
            }

            true

        }.addOnSuccessListener {
            onComplete(null)
        }.addOnFailureListener { error ->
            onComplete(error)
        }
    }


    /**
     * Sends an owner-authored notification to every entrant on one of the owner's lottery waitlists.
     *
     * @param ownerId The signed-in campsite owner's profile ID.
     * @param lotteryId The lottery document ID.
     * @param title Notification title.
     * @param content Notification message.
     * @param onComplete Receives the number of recipients and no error, or `null` and the error.
     */
    fun sendWaitlistNotification(
        ownerId: String,
        lotteryId: String,
        title: String,
        content: String,
        onComplete: (Int?, Exception?) -> Unit
    ) {
        if (ownerId.isBlank() || lotteryId.isBlank() || title.isBlank() || content.isBlank()) {
            onComplete(null, IllegalArgumentException("Owner, lottery, title, and message are required."))
            return
        }

        lotteryRef.document(lotteryId).get()
            .addOnSuccessListener { snapshot ->
                val lottery = snapshot.toObject(Lottery::class.java)?.copy(id = snapshot.id)
                if (lottery == null) {
                    onComplete(null, IllegalArgumentException("Lottery not found."))
                    return@addOnSuccessListener
                }
                if (lottery.campOwnerId != ownerId) {
                    onComplete(null, SecurityException("You can only message entrants on your own lotteries."))
                    return@addOnSuccessListener
                }

                val entrantIds = lottery.waitlistEntrantIds.filter(String::isNotBlank).distinct()
                if (entrantIds.size > 500) {
                    onComplete(null, IllegalStateException("This waitlist is too large to notify in one send."))
                    return@addOnSuccessListener
                }
                if (entrantIds.isEmpty()) {
                    onComplete(0, null)
                    return@addOnSuccessListener
                }

                val batch = db.batch()
                val notificationCollection = db.collection("notification")
                val sendGroupId = UUID.randomUUID().toString()

                entrantIds.forEach { entrantId ->
                    val notification = Notification(
                        title = title.trim(),
                        content = content.trim(),
                        date = java.time.LocalDate.now().toString(),
                        recipientId = entrantId,
                        senderId = ownerId,
                        sendGroupId = sendGroupId
                    )
                    batch.set(notificationCollection.document(notification.id), notification)
                }
                batch.commit()
                    .addOnSuccessListener { onComplete(entrantIds.size, null) }
                    .addOnFailureListener { error -> onComplete(null, error) }
            }
            .addOnFailureListener { error -> onComplete(null, error) }
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
        val batch = db.batch()
        batch.set(reservationRef.document(record.id), record)
        val notificationRef = db.collection("notification")
        val sendGroupId = UUID.randomUUID().toString()
        record.entrantIds.forEach { entrantId ->
            if (entrantId.isNotBlank()) {
                val notification = Notification(
                    title = "Reservation Received",
                    content = "A reservation is available for ${record.campsite.name.ifBlank { "the campsite" }} (${record.dates}).",
                    date = java.time.LocalDate.now().toString(),
                    id = UUID.nameUUIDFromBytes("${record.id}:$entrantId".toByteArray()).toString(),
                    recipientId = entrantId,
                    senderId = record.campOwnerId,
                    sendGroupId = sendGroupId
                )
                batch.set(notificationRef.document(notification.id), notification)
            }
        }

        batch.commit()
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
        notification: Notification? = null,
        onComplete: (Exception?) -> Unit = {}
    ) {
        val batch = db.batch()
        batch.update(reservationRef.document(reservationId), "status", status.name)
        notification?.let {
            batch.set(db.collection("notification").document(it.id), it)
        }
        batch.commit()
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
