package fr.partipirate.discord.bots.congressus.commands.personae;

import fr.partipirate.discord.bots.congressus.Configuration;

public class PersonaeHelper {

	public static String getUrl(String method) {
		StringBuilder sb = new StringBuilder();

		sb.append(Configuration.getInstance().OPTIONS.get("personae").get("url"));
		sb.append("?method=");
		sb.append(method);

		sb.append("&token=");
		sb.append(Configuration.getInstance().OPTIONS.get("personae").get("token"));

		sb.append("&secret=");
		sb.append(Configuration.getInstance().OPTIONS.get("personae").get("secret"));

		return sb.toString();
	}

}