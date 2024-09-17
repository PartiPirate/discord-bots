package fr.partipirate.discord.bots.congressus.listeners;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONWriter;

import fr.partipirate.discord.bots.congressus.commands.personae.PersonaeHelper;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.Guild;

public class OnConnectionHandler extends ListenerAdapter {

	private static List<String> DO_NOT_DISCARD = new ArrayList<>();
	private static Map<String, String> TRANSLITERATIONS = new HashMap<>();
	private static OnConnectionHandler INSTANCE;
	private static List<String> WARNED_USERS = new ArrayList<String>();
	private static String RIGHT_MESSAGE;
	private static final String RIGHTS_FILE = "right_warned_users.json";

	static {
//		DO_NOT_DISCARD.add("Administration");
//		DO_NOT_DISCARD.add("DJ Pirate");
//		DO_NOT_DISCARD.add("Camarades des Pirates");
//		TRANSLITERATIONS.put("Secrétaire de la Coordination Nationale", "Secrétaire de la CN");
//		TRANSLITERATIONS.put("Languedoc-Roussillon-Midi-Pyrénées", "Occitanie");
//		TRANSLITERATIONS.put("Nord-Pas-de-Calais-Picardie", "Hauts-de-France");
//		TRANSLITERATIONS.put("Alsace-Champagne-Ardennes-Lorraine", "Grand-Est");
//		TRANSLITERATIONS.put("Aquitaine-Limousin-Poitou-Charente", "Nouvelle-Aquitaine");
	}

	static {
		readConfiguration();		
	}

	public static OnConnectionHandler getInstance() {
		return INSTANCE;
	}

	public OnConnectionHandler() {
		INSTANCE = this;
	}
	
	@Override
	public void onUserUpdateOnlineStatus(UserUpdateOnlineStatusEvent event) {
		Member member = event.getGuild().getMember(event.getUser());

		if (member.getOnlineStatus() == OnlineStatus.ONLINE && event.getOldOnlineStatus() == OnlineStatus.OFFLINE) {
//			if (!member.getEffectiveName().equals("farlistener")) return; // test only
			System.out.println("Test permissions on " + getFullUser(event.getUser()));
			updateMember(member);
		}
	}

	public void updateMember(Member member) {
		updateMember(member, true);
	}

	public void updateMember(Member member, boolean doWarn) {
		List<String> groups = getMemberGroups(member, doWarn);
		addDiscordRoles(member, groups);
		discardDiscordRoles(member, groups);
	}

	private List<String> getMemberGroups(Member member, boolean doWarn) {
		List<String> groups = new ArrayList<>();
		
		try {
			String nickname = getFullUser(member.getUser());
			nickname = URLEncoder.encode(nickname, java.nio.charset.StandardCharsets.UTF_8.toString());
			
			String getMemberUrl = PersonaeHelper.getUrl("do_getMember") + "&nickname=" + nickname;
			
			System.out.println(getMemberUrl);
			
			URL url = new URL(getMemberUrl);
			URLConnection connection = url.openConnection();
			InputStreamReader sr = new InputStreamReader(connection.getInputStream());
			StringWriter sw = new StringWriter();
			
			char[] buffer = new char[8192];
			int nbRead;
			
			while ((nbRead = sr.read(buffer)) != -1) {
				sw.write(buffer, 0, nbRead);
			}

			sr.close();
			sw.close();

			String json = sw.toString();
			
			JSONObject object = (JSONObject) new JSONTokener(json).nextValue();
			
			if (object.has("error")) {
				if (doWarn && !isWarnedUser(member.getUser())) {
					warnUser(member.getUser());
				}
			}
			else if (object.has("groups")) {
				JSONObject resultGroups = object.getJSONObject("groups");

//				System.out.println(resultGroups);
	
				for (String groupName : resultGroups.keySet()) {
	//				System.out.println(groupName);
	//				System.out.println("is " + transliterate(groupName));
					groups.add(transliterate(groupName));
				}
			}
		}
		catch(Exception e) {
		}
			
		return groups;
	}

	private static String transliterate(String groupName) {
		String transliteration = TRANSLITERATIONS.get(groupName);
		
		return transliteration != null ? transliteration : groupName;
	}

	private void addDiscordRoles(Member member, List<String> groups) {
		Guild guild = member.getGuild();
		
		for (String group : groups) {
			System.out.println("Search role for : " + group);

			if (group.length() > 32) {
				group = group.substring(0, 32);
				System.out.println("Search role (reduce) for : " + group);
			}

			List<Role> roles = RoleHandler.getInstance().guildRoles.get(member.getGuild());
			Role role = null;
			for(int index = 0; role == null && index < roles.size(); ++index) {
				Role indexedRole = roles.get(index);
				
				System.out.println("Compare role : " + group + " to existing one : " + indexedRole.getName());

				if (indexedRole.getName().equalsIgnoreCase(group)) {
					role = indexedRole;
					System.out.println("\tFound !");
					break;
				}
			}

			if (role == null) {
				System.out.println("\tRole \"" + group + "\" not found into the list");

				role = guild.createRole().complete();
				role.getManager().setName(group).complete();

				// Do it a second time if the RoleHandler doesn't catch the role create event
				RoleHandler.getInstance().addRoleOnGuild(member.getGuild(), role);
			}

/*
			List<Role> memberRoles = member.getRoles();
			memberRoles.add(role);
			guild.modifyMemberRoles(member, memberRoles).complete();
*/
			guild.addRoleToMember(member, role).complete();
			System.out.println("Add role : " + role + " to member " + member.getNickname() + " / " + member.getEffectiveName());
		}
	}

	private void discardDiscordRoles(Member member, List<String> groups) {
		List<Role> memberRoles = member.getRoles();
		boolean rolesUpdated = false;
		Guild guild = member.getGuild();

		for (Role role : memberRoles) {
			boolean isNotDiscarded = false;
			for (String group : DO_NOT_DISCARD) {
				if (role.getName().equalsIgnoreCase(group)) {
					isNotDiscarded = true;
					break;
				}
			}

			if (isNotDiscarded) continue;

			for (String group : groups) {
				if (role.getName().equalsIgnoreCase(group)) {
					isNotDiscarded = true;
					break;
				}
			}

			if (isNotDiscarded) continue;

			guild.removeRoleFromMember(member, role).complete();
//			memberRoles.remove(role);
			rolesUpdated = true;
			System.out.println("Remove role : " + role + " to member " + member.getNickname() + " / " + member.getEffectiveName());
		}
/*
		if (rolesUpdated) {
			guild.modifyMemberRoles(member, memberRoles);
		}
*/		
	}

	private boolean isWarnedUser(User user) {
		boolean isWarnedUser =  WARNED_USERS.contains(getFullUser(user));

//		System.out.println("isWarnedUser : " + isWarnedUser);

		return isWarnedUser;
	}

	private String getFullUser(User user) {
		/**
		 * Get unique full user name
		 * for migrated accounts, it's only the name
		 * for old accounts, it's name with discriminator
		 */
		String username = user.getName();
		// for compatibility with old accounts
		String discriminator = user.getDiscriminator();
		if (!discriminator.equals("0000")) {
			username += "#" + discriminator;
		}
		return username;
	}

	private void warnUser(User user) {
    	MessageFormat messageFormat = new MessageFormat(RIGHT_MESSAGE);

		user.openPrivateChannel().queue((channel) ->
        {
            channel.sendMessage(messageFormat.format(new String[] {user.getName()})).queue();
//            channel.sendMessage(message.toString()).queue();
        });

		WARNED_USERS.add(getFullUser(user));
		
		writeConfiguration();
	}

	public static void readConfiguration() {
		try {
			FileInputStream fis = new FileInputStream(RIGHTS_FILE);
			JSONObject jsonObject = (JSONObject) new JSONTokener(fis).nextValue();

			RIGHT_MESSAGE = jsonObject.getString("message");

			JSONArray welcomed = jsonObject.getJSONArray("users");
			for(int index = 0; index < welcomed.length(); ++index) {
				WARNED_USERS.add(welcomed.getString(index));
			}

			JSONArray doNotDiscards = jsonObject.getJSONArray("do_not_discards");
			for(int index = 0; index < doNotDiscards.length(); ++index) {
				DO_NOT_DISCARD.add(doNotDiscards.getString(index));
			}

			fis.close();
		}
		catch (IOException e) {
		}
	}

	public static void writeConfiguration() {
		try {
			FileWriter fw = new FileWriter(RIGHTS_FILE);
			JSONWriter writer = new JSONWriter(fw);

			writer.object();
			writer.key("message").value(RIGHT_MESSAGE);
			writer.key("users").array();

			for (String warnedUser : WARNED_USERS) {
				writer.value(warnedUser);
			}

			writer.endArray();

			writer.key("do_not_discards").array();

			for (String doNotDiscard : DO_NOT_DISCARD) {
				writer.value(doNotDiscard);
			}

			writer.endArray();

			writer.endObject();

			fw.flush();
			fw.close();
		}
		catch (IOException e) {
		}
	}
}
