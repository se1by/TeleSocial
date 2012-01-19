package me.se1by.TeleSocial;

import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

public class basic {
	static String chatpre = ChatColor.DARK_BLUE + "[TeleSocial] " + ChatColor.GRAY;

	/**
	 * This method saves a YamlConfiguration
	 * @param file The YamlConfiguration to save
	 * @param name The name to save the YamlConfiguration as
	 * @param sender The player who is involved in the save
	 */
	public static void save(YamlConfiguration file, String name, CommandSender sender) {
	   try {
	     file.save("plugins/TeleSocial/" + name + ".yml");
	   } catch (IOException e) {
	     System.out.println("[TeleSocial] Unable to save file " + name + ".yml");
	     if (sender != null) {
	       System.out.println("[TeleSocial] Caused by player: " + sender.getName());
	       e.printStackTrace();
	     }
	   }
	}
}
