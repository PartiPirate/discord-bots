package fr.partipirate.discord.bots.congressus.listeners;

import fr.partipirate.discord.bots.congressus.Configuration;
import fr.partipirate.discord.bots.congressus.CongressusBot;
import fr.partipirate.discord.bots.congressus.NotCommandException;
import fr.partipirate.discord.bots.congressus.commands.ICommand;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class CommandHandler extends ListenerAdapter {
	
	private CongressusBot congressusBot;

	public CommandHandler(CongressusBot congressusBot) {
		this.congressusBot = congressusBot;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		String message = event.getMessage().getContentRaw();
		
		if (event.getAuthor().getName().equals("Congressus")) {
			return;
		}
		
		MessageChannel channel = event.getChannel();
        Guild guild = event.getGuild();
        User user = event.getAuthor();
        
		if (message.startsWith(Configuration.getInstance().PREFIX)) {
			try {
		        String[] commandParts = message.split(" ");
				String command = commandParts[0].toLowerCase();
				
				boolean foundCommand = false;
				
		        for (ICommand icommand : congressusBot.getCommands()) {
					if (command.equalsIgnoreCase(Configuration.getInstance().PREFIX + icommand.getKeyWord())) {
						foundCommand = true;
						icommand.doCommand(user, channel, guild, commandParts);
					}
				}

		        if (!foundCommand) {
					throw new NotCommandException(message);
		        }
			} 
			catch (NotCommandException e) {
				System.err.println("La commande \"" + message + "\" n'a pas été comprise.");
//				e.printStackTrace();
//				channel.sendMessage("La commande \"" + message + "\" n'a pas été comprise.").complete();
			}
			catch (Exception e) {
//				System.err.println("La commande \"" + message + "\" n'a pas été comprise.");
				e.printStackTrace();
//				channel.sendMessage("La commande \"" + message + "\" n'a pas été comprise.").complete();
			}
		}
	}
}