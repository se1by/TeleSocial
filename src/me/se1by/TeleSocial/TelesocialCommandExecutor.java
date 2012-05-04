package me.se1by.TeleSocial;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class TelesocialCommandExecutor implements CommandExecutor {
	/**
	 * @author Jonas Seibert
	 */
	TeleSocial plugin;
	private String chatpre = Basic.chatpre;
	String pre = "[TeleSocial] ";
	private YamlConfiguration players = YamlConfiguration
			.loadConfiguration(new File("plugins/TeleSocial/players.yml"));
	private YamlConfiguration config = YamlConfiguration
			.loadConfiguration(new File("plugins/TeleSocial/config.yml"));
	private YamlConfiguration blocked = YamlConfiguration
			.loadConfiguration(new File("plugins/TeleSocial/blocked.yml"));
	private HashMap<String, String> conferences;

	public TelesocialCommandExecutor(TeleSocial telesocial) {
		this.plugin = telesocial;
		conferences = plugin.conference;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (sender instanceof Player) {
			this.pre = chatpre;
		}
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("register") && args.length == 2) {
				String number = args[1];
				String networkID = sender.getName();
				if (!isRegistered(networkID)) {
					boolean reg = register(number, networkID, sender);
					if (reg) {
						sender.sendMessage(pre + "Registration complete!");
					} else {
						sender.sendMessage(pre + "Registration failed!");
					}
				} else {
					if(networkID + "DISABLED".equals(players.getString(sender.getName())) != null){
						changeNumber(networkID, number);
						return true;
					}
					sender.sendMessage(pre + "This name is already registered!");
					sender.sendMessage(pre
							+ "If you havn't registered that name, you can register another with /phone <name> <number>");
				}
				return true;
			} else if (args[0].equalsIgnoreCase("register") && args.length == 3) {
				String networkID = args[1];
				String number = args[2];

				if (!isRegistered(networkID)) {
					boolean reg = register(number, networkID, sender);
					if (reg) {
						sender.sendMessage(pre + "Registration complete!");
					} else {
						sender.sendMessage(pre + "Registration failed!");
					}
				} else {
					for(String s : players.getKeys(false)){
						if(players.getString(s).equalsIgnoreCase(networkID + "DISABLED")){
							changeNumber(networkID, number);
							return true;			
						}
					}
					sender.sendMessage(pre + "This name is already registered!");
					sender.sendMessage(pre
							+ "If you havn't registered that name, you can register another with /phone <name> <number>");
				}
				return true;
			} else if (args[0].equalsIgnoreCase("unregister")) {
				players.set(sender.getName(), players.getString(sender.getName()) + "DISABLED");
				Basic.save(players, "players", sender);
				sender.sendMessage(pre + "You are unregistered!");
				return true;
			} else if (args[0].equalsIgnoreCase("start") && args.length == 2) {
				String conference = args[1];
				String networkID = players.getString(sender.getName());
				boolean start = startConference(conference, networkID, sender);
				if (start) {
					sender.sendMessage(pre + "Conference " + conference
							+ " started!");
				} else {
					sender.sendMessage(pre + "Unable to start conference "
							+ conference + "!");
				}
				return true;
			} else if (args[0].equalsIgnoreCase("isreg") && args.length == 2) {
				if (isRegistered(args[1])) {
					sender.sendMessage(pre + "Player " + args[1]
							+ " is registered.");
				} else {
					sender.sendMessage(pre + "Player " + args[1]
							+ " is not registered.");
				}
				return true;
			} else if (args[0].equalsIgnoreCase("join") && args.length == 2) {
				String conference = args[1];
				boolean join = joinConference(conference, sender);
				if (join) {
					sender.sendMessage(pre + "Joining conference " + conference
							+ "...");
				} else {
					sender.sendMessage(pre + "Unable to join conference!");
				}
				return true;
			} else if (args[0].equalsIgnoreCase("delete") && args.length == 2) {
				String conference = args[1];
				boolean delete = deleteConference(conference, sender);
				if (delete) {
					sender.sendMessage(pre + "Conference " + conference
							+ " deleted!");
				} else {
					sender.sendMessage(pre + "Unable to delete conference "
							+ conference + "!");
				}
				return true;
			} else if (args[0].equalsIgnoreCase("set") && args.length == 3
					&& sender.isOp()) {
				if (args[1].equalsIgnoreCase("appkey")) {
					setAppkey(args[2]);
					return true;
				} else if (args[1].equalsIgnoreCase("baseurl")) {
					setBaseUrl(args[2]);
					return true;
				}
			} else if (args[0].equalsIgnoreCase("call")) {
				List<String> toCall = new ArrayList<String>(Arrays.asList(args));
				toCall.remove(0);
				boolean success = call(sender, toCall);
				if (!success)
					sender.sendMessage(pre + "Unable to create call!");
				return true;

			} else if (args[0].equalsIgnoreCase("list") && args.length == 1) {
				String callable = getCallable();
				if (!"".equals(callable)) {
					sender.sendMessage(pre + "Callable Player: " + callable);
				} else {
					sender.sendMessage(pre + "There is no callable player!");
				}
				return true;
			} else if (args[0].equalsIgnoreCase("block") && args.length == 2) {
				blocked.set(sender.getName() + "." + args[1], true);
				Basic.save(blocked, "blocked", sender);
				sender.sendMessage(pre + "Player " + args[1] + " blocked!");
				return true;
			} else if (args[0].equalsIgnoreCase("unblock") && args.length == 2) {
				blocked.set(sender.getName() + "." + args[1], null);
				Basic.save(blocked, "blocked", sender);
				sender.sendMessage(pre + "Player " + args[1] + " unblocked!");
				return true;
			} else {
				showHelp(sender);
			}
		} else {
			showHelp(sender);
		}
		return false;
	}

	/**
	 * This method generate a string with all callable players
	 * 
	 * @return a string with all callable players
	 */
	private String getCallable() {
		JSONObject jsonObject = ApiAccess.apiGet("/registrant?appkey="
				+ config.getString("AppKey"));
		jsonObject = (JSONObject) jsonObject.get("NetworkidListResponse");
		JSONArray jsonArray = (JSONArray) jsonObject.get("networkids");
		StringBuilder callable = new StringBuilder();
		Object[] a = jsonArray.toArray();
		for (Object o : a)
			callable.append(o + ",");
		if (callable.toString().endsWith(","))
			callable.deleteCharAt(callable.length());
		return callable.toString();
	}

	private void setBaseUrl(String url) {
		config.set("BaseUrl", url);
		Basic.save(config, "config", null);
		config = YamlConfiguration.loadConfiguration(new File(
				"plugins/TeleSocial/config.yml"));
	}

	private void setAppkey(String key) {
		config.set("AppKey", key);
		Basic.save(config, "config", null);
		config = YamlConfiguration.loadConfiguration(new File(
				"plugins/TeleSocial/config.yml"));
	}

	/**
	 * This method shows the help to the player
	 * 
	 * @param p
	 *            The CommandSender(usually a Player)
	 * @return void
	 */
	private void showHelp(CommandSender p) {
		p.sendMessage(pre
				+ "Register yourself with /phone register <YourPhoneNumber> to speak with other user of this server!");
		p.sendMessage(pre + "Call them with /phone start <ConferenceName>!");
		p.sendMessage(pre
				+ "Join a conference with /phone join <ConferenceName>");
	}

	/**
	 * This method starts a conference
	 * 
	 * @param conference
	 *            The name of the conference
	 * @param networkid
	 *            The networkID(will be the leader of the conference)
	 * @param p
	 *            The Player who started the conference
	 * @return void
	 */
	private boolean startConference(String conference, String networkid,
			CommandSender p) {
		JSONObject con = ApiAccess.apiPost("conference", "networkid",
				p.getName(), null, null);
		con = (JSONObject) con.get("ConferenceResponse");
		if (con != null) {
			if ("201".equals((String) con.get("status"))) {
				conferences.put(conference, (String) con.get("conferenceId"));
				return true;
			} else {
				String error = (String) con.get("status");
				p.sendMessage(pre + "Unable to create conference: " + error);
			}
		}
		return false;
	}

	/**
	 * This method registers the networkID
	 * 
	 * @param number
	 *            The mobile number to register
	 * @param networkID
	 *            The networkID to register
	 * @param sender
	 *            The player to send the registration response to
	 */
	private boolean register(String number, String networkID,
			CommandSender sender) {
		number = number.replaceAll("[^0-9]+", "");
		JSONObject response = ApiAccess.apiPost("registrant/", "networkid",
				networkID, "phone", number);
		response = (JSONObject) response.get("RegistrationResponse");
		String status = (String) response.get("status");
		if ("201".equals(status)) {
			players.set(sender.getName(), networkID);
			Basic.save(players, "players", sender);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method checks if name is already registered
	 * 
	 * @param name
	 * @return
	 */
	private boolean isRegistered(String name) {
		boolean registered = false;
		JSONObject response = ApiAccess.apiPost("registrant/" + name, null, null,
				null, null);
		if ("200".equals((String)response.get("status")))
			registered = true;
		return registered;
	}
	private boolean changeNumber(String networkID, String number){
		JSONObject response = ApiAccess.apiPost("registrant/" + networkID + "/" + number, null, null, null, null);
		response = (JSONObject) response.get("RegistrationResponse");
		String status = (String) response.get("status");
		if("202".equals(status))
			return true;
		return false;
	}

	/**
	 * This method adds Player p to the conference conference
	 * 
	 * @param conference
	 *            Name of the conference
	 * @param sender
	 *            Player to add to conference
	 */
	private boolean joinConference(String conference, CommandSender sender) {
		String confID = conferences.get(conference);
		JSONObject response = ApiAccess.apiPost("conference/" + confID, "networkid",
				players.getString(sender.getName()), "action", "add");
		response = (JSONObject) response.get("ConferenceResponse");
		if (response != null) {
			String status = (String) response.get("status");
			if ("202".equals(status))
				return true;
			else {
				sender.sendMessage(pre + "Error: " + status);
				return false;
			}
		} else
			return false;
	}

	/**
	 * This method deletes/stops the conference conference
	 * 
	 * @param conference
	 *            The name of the conference
	 * @param sender
	 *            The Player to send the response to
	 */
	private boolean deleteConference(String conference, CommandSender sender) {
		String confID = conferences.get(conference);
		JSONObject response = ApiAccess.apiPost("conference/" + confID, "action",
				"close", null, null);
		response = (JSONObject) response.get("ConferenceResponse");
		if (response != null) {
			String status = (String) response.get("status");
			if (status.contains("200")) {
				sender.sendMessage(pre + "Conference deleted!");
				conferences.remove(conference);
				return true;
			} else 
				sender.sendMessage(pre + "Error: " + status);
		}
		return false;
	}

	/**
	 * This method creates a call with the sender as conferenceleader. The
	 * conferencename is the name of the leader.
	 * 
	 * @param sender
	 *            The conference leader
	 * @param call
	 *            List of users to call
	 */
	private boolean call(CommandSender sender, List<String> call) {
		for (String s : call) {
			if (blocked.getBoolean(s + "." + sender.getName()))
				call.remove(s);
		}
		if (call.size() == 0)
			return false;
		boolean started = false;
		if (call.size() > 0)
			started = startConference(sender.getName(),
					players.getString(sender.getName()), sender);
		for (String s : call) {
			boolean success = addToConference(sender.getName(), s);
			if (!success)
				sender.sendMessage(pre + "Unable to add player " + s
						+ " to the call!");
		}
		return started;
	}

	/**
	 * This method adds offline users to a conference
	 * 
	 * @param conference
	 *            The name of the conference
	 * @param toAdd
	 *            The name of the player to add to the conference
	 */
	private boolean addToConference(String conference, String toAdd) {
		String confID = conferences.get(conference);
		JSONObject response = ApiAccess.apiPost("conference/" + confID, "networkid",
				players.getString(toAdd), "action", "add");
		response = (JSONObject) response.get("ConferenceResponse");
		if (response != null) {
			String status = (String) response.get("status");
			if (status.contains("202"))
				return true;
		}
		return false;
	}
}
