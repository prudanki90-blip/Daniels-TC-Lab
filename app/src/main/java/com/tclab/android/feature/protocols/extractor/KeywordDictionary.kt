
package com.tclab.android.feature.protocols.extractor

/**
 * Keyword dictionary used by the standalone Protocols feature.
 *
 * These terms help identify information in plant tissue culture
 * documents. Finding a keyword does not automatically confirm
 * a scientific value; extracted results must still be reviewed.
 */
object KeywordDictionary {

    val plantIdentity = listOf(
        "plant",
        "species",
        "scientific name",
        "common name",
        "cultivar",
        "variety",
        "genotype",
        "family",
        "donor plant",
        "source plant",
        "accession"
    )

    val explant = listOf(
        "explant",
        "shoot tip",
        "shoot apex",
        "apical meristem",
        "axillary bud",
        "nodal segment",
        "node",
        "internode",
        "leaf",
        "leaf disc",
        "petiole",
        "stem",
        "root",
        "root tip",
        "embryo",
        "cotyledon",
        "hypocotyl",
        "callus",
        "flower bud",
        "anther",
        "pollen",
        "seed",
        "immature embryo",
        "mature embryo"
    )

    val sterilization = listOf(
        "sterilization",
        "sterilisation",
        "surface sterilization",
        "surface sterilisation",
        "disinfection",
        "sodium hypochlorite",
        "calcium hypochlorite",
        "ethanol",
        "hydrogen peroxide",
        "mercuric chloride",
        "bleach",
        "sterile water",
        "rinse",
        "rinsed",
        "exposure time",
        "aseptic",
        "contamination"
    )

    val basalMedia = listOf(
        "murashige and skoog",
        "murashige & skoog",
        "ms medium",
        "ms basal medium",
        "woody plant medium",
        "wpm",
        "gamborg b5",
        "b5 medium",
        "nitsch medium",
        "white medium",
        "driver and kuniyuki",
        "dkw medium",
        "half-strength ms",
        "half strength ms",
        "quarter-strength ms",
        "quarter strength ms"
    )

    val growthRegulators = listOf(
        "6-benzylaminopurine",
        "benzylaminopurine",
        "benzyl adenine",
        "6-ba",
        "ba",
        "bap",
        "kinetin",
        "zeatin",
        "thidiazuron",
        "tdz",
        "meta-topolin",
        "m-topolin",
        "indole-3-butyric acid",
        "indole butyric acid",
        "iba",
        "1-naphthaleneacetic acid",
        "naphthaleneacetic acid",
        "naa",
        "indole-3-acetic acid",
        "indole acetic acid",
        "iaa",
        "2,4-d",
        "2,4-dichlorophenoxyacetic acid",
        "gibberellic acid",
        "ga3",
        "paclobutrazol",
        "abscisic acid",
        "aba"
    )

    val mediumIngredients = listOf(
        "sucrose",
        "glucose",
        "fructose",
        "agar",
        "gelrite",
        "phytagel",
        "gellan gum",
        "activated charcoal",
        "activated carbon",
        "myo-inositol",
        "inositol",
        "thiamine",
        "thiamine hcl",
        "pyridoxine",
        "nicotinic acid",
        "glycine",
        "casein hydrolysate",
        "coconut water",
        "banana extract",
        "potato extract",
        "amino acids"
    )

    val concentrationUnits = listOf(
        "mg/l",
        "mg l-1",
        "mg·l-1",
        "mg per litre",
        "mg per liter",
        "g/l",
        "g l-1",
        "g·l-1",
        "g per litre",
        "g per liter",
        "µm",
        "μm",
        "um",
        "mm",
        "mol/l",
        "molar",
        "micromolar",
        "millimolar",
        "ppm",
        "%",
        "percent",
        "ml/l",
        "ml per litre",
        "ml per liter"
    )

    val phTerms = listOf(
        "ph",
        "ph adjustment",
        "adjusted to ph",
        "medium ph",
        "initial ph",
        "final ph"
    )

    val preparation = listOf(
        "medium preparation",
        "culture medium preparation",
        "dissolve",
        "dissolved",
        "adjust the ph",
        "adjusted to",
        "make up to volume",
        "final volume",
        "autoclave",
        "autoclaved",
        "autoclaving",
        "filter sterilization",
        "filter sterilisation",
        "filter-sterilized",
        "filter-sterilised",
        "stock solution",
        "stock solutions",
        "working solution",
        "final concentration",
        "basal salts"
    )

    val cultureStages = mapOf(
        "initiation" to listOf(
            "initiation",
            "culture initiation",
            "establishment",
            "culture establishment"
        ),
        "multiplication" to listOf(
            "multiplication",
            "shoot multiplication",
            "proliferation",
            "multiple shoots",
            "shoot induction"
        ),
        "elongation" to listOf(
            "elongation",
            "shoot elongation",
            "shoot development"
        ),
        "rooting" to listOf(
            "rooting",
            "root induction",
            "root formation",
            "root development"
        ),
        "acclimatization" to listOf(
            "acclimatization",
            "acclimatisation",
            "hardening",
            "ex vitro",
            "transfer to soil",
            "greenhouse transfer"
        ),
        "callus_induction" to listOf(
            "callus induction",
            "callus formation",
            "callus initiation"
        ),
        "embryogenesis" to listOf(
            "somatic embryogenesis",
            "embryogenic callus",
            "somatic embryos"
        )
    )

    val environmentalConditions = listOf(
        "temperature",
        "°c",
        "degrees celsius",
        "light intensity",
        "lux",
        "µmol m-2 s-1",
        "umol m-2 s-1",
        "photoperiod",
        "light/dark cycle",
        "hours light",
        "hours dark",
        "relative humidity",
        "humidity",
        "darkness",
        "continuous light",
        "culture room",
        "incubation",
        "incubated",
        "shaking",
        "rpm",
        "culture vessel",
        "culture bottle",
        "culture jar",
        "petri dish"
    )

    val timing = listOf(
        "days",
        "weeks",
        "months",
        "duration",
        "incubation period",
        "culture period",
        "subculture",
        "sub-culture",
        "transfer interval",
        "every 2 weeks",
        "every 3 weeks",
        "every 4 weeks",
        "after 7 days",
        "after 14 days",
        "after 21 days",
        "after 28 days"
    )

    val outcomes = listOf(
        "shoot number",
        "number of shoots",
        "shoot length",
        "root number",
        "root length",
        "rooting percentage",
        "survival rate",
        "regeneration frequency",
        "regeneration percentage",
        "germination percentage",
        "callus percentage",
        "fresh weight",
        "dry weight",
        "response rate",
        "contamination rate",
        "results",
        "observations",
        "conclusion"
    )

    val troubleshooting = listOf(
        "contamination",
        "browning",
        "necrosis",
        "hyperhydricity",
        "vitrification",
        "phenolic exudation",
        "oxidation",
        "poor rooting",
        "low multiplication",
        "shoot abnormalities",
        "chlorosis",
        "culture loss",
        "troubleshooting"
    )

    val sourceReferences = listOf(
        "doi",
        "https://doi.org/",
        "reference",
        "references",
        "bibliography",
        "author",
        "published",
        "journal",
        "volume",
        "issue",
        "pages",
        "table",
        "figure",
        "supplementary material"
    )

    /**
     * Returns all dictionary terms matching the supplied text.
     *
     * Matching is case-insensitive. This function only identifies
     * keyword occurrences; it does not verify extracted values.
     */
    fun findMatches(
        text: String,
        keywords: List<String>
    ): List<String> {
        val normalizedText = text.lowercase()

        return keywords
            .distinct()
            .filter { keyword ->
                normalizedText.contains(keyword.lowercase())
            }
    }

    /**
     * Returns the culture-stage categories detected in the text.
     */
    fun findCultureStages(text: String): List<String> {
        val normalizedText = text.lowercase()

        return cultureStages
            .filterValues { keywords ->
                keywords.any { keyword ->
                    normalizedText.contains(keyword.lowercase())
                }
            }
            .keys
            .toList()
    }
}
