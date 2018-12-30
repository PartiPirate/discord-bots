package fr.partipirate.discord.bots.congressus.commands;

import java.util.List;

import fr.partipirate.discord.bots.congressus.Configuration;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;

public class CommandsCommand implements ICommand {

	private List<ICommand> commands;

	public CommandsCommand(List<ICommand> commands) {
		this.commands = commands;
	}
	
	@Override
	public String getKeyWord() {
		return "commands";
	}

	@Override
	public boolean canDoCommand(User user, Guild guild) {
		return true;
	}

	@Override
	public void doCommand(User user, MessageChannel channel, Guild guild, String[] commandParts) {
		PrivateChannel privateChannel = user.openPrivateChannel().complete();

		StringBuilder sb = new StringBuilder();
		String lineSeparator = "";

		for (ICommand command : commands) {
			sb.append(lineSeparator);
			sb.append("**_\\");
			sb.append(Configuration.getInstance().PREFIX);
			sb.append(command.getKeyWord());
			sb.append("_** : ");
			sb.append(command.getCommandHelp());
			
			lineSeparator = "\n";
		}

		privateChannel.sendMessage(sb.toString()).complete();
	}

	@Override
	public String getCommandHelp() {
		return "*évidemment*";
	}

}
