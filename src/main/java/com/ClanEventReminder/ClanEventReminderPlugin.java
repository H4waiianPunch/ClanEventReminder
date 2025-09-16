package com.ClanEventReminder;

import com.google.inject.Provides;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.ChatMessageType;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.awt.*;

@Slf4j
@PluginDescriptor(
		name = "Clan Event Reminder",
		description = "Displays reminders for clan events from GitHub URL",
		tags = {"clan", "event", "reminder"}
)
public class ClanEventReminderPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClanEventReminderConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ClanEventReminderOverlay overlay;

	private String cachedMessage;

	private Instant loginInstant;
	private Optional<Instant> lastReminderInstant = Optional.empty();

	@Provides
	ClanEventReminderConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ClanEventReminderConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);

		// If the client is already logged in when plugin starts, fetch the message immediately
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			fetchReminder();
		}

	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		cachedMessage = null;
		lastReminderInstant = Optional.empty();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			loginInstant = Instant.now();
			lastReminderInstant = Optional.empty();

			if (cachedMessage == null)
			{
				fetchReminder();
			}

			if (cachedMessage != null && !cachedMessage.isEmpty())
			{
				broadcastEvents();
				resetReminderInterval();
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (!config.reminderIntervalEnabled() || cachedMessage == null)
		{
			return;
		}

		final Instant nextReminder = getNextReminderInstant();
		if (nextReminder.compareTo(Instant.now()) < 0)
		{
			broadcastEvents();
			resetReminderInterval();
		}
	}

	private void fetchReminder()
	{
		try
		{
			String url = config.reminderUrl();
			if (url == null || url.isEmpty())
			{
				return;
			}

			URL githubUrl = new URL(url);
			try (InputStreamReader reader = new InputStreamReader(githubUrl.openStream()))
			{
				cachedMessage = new JsonParser().parse(reader).toString();
				log.info("Fetched reminder JSON: {}", cachedMessage);
			}
		}
		catch (Exception e)
		{
			log.error("Failed to fetch reminder from GitHub", e);
		}
	}

	private void broadcastEvents()
	{
		try
		{
			JsonObject json = new JsonParser().parse(cachedMessage).getAsJsonObject();

			for (int i = 1; i <= 3; i++)
			{
				String key = "Event" + i;
				if (json.has(key) && !json.get(key).isJsonNull())
				{
					String eventMessage = json.get(key).getAsString();
					if (!eventMessage.isEmpty())
					{
						// Apply configurable chatbox color
						String formattedMessage;
						Color c = config.chatboxColor();
						if (c != null)
						{
							String hex = String.format("%06x", c.getRGB() & 0xFFFFFF);
							formattedMessage = "<col=" + hex + ">" + eventMessage + "</col>";
						}
						else
						{
							formattedMessage = eventMessage; // default game color
						}

						client.addChatMessage(ChatMessageType.GAMEMESSAGE, "Clan Event", formattedMessage, null);
					}
				}
			}
		}
		catch (Exception e)
		{
			log.error("Failed to parse events from JSON", e);
		}
	}

	private Instant getNextReminderInstant()
	{
		final Duration reminderDuration = Duration.ofMinutes(config.reminderIntervalMinutes());
		if (lastReminderInstant.isPresent())
		{
			return lastReminderInstant.get().plus(reminderDuration);
		}
		return loginInstant.plus(reminderDuration);
	}

	private void resetReminderInterval()
	{
		lastReminderInstant = Optional.of(Instant.now());
	}

	public String getCachedMessage()
	{
		return cachedMessage;
	}
}
