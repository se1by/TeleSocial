package me.se1by.TeleSocial;

import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class basic {
	static String chatpre = ChatColor.DARK_BLUE + "[TeleSocial] " + ChatColor.GRAY;

	/**
	 * This method saves a YamlConfiguration
	 * @param file The YamlConfiguration to save
	 * @param name The name to save the YamlConfiguration as
	 * @param player The player who is involved in the save
	 */
	public static void save(YamlConfiguration file, String name, Player player) {
	   try {
	     file.save("plugins/RewardMe/" + name + ".yml");
	   } catch (IOException e) {
	     System.out.println("[RewardMe] Unable to save file " + name + ".yml");
	     if (player != null) {
	       System.out.println("[RewardMe] Caused by player: " + player.getName());
	       e.printStackTrace();
	     }
	   }
	}
}
