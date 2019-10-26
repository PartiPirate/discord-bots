package fr.partipirate.discord.bots.congressus.listeners;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import fr.partipirate.discord.bots.congressus.Configuration;
import fr.partipirate.discord.bots.congressus.commands.congressus.CongressusHelper;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class ExternalChatHandler extends ListenerAdapter {

	public ExternalChatHandler() {
	}

	@Override
	public void onMessageUpdate(MessageUpdateEvent event) {
		handleMessage(event.getAuthor(), event.getTextChannel(), event.getMessage());
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		handleMessage(event.getAuthor(), event.getTextChannel(), event.getMessage());
	}

	private void handleMessage(User author, TextChannel textChannel, Message sourceMessage) {
		String message = sourceMessage.getContentRaw();
		
		if (author.getName().equals("Congressus")) {
			return;
		}
		
		if (textChannel == null) {
			return;
		}
		
		try {
			String user = author.getName() + "#" + author.getDiscriminator();
			user = URLEncoder.encode(user, java.nio.charset.StandardCharsets.UTF_8.toString());

			String channel = URLEncoder.encode(textChannel.getName(), java.nio.charset.StandardCharsets.UTF_8.toString());
			
			StringBuilder urlBuilder = new StringBuilder();
			urlBuilder.append(CongressusHelper.getUrl());
			urlBuilder.append("meeting_api.php?method=do_externalChat");

			urlBuilder.append("&token=");
			urlBuilder.append(Configuration.getInstance().OPTIONS.get("congressus").get("token"));

			urlBuilder.append("&secret=");
			urlBuilder.append(Configuration.getInstance().OPTIONS.get("congressus").get("secret"));

			urlBuilder.append("&user=");
			urlBuilder.append(user);

			if (author.getAvatarUrl() != null) {
				urlBuilder.append("&avatar=");
				urlBuilder.append(URLEncoder.encode(author.getAvatarUrl(), java.nio.charset.StandardCharsets.UTF_8.toString()));
			}

			urlBuilder.append("&channel=");
			urlBuilder.append(channel);

			urlBuilder.append("&messageId=");
			urlBuilder.append(sourceMessage.getId());

			urlBuilder.append("&message=");
			urlBuilder.append(URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8.toString()));
			
			String externalChatUrl = urlBuilder.toString();
			
			URL url = new URL(externalChatUrl);
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
		}
		catch (Exception e) {
		}
	}
}