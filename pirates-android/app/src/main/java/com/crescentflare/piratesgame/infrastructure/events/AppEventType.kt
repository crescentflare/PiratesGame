package com.crescentflare.piratesgame.infrastructure.events

/**
 * Event system: defines the possible event types
 */
enum class AppEventType(val value: String) {

    Unknown("unknown"),
    OpenWebSite("http"),
    OpenSecureWebsite("https"),
    NavigateApp("navigate"),
    Alert("alert");

    companion object {

        fun fromString(string: String?): AppEventType {
            for (enum in AppEventType.values()) {
                if (enum.value == string) {
                    return enum
                }
            }
            return Unknown
        }

    }

}
