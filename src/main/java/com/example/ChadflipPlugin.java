package com.chadflip;

import java.util.HashMap;
import java.util.Map;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.events.GrandExchangeOfferChanged;
import net.runelite.client.eventbus.Subscribe;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Chadflip"
)
public class ChadflipPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ChadflipConfig config;

@Override
protected void startUp() throws Exception
{
    log.info("Chadflip started!");
}

	@Override
	protected void shutDown() throws Exception
	{
		log.debug("ChadflipPlugin stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Chadflip says " + config.greeting(), null);
		}
	}

	private final Map<Integer, Integer> lastQty = new HashMap<>();
private final Map<Integer, Integer> lastSpent = new HashMap<>();

	@Provides
	ChadflipConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ChadflipConfig.class);
	}
	@Subscribe
public void onGrandExchangeOfferChanged(GrandExchangeOfferChanged event)
{
	log.info("Chadflip saw GE event: slot {} state {} qty {}",
    event.getSlot(), event.getOffer().getState(), event.getOffer().getQuantitySold());
    int slot = event.getSlot();
    GrandExchangeOffer offer = event.getOffer();
    GrandExchangeOfferState state = offer.getState();

    if (state == GrandExchangeOfferState.EMPTY)
    {
        lastQty.remove(slot);
        lastSpent.remove(slot);
        return;
    }

    int qty = offer.getQuantitySold();
    int spent = offer.getSpent();

    // put() stores the new value and returns the old one (null if none)
    Integer prevQty = lastQty.put(slot, qty);
    Integer prevSpent = lastSpent.put(slot, spent);

    if (prevQty == null || prevSpent == null)
    {
        return; // first time we've seen this slot: just remember the baseline
    }

    int filled = qty - prevQty;
    int gp = spent - prevSpent;
    if (filled <= 0)
    {
        return; // nothing new (e.g. a cancel with no extra fills)
    }

    boolean isBuy = state == GrandExchangeOfferState.BUYING
        || state == GrandExchangeOfferState.BOUGHT
        || state == GrandExchangeOfferState.CANCELLED_BUY;

    log.info("{} fill: item {} x{} for {} gp total ({} each)",
        isBuy ? "BUY" : "SELL", offer.getItemId(), filled, gp, gp / filled);
}
}
