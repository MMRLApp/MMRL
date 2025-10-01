package com.dergoogler.mmrl.ext

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

inline fun <reified T> List<List<T>>.merge(): List<T> {
    val values = mutableListOf<T>()
    forEach { values.addAll(it) }
    return values
}

@OptIn(ExperimentalContracts::class)
inline fun <T, R> List<T>?.ifNotEmpty(block: (List<T>) -> R): R? {
    contract {
        returns(true) implies (this@ifNotEmpty != null)
    }

    return this?.takeIf { it.isNotEmpty() }?.let(block)
}

@OptIn(ExperimentalContracts::class)
fun <T> List<T>?.isNotNullOrEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNotNullOrEmpty != null)
    }

    return !this.isNullOrEmpty()
}

/**
 * Adds an element to the list if it is not already present.
 *
 * @param element The element to add.
 */
fun <E> MutableList<E>.addIfNotThere(element: E) {
    if (this.contains(element)) return
    this.add(element)
}