package com.ClanEventReminder;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("claneventreminder")
public interface ClanEventReminderConfig extends Config
{
	@ConfigItem(
			keyName = "githubURL",
			name = "Github URL",
			description = "Paste the GitHub URL provided by your clan leader",
			position = 0
	)
	default String reminderUrl()
	{
		return "";
	}

	@ConfigItem(
			keyName = "showOverlay",
			name = "Show Overlay",
			description = "Display the clan reminder as an overlay on screen",
			position = 1
	)
	default boolean showOverlay()
	{
		return true;
	}

	@ConfigItem(
			keyName = "chatboxColor",
			name = "Chatbox text colour",
			description = "Changes the color of the reminder text in the chatbox.",
			position = 2
	)
	default Color chatboxColor()
	{
		return null;
	}

	@ConfigItem(
			keyName = "reminderIntervalMinutes",
			name = "Event Reminder Interval (minutes)",
			description = "How often (in minutes) to re-broadcast the clan events",
			position = 3
	)
	default int reminderIntervalMinutes()
	{
		return 60; // default to 1 hour
	}

	@ConfigItem(
			keyName = "reminderIntervalEnabled",
			name = "Enable Repeating Broadcast",
			description = "If enabled, the plugin will re-broadcast events on a timer",
			position = 4
	)
	default boolean reminderIntervalEnabled()
	{
		return false;
	}

}