package me.se1by.TeleSocial;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class telesocial extends JavaPlugin{

	private pListener pListener = new pListener();
	private telesocialCommandExecutor myExecutor = new telesocialCommandExecutor(this);
	public static HashMap<String, String>online = new HashMap<String, String>();
	public HashMap<String, String>conference = new HashMap<String, String>();

	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		System.out.println("[TeleSocial] disabled");
	}

	@Override
	public void onEnable() {
		// TODO Auto-generated method stub
		myExecutor = new telesocialCommandExecutor(this);
		getCommand("phone").setExecutor(myExecutor);
	
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvent(Type.PLAYER_JOIN, pListener , Event.Priority.Normal, this);
		System.out.println("[TeleSocial] enabled");
	}

}
