package fr.partipirate.discord.bots.congressus.commands.radio;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map.Entry;
import java.util.Properties;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import fr.partipirate.discord.bots.congressus.Configuration;

public class RadioHelper {

	public static String getUrl() {
		StringBuilder sb = new StringBuilder();

		sb.append(Configuration.getInstance().OPTIONS.get("radio").get("url"));

		return sb.toString();
	}

	public static String getUrl(String method) throws UnsupportedEncodingException {
		return getUrl(method, new Properties());
	}

	public static String getUrl(String method, Properties parameters) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();

		sb.append(getUrl());
		sb.append("api.php?method=");
		sb.append(method);

		sb.append("&token=");
		sb.append(Configuration.getInstance().OPTIONS.get("radio").get("token"));

		sb.append("&secret=");
		sb.append(Configuration.getInstance().OPTIONS.get("radio").get("secret"));

		for (Entry<Object, Object> property : parameters.entrySet()) {
			sb.append("&");
			sb.append(property.getKey().toString());
			sb.append("=");
			sb.append(URLEncoder.encode(property.getValue().toString(), "UTF-8"));
		}

		return sb.toString();
	}

	public static JSONObject getNext() {
		try {
			JSONObject object = call(getUrl("do_getNext"));

			return object;
		} 
		catch (Exception e) {
		}

		return null;
	}

	public static boolean deleteTrack(String trackUrl) {
		try {
			Properties parameters = new Properties();
			parameters.setProperty("url", trackUrl);

			JSONObject object = call(getUrl("do_deleteTrack", parameters));

			if (object.has("status")) {
				return object.getBoolean("status");
			}
		} 
		catch (Exception e) {
		}

		return true;
	}

	public static boolean addTrack(AudioTrack track) {
		try {
			Properties parameters = new Properties();
			parameters.setProperty("url", track.getInfo().uri);
			parameters.setProperty("title", track.getInfo().title);
			parameters.setProperty("author", track.getInfo().author);
			parameters.setProperty("duration", String.valueOf(track.getInfo().length / 1000));

			JSONObject object = call(getUrl("do_addTrack", parameters));
			
			if (object.has("status")) {
				return object.getBoolean("status");
			}
		} 
		catch (Exception e) {
		}

		return true;
	}

	public static boolean hasTrack(String trackUrl) {
		try {
			Properties parameters = new Properties();
			parameters.setProperty("url", trackUrl);

			JSONObject object = call(getUrl("do_hasTrack", parameters));

			if (object.has("status")) {
				return object.getBoolean("status");
			}
		} 
		catch (Exception e) {
		}

		return true;
	}

	private static JSONObject call(String apiCallUrl) throws IOException {
		URL url = new URL(apiCallUrl);
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
		
		return object;
	}
	
}