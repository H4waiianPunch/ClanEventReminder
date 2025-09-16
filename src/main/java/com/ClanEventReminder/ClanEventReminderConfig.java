package com.ClanEventReminder;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("claneventreminder")
public interface ClanEventReminderConfig extends Config
{
	@ConfigItem(
			keyName = "githubURL",
			name = "Github URL",
			description = "Paste the GitHub URL provided by your clan leader"
	)
	default String reminderUrl()
	{
		return "";
	}

	@ConfigItem(
			keyName = "showOverlay",
			name = "Show Overlay",
			description = "Display the clan reminder as an overlay on screen"
	)
	default boolean showOverlay()
	{
		return true;
	}
}