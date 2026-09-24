package com.chadflip;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("chadflip")
public interface ChadflipConfig extends Config
{
	@ConfigItem(
		keyName = "greeting",
		name = "Welcome Greeting",
		description = "Welcome to Chad's sick flipper"
	)
	default String greeting()
	{
		return "Hello";
	}
}
