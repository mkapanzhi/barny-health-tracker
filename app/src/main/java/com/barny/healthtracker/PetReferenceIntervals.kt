package com.barny.healthtracker

enum class PetSpecies(val storageValue: String) {
    CAT("cat"), DOG("dog");

    companion object {
        fun fromStorage(value: String?): PetSpecies? = values().firstOrNull { it.storageValue == value }
    }
}

object PetReferenceIntervals {
    private data class Ranges(val unit: String, val dog: ClosedFloatingPointRange<Float>, val cat: ClosedFloatingPointRange<Float>)

    data class Interval(val min: Float, val max: Float, val unit: String)

    // Cornell AHDC: Advia 2120 hemogram and Cobas 501 serum chemistry.
    // These are analyzer-specific examples, not universal clinical thresholds.
    private val laboratory = mapOf(
        "Лейкоциты" to Ranges("10⁹/L", 5.7f..14.2f, 5.1f..16.2f),
        "Эритроциты" to Ranges("10¹²/L", 5.7f..8.5f, 6.9f..10.1f),
        "Гемоглобин" to Ranges("g/L", 141f..201f, 109f..157f),
        "Гематокрит" to Ranges("%", 41f..58f, 31f..48f),
        "Тромбоциты" to Ranges("10⁹/L", 186f..545f, 195f..624f),
        HealthParams.ALT_RU to Ranges("U/L", 17f..95f, 28f..109f),
        HealthParams.AST_RU to Ranges("U/L", 18f..56f, 17f..48f),
        HealthParams.GGT_RU to Ranges("U/L", 0f..8f, 0f..2f),
        HealthParams.ALP_RU to Ranges("U/L", 7f..115f, 11f..49f),
        "Общий белок" to Ranges("g/L", 55f..72f, 66f..84f),
        "Альбумин" to Ranges("g/L", 32f..41f, 32f..43f),
        "Глобулины" to Ranges("g/L", 19f..37f, 29f..47f),
        "Глюкоза" to Ranges("mmol/L", 3.8f..5.8f, 3.9f..10.1f),
        "Мочевина" to Ranges("mmol/L", 3.2f..9.3f, 6.1f..12.5f),
        "Креатинин" to Ranges("µmol/L", 53f..124f, 71f..186f)
    )

    fun laboratoryInterval(key: String, species: PetSpecies): Interval? {
        val ranges = laboratory[key] ?: return null
        val range = when (species) {
            PetSpecies.CAT -> ranges.cat
            PetSpecies.DOG -> ranges.dog
        }
        return Interval(range.start, range.endInclusive, ranges.unit)
    }
}
