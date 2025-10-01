package com.dergoogler.mmrl.model.ui

import com.dergoogler.mmrl.model.online.OnlineModule
import kotlin.math.min

data class TopCategory(
    val label: String,
    val modules: List<OnlineModule>
) {
    companion object {
        fun fromModuleList(
            modules: List<OnlineModule>,
            maxCategories: Int = 5,
            maxModulesPerCategory: Int = 9
        ): List<TopCategory> {
            if (modules.isEmpty()) return emptyList()

            val categoryToModules = mutableMapOf<String, MutableList<OnlineModule>>()

            modules.forEach { module ->
                val categories = module.categories
                if (categories.isNullOrEmpty()) {
                    categoryToModules.getOrPut("Uncategorized") { mutableListOf() }
                        .add(module)
                } else {
                    categories.forEach { category ->
                        categoryToModules.getOrPut(category) { mutableListOf() }
                            .add(module)
                    }
                }
            }

            if (categoryToModules.isEmpty()) return emptyList()

            val topCategoryEntries = categoryToModules.entries
                .sortedByDescending { it.value.size }
                .take(maxCategories)

            return topCategoryEntries.mapNotNull { (categoryName, categoryModules) ->
                val realModules = categoryModules.shuffled()
                    .take(maxModulesPerCategory)
                    .toMutableList()

                // Skip categories with no real modules
                if (realModules.isEmpty()) return@mapNotNull null

                // Fill to nearest multiple of 3 but never with more fillers than real items
                val remainder = realModules.size % 3
                val fillersNeeded =
                    if (remainder == 0) 0
                    else min(3 - remainder, realModules.size)

                repeat(fillersNeeded) {
                    realModules.add(OnlineModule.example())
                }

                TopCategory(
                    label = categoryName,
                    modules = realModules
                )
            }
        }
    }
}
