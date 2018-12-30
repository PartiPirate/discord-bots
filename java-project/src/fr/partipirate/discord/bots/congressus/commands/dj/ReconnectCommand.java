package fr.partipirate.discord.bots.congressus.commands.dj;

import fr.partipirate.discord.bots.congressus.CongressusBot;
import fr.partipirate.discord.bots.congressus.commands.ICommand;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;

public class ReconnectCommand extends ADJCommand implements ICommand {

	private CongressusBot bot;

	public ReconnectCommand(CongressusBot bot) {
		this.bot = bot;
	}

	@Override
	public String getKeyWord() {
		return "reconnect";
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
		else if (commandParts.length > 1) {
			StringBuilder channelBuilder = new StringBuilder();
			String separator = "";

			for (int i = 1; i < commandParts.length; i++) {
				channelBuilder.append(separator);
				separator = " ";
				channelBuilder.append(commandParts[i]);
			}

			bot.disconnect(guild.getAudioManager());
			bot.connectToVoiceChannel(guild.getAudioManager(), channelBuilder.toString());
		}
		else {
			bot.disconnect(guild.getAudioManager());
			bot.connectToVoiceChannel(guild.getAudioManager(), "");
		}
	}

	@Override
	public String getCommandHelp() {
		return "Permet au bot de se reconnecter sur le vocal et, avec un paramètre, de changer de salon.";
	}
}