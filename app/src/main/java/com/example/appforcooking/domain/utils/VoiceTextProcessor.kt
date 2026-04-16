package com.example.appforcooking.domain.utils

import com.example.appforcooking.domain.models.Product

class VoiceTextProcessor(
    private val getAllProducts: suspend () -> List<Product>
) {

    data class ProcessingResult(
        val uniqueProducts: Map<String, Product>,
        val ambiguousProducts: Map<String, List<Product>>,
        val notFound: List<String>
    )

    suspend fun processVoiceText(spokenText: String): ProcessingResult {
        val allProducts = getAllProducts()
        val candidates = extractProductCandidates(spokenText, allProducts)
        return matchWithDatabase(candidates, allProducts)
    }

    private fun extractProductCandidates(text: String, allProducts: List<Product>): List<String> {
        val normalizedText = text.lowercase().trim()

        val multiWordProducts = allProducts
            .map { it.name.lowercase() }
            .filter { it.contains(" ") }
            .sortedByDescending { it.split(" ").size }

        val result = mutableListOf<String>()
        var remainingText = normalizedText

        for (productName in multiWordProducts) {
            if (remainingText.contains(productName)) {
                result.add(productName)
                remainingText = remainingText.replace(productName, "")
            }
        }

        val separators = Regex("[,;.\\n]+|\\s+и\\s+|\\s+плюс\\s+|\\s+")
        val words = remainingText.split(separators)
            .map { it.trim() }
            .filter { it.isNotEmpty() && it.length > 1 }

        result.addAll(words)
        return result.distinct()
    }

    private fun matchWithDatabase(
        candidates: List<String>,
        allProducts: List<Product>
    ): ProcessingResult {
        val uniqueMatches = mutableMapOf<String, Product>()
        val ambiguousMatches = mutableMapOf<String, List<Product>>()
        val notFound = mutableListOf<String>()

        for (candidate in candidates) {
            val exactMatches = allProducts.filter {
                it.name.equals(candidate, ignoreCase = true)
            }

            if (exactMatches.size == 1) {
                uniqueMatches[candidate] = exactMatches.first()
                continue
            }

            if (exactMatches.size > 1) {
                ambiguousMatches[candidate] = exactMatches
                continue
            }

            val partialMatches = allProducts.filter {
                it.name.lowercase().contains(candidate.lowercase())
            }

            if (partialMatches.size == 1) {
                uniqueMatches[candidate] = partialMatches.first()
            } else if (partialMatches.size > 1) {
                ambiguousMatches[candidate] = partialMatches
            } else {
                notFound.add(candidate)
            }
        }

        return ProcessingResult(uniqueMatches, ambiguousMatches, notFound)
    }
}