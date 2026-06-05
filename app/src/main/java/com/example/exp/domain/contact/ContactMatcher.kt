package com.example.exp.domain.contact

class ContactMatcher {

    private val contacts = setOf(
        "aman raj",
        "rahul sharma",
        "mom",
        "dad"
    )

    fun getScore(name: String): Int {

        val normalized = name
            .trim()
            .lowercase()

        return if (contacts.contains(normalized)) {
            70
        } else {
            0
        }
    }
}