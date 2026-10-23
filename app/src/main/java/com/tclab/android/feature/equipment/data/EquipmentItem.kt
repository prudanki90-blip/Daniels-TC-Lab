package com.tclab.android.feature.equipment.data

/**
 * One item or type of equipment in the shared TC Lab inventory.
 * Dates use YYYY-MM-DD until persistence is implemented.
 */
data class EquipmentItem(
    val id: String,
    val name: String,
    val category: String,
    val description: String = "",
    val manufacturer: String = "",
    val model: String = "",
    val serialNumber: String = "",
    val quantity: Int = 1,
    val location: String = "",
    val condition: String = "Good",
    val availability: String = "Available",
    val imageUri: String? = null,
    val bundledImagePath: String? = null,
    val imageSource: String = "",
    val imageLicence: String = "",
    val imageAttribution: String = "",
    val purchaseDate: String? = null,
    val lastMaintenanceDate: String? = null,
    val nextMaintenanceDate: String? = null,
    val calibrationDueDate: String? = null,
    val notes: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    init {
        require(id.isNotBlank()) { "Equipment ID must not be blank." }
        require(name.isNotBlank()) { "Equipment name must not be blank." }
        require(category.isNotBlank()) { "Equipment category must not be blank." }
        require(quantity >= 0) { "Equipment quantity cannot be negative." }
    }
}

object EquipmentCategories {
    val all: List<String> = listOf(
        "Aseptic work",
        "Sterilization",
        "Culture environment",
        "Culture vessels",
        "Preparation and mixing",
        "Measurement",
        "Observation and imaging",
        "Storage",
        "Safety and PPE",
        "Waste management",
        "Other"
    )
}
