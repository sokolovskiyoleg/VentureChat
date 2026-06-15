package mineverse.Aust1n46.chat.localization;

import mineverse.Aust1n46.chat.utilities.Format;

/**
 * Messages configurable in Messages.yml file.
 */
public enum LocalizedMessage {
    BLOCK_COMMAND_PLAYER("BlockCommandPlayer"),
    BLOCK_COMMAND_SENDER("BlockCommandSender"),
    BLOCKED_COMMAND("BlockedCommand"),
    BLOCKING_MESSAGE("BlockingMessage"),
    CLEAR_CHAT_SENDER("ClearChatSender"),
    CLEAR_CHAT_SERVER("ClearChatServer"),
    CHANNEL_COOLDOWN("ChannelCooldown"),
    CHANNEL_LIST("ChannelList"),
    CHANNEL_LIST_HEADER("ChannelListHeader"),
    CHANNEL_LIST_WITH_PERMISSIONS("ChannelListWithPermissions"),
    CHANNEL_NO_PERMISSION("ChannelNoPermission"),
    CHANNEL_NO_PERMISSION_VIEW("ChannelNoPermissionView"),
    CHANNEL_NO_SPEAK_PERMISSIONS("ChannelNoSpeakPermissions"),
    CHANNEL_PLAYER_LIST_HEADER("ChannelPlayerListHeader"),
    COMMAND_INVALID_ARGUMENTS("CommandInvalidArguments"),
    COMMAND_INVALID_ARGUMENTS_IGNORE("CommandInvalidArgumentsIgnore"),
    COMMAND_MUST_BE_RUN_BY_PLAYER("CommandMustBeRunByPlayer"),
    COMMAND_NO_PERMISSION("CommandNoPermission"),
    COMMAND_NOT_BLOCKABLE("CommandNotBlockable"),
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
    IGNORE_PLAYER_OFF("IgnorePlayerOff"),
    IGNORE_PLAYER_ON("IgnorePlayerOn"),
    IGNORE_YOURSELF("IgnoreYourself"),
    IGNORING_MESSAGE("IgnoringMessage"),
    INVALID_CHANNEL("InvalidChannel"),
    INVALID_HASH("InvalidHash"),
    LEAVE_CHANNEL("LeaveChannel"),
    LISTEN_CHANNEL("ListenChannel"),
    MESSAGE_TOGGLE_OFF("MessageToggleOff"),
    MESSAGE_TOGGLE_ON("MessageToggleOn"),
    MUST_LISTEN_ONE_CHANNEL("MustListenOneChannel"),
    NO_PLAYER_TO_REPLY_TO("NoPlayerToReplyTo"),
    NOTIFICATIONS_OFF("NotificationsOff"),
    NOTIFICATIONS_ON("NotificationsOn"),
    PLAYER_OFFLINE("PlayerOffline"),
    PLAYER_OFFLINE_NO_PERMISSIONS_CHECK("PlayerOfflineNoPermissionsCheck"),
    RANGED_SPY_OFF("RangedSpyOff"),
    RANGED_SPY_ON("RangedSpyOn"),
    SET_CHANNEL("SetChannel"),
    SET_CHANNEL_ALL_PLAYER("SetChannelAllPlayer"),
    SET_CHANNEL_ALL_SENDER("SetChannelAllSender"),
    SET_CHANNEL_PLAYER_CHANNEL_NO_PERMISSION("SetChannelPlayerChannelNoPermission"),
    SET_CHANNEL_SENDER("SetChannelSender"),
    SPAM_WARNING("SpamWarning"),
    SPY_OFF("SpyOff"),
    SPY_ON("SpyOn"),
    UNBLOCK_COMMAND_PLAYER("UnblockCommandPlayer"),
    UNBLOCK_COMMAND_SENDER("UnblockCommandSender"),
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
