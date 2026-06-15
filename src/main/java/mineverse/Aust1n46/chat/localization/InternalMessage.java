package mineverse.Aust1n46.chat.localization;

import mineverse.Aust1n46.chat.utilities.Format;

/**
 * Messages internal to the plugin
 */
public enum InternalMessage {
    EMPTY_STRING("");

    private final String message;

    InternalMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return Format.FormatStringAll(this.message);
    }
}