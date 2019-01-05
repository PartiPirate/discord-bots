package fr.partipirate.discord.bots.congressus.commands.recorder;

import fr.partipirate.discord.bots.congressus.CongressusBot;
import fr.partipirate.discord.bots.congressus.commands.ICommand;
import fr.partipirate.discord.bots.congressus.listeners.AudioRecorderHandler;
import net.dv8tion.jda.core.audio.AudioReceiveHandler;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;

public class RecordCommand implements ICommand {

	private CongressusBot bot;

	public RecordCommand(CongressusBot bot) {
		this.bot = bot;
	}

	@Override
	public String getKeyWord() {
		return "record";
	}

	@Override
	public boolean canDoCommand(User user, Guild guild) {
		return user.getName().equalsIgnoreCase("farlistener");
	}

	@Override
	public void doCommand(User user, MessageChannel channel, Guild guild, String[] commandParts) {
		if (channel instanceof PrivateChannel) {
        	channel.sendMessage("*Commande à faire publiquement*").complete();
		}
		else if (!canDoCommand(user, guild)) {
        	channel.sendMessage("*Vous n'avez pas les droits suffisants*").complete();
		}
		else {
			StringBuilder vocalChannelBuilder = new StringBuilder();
			String separator = "";
			for (int i = 1; i < commandParts.length; i++) {
				vocalChannelBuilder.append(separator);
				vocalChannelBuilder.append(commandParts[i]);
				separator = " ";
			}

			String vocalChannel = vocalChannelBuilder.toString();

			bot.connectToVoiceChannel(guild.getAudioManager(), vocalChannel);

			AudioRecorderHandler recorder = new AudioRecorderHandler();

			if (guild.getAudioManager().getReceiveHandler() != null) {
				AudioReceiveHandler previousHandler = guild.getAudioManager().getReceiveHandler();
				if (previousHandler instanceof AudioRecorderHandler) {
					((AudioRecorderHandler)previousHandler).endRecording();
				}
			}

			guild.getAudioManager().setReceivingHandler(recorder);

			recorder.startRecording();
			
			channel.sendMessage("*L'enregistrement commence sur **" + vocalChannel + "***").complete();
		}
	}

	@Override
	public String getCommandHelp() {
		return "Enregistre le son sur le canal indiqué";
	}
}