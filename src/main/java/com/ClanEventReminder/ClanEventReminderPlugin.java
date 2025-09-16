package com.ClanEventReminder;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.awt.*;
import java.io.IOException;

@Slf4j
@PluginDescriptor(
		name = "Clan Event Reminder",
		description = "Displays clan event reminders from a GitHub JSON file",
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

	private final OkHttpClient httpClient = new OkHttpClient();
	private String cachedMessage = null;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Clan Event Reminder started!");
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
		log.info("Clan Event Reminder stopped!");
		overlayManager.remove(overlay);
		cachedMessage = null;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			// Fetch reminder if we don’t have it yet
			if (cachedMessage == null)
			{
				fetchReminder();
			}

			// If we have JSON data, parse up to 3 events
			if (cachedMessage != null && !cachedMessage.isEmpty())
			{
				try
				{
					JsonParser parser = new JsonParser();
					JsonObject json = parser.parse(cachedMessage).getAsJsonObject();

					for (int i = 1; i <= 3; i++)
					{
						String key = "Event" + i;
						if (json.has(key) && !json.get(key).isJsonNull())
						{
							String eventMessage = json.get(key).getAsString();
							if (!eventMessage.isEmpty())
							{
								// Apply user-selected color if available, otherwise use game default
								Color chatColor = config.chatboxColor();
								if (chatColor != null)
								{
									String hexColor = String.format("%06x", chatColor.getRGB() & 0xFFFFFF);
									eventMessage = "<col=" + hexColor + ">" + eventMessage + "</col>";
								}

								client.addChatMessage(
										ChatMessageType.GAMEMESSAGE,
										"Clan Event",
										eventMessage,
										null
								);
							}
						}
					}
				}
				catch (Exception e)
				{
					log.error("Failed to parse events from JSON", e);
				}
			}
		}
	}




	private void fetchReminder()
	{
		String url = config.reminderUrl();
		if (url == null || url.isEmpty())
		{
			log.warn("No reminder URL set in config");
			return;
		}

		Request request = new Request.Builder()
				.url(url)
				.build();

		try (Response response = httpClient.newCall(request).execute())
		{
			if (!response.isSuccessful())
			{
				log.error("Failed to fetch reminder: {}", response);
				return;
			}

			// Store the JSON as a string; parsing happens later
			cachedMessage = response.body().string();
			log.info("Fetched reminder JSON: {}", cachedMessage);
		}
		catch (IOException e)
		{
			log.error("Error fetching reminder", e);
		}
	}



	public String getCachedMessage()
	{
		return cachedMessage;
	}

	@Provides
	ClanEventReminderConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ClanEventReminderConfig.class);
	}
}