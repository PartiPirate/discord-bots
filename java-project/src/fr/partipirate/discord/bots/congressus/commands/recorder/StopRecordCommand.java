package fr.partipirate.discord.bots.congressus.commands.recorder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import fr.partipirate.discord.bots.congressus.commands.ICommand;
import fr.partipirate.discord.bots.congressus.listeners.AudioRecorderHandler;
import fr.partipirate.discord.bots.congressus.Configuration;

import net.dv8tion.jda.core.audio.AudioReceiveHandler;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;

public class StopRecordCommand implements ICommand {

	public StopRecordCommand() {
	}

	@Override
	public String getKeyWord() {
		return "stopRecord";
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

			if (guild.getAudioManager().getReceiveHandler() != null) {
				AudioReceiveHandler previousHandler = guild.getAudioManager().getReceiveHandler();
				if (previousHandler instanceof AudioRecorderHandler) {
					AudioRecorderHandler recorder = (AudioRecorderHandler)previousHandler;
					recorder.endRecording();

					channel.sendMessage("*L'enregistrement est terminé sur **" + vocalChannel + "***").complete();
	
					String source = recorder.getFilename("pcm");
					String destination = recorder.getFilename("mp3");

					channel.sendMessage("*Début de l'encodage mp3*").complete();

					try {
//						ffmpeg -f s16be -ar 48k -ac 2 -i 1-1508429227280.pcm 1-1508429227280.pcm.mp3
						Process process = new ProcessBuilder(
								"ffmpeg","-f","s16be","-ar","48k","-ac","2","-i", source, destination).start();
						InputStream is = process.getInputStream();
						InputStreamReader isr = new InputStreamReader(is);
						BufferedReader br = new BufferedReader(isr);
						String line;
	
//						System.out.printf("Output of running %s is:", Arrays.toString(args));
	
						while ((line = br.readLine()) != null) {
						  System.out.println(line);
						}

						channel.sendMessage("*Fin de l'encodage en mp3*").complete();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					
//					File sourceFile = new File(source);
//					sourceFile.delete();
				}
			}

			guild.getAudioManager().closeAudioConnection();
		}
	}

	@Override
	public String getCommandHelp() {
		return "Arrête l'enregistrement sonore sur le canal indiqué";
	}

}