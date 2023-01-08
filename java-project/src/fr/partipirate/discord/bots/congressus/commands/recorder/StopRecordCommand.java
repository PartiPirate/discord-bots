package fr.partipirate.discord.bots.congressus.commands.recorder;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import fr.partipirate.discord.bots.congressus.Configuration;
import fr.partipirate.discord.bots.congressus.commands.ICommand;
import fr.partipirate.discord.bots.congressus.listeners.AudioRecorderHandler;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

public class StopRecordCommand extends ARecorderCommand implements ICommand {

	public StopRecordCommand() {
	}

	@Override
	public String getKeyWord() {
		return "stopRecord";
	}

	private void mp3ize(String source, String destination) throws IOException {
		System.out.println("MP3ize " + source + " to " + destination);

//			ffmpeg -f s16be -ar 48k -ac 2 -i 1-1508429227280.pcm 1-1508429227280.pcm.mp3
			Process process = new ProcessBuilder(
					"ffmpeg","-f","s16be","-ar","48k","-ac","2","-i", source, destination).start();
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;

//			System.out.printf("Output of running %s is:", Arrays.toString(args));

			while ((line = br.readLine()) != null) {
			  System.out.println(line);
			}
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
/*			
			StringBuilder vocalChannelBuilder = new StringBuilder();
			String separator = "";
			for (int i = 1; i < commandParts.length; i++) {
				vocalChannelBuilder.append(separator);
				vocalChannelBuilder.append(commandParts[i]);
				separator = " ";
			}
			
			String vocalChannel = vocalChannelBuilder.toString();
*/
			if (guild.getAudioManager().getReceivingHandler() != null) {
				AudioReceiveHandler previousHandler = guild.getAudioManager().getReceivingHandler();
				if (previousHandler instanceof AudioRecorderHandler) {
					AudioRecorderHandler recorder = (AudioRecorderHandler)previousHandler;
					recorder.endRecording();

//					channel.sendMessage("*L'enregistrement est terminé sur **" + vocalChannel + "***").complete();
					channel.sendMessage("*L'enregistrement est terminé*").complete();
					channel.sendMessage("*Début de l'encodage mp3*").complete();

					try {
						String source = recorder.getFilename("pcm");
						String destination = recorder.getFilename("mp3");
						mp3ize(source, destination);

						for (String filename : recorder.getUserFileOutputStreams().keySet()) {
							FileOutputStream fis = recorder.getUserFileOutputStreams().get(filename);
							fis.close();

							source = filename;
							destination = filename.replace(".pcm", ".mp3");
							mp3ize(source, destination);
						}
						
						channel.sendMessage("*Fin de l'encodage en mp3*").complete();

						if (Configuration.getInstance().OPTIONS.get("recorder") != null && Configuration.getInstance().OPTIONS.get("recorder").get("host") != null) {
							channel.sendMessage("Enregistrement disponible ici : " + Configuration.getInstance().OPTIONS.get("recorder").get("host") + recorder.getRelativeFilePath("mp3")).complete();
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					
//					File sourceFile = new File(source);
//					sourceFile.delete();
				}
			}

			guild.getAudioManager().closeAudioConnection();
			guild.getAudioManager().setReceivingHandler(null);
		}
	}

	@Override
	public String getCommandHelp() {
		return "Arrête l'enregistrement sonore en cours";
	}

}