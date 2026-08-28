package com.vvv.openexpensetracker.domain.model

import openexpensetracker.shared.generated.resources.Res
import openexpensetracker.shared.generated.resources.category_bills
import openexpensetracker.shared.generated.resources.category_car
import openexpensetracker.shared.generated.resources.category_closes
import openexpensetracker.shared.generated.resources.category_entertainment
import openexpensetracker.shared.generated.resources.category_food
import openexpensetracker.shared.generated.resources.category_health
import openexpensetracker.shared.generated.resources.category_others
import openexpensetracker.shared.generated.resources.category_sport
import openexpensetracker.shared.generated.resources.category_transport
import org.jetbrains.compose.resources.StringResource

// String constants for expense categories
object Category {
    const val FOOD = "food"
    const val TRANSPORT = "transport"
    const val CAR = "car"
    const val SPORT = "sport"
    const val BILLS = "bills"
    const val ENTERTAINMENT = "entertainment"
    const val CLOSES = "closes"
    const val HEALTH = "health"
    const val OTHERS = "others"

    val list = listOf(FOOD, TRANSPORT, BILLS, ENTERTAINMENT, HEALTH, CAR, SPORT, CLOSES, OTHERS)

    fun getCategoryNameResource(category: String): StringResource {
        return when (category) {
            FOOD -> Res.string.category_food
            TRANSPORT -> Res.string.category_transport
            CAR -> Res.string.category_car
            SPORT -> Res.string.category_sport
            BILLS -> Res.string.category_bills
            ENTERTAINMENT -> Res.string.category_entertainment
            CLOSES -> Res.string.category_closes
            HEALTH -> Res.string.category_health
            else -> Res.string.category_others
        }
    }
}
