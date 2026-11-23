package com.twinspace.app.model

data class TwinSpaceSettings(
    val allowSameAppTwice: Boolean = false,
    val preferFreeformWhenSupported: Boolean = false,
    val showPrivacyDisclosureOnStart: Boolean = true
)
