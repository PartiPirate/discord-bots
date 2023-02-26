package fr.partipirate.discord.bots.congressus.commands.dj;

import fr.partipirate.discord.bots.congressus.CongressusBot;
import fr.partipirate.discord.bots.congressus.commands.ICommand;
import fr.partipirate.discord.bots.congressus.listeners.RadioHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

public class TitleAwareCommand extends ADJCommand implements ICommand {

	private CongressusBot bot;

	public TitleAwareCommand(CongressusBot bot) {
		this.bot = bot;
	}
	
	@Override
	public String getKeyWord() {
		return "titleAware";
	}

	@Override
	public boolean canDoCommand(User user, Guild guild) {
		return isDJ(getMember(user, guild));
	}

	@Override
	public void doCommand(User user, MessageChannel channel, Guild guild, String[] commandParts) {
		if (channel instanceof PrivateChannel) {
        	channel.sendMessage("*Commande à faire publiquement*").complete();
		}
		else if (!isDJ(getMember(user, guild))) {
        	channel.sendMessage("*Vous n'avez pas les droits suffisants*").complete();
		}
		else if (commandParts.length > 1){
			boolean isTitleAware = commandParts[1].equalsIgnoreCase("on");
			RadioHandler.getInstance().setTitleAware(isTitleAware);
		}
		else {
			boolean isTitleAware = RadioHandler.getInstance().getTitleAware();
        	channel.sendMessage("J'ajoute le titre de la piste en cours à mon pseudo : " + (isTitleAware ? "oui" : "non")).complete();
		}
	}
	
	@Override
	public String getCommandHelp() {
		return "Indique si le bot ajoute le titre de la piste en cours à son pseudo ou, s'il y a un paramètre, le change (on / off).";
	}
}