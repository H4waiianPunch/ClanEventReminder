package com.ClanEventReminder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
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

    private static final int MAX_WIDTH = 200; // maximum width in pixels

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

        // Add title
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Clan Event Reminder:")
                .build());

        try
        {
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(cachedMessage).getAsJsonObject();


            FontMetrics fm = graphics.getFontMetrics();
            int longestWidth = fm.stringWidth("Clan Event Reminder:"); // start with title width

            for (int i = 1; i <= 3; i++)
            {
                String key = "Event" + i;
                if (json.has(key) && !json.get(key).isJsonNull())
                {
                    String eventMessage = json.get(key).getAsString();
                    if (!eventMessage.isEmpty())
                    {
                        // Truncate if too long
                        int textWidth = fm.stringWidth(eventMessage);
                        if (textWidth > MAX_WIDTH)
                        {
                            int avgCharWidth = textWidth / eventMessage.length();
                            int maxChars = MAX_WIDTH / avgCharWidth;
                            eventMessage = eventMessage.substring(0, Math.max(0, maxChars - 3)) + "...";
                        }

                        panelComponent.getChildren().add(LineComponent.builder()
                                .left(eventMessage)
                                .build());

                        longestWidth = Math.max(longestWidth, fm.stringWidth(eventMessage));
                    }
                }
            }

            // Adjust panel width based on longest line
            panelComponent.setPreferredSize(new Dimension(longestWidth + 10, 0)); // +10 padding

        }
        catch (Exception e)
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(cachedMessage)
                    .build());
        }

        return panelComponent.render(graphics);
    }
}
