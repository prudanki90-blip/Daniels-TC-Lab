
package com.tclab.android.feature.protocols.extractor

import com.tclab.android.feature.protocols.data.CultureConditions
import com.tclab.android.feature.protocols.data.CultureMedium
import com.tclab.android.feature.protocols.data.CultureStage
import com.tclab.android.feature.protocols.data.CultureStageType
import com.tclab.android.feature.protocols.data.ExplantInformation
import com.tclab.android.feature.protocols.data.ExtractionConfidence
import com.tclab.android.feature.protocols.data.ExtractionEvidence
import com.tclab.android.feature.protocols.data.PlantIdentity
import com.tclab.android.feature.protocols.data.Protocol
import com.tclab.android.feature.protocols.data.ProtocolReviewStatus
import com.tclab.android.feature.protocols.data.ProtocolSource
import com.tclab.android.feature.protocols.data.SterilizationStep

/**
 * Offline first-pass extractor for plant tissue-culture protocols.
 *
 * Extracted information is provisional and must be reviewed.
 * Missing values are not invented.
 */
class ProtocolExtractor {

    data class ExtractionResult(
        val protocol: Protocol,
        val detectedTopics: List<String>,
        val warnings: List<String>
    )

    fun extract(
        text: String,
        fileName: String = "",
        mimeType: String = ""
    ): ExtractionResult {
        val sourceText = text.trim()
        val evidence = mutableListOf<ExtractionEvidence>()
        val warnings = mutableListOf<String>()

        if (sourceText.isBlank()) {
            val emptyProtocol = Protocol(
                source = ProtocolSource(
                    originalFileName = fileName,
                    detectedFormat = mimeType,
                    originalTextAvailable = false
                )
            )

            return ExtractionResult(
                protocol = emptyProtocol,
                detectedTopics = emptyList(),
                warnings = listOf(
                    "No readable text was provided. Check whether the document is scanned or empty."
                )
            )
        }

        fun findFirst(
            fieldPath: String,
            patterns: List<Regex>
        ): String {
            for (pattern in patterns) {
                val match = pattern.find(sourceText) ?: continue
                val value = match.groupValues
                    .getOrNull(1)
                    ?.trim()
                    .orEmpty()

                if (value.isNotBlank()) {
                    evidence += ExtractionEvidence(
                        fieldPath = fieldPath,
                        extractedValue = value,
                        sourceSnippet = match.value.take(300),
                        sourceLocation = "Character ${match.range.first}",
                        confidence = ExtractionConfidence.MEDIUM
                    )
                    return value
                }
            }
            return ""
        }

        val title = sourceText.lineSequence()
            .map { it.trim().trim('#', '*', ' ', '\t') }
            .firstOrNull {
                it.length in 4..180 &&
                    !it.endsWith(":") &&
                    !it.startsWith("http", ignoreCase = true)
            }
            .orEmpty()

        val scientificName = findFirst(
            "plant.scientificName",
            listOf(
                Regex(
                    """(?im)^\s*(?:scientific\s+name|species|botanical\s+name)\s*[:\-]\s*([^\r\n]+)"""
                )
            )
        )

        val commonName = findFirst(
            "plant.commonName",
            listOf(
                Regex(
                    """(?im)^\s*common\s+name\s*[:\-]\s*([^\r\n]+)"""
                )
            )
        )

        val family = findFirst(
            "plant.family",
            listOf(
                Regex(
                    """(?im)^\s*(?:plant\s+)?family\s*[:\-]\s*([^\r\n]+)"""
                )
            )
        )

        val cultivar = findFirst(
            "plant.cultivarOrVariety",
            listOf(
                Regex(
                    """(?im)^\s*(?:cultivar|variety|genotype|accession)\s*[:\-]\s*([^\r\n]+)"""
                )
            )
        )

        val explantType = findFirst(
            "explant.explantType",
            listOf(
                Regex(
                    """(?im)^\s*explant(?:\s+type)?\s*[:\-]\s*([^\r\n]+)"""
                )
            )
        )

        val sourceTissue = findFirst(
            "explant.sourceTissue",
            listOf(
                Regex(
                    """(?im)^\s*(?:source\s+tissue|explant\s+source)\s*[:\-]\s*([^\r\n]+)"""
                )
            )
        )

        val basalMediumMatches = KeywordDictionary.findMatches(
            sourceText,
            KeywordDictionary.basalMedia
        )

        val basalMedium = basalMediumMatches.firstOrNull().orEmpty()

        val targetPh = findFirst(
            "media[0].targetPh",
            listOf(
                Regex(
                    """(?i)\b(?:adjusted\s+to\s+)?pH\s*(?:of|=|:)?\s*([0-9]+(?:\.[0-9]+)?)"""
                )
            )
        )

        val temperature = findFirst(
            "generalConditions.temperature",
            listOf(
                Regex(
                    """(?i)\b(?:temperature|incubated\s+at|maintained\s+at)\s*[:=]?\s*([0-9]+(?:\.[0-9]+)?\s*(?:°\s*C|°C|degrees?\s+Celsius))"""
                )
            )
        )

        val photoperiod = findFirst(
            "generalConditions.photoperiod",
            listOf(
                Regex(
                    """(?i)\bphotoperiod\s*[:=]\s*([^\r\n.;]+)"""
                ),
                Regex(
                    """(?i)\b([0-9]{1,2}\s*(?:h|hours?)\s*light\s*[/:\-]\s*[0-9]{1,2}\s*(?:h|hours?)\s*dark)"""
                )
            )
        )

        val humidity = findFirst(
            "generalConditions.humidity",
            listOf(
                Regex(
                    """(?i)\b(?:relative\s+)?humidity\s*[:=]?\s*([0-9]+(?:\s*[-–]\s*[0-9]+)?\s*%)"""
                )
            )
        )

        val lightIntensity = findFirst(
            "generalConditions.lightIntensity",
            listOf(
                Regex(
                    """(?i)\b([0-9]+(?:\.[0-9]+)?\s*(?:µmol|μmol|umol)\s*m\s*[-−]?\s*2\s*s\s*[-−]?\s*1)"""
                ),
                Regex(
                    """(?i)\b([0-9]+(?:\.[0-9]+)?\s*lux)"""
                )
            )
        )

        val sterilizationLines = sourceText
            .lineSequence()
            .map { it.trim() }
            .filter { line ->
                line.isNotBlank() &&
                    KeywordDictionary.sterilization.any { keyword ->
                        line.contains(keyword, ignoreCase = true)
                    }
            }
            .distinct()
            .toList()

        val sterilizationSteps = sterilizationLines.mapIndexed {
                index, line ->
            val stepEvidence = ExtractionEvidence(
                fieldPath = "sterilizationSteps[$index]",
                extractedValue = line,
                sourceSnippet = line.take(300),
                sourceLocation = "Detected source line ${index + 1}",
                confidence = ExtractionConfidence.UNREVIEWED
            )

            evidence += stepEvidence

            SterilizationStep(
                order = index + 1,
                materialOrReagent = findReagent(line),
                concentration = findConcentration(line),
                duration = findDuration(line),
                action = line,
                sourceEvidenceIds = listOf(stepEvidence.id)
            )
        }

        val medium = if (
            basalMedium.isNotBlank() || targetPh.isNotBlank()
        ) {
            CultureMedium(
                name = basalMedium,
                basalMedium = basalMedium,
                targetPh = targetPh,
                notes = "First-pass extraction. Review the original source."
            )
        } else {
            null
        }

        val detectedStageNames =
            KeywordDictionary.findCultureStages(sourceText)

        val stages = detectedStageNames.map { stage ->
            val stageType = when (stage.lowercase()) {
                "initiation" ->
                    CultureStageType.INITIATION

                "multiplication" ->
                    CultureStageType.MULTIPLICATION

                "elongation" ->
                    CultureStageType.SHOOT_ELONGATION

                "rooting" ->
                    CultureStageType.ROOTING

                "acclimatization" ->
                    CultureStageType.ACCLIMATIZATION

                else ->
                    CultureStageType.OTHER
            }

            CultureStage(
                stageType = stageType,
                customStageName = if (
                    stageType == CultureStageType.OTHER
                ) stage else "",
                notes = "Stage keyword detected. Review its actual procedure and conditions."
            )
        }

        val plant = PlantIdentity(
            commonName = commonName,
            scientificName = scientificName,
            family = family,
            cultivarOrVariety = cultivar
        )

        val explant = ExplantInformation(
            explantType = explantType,
            sourceTissue = sourceTissue
        )

        val conditions = CultureConditions(
            temperature = temperature,
            lightIntensity = lightIntensity,
            photoperiod = photoperiod,
            humidity = humidity
        )

        val detectedTopics = buildList {
            if (basalMediumMatches.isNotEmpty()) {
                add("Culture media")
            }

            if (
                KeywordDictionary.findMatches(
                    sourceText,
                    KeywordDictionary.growthRegulators
                ).isNotEmpty()
            ) {
                add("Growth regulators")
            }

            if (sterilizationLines.isNotEmpty()) {
                add("Sterilization")
            }

            if (detectedStageNames.isNotEmpty()) {
                add("Culture stages")
            }

            if (
                KeywordDictionary.findMatches(
                    sourceText,
                    KeywordDictionary.outcomes
                ).isNotEmpty()
            ) {
                add("Reported outcomes")
            }

            if (
                KeywordDictionary.findMatches(
                    sourceText,
                    KeywordDictionary.sourceReferences
                ).isNotEmpty()
            ) {
                add("References")
            }

            if (
                KeywordDictionary.findMatches(
                    sourceText,
                    KeywordDictionary.troubleshooting
                ).isNotEmpty()
            ) {
                add("Troubleshooting")
            }
        }

        if (scientificName.isBlank()) {
            warnings +=
                "Scientific name was not found in an explicitly labelled field."
        }

        if (basalMediumMatches.size > 1) {
            warnings +=
                "Multiple basal-medium terms were found. Review the source before selecting a medium."
        }

        if (
            KeywordDictionary.findMatches(
                sourceText,
                KeywordDictionary.growthRegulators
            ).isNotEmpty()
        ) {
            warnings +=
                "Growth regulators were detected. Confirm each concentration, unit, and stock-versus-final-medium basis."
        }

        if (sterilizationLines.isNotEmpty()) {
            warnings +=
                "Sterilization details were detected. Verify reagent concentrations, exposure times, and rinse steps against the source."
        }

        warnings +=
            "This is a first-pass text extraction. Review every field before using a protocol experimentally."

        val protocol = Protocol(
            title = title,
            plant = plant,
            explant = explant,
            sterilizationSteps = sterilizationSteps,
            media = listOfNotNull(medium),
            stages = stages,
            generalConditions = conditions,
            evidence = evidence,
            source = ProtocolSource(
                originalFileName = fileName,
                detectedFormat = mimeType,
                originalTextAvailable = true,
                importWarnings = warnings
            ),
            reviewStatus = ProtocolReviewStatus.NEEDS_REVIEW
        )

        return ExtractionResult(
            protocol = protocol,
            detectedTopics = detectedTopics,
            warnings = warnings
        )
    }

    private fun findReagent(text: String): String {
        val reagents = listOf(
            "sodium hypochlorite",
            "calcium hypochlorite",
            "hydrogen peroxide",
            "mercuric chloride",
            "ethanol",
            "bleach",
            "sterile water"
        )

        return reagents.firstOrNull {
            text.contains(it, ignoreCase = true)
        }.orEmpty()
    }

    private fun findConcentration(text: String): String {
        return Regex(
            """(?i)\b[0-9]+(?:\.[0-9]+)?\s*(?:%|mg\s*/\s*l|g\s*/\s*l|ppm)"""
        ).find(text)?.value.orEmpty()
    }

    private fun findDuration(text: String): String {
        return Regex(
            """(?i)\b[0-9]+(?:\.[0-9]+)?\s*(?:seconds?|minutes?|mins?|hours?|hrs?)\b"""
        ).find(text)?.value.orEmpty()
    }
}
