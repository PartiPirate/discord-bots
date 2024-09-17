package fr.partipirate.discord.bots.congressus.listeners;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONWriter;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class FirstMemberConnectionHandler extends ListenerAdapter {

	private static List<String> warnedUsers = new ArrayList<String>();
	private static String WELCOME_MESSAGE;
	private static final String WELCOME_FILE = "welcome.json";

	static {
		readConfiguration();		
	}
	
	@Override
	public void onUserUpdateOnlineStatus(UserUpdateOnlineStatusEvent event) {
		Member member = event.getGuild().getMember(event.getUser());
		
		if (member.getOnlineStatus() == OnlineStatus.ONLINE && event.getOldOnlineStatus() == OnlineStatus.OFFLINE && !isWarnedUser(event.getUser())) {
			warnUser(event.getJDA(), event.getUser());
		}
	}

	private String getFullUser(User user) {
		String username = user.getName();
		// for compatibility with old accounts
		String discriminator = user.getDiscriminator();
		if (!discriminator.equals("0000")) {
			username += "#" + discriminator;
		}
		return username;
	}

	private boolean isWarnedUser(User user) {
		boolean isWarnedUser =  warnedUsers.contains(getFullUser(user));

//		System.out.println("isWarnedUser : " + isWarnedUser);

		return isWarnedUser;
	}

	private void warnUser(JDA jda, User user) {
    	MessageFormat messageFormat = new MessageFormat(WELCOME_MESSAGE);

		user.openPrivateChannel().queue((channel) ->
        {
            channel.sendMessage(messageFormat.format(new String[] {user.getName()})).queue();
        });

		warnedUsers.add(getFullUser(user));
		
		writeConfiguration();
	}

	public static void readConfiguration() {
		try {
			FileInputStream fis = new FileInputStream(WELCOME_FILE);
			JSONObject jsonObject = (JSONObject) new JSONTokener(fis).nextValue();
			
			WELCOME_MESSAGE = jsonObject.getString("welcome");
			
			JSONArray welcomed = jsonObject.getJSONArray("welcomed");
			for(int index = 0; index < welcomed.length(); ++index) {
				warnedUsers.add(welcomed.getString(index));
			}
			
			fis.close();
		}
		catch (IOException e) {
		}
	}
	
	public static void writeConfiguration() {
		try {
			FileWriter fw = new FileWriter(WELCOME_FILE);
			JSONWriter writer = new JSONWriter(fw);
			
			writer.object();
			writer.key("welcome").value(WELCOME_MESSAGE);
			writer.key("welcomed").array();
			
			for (String warnedUser : warnedUsers) {
				writer.value(warnedUser);
			}
			
			writer.endArray();
			writer.endObject();
			
			fw.flush();
			fw.close();
		}		
		catch (IOException e) {
		}
	}
	
	public static void main(String[] args) {
		System.out.println(WELCOME_MESSAGE);
    	MessageFormat messageFormat = new MessageFormat(WELCOME_MESSAGE);
    	System.out.println(messageFormat.format(new String[] {"Mini Dictateur"}));
    	
    	System.out.println(warnedUsers.size());
    	
    	writeConfiguration();
	}
}