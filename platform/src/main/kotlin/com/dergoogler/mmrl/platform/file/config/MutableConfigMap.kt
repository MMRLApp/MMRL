package com.dergoogler.mmrl.platform.file.config

import com.dergoogler.mmrl.ext.toDataClass

class MutableConfigMap<V : Any?> : LinkedHashMap<String, V>(), MutableConfig<V> {
    override infix fun String.change(that: V): V? = put(this, that)
    override infix fun String.to(that: V): V? = change(that)

    /**
     * Modifies an element at a specific index within a list of data classes.
     *
     * This function takes a list of data classes, an index, and a builder lambda.
     * It retrieves the element at the given index, converts it to a `MutableConfigMap`,
     * applies the transformations defined in the builder lambda to this map,
     * and then converts the modified map back to an instance of the original data class type.
     * Finally, it returns a new list with the modified element at the specified index.
     *
     * The builder lambda receives the `MutableConfigMap` of the element being modified
     * as its receiver (`this`) and the original list as a parameter, allowing for
     * context-aware modifications.
     *
     * @param T The type of the elements in the list, which must be a data class (`Any`).
     * @param index The index of the element to modify.
     * @param builder A lambda function that defines how to modify the element.
     *                It takes the `MutableConfigMap` of the element as its receiver
     *                and the original list as a parameter.
     * @return A new list with the element at the specified index modified.
     * @throws IllegalArgumentException if the provided index is out of bounds for the list.
     *
     * @sample
     * ```kotlin
     * data class User(val id: Int, val name: String, var age: Int)
     *
     * val users = listOf(
     *     User(1, "Alice", 30),
     *     User(2, "Bob", 25)
     * )
     *
     * // Modify Alice's age
     * val updatedUsers = users.modify(0) { originalList ->
     *     // 'this' refers to the MutableConfigMap of User(1, "Alice", 30)
     *     // 'originalList' is the 'users' list
     *     this["age"] = 31
     * }
     *
     * // updatedUsers will be:
     * // [User(id=1, name=Alice, age=31), User(id=2, name=Bob, age=25)]
     *
     * // Example: Modify based on another element in the list
     * val usersWithAgeAdjusted = users.modify(1) { originalList ->
     */
    inline fun <reified T : Any> List<T>.modify(
        index: Int,
        builder: MutableConfigMap<Any?>.(List<T>) -> Unit,
    ): List<T> {
        require(index in 0 until size) { "Index $index is out of bounds for list of size $size" }

        val currentElement = this[index]
        val configMap = currentElement.toMutableConfig()
        configMap.builder(this)
        val modifiedElement = configMap.toDataClass<T>()

        return this.toMutableList().apply {
            this[index] = modifiedElement
        }
    }

    /**
     * Modifies the properties of a data class instance using a mutable configuration map.
     *
     * This function allows you to conveniently modify the properties of a data class instance
     * using a lambda expression that operates on a [MutableConfigMap]. The [MutableConfigMap]
     * provides a flexible way to set and update property values.
     *
     * @param T The type of the data class.
     * @param builder A lambda expression that takes a [MutableConfigMap] and the original data class instance
     *                as input and allows you to modify the property values.
     * @return A new instance of the data class with the modified properties.
     *
     * @see toMutableConfig
     * @see toDataClass
     */
    inline fun <reified T : Any> T.modify(
        builder: MutableConfigMap<Any?>.(T) -> Unit,
    ): T {
        val gg = T::class.toMutableConfig()
        gg.builder(this)
        return gg.toDataClass()
    }
}

fun <V : Any?, C> buildMutableConfig(
    data: C,
    builder: MutableConfigMap<V>.(C) -> Unit,
): Map<String, V> {
    val map = MutableConfigMap<V>()
    map.builder(data)
    return map
}