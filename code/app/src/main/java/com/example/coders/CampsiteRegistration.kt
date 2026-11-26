/**
 * Supplies pure validation and record-building helpers for campsite registration.
 *
 * Outstanding issues: None known.
 */

package com.example.coders

const val ONLY_REGISTERED_USERS_MESSAGE = "Only registered users can register a campsite"
const val CAMPSITE_REGISTERED_MESSAGE = "Campsite Registered"
const val CAMPSITE_REGISTERED_RESULT_KEY = "campsite_registered_message"
const val CAMPSITE_IMAGE_PREFIX = "campsite_"
const val CAMPSITE_IMAGE_DESCRIPTION = "Campsite image"
const val MAX_CAMPSITE_TAGS = 10
const val MAX_CAMPSITE_TAG_LENGTH = 30

// The following class was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
/** Describes whether the locally saved account can register a campsite. */
sealed interface CampsiteRegistrationAccess {
    // The following class was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Indicates that the saved profile is still being loaded. */
    data object Loading : CampsiteRegistrationAccess

    // The following class was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Indicates that a registered campsite owner may register a campsite. */
    data class Allowed(val owner: Profile) : CampsiteRegistrationAccess

    // The following class was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Indicates that the current account cannot register a campsite. */
    data class Rejected(val hasSavedAccount: Boolean) : CampsiteRegistrationAccess
}

// The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
/**
 * Determines campsite-registration access from a saved profile ID and loaded profile.
 *
 * @param savedProfileId The profile ID saved on this device, if any.
 * @param profile The profile loaded for that ID, if any.
 * @return The corresponding registration access state.
 */
fun campsiteRegistrationAccess(
    savedProfileId: String?,
    profile: Profile?
): CampsiteRegistrationAccess = when {
    savedProfileId.isNullOrBlank() ->
        CampsiteRegistrationAccess.Rejected(false)

    profile != null && profile.id == savedProfileId ->
        CampsiteRegistrationAccess.Allowed(profile)

    else ->
        CampsiteRegistrationAccess.Rejected(true)
}

// The following class was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
/** Identifies a required campsite registration field and its user-facing label. */
enum class CampsiteRequiredField(val label: String) {
    NAME("Name"),
    LOCATION("Location"),
    DESCRIPTION("Description (amenities)"),
    OCCUPANCY("Maximum occupancy (a whole number above 0)"),
    MAP_LOCATION("Map location")
}

// The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
/**
 * Parses a valid positive whole-number campsite occupancy.
 *
 * @param occupancy The user-entered occupancy value.
 * @return The positive capacity, or `null` if the value is invalid.
 */
fun parseCampsiteOccupancy(occupancy: String): Int? =
    occupancy.trim().toIntOrNull()?.takeIf { it > 0 }

// The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
/**
 * Lists each required field that is absent or invalid in display order.
 *
 * @return The missing required fields in [CampsiteRequiredField] order.
 */
fun missingCampsiteFields(
    name: String,
    location: String,
    description: String,
    occupancy: String,
    latitude: Double?,
    longitude: Double?
): List<CampsiteRequiredField> = buildList {
    if (name.isBlank()) add(CampsiteRequiredField.NAME)
    if (location.isBlank()) add(CampsiteRequiredField.LOCATION)
    if (description.isBlank()) add(CampsiteRequiredField.DESCRIPTION)
    if (parseCampsiteOccupancy(occupancy) == null) add(CampsiteRequiredField.OCCUPANCY)
    if (latitude == null || longitude == null) add(CampsiteRequiredField.MAP_LOCATION)
}

// The following class was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
/** Represents the outcome of attempting to add a campsite tag. */
sealed interface CampsiteTagResult {
    // The following class was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Contains the updated ordered tag list after a successful addition. */
    data class Added(val tags: List<String>) : CampsiteTagResult

    // The following class was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Indicates that the candidate tag was blank. */
    data object Blank : CampsiteTagResult

    // The following class was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Indicates that the candidate tag exceeds [MAX_CAMPSITE_TAG_LENGTH]. */
    data object TooLong : CampsiteTagResult

    // The following class was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Indicates that the candidate tag already exists, ignoring case. */
    data object Duplicate : CampsiteTagResult

    // The following class was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
    /** Indicates that the tag list already has [MAX_CAMPSITE_TAGS] items. */
    data object LimitReached : CampsiteTagResult
}

// The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
/**
 * Produces a consistently formatted campsite tag.
 *
 * @param raw The tag text entered by the user.
 * @return Trimmed text with whitespace runs collapsed to one space.
 */
fun normalizeCampsiteTag(raw: String): String = raw.trim().replace(Regex("\\s+"), " ")

// The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
/**
 * Adds a valid, distinct campsite tag without changing the existing order.
 *
 * @param current Existing campsite tags.
 * @param raw The candidate tag entered by the user.
 * @return The addition outcome or validation reason.
 */
fun addCampsiteTag(current: List<String>, raw: String): CampsiteTagResult {
    val tag = normalizeCampsiteTag(raw)
    return when {
        tag.isEmpty() -> CampsiteTagResult.Blank
        tag.length > MAX_CAMPSITE_TAG_LENGTH -> CampsiteTagResult.TooLong
        current.any { it.equals(tag, ignoreCase = true) } -> CampsiteTagResult.Duplicate
        current.size >= MAX_CAMPSITE_TAGS -> CampsiteTagResult.LimitReached
        else -> CampsiteTagResult.Added(current + tag)
    }
}

// The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
/**
 * Returns the deterministic Firestore image ID for a campsite.
 *
 * @param campsiteId The registered campsite ID.
 * @return The image document ID for the campsite's optional image.
 */
fun campsiteImageId(campsiteId: String): String = "$CAMPSITE_IMAGE_PREFIX$campsiteId"

// The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
/**
 * Creates the optional image record stored separately from a campsite document.
 *
 * @param campsiteId The registered campsite ID.
 * @param uploaderId The camp owner's profile ID.
 * @param dataUrl The bounded JPEG data URL selected by the owner.
 * @return The canonical campsite image record.
 */
fun campsiteImageRecord(campsiteId: String, uploaderId: String, dataUrl: String): Image = Image(
    id = campsiteImageId(campsiteId),
    campsiteId = campsiteId,
    uploaderId = uploaderId,
    description = CAMPSITE_IMAGE_DESCRIPTION,
    url = dataUrl
)

// The following function was added using OpenAI, Codex, "Implement US 02.01.01: register campsites with required details, an optional encoded image, and tags", 2026-11-25.
/**
 * Builds the Firestore-ready campsite model from validated registration input.
 *
 * @return A campsite with normalized required fields and no embedded image data.
 */
fun buildCampsiteForRegistration(
    owner: Profile,
    name: String,
    location: String,
    description: String,
    rules: String,
    capacity: Int,
    latitude: Double,
    longitude: Double,
    tags: List<String>
): Campsite {
    val trimmedRules = rules.trim()
    val trimmedDescription = description.trim()
    return Campsite(
        name = name.trim(),
        location = location.trim(),
        description = if (trimmedRules.isBlank()) {
            trimmedDescription
        } else {
            "$trimmedDescription\n\nRules: $trimmedRules"
        },
        capacity = capacity,
        mapLocation = "$latitude,$longitude",
        campOwner = owner,
        campOwnerId = owner.id,
        tags = tags,
        images = emptyList()
    )
}
