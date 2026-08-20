package com.vvv.openexpensetracker.presentation.theme

import com.vvv.openexpensetracker.domain.model.Category
import openexpensetracker.shared.generated.resources.Res
import openexpensetracker.shared.generated.resources.category_food
import openexpensetracker.shared.generated.resources.category_transport
import openexpensetracker.shared.generated.resources.category_utilities
import openexpensetracker.shared.generated.resources.category_entertainment
import openexpensetracker.shared.generated.resources.category_health
import openexpensetracker.shared.generated.resources.category_shopping
import openexpensetracker.shared.generated.resources.category_others
import org.jetbrains.compose.resources.StringResource

fun getCategoryNameResource(category: String): StringResource {
    return when (category) {
        Category.FOOD -> Res.string.category_food
        Category.TRANSPORT -> Res.string.category_transport
        Category.UTILITIES -> Res.string.category_utilities
        Category.ENTERTAINMENT -> Res.string.category_entertainment
        Category.HEALTH -> Res.string.category_health
        Category.SHOPPING -> Res.string.category_shopping
        else -> Res.string.category_others
    }
}
