
package com.tclab.android.feature.protocols.data

import java.util.UUID

/**
 * Standalone data model for plant tissue-culture protocols.
 * This file has no dependencies on other TC Lab features.
 */
data class Protocol(
    val id: String = newId(),
    val title: String = "",
    val plant: PlantIdentity = PlantIdentity(),
    val explant: ExplantInformation = ExplantInformation(),
    val sterilizationSteps: List<SterilizationStep> = emptyList(),
    val media: List<CultureMedium> = emptyList(),
    val stages: List<CultureStage> = emptyList(),
    val generalConditions: CultureConditions = CultureConditions(),
    val outcomes: List<ProtocolOutcome> = emptyList(),
    val references: List<ProtocolReference> = emptyList(),
    val evidence: List<ExtractionEvidence> = emptyList(),
    val source: ProtocolSource = ProtocolSource(),
    val tags: List<String> = emptyList(),
    val notes: String = "",
    val reviewStatus: ProtocolReviewStatus =
        ProtocolReviewStatus.NEEDS_REVIEW,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val updatedAtEpochMillis: Long = System.currentTimeMillis()
)

data class PlantIdentity(
    val commonName: String = "",
    val scientificName: String = "",
    val family: String = "",
    val cultivarOrVariety: String = "",
    val accessionOrLine: String = "",
    val donorPlantSource: String = "",
    val additionalDetails: String = ""
)

data class ExplantInformation(
    val explantType: String = "",
    val sourceTissue: String = "",
    val developmentalStage: String = "",
    val approximateSize: String = "",
    val collectionSeasonOrDate: String = "",
    val preparationMethod: String = "",
    val notes: String = ""
)

data class SterilizationStep(
    val id: String = newId(),
    val order: Int = 0,
    val materialOrReagent: String = "",
    val concentration: String = "",
    val duration: String = "",
    val temperature: String = "",
    val action: String = "",
    val rinseDetails: String = "",
    val sourceEvidenceIds: List<String> = emptyList(),
    val notes: String = ""
)

data class CultureMedium(
    val id: String = newId(),
    val name: String = "",
    val basalMedium: String = "",
    val purpose: String = "",
    val ingredients: List<MediumIngredient> = emptyList(),
    val targetPh: String = "",
    val preparationMethod: String = "",
    val sterilizationMethod: String = "",
    val sourceEvidenceIds: List<String> = emptyList(),
    val notes: String = ""
)

data class MediumIngredient(
    val id: String = newId(),
    val name: String = "",
    val role: String = "",
    val amount: String = "",
    val unit: String = "",
    val concentrationBasis: ConcentrationBasis =
        ConcentrationBasis.UNSPECIFIED,
    val stockSolutionDetails: String = "",
    val sourceEvidenceIds: List<String> = emptyList(),
    val notes: String = ""
)

enum class ConcentrationBasis {
    FINAL_MEDIUM,
    STOCK_SOLUTION,
    WORKING_SOLUTION,
    REAGENT_STRENGTH,
    UNSPECIFIED
}

data class CultureStage(
    val id: String = newId(),
    val stageType: CultureStageType = CultureStageType.OTHER,
    val customStageName: String = "",
    val purpose: String = "",
    val mediumId: String? = null,
    val duration: String = "",
    val subcultureInterval: String = "",
    val conditions: CultureConditions = CultureConditions(),
    val procedure: String = "",
    val expectedObservations: String = "",
    val reportedResults: String = "",
    val sourceEvidenceIds: List<String> = emptyList(),
    val notes: String = ""
)

enum class CultureStageType {
    INITIATION,
    ESTABLISHMENT,
    MULTIPLICATION,
    SHOOT_ELONGATION,
    ROOTING,
    ACCLIMATIZATION,
    MAINTENANCE,
    OTHER
}

data class CultureConditions(
    val temperature: String = "",
    val lightIntensity: String = "",
    val photoperiod: String = "",
    val lightDescription: String = "",
    val humidity: String = "",
    val vesselType: String = "",
    val atmosphereOrGasConditions: String = "",
    val agitationOrShaking: String = "",
    val otherConditions: String = ""
)

data class ProtocolOutcome(
    val id: String = newId(),
    val metric: String = "",
    val value: String = "",
    val unit: String = "",
    val conditionOrTreatment: String = "",
    val observationTime: String = "",
    val sampleSize: String = "",
    val variabilityOrStatistics: String = "",
    val sourceEvidenceIds: List<String> = emptyList(),
    val notes: String = ""
)

data class ProtocolReference(
    val id: String = newId(),
    val title: String = "",
    val authors: String = "",
    val publicationYear: String = "",
    val journalOrPublisher: String = "",
    val doi: String = "",
    val url: String = "",
    val citationText: String = "",
    val accessedDate: String = ""
)

data class ProtocolSource(
    val originalFileName: String = "",
    val detectedFormat: String = "",
    val sourceUri: String = "",
    val sourceTitle: String = "",
    val sourceAuthor: String = "",
    val importedAtEpochMillis: Long? = null,
    val originalTextAvailable: Boolean = false,
    val importWarnings: List<String> = emptyList()
)

/**
 * Keeps evidence for each extracted field so it can be checked
 * against the original document.
 */
data class ExtractionEvidence(
    val id: String = newId(),
    val fieldPath: String = "",
    val extractedValue: String = "",
    val sourceSnippet: String = "",
    val sourceLocation: String = "",
    val confidence: ExtractionConfidence =
        ExtractionConfidence.UNREVIEWED,
    val reviewNote: String = ""
)

enum class ExtractionConfidence {
    HIGH,
    MEDIUM,
    LOW,
    CONFLICTING,
    UNREVIEWED
}

enum class ProtocolReviewStatus {
    IMPORTED,
    NEEDS_REVIEW,
    REVIEWED,
    VERIFIED_BY_USER
}

private fun newId(): String = UUID.randomUUID().toString()
