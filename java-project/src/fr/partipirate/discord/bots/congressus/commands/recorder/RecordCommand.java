package fr.partipirate.discord.bots.congressus.commands.recorder;

import fr.partipirate.discord.bots.congressus.CongressusBot;
import fr.partipirate.discord.bots.congressus.commands.ICommand;
import fr.partipirate.discord.bots.congressus.listeners.AudioRecorderHandler;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

public class RecordCommand extends ARecorderCommand implements ICommand {

	private CongressusBot bot;

	public RecordCommand(CongressusBot bot) {
		this.bot = bot;
	}

	@Override
	public String getKeyWord() {
		return "record";
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

			VoiceChannel voicedChannel = bot.connectToVoiceChannel(guild.getAudioManager(), vocalChannel);

			System.out.println("Connect on " + voicedChannel.getName());
			
			AudioRecorderHandler recorder = new AudioRecorderHandler(voicedChannel);
			recorder.setCommandChannel(channel);

			if (guild.getAudioManager().getReceivingHandler() != null) {
				AudioReceiveHandler previousHandler = guild.getAudioManager().getReceivingHandler();
				if (previousHandler instanceof AudioRecorderHandler) {
					((AudioRecorderHandler)previousHandler).endRecording();
				}
			}

			guild.getAudioManager().setReceivingHandler(recorder);

			recorder.startRecording();
		}
	}

	@Override
	public String getCommandHelp() {
		return "Enregistre le son sur le canal indiqué";
	}
}