package com.arrowchecker;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ArrowCheckerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ArrowCheckerPlugin.class);
		RuneLite.main(args);
	}
}