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

				if (isRegistered(sender.getName())) {
					boolean b = changeNumber(networkID, number);
					if (b)
						sender.sendMessage(pre + "Registration complete!");
					else
						sender.sendMessage(pre + "Registration failed!");
				} else {
					boolean b = register(number, networkID, sender);
					if (b)
						sender.sendMessage(pre + "Registration complete!");
					else
						sender.sendMessage(pre + "Registration failed!");
				}
				return true;
			} else if (args[0].equalsIgnoreCase("unregister")) {
				players.set(sender.getName(), false);
				Basic.save(players, "players", sender);
				sender.sendMessage(pre + "You are unregistered!");
				return true;
			} else if (args[0].equalsIgnoreCase("start") && args.length == 2) {
				String conference = args[1];
				if (!players.getBoolean(sender.getName())) {
					sender.sendMessage(pre + "You have to register first!");
					return true;
				}
				boolean start = startConference(conference, sender);
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
				if (!players.getBoolean(sender.getName())) {
					sender.sendMessage(pre + "You have to register first!");
					return true;
				}
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
				if (!players.getBoolean(sender.getName())) {
					sender.sendMessage(pre + "You have to register first!");
					return true;
				}
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
				if (!isRegistered(sender.getName())) {
					sender.sendMessage(pre + "You have to register first!");
					return true;
				}
				List<String> toCall = new ArrayList<String>(Arrays.asList(args));
				toCall.remove(0);
				boolean success = call(sender, toCall);
				if (!success)
					sender.sendMessage(pre + "Unable to create call!");
				else
					sender.sendMessage(pre + "Calling " + toCall.toString().replaceAll("\\[", "").replaceAll("\\]", ""));
				return true;

			} else if (args[0].equalsIgnoreCase("list") && args.length == 1) {
				String callable = getCallable(sender);
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
	private String getCallable(CommandSender sender) {
		JSONObject jsonObject = ApiAccess.apiGet("registrant?appkey="
				+ config.getString("AppKey"));
		jsonObject = (JSONObject) jsonObject.get("NetworkidListResponse");
		JSONArray jsonArray = (JSONArray) jsonObject.get("networkids");
		StringBuilder callable = new StringBuilder();
		Object[] a = jsonArray.toArray();
		for (Object o : a)
			if (players.getBoolean(o.toString())
					&& (!blocked.getBoolean(o.toString() + sender.getName())))
				callable.append(o + ", ");
		if (callable.toString().endsWith(", "))
			callable.deleteCharAt(callable.length() - 1);
		callable.deleteCharAt(callable.length() - 1);
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
	 * @param p
	 *            The Player who started the conference
	 * @return void
	 */
	private boolean startConference(String conference, CommandSender p) {
		JSONObject con = ApiAccess.apiPost("conference", "networkid",
				p.getName(), null, null);
		con = (JSONObject) con.get("ConferenceResponse");
		if (con != null) {
			String status = con.get("status").toString();
			if ("201".equals(status)) {
				conferences.put(conference, (String) con.get("conferenceId"));
				return true;
			} else {
				System.out.println(Basic.consolePre + "Error while starting conference " + conference + ":\nStatus: " + status);
				p.sendMessage(pre + "Unable to create conference: " + status);
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
		if (response.containsKey("ErrorResponse")) {
			sender.sendMessage(pre
					+ "An error occured: "
					+ ((JSONObject) response.get("ErrorResponse")).get(
							"message").toString());
			return false;
		}
		response = (JSONObject) response.get("RegistrationResponse");
		String status = response.get("status").toString();
		if ("201".equals(status)) {
			players.set(sender.getName(), true);
			Basic.save(players, "players", sender);
			return true;
		} else {
			System.out.println(Basic.consolePre
					+ "Error while registering networkID " + networkID
					+ ":\nStatus: " + status);
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
		JSONObject response = ApiAccess.apiPost("registrant/" + name, "query",
				"related", null, null);
		response = (JSONObject) response.get("RegistrantResponse");
		String status = response.get("status").toString();
		if ("200".equals(status))
			registered = true;
		return registered;
	}

	private boolean changeNumber(String networkID, String number) {
		JSONObject response = ApiAccess.apiPost("registrant/" + networkID + "/"
				+ number, null, null, null, null);
		response = (JSONObject) response.get("RegistrationResponse");
		String status = response.get("status").toString();
		if ("202".equals(status)){
			players.set(networkID, true);
			Basic.save(players, "players", null);
			return true;
		}
		else
			System.out.println(Basic.consolePre + "Error while changing number of networkID " + networkID + " to " + number + ":\nStatus: " + status);
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
		JSONObject response = ApiAccess.apiPost("conference/" + confID,
				"networkid", sender.getName(), "action", "add");
		response = (JSONObject) response.get("ConferenceResponse");
		if (response != null) {
			String status = response.get("status").toString();
			if ("202".equals(status))
				return true;
			else {
				System.out.println(Basic.consolePre + "Error while joining conference " + conference + ":\nStatus: " + status);
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
		JSONObject response = ApiAccess.apiPost("conference/" + confID,
				"action", "close", null, null);
		response = (JSONObject) response.get("ConferenceResponse");
		if (response != null) {
			String status = response.get("status").toString();
			if ("200".equals(status)) {
				sender.sendMessage(pre + "Conference deleted!");
				conferences.remove(conference);
				return true;
			} else{
				System.out.println(Basic.consolePre + "Error while deleting conference " + conference + ":\nStatus: " + status);
				sender.sendMessage(pre + "Error: " + status);
			}
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
			if (!players.getBoolean(s))
				call.remove(s);
		}
		if (call.size() == 0)
			return false;
		boolean started = false;
		if (call.size() > 0)
			started = startConference(sender.getName(), sender);
		for (String s : call) {
			if (!isRegistered(s)) {
				sender.sendMessage(pre + s + " has to register first!");
				continue;
			}
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
		JSONObject response = ApiAccess.apiPost("conference/" + confID,
				"networkid", toAdd, "action", "add");
		response = (JSONObject) response.get("ConferenceResponse");
		if (response != null) {
			String status = response.get("status").toString();
			if ("202".equals(status))
				return true;
			else
				System.out.println(Basic.consolePre + "Error while adding "
						+ toAdd + " to conference " + conference
						+ ":\nStatus: " + status);
		}
		return false;
	}
}
