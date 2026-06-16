package mineverse.Aust1n46.chat.localization;

import mineverse.Aust1n46.chat.utilities.Format;

/**
 * Messages configurable in Messages.yml file.
 */
public enum LocalizedMessage {
    BLOCKING_MESSAGE("BlockingMessage"),
    CLEAR_CHAT_SENDER("ClearChatSender"),
    CLEAR_CHAT_SERVER("ClearChatServer"),
    CHANNEL_COOLDOWN("ChannelCooldown"),
    CHANNEL_NO_SPEAK_PERMISSIONS("ChannelNoSpeakPermissions"),
    ALIAS_NO_PERMISSION("AliasNoPermission"),
    COMMAND_INVALID_ARGUMENTS("CommandInvalidArguments"),
    COMMAND_INVALID_ARGUMENTS_IGNORE("CommandInvalidArgumentsIgnore"),
    COMMAND_MUST_BE_RUN_BY_PLAYER("CommandMustBeRunByPlayer"),
    COMMAND_NO_PERMISSION("CommandNoPermission"),
    COMMANDSPY_OFF("CommandSpyOff"),
    COMMANDSPY_ON("CommandSpyOn"),
    CONFIG_RELOADED("ConfigReloaded"),
    ENTER_PRIVATE_CONVERSATION("EnterPrivateConversation"),
    ENTER_PRIVATE_CONVERSATION_SPY("EnterPrivateConversationSpy"),
    EXIT_PRIVATE_CONVERSATION("ExitPrivateConversation"),
    EXIT_PRIVATE_CONVERSATION_SPY("ExitPrivateConversationSpy"),
    FILTER_OFF("FilterOff"),
    FILTER_ON("FilterOn"),
    FORCE_ALL("ForceAll"),
    FORCE_PLAYER("ForcePlayer"),
    IGNORE_LIST_HEADER("IgnoreListHeader"),
    IGNORE_PLAYER_CANT("IgnorePlayerCant"),
    IGNORE_MESSAGE_TO_IGNORED_PLAYER("MessageToIgnoredPlayer"),
    IGNORE_PLAYER_OFF("IgnorePlayerOff"),
    IGNORE_PLAYER_ON("IgnorePlayerOn"),
    IGNORE_YOURSELF("IgnoreYourself"),
    IGNORING_MESSAGE("IgnoringMessage"),
    MESSAGE_TOGGLE_OFF("MessageToggleOff"),
    MESSAGE_TOGGLE_ON("MessageToggleOn"),
    NO_PLAYER_TO_REPLY_TO("NoPlayerToReplyTo"),
    NOTIFICATIONS_OFF("NotificationsOff"),
    NOTIFICATIONS_ON("NotificationsOn"),
    PLAYER_OFFLINE("PlayerOffline"),
    RANGED_SPY_OFF("RangedSpyOff"),
    RANGED_SPY_ON("RangedSpyOn"),
    SPAM_WARNING("SpamWarning"),
    SPY_OFF("SpyOff"),
    SPY_ON("SpyOn"),
    UNITS_DAY_PLURAL("UnitsDayPlural"),
    UNITS_DAY_SINGULAR("UnitsDaySingular"),
    UNITS_HOUR_PLURAL("UnitsHourPlural"),
    UNITS_HOUR_SINGULAR("UnitsHourSingular"),
    UNITS_MINUTE_PLURAL("UnitsMinutePlural"),
    UNITS_MINUTE_SINGULAR("UnitsMinuteSingular"),
    UNITS_SECOND_PLURAL("UnitsSecondPlural"),
    UNITS_SECOND_SINGULAR("UnitsSecondSingular");

    private final String message;

    LocalizedMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return Format.FormatStringAll(Localization.getLocalization().getString(this.message));
    }
}
