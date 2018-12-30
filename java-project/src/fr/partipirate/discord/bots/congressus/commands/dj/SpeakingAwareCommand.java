package fr.partipirate.discord.bots.congressus.commands.dj;

import fr.partipirate.discord.bots.congressus.CongressusBot;
import fr.partipirate.discord.bots.congressus.commands.ICommand;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;

public class SpeakingAwareCommand extends ADJCommand implements ICommand {

	private CongressusBot bot;

	public SpeakingAwareCommand(CongressusBot bot) {
		this.bot = bot;
	}
	
	@Override
	public String getKeyWord() {
		return "speakingAware";
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
			boolean isSpeakingAware = commandParts[1].equalsIgnoreCase("on");
			bot.setSpeakingAware(isSpeakingAware);
		}
		else {
			boolean isSpeakingAware = bot.isSpeakingAware();
        	channel.sendMessage("Je suis attentif au fait que les gens parlent : " + (isSpeakingAware ? "oui" : "non")).complete();
		}
	}
	
	@Override
	public String getCommandHelp() {
		return "Donne l'état d'attention de la parole des auditeurs ou, s'il y a un paramètre, le change (on / off).";
	}
}