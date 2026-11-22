package com.barny.healthtracker

import android.graphics.Color

object HealthParams {
    const val ALT_RU = "АЛТ (аланинаминотрансфераза)"
    const val AST_RU = "АСТ (аспартатаминотрансфераза)"
    const val GGT_RU = "ГГТ (гамма‑глутамилтрансфераза)"
    const val ALP_RU = "ЩФ (ALP, щелочная фосфатаза)"
    const val WEIGHT_RU = "Вес"

    @Volatile var species: PetSpecies? = null
        private set

    fun selectSpecies(value: PetSpecies?) { species = value }

    val ALL_PARAMS = arrayOf("Лейкоциты", "Эритроциты", "Гемоглобин", "Гематокрит", "Тромбоциты", ALT_RU, AST_RU, GGT_RU, ALP_RU, "Общий белок", "Альбумин", "Глобулины", "Глюкоза", "Мочевина", "Креатинин", WEIGHT_RU, "Температура")

    val ABBREVIATIONS = ALL_PARAMS.zip(listOf("WBC", "RBC", "Hb", "Ht", "Plt", "ALT", "AST", "GGT", "ALP", "TP", "Alb", "Glb", "Gluc", "BUN", "Creat", "Wt", "Temp")).toMap()

    val NORMS: Map<String, Pair<Float, Float>>
        get() {
            val selected = species ?: return emptyMap()
            return ALL_PARAMS.mapNotNull { key ->
                val interval = PetReferenceIntervals.laboratoryInterval(key, selected)
                when {
                    interval != null -> key to (interval.min to interval.max)
                    key == "Температура" -> key to (37.5f to 39.5f)
                    else -> null
                }
            }.toMap()
        }

    val COLORS = ALL_PARAMS.zip(listOf(
        Color.rgb(255,100,100), Color.rgb(100,255,150), Color.rgb(100,150,255),
        Color.rgb(255,200,100), Color.rgb(255,150,255), Color.rgb(255,100,150),
        Color.rgb(200,255,100), Color.rgb(150,100,255), Color.rgb(100,255,255),
        Color.rgb(255,255,100), Color.rgb(100,255,100), Color.rgb(255,150,100),
        Color.rgb(150,255,150), Color.rgb(255,200,150), Color.rgb(100,200,255),
        Color.rgb(120,120,255), Color.rgb(255,165,0)
    )).toMap()

    private val descriptions = mapOf(
        "Лейкоциты" to "Белые кровяные клетки, участвующие в защите организма.",
        "Эритроциты" to "Красные кровяные клетки, переносящие кислород.",
        "Гемоглобин" to "Белок эритроцитов, обеспечивающий перенос кислорода.",
        "Гематокрит" to "Доля объёма крови, приходящаяся на эритроциты.",
        "Тромбоциты" to "Клеточные элементы, участвующие в свёртывании крови.",
        ALT_RU to "Фермент, используемый при оценке состояния печени.",
        AST_RU to "Фермент, содержащийся в печени, мышцах и других тканях.",
        GGT_RU to "Фермент, используемый при оценке печени и желчных путей.",
        ALP_RU to "Фермент печени, желчных путей и костной ткани.",
        "Общий белок" to "Суммарное содержание белков крови.",
        "Альбумин" to "Основной белок плазмы крови.",
        "Глобулины" to "Группа белков крови, включая иммунные белки.",
        "Глюкоза" to "Основной источник энергии для клеток.",
        "Мочевина" to "Продукт обмена белков, выводимый почками.",
        "Креатинин" to "Показатель, используемый при оценке функции почек.",
        WEIGHT_RU to "Масса тела. Универсальной нормы по виду нет: учитывайте породу, возраст, упитанность и рекомендации ветеринара.",
        "Температура" to "Температура тела. В приложении сохранён исходный ориентировочный диапазон 37.5–39.5 °C; он не является диагностическим порогом."
    )

    val DESCRIPTIONS: Map<String, String>
        get() = descriptions.mapValues { (key, text) ->
            val selected = species
            val interval = selected?.let { PetReferenceIntervals.laboratoryInterval(key, it) }
            if (interval == null) text else {
                val animal = if (selected == PetSpecies.CAT) "Кот" else "Собака"
                "$text $animal: ${interval.min}–${interval.max} ${interval.unit}. Ориентировочные лабораторные референсы Cornell AHDC (Advia 2120 / Cobas 501). Приоритет у диапазона на бланке вашей лаборатории. Отклонение не является диагнозом."
            }
        }
}
