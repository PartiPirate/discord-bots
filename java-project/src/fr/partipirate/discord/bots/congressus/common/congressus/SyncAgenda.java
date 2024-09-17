package fr.partipirate.discord.bots.congressus.common.congressus;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.apache.commons.text.StringEscapeUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import fr.partipirate.discord.bots.congressus.commands.congressus.CongressusHelper;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.internal.managers.ScheduledEventManagerImpl;

public class SyncAgenda {

    public static String syncAgenda(Guild guild) {
	/**
	 * Synchronize Congressus meetings with Discord events
	 */
	long now = System.currentTimeMillis();
	String congressusMeetingsUrl = CongressusHelper.getUrl() + "do_getMeetings.php?from=" + now + "&to="
		+ (now + 86400000L * 30);

	try {
	    URL url = new URL(congressusMeetingsUrl);
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
	    if (object.getInt("success") != 1) return "Impossible de lister les réunions";

	    JSONArray congressusMeetings = object.getJSONArray("result");
	    List<ScheduledEvent> discordEvents = guild.getScheduledEvents();

	    int errors = createMissingEvents(congressusMeetings, discordEvents, guild);

	    deleteOldEvents(congressusMeetings, discordEvents, guild);

		String msg = "La liste des événements a été mise à jour";
	    if (errors > 0) msg += " avec " + errors + " erreurs";
	    return msg;
	}
	catch (Exception e) {
	    for (StackTraceElement el : e.getStackTrace())
		System.err.println(el);
	    return "Erreur pendant la mise à jour des événements : " + e.getMessage();
	}
    }

    protected static ScheduledEvent getEvent(JSONObject meeting, List<ScheduledEvent> discordEvents) {
	/**
	 * Look on all discord scheduled events if a meeting is already exists The only
	 * unique value of a meeting is this ID, visible on meeting json or url As we
	 * add url on event description, a meeting already exists if : - Description
	 * contains the same URL as my meeting - other ? … Return null if not found
	 */

	String url = meeting.getString("url");
	for (ScheduledEvent event : discordEvents) {
	    if (event.getDescription().contains(url)) return event;
	}

	return null;
    }

    protected static int createMissingEvents(JSONArray congressusMeetings, List<ScheduledEvent> discordEvents,
	    Guild guild) {
	/**
	 *  Create or update missing discord events
	 * 
	 * @return number of errors during creation
	 */
	int errors = 0;
	for (int i = 0; i < congressusMeetings.length(); ++i) {
	    JSONObject meeting = congressusMeetings.getJSONObject(i);
		try {
	    createEvent(meeting, discordEvents, guild);
		} catch (Exception e) {
			System.err.println("Erreur pendant la création de l'événement " + meeting.getString("meetingTitle"));
			errors++;
		}
	}
	return errors;
    }

    protected static GuildChannel getDiscordChannel(JSONObject meeting, Guild guild) {
	/**
	 * Find if meeting location is on a known channel
	 * 
	 * @return VoiceChannel or null
	 */

	if (meeting.has("location") && meeting.getJSONObject("location").has("discord")) {
	    // look for discord channel
	    JSONObject locationDiscord = meeting.getJSONObject("location").getJSONObject("discord");

	    if (locationDiscord.has("vocal")) {
		String chan = locationDiscord.getJSONObject("vocal").getString("title");
		List<VoiceChannel> channels = guild.getVoiceChannelsByName(chan, true);

		if (channels.size() > 0) {
		    // channels found, we will use first
		    return channels.get(0);
		}
	    }

	    if (locationDiscord.has("text")) {
		String chan = locationDiscord.getJSONObject("text").getString("title");
		List<TextChannel> channels = guild.getTextChannelsByName(chan, true);
		if (channels.size() > 0) {
		    // channels found, we will use first
		    return channels.get(0);
		}
	    }
	}
	return null;

    }

    protected static String getExternalLocation(JSONObject meeting) {
	/**
	 * Get external location of a meeting
	 */

	if (meeting.has("location")) {
	    JSONObject location = meeting.getJSONObject("location");
	    if (location.has("extra")) {
		return StringEscapeUtils.unescapeHtml4(location.getString("extra").split("\\n")[0]).trim();
	    }

	    if (location.has("type")) return StringEscapeUtils.unescapeHtml4(location.getString("type")).trim();
	}
	return "Emplacement inconnu";
    }

    protected static String createDescription(JSONObject meeting) {
	/**
	 * Create description string from meeting
	 */
	String description = StringEscapeUtils.unescapeHtml4(meeting.getString("title"));
	if (meeting.has("location") && meeting.getJSONObject("location").has("extra"))
	    description += "\n" + meeting.getJSONObject("location").getString("extra");

	if (meeting.has("location") && meeting.getJSONObject("location").has("discord")) {
	    JSONObject discordLocation = meeting.getJSONObject("location").getJSONObject("discord");

	    if (discordLocation.has("vocal")) {
		description += "\nVocal : " + discordLocation.getJSONObject("vocal").get("title");
	    }

	    if (discordLocation.has("text")) {
		description += "\nTexte : " + discordLocation.getJSONObject("text").get("title");
	    }
	}

	description += "\n" + meeting.getString("url");

	return description.trim();
    }

    protected static void createEvent(JSONObject meeting, List<ScheduledEvent> discordEvents, Guild guild) {
	/**
	 * Create or update a Discord event with datas from congressus meeting
	 * 
	 * JDA dont implement ScheduledEvents. If event updated, we need to
	 * delete/recreate event
	 */

	String title = meeting.getString("meetingTitle").trim();
	ScheduledEvent event = getEvent(meeting, discordEvents);

	String description = createDescription(meeting);
	OffsetDateTime start = timestampToOffsetDateTime(meeting.getLong("start"));
	OffsetDateTime end = timestampToOffsetDateTime(meeting.getLong("end"));

	if (start.compareTo(OffsetDateTime.now()) <= 0) {
	    // System.out.println("Event '" + title + "' start in the past, it's
	    // forbidden");
	    return;
	}

	GuildChannel channel = getDiscordChannel(meeting, guild);
	String externalLocation = getExternalLocation(meeting);

	if (event != null) {
	    ScheduledEventManagerImpl eventManager = new ScheduledEventManagerImpl(event);
	    boolean updated = false;

	    if (!event.getName().equals(title)) {
		eventManager.setName(title).complete();
		updated = true;
	    }

	    if (!event.getDescription().equals(description)) {
		eventManager.setDescription(description).complete();
		updated = true;
	    }

	    if (channel != null) {
		if (event.getChannel() != channel) {
		    eventManager.setLocation(channel).complete();
		    updated = true;
		}
	    }
	    else if (!event.getLocation().equals(externalLocation)) {
		// when updating external events, we need to set up again start and end dates
		eventManager.setStartTime(start).setEndTime(end).setLocation(externalLocation).complete();
		updated = true;
	    }

	    if (event.getStartTime().compareTo(start) != 0) {
		eventManager.setStartTime(start).complete();
		updated = true;
	    }

	    if (event.getEndTime().compareTo(end) != 0) {
		eventManager.setEndTime(end).complete();
		updated = true;
	    }

	    if (updated) {
		System.out.println("Event updated : " + title);
	    }

	    return;
	}

	if (channel != null) {
	    guild.createScheduledEvent(title, channel, start).setDescription(description).setEndTime(end).complete();
	}
	else {
	    guild.createScheduledEvent(title, externalLocation, start, end).setDescription(description).complete();
	}

	System.out.println("Event created : " + title);
    }

    protected static OffsetDateTime timestampToOffsetDateTime(Long ts) {
	/* Convert Timestamp object to OffsetDateTime object */
	Timestamp timestamp = new Timestamp(ts);
	LocalDateTime localDateTime = timestamp.toLocalDateTime();
	ZoneOffset systemZoneOffset = ZoneId.systemDefault().getRules().getOffset(Instant.now());
	return localDateTime.atOffset(systemZoneOffset);
    }

    protected static boolean isCongressusEvent(JSONArray congressusMeetings, long id) {
	/* Check on array if there is a meeting with this id */
	for (int i = 0; i < congressusMeetings.length(); ++i) {
	    JSONObject meeting = congressusMeetings.getJSONObject(i);
	    if (meeting.getLong("id") == id) return true;
	}

	return false;
    }

    protected static long extractIdFromDescription(String description) {
	/* Extract ID on url from description */

	String regex = "https://(.+)id=([0-9]+)(.*)";
	Pattern p = Pattern.compile(regex);

	for (String part : description.split("\\s")) {
	    try {
		Matcher m = p.matcher(part);
		if (m.matches()) {
		    long id = Long.parseLong(m.group(2));
		    return id;
		}
	    }
	    catch (Exception e) {
	    }
	}

	return -1;
    }

    protected static void deleteOldEvents(JSONArray congressusMeetings, List<ScheduledEvent> discordEvents,
	    Guild guild) {
	/*
	 * Delete discord events that have congressus url on description but not on
	 * current meetings
	 */

	String baseUrl = CongressusHelper.getUrl();

	for (ScheduledEvent event : discordEvents) {
	    String description = event.getDescription();
	    if (description.contains(baseUrl)) {
		long id = extractIdFromDescription(description);

		if (id > 0) {
		    if (!isCongressusEvent(congressusMeetings, id)) {
			// System.out.println("Discord event '" + event.getName() + "' points to unknown
			// Congressus meeting id " + id);
			event.delete().complete();
		    }
		}
		else {
		    System.out.println("Unable to find id on event " + event.getName());
		}
	    }
	}
    }
}