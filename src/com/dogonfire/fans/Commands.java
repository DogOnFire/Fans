package com.dogonfire.fans;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.dogonfire.fans.PlayerManager.FamousPlayer;


public class Commands 
{
	private Fans  plugin;

	Commands(Fans p)
	{
		this.plugin = p;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		Player player = null;
		
		if (sender instanceof Player)
		{
			player = (Player) sender;			
		}
			
		if (cmd.getName().equalsIgnoreCase("fan")) 
		{			
			if (args.length == 0)
			{
				commandInfo(player);
				return true;
			}

			if (args.length == 1)
			{			
				switch(args[0].toLowerCase())
				{
					case "clean" : commandClean(); break;				
					case "top" : commandTop(player, args[0]); break;				
					default : commandToggleFanOfPlayer(player, args[0]); break;
				}
				
			}
						
			return true;					
		}	

		return true;
	}

	public void commandInfo(Player player)
	{
		player.sendMessage(ChatColor.YELLOW + "---------- " + this.plugin.getDescription().getFullName() + " ----------");
		player.sendMessage(ChatColor.AQUA + "By DogOnFire");
		
		player.sendMessage("");

		player.sendMessage(ChatColor.AQUA + "You have " + ChatColor.GOLD + plugin.getPlayerManager().getFansForPlayer(player.getUniqueId()).size() + ChatColor.AQUA + " fans.");
		
		int playerFameLevel = plugin.getPlayerManager().getPlayerFameLevel(player.getUniqueId());
		
		player.sendMessage(ChatColor.AQUA + "You are " + ChatColor.GOLD + plugin.getPlayerManager().getPlayerFameLabel(playerFameLevel) + ChatColor.AQUA + "!.");				

		if(playerFameLevel > 0)
		{				
			long min = plugin.getPlayerManager().getPlayerMinutesToPayout(player.getUniqueId());
			player.sendMessage(ChatColor.AQUA + "You will get " + ChatColor.GOLD + plugin.getPlayerManager().getPlayerFameSponsorAmount(playerFameLevel) + ChatColor.AQUA + " wanks when you are online for " + ChatColor.GOLD + min + ChatColor.AQUA + " min more!.");				
		}		
		
		player.sendMessage("");

		player.sendMessage(ChatColor.AQUA + "Use " + ChatColor.WHITE + "/fan <playername>" + ChatColor.AQUA + " to be a fan/stop being fan of another player!");				
		player.sendMessage(ChatColor.AQUA + "Use " + ChatColor.WHITE + "/fan top" + ChatColor.AQUA + " to see the most famous players!");				
		player.sendMessage(ChatColor.AQUA + "");				
	}	

	public void commandClean()
	{
		plugin.getPlayerManager().CleanFans();
		
		plugin.log("Cleaning Done.");
	}
	
	public void commandTop(Player player, String idolPlayerName)
	{		
		int n = 1;
		
		for(FamousPlayer famousPlayer : plugin.getPlayerManager().getFamousPlayers())
		{
			if(famousPlayer.numberOfFans > 0)
			{
				String playerName = plugin.getServer().getOfflinePlayer(UUID.fromString(famousPlayer.playerId)).getName();
				player.sendMessage("" + ChatColor.WHITE + (n++) + " - " + playerName + "   " + ChatColor.GOLD + famousPlayer.numberOfFans + ChatColor.WHITE + " fans");	
			}
			
			if(n > 10)
			{
				break;
			}
		}
	}
	
	public void commandToggleFanOfPlayer(Player player, String idolPlayerName)
	{		
		UUID idolId = plugin.getServer().getOfflinePlayer(idolPlayerName).getUniqueId();

		if(idolId.equals(player.getUniqueId()))
		{
			player.sendMessage(ChatColor.RED + "Come on. You can't be a fan of yourself. And you know that.");						
			return;
		}

		if(plugin.getServer().getOfflinePlayer(idolId)==null || !plugin.getServer().getOfflinePlayer(idolId).hasPlayedBefore())
		{
			player.sendMessage(ChatColor.RED + "That player has not played on this server.");						
			return;
		}

		if(!plugin.getPlayerManager().isPlayerFanOfPlayer(player.getUniqueId(), idolId))
		{		
			plugin.getPlayerManager().addFanForPlayer(idolId, player.getUniqueId());

			player.sendMessage(ChatColor.GREEN + "You are now a fan of " + ChatColor.GOLD + idolPlayerName + ChatColor.GREEN + "!");			
		
			Player idolPlayer = plugin.getServer().getPlayer(idolPlayerName);
		
			if(idolPlayer!=null)
			{
				idolPlayer.sendMessage(ChatColor.GOLD + player.getName() + ChatColor.GREEN + " just became a fan of you!");				
			}
		}
		else
		{
			plugin.getPlayerManager().removeFanForPlayer(idolId, player.getUniqueId());

			player.sendMessage(ChatColor.RED + "You are no longer a fan of " + ChatColor.GOLD + idolPlayerName + ChatColor.RED + "!");			
			
			Player idolPlayer = plugin.getServer().getPlayer(idolPlayerName);
			
			if(idolPlayer!=null)
			{
				idolPlayer.sendMessage(ChatColor.GOLD + player.getName() + ChatColor.RED + " is no longer a fan of you!");				
			}			
		}
	}		

	public void commandReload(Player player)
	{
		this.plugin.reloadSettings();
		
		if (player == null)
		{
			this.plugin.log(this.plugin.getDescription().getFullName() + ": Reloaded configuration.");
		}
		else
		{
			player.sendMessage(ChatColor.YELLOW + this.plugin.getDescription().getFullName() + ": " + ChatColor.WHITE + "Reloaded configuration.");
		}
	}
}