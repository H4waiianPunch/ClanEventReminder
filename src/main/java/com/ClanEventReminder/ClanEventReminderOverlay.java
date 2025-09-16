package com.ClanEventReminder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

import javax.inject.Inject;
import java.awt.*;

public class ClanEventReminderOverlay extends Overlay
{
    private final ClanEventReminderPlugin plugin;
    private final ClanEventReminderConfig config;
    private final RuneLiteConfig runeLiteConfig;
    private final PanelComponent panelComponent = new PanelComponent();

    @Inject
    public ClanEventReminderOverlay(
            ClanEventReminderPlugin plugin,
            ClanEventReminderConfig config,
            RuneLiteConfig runeLiteConfig)
    {
        this.plugin = plugin;
        this.config = config;
        this.runeLiteConfig = runeLiteConfig;

        setPosition(OverlayPosition.TOP_LEFT);

        // Preserve skin-aware background
        panelComponent.setBackgroundColor(runeLiteConfig.overlayBackgroundColor());
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();

        if (!config.showOverlay())
        {
            return null;
        }

        String cachedMessage = plugin.getCachedMessage();
        if (cachedMessage == null || cachedMessage.isEmpty())
        {
            return null;
        }

        // Add title line (native font/color)
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Clan Event Reminder:")
                .build());

        try
        {
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(cachedMessage).getAsJsonObject();

            // Add each event line if present
            for (int i = 1; i <= 3; i++)
            {
                String key = "Event" + i;
                if (json.has(key) && !json.get(key).isJsonNull())
                {
                    String eventMessage = json.get(key).getAsString();
                    if (!eventMessage.isEmpty())
                    {
                        panelComponent.getChildren().add(LineComponent.builder()
                                .left(eventMessage)
                                .build());
                    }
                }
            }
        }
        catch (Exception e)
        {
            // Fallback: just show raw message
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(cachedMessage)
                    .build());
        }

        return panelComponent.render(graphics);
    }
}