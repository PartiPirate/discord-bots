package fr.partipirate.discord.bots.congressus.commands.radio;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.json.JSONObject;
import org.json.JSONTokener;

import fr.partipirate.discord.bots.congressus.Configuration;

public class RadioHelper {

	public static String getUrl() {
		StringBuilder sb = new StringBuilder();

		sb.append(Configuration.getInstance().OPTIONS.get("radio").get("url"));

		return sb.toString();
	}

	public static String getUrl(String method) {
		StringBuilder sb = new StringBuilder();

		sb.append(getUrl());
		sb.append("api.php?method=");
		sb.append(method);

		sb.append("&token=");
		sb.append(Configuration.getInstance().OPTIONS.get("radio").get("token"));

		sb.append("&secret=");
		sb.append(Configuration.getInstance().OPTIONS.get("radio").get("secret"));

		return sb.toString();
	}

	public static boolean deleteTrack(String trackUrl) {
		try {
			StringBuilder apiCallUrlBuilder = new StringBuilder();
			apiCallUrlBuilder.append(getUrl("do_deleteTrack"));
			apiCallUrlBuilder.append("&url=");
			apiCallUrlBuilder.append(URLEncoder.encode(trackUrl, "UTF-8"));

			String apiCallUrl = apiCallUrlBuilder.toString();
			System.out.println(apiCallUrl);

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

			if (object.has("status")) {
				return object.getBoolean("status");
			}
		} 
		catch (Exception e) {
		}

		return true;
	}

}