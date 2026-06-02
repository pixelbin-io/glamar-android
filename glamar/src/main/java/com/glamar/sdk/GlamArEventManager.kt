package com.glamar.sdk

typealias GlamArEventCallback = (Any?) -> Unit

object GlamArEventManager {

    private val eventListeners = mutableMapOf<String, GlamArEventCallback>()

    /**
     * Register a single event listener
     */
    fun addEventListener(event: String, callback: GlamArEventCallback) {
        eventListeners[event] = callback
    }

    /**
     * Register multiple event listeners at once
     */
    fun addEventListeners(vararg listeners: Pair<String, GlamArEventCallback>) {
        for ((event, callback) in listeners) {
            eventListeners[event] = callback
        }
    }

    /**
     * Remove a listener for a specific event
     */
    fun removeEventListener(event: String) {
        eventListeners.remove(event)
    }

    /**
     * Clear all listeners — use this when WebView is destroyed
     */
    fun clearAllListeners() {
        eventListeners.clear()
    }

    /**
     * Internal: Dispatch an event to the registered listener
     */
    fun dispatchEvent(event: String, payload: Any?) {
        if (eventListeners.containsKey(event)) {
            eventListeners[event]?.invoke(payload)
        } else {
            GlamArLogger.d("GlamAR", "No listener registered for event: ${event}")
        }
    }
}
