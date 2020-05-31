package fr.partipirate.discord.bots.congressus.commands.congressus;

import java.math.BigInteger;
import java.util.List;

import fr.partipirate.discord.bots.congressus.Configuration;

public class CongressusHelper {

	public static String getUrl() {
		StringBuilder sb = new StringBuilder();

		sb.append(Configuration.getInstance().OPTIONS.get("congressus").get("url"));

		return sb.toString();
	}

	public static String getUrl(String method) {
		StringBuilder sb = new StringBuilder();

		sb.append(getUrl());
		sb.append("?method=");
		sb.append(method);

		sb.append("&token=");
		sb.append(Configuration.getInstance().OPTIONS.get("congressus").get("token"));

		sb.append("&secret=");
		sb.append(Configuration.getInstance().OPTIONS.get("congressus").get("secret"));

		return sb.toString();
	}

	public static String getMessagesUrl() {
		StringBuilder sb = new StringBuilder();

		sb.append(getUrl());

		sb.append("do_getMessages.php?a=b");
		
		sb.append("&token=");
		sb.append(Configuration.getInstance().OPTIONS.get("congressus").get("token"));

		sb.append("&secret=");
		sb.append(Configuration.getInstance().OPTIONS.get("congressus").get("secret"));

		return sb.toString();
	}

	public static String getConsumeMessageUrl(List<BigInteger> consumedIds) {
		StringBuilder sb = new StringBuilder();

		sb.append(getUrl());

		sb.append("do_consumeMessages.php?a=b");

		sb.append("&token=");
		sb.append(Configuration.getInstance().OPTIONS.get("congressus").get("token"));

		sb.append("&secret=");
		sb.append(Configuration.getInstance().OPTIONS.get("congressus").get("secret"));

		for (BigInteger consumedId : consumedIds) {
			sb.append("&messageIds[]=");
			sb.append(consumedId.toString());
		}
		
		return sb.toString();
	}
}