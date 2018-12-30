package fr.partipirate.discord.bots.congressus.commands.radio;

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

}