package fr.partipirate.discord.bots.congressus.commands.congressus;

import java.awt.Color;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import fr.partipirate.discord.bots.congressus.commands.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class NextMeetingCommand extends ACongressusCommand implements ICommand {

	@Override
	public String getKeyWord() {
		return "next";
	}

	@Override
	public boolean canDoCommand(User user, Guild guild) {
		return true;
	}

	@Override
	public void doCommand(User user, MessageChannel channel, Guild guild, String[] commandParts) {
		long now = System.currentTimeMillis();
		String meetings = CongressusHelper.getUrl() + "do_getMeetings.php?from=" + now + "&to=" + (now + 86400000L * 14);
		
		try {
			URL url = new URL(meetings);
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
			JSONArray result = object.getJSONArray("result");

			long minMeetingStart = Long.MAX_VALUE;
			JSONObject minMeeting = null;
			
			for(int i = 0; i < result.length(); ++i) {
				JSONObject meeting = result.getJSONObject(i);
				long meetingStart = meeting.getLong("start");
				
				if (meetingStart > now && meetingStart < minMeetingStart) {
					minMeetingStart = meetingStart;
					minMeeting = meeting;
				}
			}
			
			if (minMeeting != null) {
//				StringBuilder sb = new StringBuilder();

				// Create the EmbedBuilder instance
				EmbedBuilder eb = new EmbedBuilder();				
				
				
				String title = minMeeting.getString("meetingTitle");
				
				/*
				title = title.replaceAll("(?:\\n|\\r)", " ");
				String location = null;
			    Pattern regex = Pattern.compile("([a-zA-Z0-9\\- éèà]*) \\- ([a-z A-Zéèà(:\\/\\.\\-=\\?\\%0-9\\&\\;\\,\\')]*)");
			    Matcher matcher = regex.matcher(title);
			    while(matcher.find()) {
			    	System.out.println(title);
			    	
			    	location = matcher.group(2);
			    	title = matcher.group(1);
			    	
			    	location = location.replaceAll("\\&eacute\\;", "é");
			    }
			    */
				
//				sb.append("Prochaine réunion : ***");
//				sb.append(title);
//				sb.append("*** ");
///*
//				Calendar calendar = Calendar.getInstance();
//				calendar.setTimeInMillis(minMeetingStart);
//				
//				String pattern = "'le' EEEEE dd MMMMM yyyy 'à' HH:mm";
//				SimpleDateFormat simpleDateFormat =
//				        new SimpleDateFormat(pattern, new Locale("fr", "FR"));
//				sb.append(simpleDateFormat.format(calendar.getTime()));
//*/				        
//				sb.append(minMeeting.getString("meetingDatetime"));
//
////				if (location != null) {
//					sb.append("\nLieu de la réunion : " + minMeeting.getJSONObject("location").getString("type"));
////				}
//				
//				if (minMeeting.getJSONObject("location").has("discord")) {
//					sb.append(" (:hash: : #");
//					sb.append(minMeeting.getJSONObject("location").getJSONObject("discord").getJSONObject("text").getString("title"));
//					sb.append(", :sound: : ");
//					sb.append(minMeeting.getJSONObject("location").getJSONObject("discord").getJSONObject("vocal").getString("title"));
//					sb.append(")");
//				}
//					
//				sb.append("\nLien de la réunion : ");
//				sb.append(minMeeting.getString("url"));
//				
////				sb.append(CongressusHelper.getUrl());
////				sb.append("meeting.php?id=" + minMeeting.getInt("id"));
//
////				channel.sendMessage(sb.toString()).complete();
				
				eb.setTitle(title, minMeeting.getString("url"));

				eb.setColor(new Color(0xF40C0C));
				
//				eb.setDescription("Text");
				eb.addField("Horaire", StringEscapeUtils.unescapeHtml4(minMeeting.getString("meetingDatetime")), false);
				if (minMeeting.has("location")) {
					if (minMeeting.getJSONObject("location").has("type")) {
						eb.addField("Lieu", StringEscapeUtils.unescapeHtml4(minMeeting.getJSONObject("location").getString("type")), false);
					}
					if (minMeeting.getJSONObject("location").has("discord")) {
						if (minMeeting.getJSONObject("location").getJSONObject("discord").has("text")) {
							eb.addField(":hash:", StringEscapeUtils.unescapeHtml4(minMeeting.getJSONObject("location").getJSONObject("discord").getJSONObject("text").getString("title")), true);
						}
						if (minMeeting.getJSONObject("location").getJSONObject("discord").has("vocal")) {
							eb.addField(":sound:", StringEscapeUtils.unescapeHtml4(minMeeting.getJSONObject("location").getJSONObject("discord").getJSONObject("vocal").getString("title")), true);
						}
					}
				}
				
				channel.sendMessageEmbeds(eb.build()).complete();
			}
			else {
				channel.sendMessage("Pas de réunion prévue").complete();
			}

		}
		catch(Exception e) {
			channel.sendMessage(e.getMessage()).complete();
		}
	}

	@Override
	public String getCommandHelp() {
		return "Donne les informations de la prochaine réunion, s'il y en a une.";
	}
}