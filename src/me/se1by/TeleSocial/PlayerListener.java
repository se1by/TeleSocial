package me.se1by.TeleSocial;

import java.io.File;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener{
	int userOnline = 0;
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		Player p = e.getPlayer();
		new YamlConfiguration();
		YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/TeleSocial/players.yml"));		
		String msg =userOnline + " other TeleSocial user are online on your server!";
		String msg1 = "Call them with /phone start <conference>!";
		
		if(userOnline>1){
			p.sendMessage(msg);
			p.sendMessage(msg1);
		}
		if(config.getString(p.getName()) != null){
			userOnline++;
			TeleSocial.online.put(p.getName(), "their network id");
		}
		else{
			p.sendMessage("Register yourself with /phone register <YourPhoneNumber> to speak with other user of this server!");
			p.sendMessage(msg1);
		}
	}
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e){
		Player p = e.getPlayer();
		new YamlConfiguration();
		YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/TeleSocial/players.yml"));		

		if(config.getString(p.getName()) != null){
			userOnline--;
			TeleSocial.online.remove(p.getName());
		}
	}

}
