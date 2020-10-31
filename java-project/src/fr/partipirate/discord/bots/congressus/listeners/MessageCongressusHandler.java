package fr.partipirate.discord.bots.congressus.listeners;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import fr.partipirate.discord.bots.congressus.Configuration;
import fr.partipirate.discord.bots.congressus.CongressusBot;
import fr.partipirate.discord.bots.congressus.GuildMusicManager;
import fr.partipirate.discord.bots.congressus.commands.congressus.CongressusHelper;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.managers.GuildController;
import net.dv8tion.jda.core.managers.RoleManager;
import net.dv8tion.jda.core.requests.restaction.ChannelAction;

public class MessageCongressusHandler extends ListenerAdapter {
	private static long DELAY = 600000L; // 10mn, by default
	private static String CONSUMER = "discord"; // discord, by default
	private static MessageCongressusHandler INSTANCE;
	private CongressusBot congressusBot;

	public MessageCongressusHandler(CongressusBot congressusBot) {
		this.congressusBot = congressusBot;

		launchMessageConsumerThread();

		if (Configuration.getInstance().OPTIONS.get("message") != null) {
			String delayString = Configuration.getInstance().OPTIONS.get("message").get("delay");
			if (delayString != null) {
				DELAY = Integer.parseInt(delayString);
			}

			String consumer = Configuration.getInstance().OPTIONS.get("message").get("consumer");
			if (delayString != null) {
				CONSUMER = consumer;
			}
		}
		
		
		INSTANCE = this;
	}

	public static MessageCongressusHandler getInstance() {
		return INSTANCE;
	}

	private void launchMessageConsumerThread() {
		Thread messageConsumerThreadThread = new Thread() {
			@Override
			public void run() {
				while (true) { // check the bot connection status
					synchronized (this) {
						try {
//							System.out.println("Check radio status");
							checkMessages();
							this.wait(DELAY);
						} 
						catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		};
		messageConsumerThreadThread.start();
	}

	protected void checkMessages() {
		try {
			URL url = new URL(CongressusHelper.getMessagesUrl());
			URLConnection connection = url.openConnection();
			String json = getConnectionContent(connection);

			System.out.println(json);

			JSONObject object = (JSONObject) new JSONTokener(json).nextValue();
			JSONArray messages = object.getJSONArray("messages");

			List<BigInteger> consumedIds = new ArrayList<BigInteger>();

			for (int messageIndex = 0; messageIndex < messages.length(); messageIndex++) {
				System.out.println("Index : " + messageIndex);

				JSONObject message = messages.getJSONObject(messageIndex);

				System.out.println("Message : " + message);

				BigInteger id  = message.getBigInteger("mes_id");
				String consumer  = message.getString("mes_to");

				System.out.println("Id : " + id);
				System.out.println("Consumer : " + consumer);

				JSONObject internalMessage = message.getJSONObject("mes_message");

				boolean handled = handleMessage(id, consumer, internalMessage);

				if (handled) {
					consumedIds.add(id);
				}
			}

			String consumeMessageUrl = CongressusHelper.getConsumeMessageUrl(consumedIds); 

			System.out.println(consumeMessageUrl);

			url = new URL(consumeMessageUrl);
			connection = url.openConnection();
			getConnectionContent(connection);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private boolean handleMessage(BigInteger id, String consumer, JSONObject message) {
		System.out.println("Id : " + id + ", consumer :" + consumer);
		
		if (!consumer.equals(CONSUMER)) {
			System.out.println("Not for me");
			return false;
		}

		String action = message.getString("action");
		String type = message.getString("type");

		System.out.println(action + "-" + type);

		try {
			switch(action + "-" + type) {
				case "mute-all":
					return muteAll(message);
				case "mute-user":
					return muteUser(message);
				case "unmute-user":
					return unmuteUser(message);
				case "play-jingle":
					return playJingle(message);
				case "play-track":
					return playTrack(message);
				case "create-message":
					return createMessage(message);
				case "create-role":
					return createRole(message);
				case "addinchannel-role":
					return addRoleInChannel(message);
				case "create-channel":
					return createChannel(message);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			return true;
		}

		return false;
	}

	private boolean muteAll(JSONObject message) {
		List<String> exceptIds = new ArrayList<String>();

		for (AudioManager audioManager : congressusBot.getJDA().getAudioManagers()) {
			VoiceChannel voiceChannel = audioManager.getConnectedChannel();
			List<Member> members = voiceChannel.getMembers();

			for (Member member : members) {
				if (exceptIds.size() == 0 || !exceptIds.contains(member.getUser().getId())) {
					audioManager.getGuild().getController().setMute(member, true);
				}
			}
		}

		return true;
	}

	private boolean muteUser(JSONObject message) {
		for (AudioManager audioManager : congressusBot.getJDA().getAudioManagers()) {
			VoiceChannel voiceChannel = audioManager.getConnectedChannel();
			List<Member> members = voiceChannel.getMembers();

			for (Member member : members) {
				if (member.getUser().getId() == message.getString("id")) {
					audioManager.getGuild().getController().setMute(member, true);
					return true;
				}
			}
		}

		return true;
	}

	private boolean unmuteUser(JSONObject message) {
		for (AudioManager audioManager : congressusBot.getJDA().getAudioManagers()) {
			VoiceChannel voiceChannel = audioManager.getConnectedChannel();
			List<Member> members = voiceChannel.getMembers();

			for (Member member : members) {
				if (member.getUser().getId() == message.getString("id")) {
					audioManager.getGuild().getController().setMute(member, false);
					return true;
				}
			}
		}

		return true;
	}

	private boolean playJingle(JSONObject message) {
		Map<Long, GuildMusicManager> managers = congressusBot.getMusicManagers();

		for (Iterator<GuildMusicManager> iterator = managers.values().iterator(); iterator.hasNext();) {
			GuildMusicManager manager = iterator.next();
			manager.scheduler.clear();
			manager.scheduler.nextTrack();

			RadioHandler.getInstance().getJingle(manager, Integer.toString(message.getInt("data")));
		}

		return true;
	}

	private boolean playTrack(JSONObject message) {
		Map<Long, GuildMusicManager> managers = congressusBot.getMusicManagers();

		for (Iterator<GuildMusicManager> iterator = managers.values().iterator(); iterator.hasNext();) {
			GuildMusicManager manager = iterator.next();
			manager.scheduler.clear();
			manager.scheduler.nextTrack();

			RadioHandler.getInstance().getTrack(manager, Integer.toString(message.getInt("data")));
		}

		return true;
	}

	private boolean createMessage(JSONObject message) {
		Guild guild = congressusBot.getJDA().getGuilds().get(0);

		String channelName = message.getString("channel");
		TextChannel textChannel = getTextChannel(guild, channelName);

		if (textChannel == null) return false;

		String content = message.getString("content"); 

		Message discordMessage = textChannel.sendMessage(content).complete();
		String discordMessageId = discordMessage.getId();

		if (message.has("isPinned") && message.getBoolean("isPinned")) {
			textChannel.pinMessageById(discordMessageId).complete();
		}

		return true;
	}
	
	private boolean createChannel(JSONObject message) {
		String label = message.getString("label");

		Guild guild = congressusBot.getJDA().getGuilds().get(0);
		GuildController controller = new GuildController(guild);

		ChannelAction channel = controller.createTextChannel(label);

		if (message.has("topic")) {
			String topic = message.getString("topic");
			channel.setTopic(topic);
		}

		if (message.has("category")) {
			String category = message.getString("category");
			List<Category> categories = guild.getCategoriesByName(category, true);
			
			if (categories.size() > 0) {
				channel.setParent(categories.get(0));
			}
		}

		channel.complete();

		JSONArray rights = message.getJSONArray("rights");
		Role everyoneRole = guild.getPublicRole();
		TextChannel textChannel = getTextChannel(guild, label);

		setChannelRolePermissions(textChannel, everyoneRole, rights);

		System.out.println("Create channel");

		return true;
	}

	private boolean createRole(JSONObject message) {
		Guild guild = congressusBot.getJDA().getGuilds().get(0);
		GuildController controller = new GuildController(guild);

		Role role = controller.createRole().complete();
		RoleManager roleManager = role.getManager();

		String label = message.getString("label");
		roleManager.setName(label);

		if (message.has("color")) {
			Color color = Color.decode(message.getString("color"));
			roleManager.setColor(color);
		}

		roleManager.complete();

		System.out.println("Create role");

		if (message.has("permissions")) {
			JSONArray permissions = message.getJSONArray("permissions");
			for (int permissionIndex = 0; permissionIndex < permissions.length(); permissionIndex++) {
				JSONObject permission = permissions.getJSONObject(permissionIndex);
				String channelName = permission.getString("channel");

				TextChannel textChannel = getTextChannel(guild, channelName);

				if (textChannel == null) continue;

				JSONArray rights = permission.getJSONArray("rights");
				setChannelRolePermissions(textChannel, role, rights);
			}
		}

		return true;
	}

	private boolean addRoleInChannel(JSONObject message) {
		Guild guild = congressusBot.getJDA().getGuilds().get(0);

		String roleLabel = message.getString("role");
		Role role = getRole(guild, roleLabel);

		if (role == null) return false;

		String channelName = message.getString("channel");
		TextChannel textChannel = getTextChannel(guild, channelName);

		if (textChannel == null) return false;

		JSONArray rights = message.getJSONArray("rights");
		setChannelRolePermissions(textChannel, role, rights);

		System.out.println("Add role in channel");

		return true;
	}

	private TextChannel getTextChannel(Guild guild, String channelName) {
		TextChannel textChannel = null;

		for (Channel channel : guild.getChannels()) {
			if ((channel instanceof TextChannel) && channel.getName().equalsIgnoreCase(channelName)) {
				textChannel = (TextChannel) channel;
				break;
			}
		}

		return textChannel;
	}

	private Role getRole(Guild guild, String roleName) {
		for (Role role : guild.getRoles()) {
			if (role.getName().equalsIgnoreCase(roleName)) return role;
		}

		return null;
	}

	private void setChannelRolePermissions(TextChannel textChannel, Role role, JSONArray rights) {
		List<Permission> allowPermissions = new ArrayList<Permission>();
		List<Permission> denyPermissions = new ArrayList<Permission>();

		for (int rightIndex = 0; rightIndex < rights.length(); rightIndex++) {
			String right = rights.getString(rightIndex);

			switch (right) {
				case "ALLOW_CREATE_INSTANT_INVITE":
					allowPermissions.add(Permission.CREATE_INSTANT_INVITE);
					break;
				case "DENY_CREATE_INSTANT_INVITE":
					denyPermissions.add(Permission.CREATE_INSTANT_INVITE);
					break;
				case "ALLOW_MANAGE_CHANNEL":
					allowPermissions.add(Permission.MANAGE_CHANNEL);
					break;
				case "DENY_MANAGE_CHANNEL":
					denyPermissions.add(Permission.MANAGE_CHANNEL);
					break;
				case "ALLOW_MANAGE_PERMISSIONS":
					allowPermissions.add(Permission.MANAGE_PERMISSIONS);
					break;
				case "DENY_MANAGE_PERMISSIONS":
					denyPermissions.add(Permission.MANAGE_PERMISSIONS);
					break;
				case "ALLOW_MANAGE_WEBHOOKS":
					allowPermissions.add(Permission.MANAGE_WEBHOOKS);
					break;
				case "DENY_MANAGE_WEBHOOKS":
					denyPermissions.add(Permission.MANAGE_WEBHOOKS);
					break;
				case "ALLOW_MESSAGE_READ":
					allowPermissions.add(Permission.MESSAGE_READ);
					break;
				case "DENY_MESSAGE_READ":
					denyPermissions.add(Permission.MESSAGE_READ);
					break;
				case "ALLOW_MESSAGE_WRITE":
					allowPermissions.add(Permission.MESSAGE_WRITE);
					break;
				case "DENY_MESSAGE_WRITE":
					denyPermissions.add(Permission.MESSAGE_WRITE);
					break;
				case "ALLOW_MESSAGE_TTS":
					allowPermissions.add(Permission.MESSAGE_TTS);
					break;
				case "DENY_MESSAGE_TTS":
					denyPermissions.add(Permission.MESSAGE_TTS);
					break;
				case "ALLOW_MESSAGE_MANAGE":
					allowPermissions.add(Permission.MESSAGE_MANAGE);
					break;
				case "DENY_MESSAGE_MANAGE":
					denyPermissions.add(Permission.MESSAGE_MANAGE);
					break;
				case "ALLOW_MESSAGE_EMBED_LINKS":
					allowPermissions.add(Permission.MESSAGE_EMBED_LINKS);
					break;
				case "DENY_MESSAGE_EMBED_LINKS":
					denyPermissions.add(Permission.MESSAGE_EMBED_LINKS);
					break;
				case "ALLOW_MESSAGE_ATTACH_FILES":
					allowPermissions.add(Permission.MESSAGE_ATTACH_FILES);
					break;
				case "DENY_MESSAGE_ATTACH_FILES":
					denyPermissions.add(Permission.MESSAGE_ATTACH_FILES);
					break;
				case "ALLOW_MESSAGE_HISTORY":
					allowPermissions.add(Permission.MESSAGE_HISTORY);
					break;
				case "DENY_MESSAGE_HISTORY":
					denyPermissions.add(Permission.MESSAGE_HISTORY);
					break;
				case "ALLOW_MESSAGE_MENTION_EVERYONE":
					allowPermissions.add(Permission.MESSAGE_MENTION_EVERYONE);
					break;
				case "DENY_MESSAGE_MENTION_EVERYONE":
					denyPermissions.add(Permission.MESSAGE_MENTION_EVERYONE);
					break;
				case "ALLOW_MESSAGE_EXT_EMOJI":
					allowPermissions.add(Permission.MESSAGE_EXT_EMOJI);
					break;
				case "DENY_MESSAGE_EXT_EMOJI":
					denyPermissions.add(Permission.MESSAGE_EXT_EMOJI);
					break;
				case "ALLOW_MESSAGE_ADD_REACTION":
					allowPermissions.add(Permission.MESSAGE_ADD_REACTION);
					break;
				case "DENY_MESSAGE_ADD_REACTION":
					denyPermissions.add(Permission.MESSAGE_ADD_REACTION);
					break;
				case "ALLOW_ALL":
					allowPermissions.add(Permission.CREATE_INSTANT_INVITE);
					allowPermissions.add(Permission.MANAGE_CHANNEL);
					allowPermissions.add(Permission.MANAGE_PERMISSIONS);
					allowPermissions.add(Permission.MANAGE_WEBHOOKS);
					allowPermissions.add(Permission.MESSAGE_READ);
					allowPermissions.add(Permission.MESSAGE_WRITE);
					allowPermissions.add(Permission.MESSAGE_TTS);
					allowPermissions.add(Permission.MESSAGE_MANAGE);
					allowPermissions.add(Permission.MESSAGE_EMBED_LINKS);
					allowPermissions.add(Permission.MESSAGE_ATTACH_FILES);
					allowPermissions.add(Permission.MESSAGE_HISTORY);
					allowPermissions.add(Permission.MESSAGE_MENTION_EVERYONE);
					allowPermissions.add(Permission.MESSAGE_EXT_EMOJI);
					allowPermissions.add(Permission.MESSAGE_ADD_REACTION);
					break;
				case "DENY_ALL":
					denyPermissions.add(Permission.CREATE_INSTANT_INVITE);
					denyPermissions.add(Permission.MANAGE_CHANNEL);
					denyPermissions.add(Permission.MANAGE_PERMISSIONS);
					denyPermissions.add(Permission.MANAGE_WEBHOOKS);
					denyPermissions.add(Permission.MESSAGE_READ);
					denyPermissions.add(Permission.MESSAGE_WRITE);
					denyPermissions.add(Permission.MESSAGE_TTS);
					denyPermissions.add(Permission.MESSAGE_MANAGE);
					denyPermissions.add(Permission.MESSAGE_EMBED_LINKS);
					denyPermissions.add(Permission.MESSAGE_ATTACH_FILES);
					denyPermissions.add(Permission.MESSAGE_HISTORY);
					denyPermissions.add(Permission.MESSAGE_MENTION_EVERYONE);
					denyPermissions.add(Permission.MESSAGE_EXT_EMOJI);
					denyPermissions.add(Permission.MESSAGE_ADD_REACTION);
					break;
			}
		}

//		role.getManager().setPermissions(rolePermissions).complete();
		textChannel.createPermissionOverride(role).setAllow(allowPermissions).setDeny(denyPermissions).complete();
	}
	
	public String getConnectionContent(URLConnection connection) throws IOException {
		InputStreamReader sr = new InputStreamReader(connection.getInputStream());
		StringWriter sw = new StringWriter();

		char[] buffer = new char[8192];
		int nbRead;

		while ((nbRead = sr.read(buffer)) != -1) {
			sw.write(buffer, 0, nbRead);
		}

		sr.close();
		sw.close();

		return sw.toString();
	}
}