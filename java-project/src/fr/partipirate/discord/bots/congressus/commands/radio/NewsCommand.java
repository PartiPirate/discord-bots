package fr.partipirate.discord.bots.congressus.commands.radio;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

import fr.partipirate.discord.bots.congressus.CongressusBot;
import fr.partipirate.discord.bots.congressus.GuildMusicManager;
import fr.partipirate.discord.bots.congressus.commands.ICommand;
import fr.partipirate.discord.bots.congressus.commands.dj.ADJCommand;
import fr.partipirate.discord.bots.congressus.listeners.RadioHandler;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class NewsCommand extends ADJCommand implements ICommand {

	private CongressusBot bot;

	private long lastPlay = 0;
	private long newsUntil = 0;
	private String newsUrl = null;

	public NewsCommand(CongressusBot bot) {
		this.bot = bot;
	}

	@Override
	public String getKeyWord() {
		return "news";
	}

	@Override
	public boolean canDoCommand(User user, Guild guild) {
		return isDJ(getMember(user, guild));
	}

	@Override
	public void doCommand(User user, MessageChannel channel, Guild guild, String[] commandParts) {
		if (commandParts.length == 0) {
			if (newsUrl == null) {
				channel.sendMessage("*Pas de nouvelles à jouer*").complete();
			}
			else if (System.currentTimeMillis() > newsUntil) {
				channel.sendMessage("*Pas de nouvelles à jouer*").complete();
			}
			else if (System.currentTimeMillis() - lastPlay > 3600000L) {
				lastPlay = System.currentTimeMillis();
				channel.sendMessage("*Dés que c'est fini, les news sont à vous*").complete();

				// joue la piste sonore
				Map<Long, GuildMusicManager> managers = bot.getMusicManagers();

				for (Iterator<GuildMusicManager> iterator = managers.values().iterator(); iterator.hasNext();) {
					GuildMusicManager manager = iterator.next();

					RadioHandler.getInstance().loadUrl(manager, newsUrl);
				}
			}
			else {
				channel.sendMessage("*Les nouvelles ont été jouées il n'y a pas si longtemps, redemande plus tard*").complete();
			}
		}
		else if (!isDJ(getMember(user, guild))) {
			channel.sendMessage("*Vous n'avez pas les droits suffisants*").complete();
		}
		else if (commandParts.length != 2) {
			channel.sendMessage("*Le nombre de paramètres est inadéquat*").complete();
		} 
		else {
			newsUrl = commandParts[1];

			Calendar calendar = Calendar.getInstance();
			String date = commandParts[0];
			String[] dateParts = date.split("-");

			calendar.set(Integer.parseInt(dateParts[0]), Integer.parseInt(dateParts[1]), Integer.parseInt(dateParts[2]));

			newsUntil = calendar.getTimeInMillis();
		}
	}

	@Override
	public String getCommandHelp() {
		return "Permet de jouer les nouvelles (sans abus) - [AAAA-MM-JJ] [url] Permet de remplacer jusqu'à une certaine date une piste de nouvelles";
	}
}