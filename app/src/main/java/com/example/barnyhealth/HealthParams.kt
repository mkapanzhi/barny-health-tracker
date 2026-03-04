package com.example.barnyhealth

import kotlin.Pair
import android.graphics.Color

object HealthParams {
    val ALL_PARAMS = arrayOf("WBC", "RBC", "Hb", "Ht", "Plt", "ALT", "AST", "GGT", "ALP", "TP", "Alb", "Glb", "Gluc", "BUN", "Creat")

    val NORMS = mapOf(
        "WBC" to Pair(5.5f, 19.5f),
        "RBC" to Pair(5f, 10f),
        "Hb" to Pair(80f, 150f),
        "Ht" to Pair(25f, 45f),
        "Plt" to Pair(200f, 500f),
        "ALT" to Pair(10f, 100f),
        "AST" to Pair(10f, 100f),
        "GGT" to Pair(0f, 10f),
        "ALP" to Pair(10f, 100f),
        "TP" to Pair(60f, 80f),
        "Alb" to Pair(25f, 37f),
        "Glb" to Pair(25f, 45f),
        "Gluc" to Pair(3.9f, 6.7f),
        "BUN" to Pair(5f, 12f),
        "Creat" to Pair(80f, 180f)
    )

    val COLORS = mapOf(
        // Кровь (синие тона)
        "WBC" to Color.rgb(54, 162, 235),    // синий
        "RBC" to Color.rgb(75, 192, 192),    // бирюзовый
        "Hb" to Color.rgb(255, 99, 132),     // розовый
        "Ht" to Color.rgb(153, 102, 255),    // фиолетовый
        "Plt" to Color.rgb(255, 159, 64),    // оранжевый

        // Биохимия (зеленые/коричневые)
        "ALT" to Color.rgb(54, 235, 162),    // мятный
        "AST" to Color.rgb(75, 235, 192),    // светло-зеленый
        "GGT" to Color.rgb(255, 205, 86),    // желтый
        "ALP" to Color.rgb(201, 203, 207),   // серый

        // Белки/другое (бордовый/коричневый)
        "TP" to Color.rgb(255, 99, 71),      // томат
        "Alb" to Color.rgb(255, 159, 64),    // оранжевый (дубль, но другой оттенок)
        "Glb" to Color.rgb(99, 255, 132),    // ярко-зеленый
        "Gluc" to Color.rgb(132, 99, 255),   // индиго
        "BUN" to Color.rgb(235, 54, 162),    // magenta
        "Creat" to Color.rgb(192, 75, 235)   // фиолетово-розовый
    )
}


