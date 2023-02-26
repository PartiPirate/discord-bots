package fr.partipirate.discord.bots.congressus.commands.congressus;

import fr.partipirate.discord.bots.congressus.commands.ICommand;
import fr.partipirate.discord.bots.congressus.common.congressus.SyncAgenda;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.User;


public class SyncAgendaCommand extends ACongressusCommand implements ICommand {

	@Override
	public String getKeyWord() {
		return "agenda";
	}

	@Override
	public boolean canDoCommand(User user, Guild guild) {
		return true;
	}

	@Override
	public void doCommand(User user, MessageChannel channel, Guild guild, String[] commandParts) {
		String message = SyncAgenda.syncAgenda(guild);
		if (message != null)
			channel.sendMessage(message).complete();
	}

	@Override
	public String getCommandHelp() {
		return "Synchronise l'agenda Discord avec l'agenda Congressus sur le prochain mois";
	}

}