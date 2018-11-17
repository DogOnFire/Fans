package com.dogonfire.fans;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class PlayerManager
{
	private Fans plugin;
    List<String> traits = new ArrayList<String>();
	private FileConfiguration playersConfig = null;
	private File playersConfigFile = null;
	HashMap<UUID, Long> registeredOnlinePlayers = new HashMap<UUID, Long>();
	
	public PlayerManager(Fans p)
	{
		this.plugin = p;
	}

	public void load()
	{	
		if (this.playersConfigFile == null)
		{
			this.playersConfigFile = new File(plugin.getDataFolder(), "players.yml");
		}

		this.playersConfig = YamlConfiguration.loadConfiguration(playersConfigFile);

		this.plugin.log("Loaded " + playersConfig.getKeys(false).size() + " players.");
		
		CleanFans();
	}

	public void save()
	{
		if ((this.playersConfig == null) || (playersConfigFile == null))
		{
			return;
		}

		try
		{
			this.playersConfig.save(playersConfigFile);
		}
		catch (Exception ex)
		{
			this.plugin.log("Could not save config to " + playersConfigFile + ": " + ex.getMessage());
		}
	}
		
	

	public List<String> getFansForPlayer(UUID playerId)
	{
		List<String> fans = this.playersConfig.getStringList(playerId + ".Fans");
		
		if(fans==null)
		{
			fans = new ArrayList<String>();
		}

		return fans;		
	}
	
	public boolean isPlayerFanOfPlayer(UUID playerId, UUID idolId)
	{
		List<String> fans = this.playersConfig.getStringList(idolId + ".Fans");

		if (fans == null)
		{
			return false;
		}

		return fans.contains(playerId.toString());
	}

	public void addFanForPlayer(UUID playerId, UUID fanId)
	{		
		List<String> fans = this.playersConfig.getStringList(playerId + ".Fans");
		
		if (fans == null)
		{
			fans = new ArrayList<String>();
		}
	
		if(fans.contains(fanId.toString()))
		{
			return;
		}

		fans.add(fanId.toString());
		this.playersConfig.set(playerId + ".Fans", fans);
			
		save();
	}
	
	public void removeFanForPlayer(UUID playerId, UUID fanId)
	{		
		List<String> fans = this.playersConfig.getStringList(playerId + ".Fans");
		
		if (fans == null)
		{
			fans = new ArrayList<String>();
		}
	
		if(!fans.contains(fanId.toString()))
		{
			return;
		}

		fans.remove(fanId.toString());
		this.playersConfig.set(playerId + ".Fans", fans);
			
		save();
	}
	
	public class FamousPlayer
	{
		public String playerId;
		public int numberOfFans;
		
		FamousPlayer(String playerId, int numberOfFans)
		{
			this.playerId = playerId;
			this.numberOfFans = numberOfFans;
		}
	}
	
	public void CleanFans()
	{
		Set<String> players = this.playersConfig.getKeys(false);

		for(String playerIdString : players)
		{			
			List<String> fans = this.playersConfig.getStringList(playerIdString + ".Fans");

			for(String fan : fans)
			{			
				if(fan.equals(playerIdString))
				{
					UUID playerId = UUID.fromString(playerIdString);
					fans.remove(playerIdString);
					this.playersConfig.set(playerIdString + ".Fans", fans);
					this.plugin.log("Removed self fan of " + plugin.getServer().getOfflinePlayer(playerId).getName());
					break;
				}

				UUID fanId = UUID.fromString(fan);
				
				if(plugin.getServer().getOfflinePlayer(fanId)==null || !plugin.getServer().getOfflinePlayer(fanId).hasPlayedBefore())
				{
					fans.remove(fanId);
					this.playersConfig.set(playerIdString + ".Fans", fans);
					this.plugin.log("Removed invalid fan of " + plugin.getServer().getOfflinePlayer(fanId).getName());
					break;
				}
			}
		}		
		
		save();
	}
	
	public List<FamousPlayer> getFamousPlayers()
	{
		HashMap<String, FamousPlayer> famousPeople = new HashMap<String, FamousPlayer>();

		Set<String> players = this.playersConfig.getKeys(false);

		for(String playerIdString : players)
		{			
			List<String> fans = this.playersConfig.getStringList(playerIdString + ".Fans");
					
			FamousPlayer n = new FamousPlayer(playerIdString, fans.size());

			famousPeople.put(playerIdString, n);
		}
				
		ArrayList<FamousPlayer> sortedList = new ArrayList<FamousPlayer>(famousPeople.values());
		
		Collections.sort(sortedList, new FanComparator(famousPeople));

		return sortedList;		
	}
	
	public int getPlayerFameLevel(UUID playerId)
	{
		HashMap<String, FamousPlayer> famousPeople = new HashMap<String, FamousPlayer>();

		Set<String> players = this.playersConfig.getKeys(false);

		for(String playerIdString : players)
		{			
			List<String> fans = this.playersConfig.getStringList(playerIdString + ".Fans");

			if(fans!=null && fans.size() > 0)
			{
				FamousPlayer n = new FamousPlayer(playerIdString, fans.size());
			
				famousPeople.put(playerIdString, n);
			}
		}
				
		ArrayList<FamousPlayer> sortedList = new ArrayList<FamousPlayer>(famousPeople.values());
		
		Collections.sort(sortedList, new FanComparator(famousPeople));

		int n = 0;
		int playerPosition = sortedList.size() + 1;
		int playerFameLevel = 0;
		
		for(FamousPlayer famousPlayer : sortedList)
		{
			if(famousPlayer.playerId.equals(playerId.toString()))
			{
				playerPosition = n;
			}
			
			n++;			
		}
		
		float rank = (float)playerPosition / (float)sortedList.size();
		
		if(rank < 0.1)
		{
			playerFameLevel = 1;
		}
		else if(rank < 0.2)
		{
			playerFameLevel = 2;
		}
				
		return playerFameLevel;
	}
	
	public String getPlayerFameLabel(int playerFameLevel)
	{
		String label = "";
		
		switch(playerFameLevel)
		{
			case 1 : label = "a Celebrity"; break; 
			case 2 : label = "an upcoming star"; break; 
			default : label = "not famous"; break; 		
		}
		
		return label;
	}
	
	
	public int getPlayerFameSponsorAmount(int playerFameLevel)
	{
		int amount = 0;
		
		switch(playerFameLevel)
		{
			case 1 : amount = 100; break; 
			case 2 : amount = 10; break; 
			default : amount = 0; break; 		
		}
		
		return amount;
	}

	public long getPlayerMinutesToPayout(UUID playerId)
	{
		if(this.registeredOnlinePlayers.containsKey(playerId))
		{
			return (60*60*1000 - (System.currentTimeMillis() - (Long)this.registeredOnlinePlayers.get(playerId))) / (60*1000);
		}
		
		return -1;
	}
	
	public class FanComparator implements Comparator<FamousPlayer>
	{
		private HashMap<String, FamousPlayer> famousPeople;
		
		public FanComparator(HashMap<String, FamousPlayer> famousPeople)
		{
			this.famousPeople = famousPeople;
		}

		public int compare(FamousPlayer object1, FamousPlayer object2)
		{
			FamousPlayer player1 = object1;
			FamousPlayer player2 = object2;

			return (player2.numberOfFans - player1.numberOfFans);
		}
	}

	public void registerPlayerLogin(Player player)
	{		
		if(!registeredOnlinePlayers.containsKey(player.getUniqueId()))
		{
			registeredOnlinePlayers.put(player.getUniqueId(), System.currentTimeMillis());
		}
		
	}

	public void registerPlayerLogout(Player player)
	{	
		if(registeredOnlinePlayers.containsKey(player.getUniqueId()))
		{
			registeredOnlinePlayers.remove(player.getUniqueId());
		}
		
	}
	
	public void update()
	{
		for(UUID playerId : registeredOnlinePlayers.keySet())
		{
			if(System.currentTimeMillis() - (Long)registeredOnlinePlayers.get(playerId) > 60*60*1000)
			{
				registeredOnlinePlayers.put(playerId, System.currentTimeMillis());
				
				Player player = plugin.getServer().getPlayer(playerId);
			
				int playerFameLevel = this.getPlayerFameLevel(playerId);
				String label = this.getPlayerFameLabel(playerFameLevel);
				int amount = this.getPlayerFameSponsorAmount(playerFameLevel);
			
				if(playerFameLevel > 0)
				{
					plugin.getEconomyManager().depositPlayer(player.getName(), amount);
				
					player.sendMessage(ChatColor.GREEN + "You got " + ChatColor.GOLD + amount + ChatColor.GREEN + " wanks from your sponsors just for being " + ChatColor.GREEN + label + "!");
					plugin.log(player.getName() + " was paid a sponsership of " + amount + " for being " + label);
				}
			}
		}		
	}
}