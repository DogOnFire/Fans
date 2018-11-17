package com.dogonfire.fans;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener
{
	private Fans	plugin	= null;

	public PlayerListener(Fans plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		plugin.getPlayerManager().registerPlayerLogin(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		plugin.getPlayerManager().registerPlayerLogout(event.getPlayer());
	}
}
