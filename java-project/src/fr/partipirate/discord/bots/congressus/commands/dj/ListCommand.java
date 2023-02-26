package fr.partipirate.discord.bots.congressus.commands.dj;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import fr.partipirate.discord.bots.congressus.CongressusBot;
import fr.partipirate.discord.bots.congressus.TrackScheduler;
import fr.partipirate.discord.bots.congressus.commands.ICommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

public class ListCommand extends ADJCommand implements ICommand {

	private CongressusBot bot;

	public ListCommand(CongressusBot bot) {
		this.bot = bot;
	}
	
	@Override
	public String getKeyWord() {
		return "list";
	}

	@Override
	public boolean canDoCommand(User user, Guild guild) {
		return true;
	}

	@Override
	public void doCommand(User user, MessageChannel channel, Guild guild, String[] commandParts) {
		if (channel instanceof PrivateChannel) {
        	channel.sendMessage("*Commande à faire publiquement*").complete();
		}
		else if (getPlayer(guild).getPlayingTrack() == null) { // No song is playing
        	channel.sendMessage("Aucune musique ").complete();
        }
        else {
        	AudioTrack track = getPlayer(guild).getPlayingTrack();
        	AudioTrack[] tracks = getScheduler(guild).getTracks();
        	
        	long offsetTime = track.getInfo().length - track.getPosition();
        	long now = System.currentTimeMillis();
        	
        	StringBuilder sb = new StringBuilder();
        	sb.append("```");
        	String lineSeparator = "\n";
			String pattern = "'le' EEEEE dd MMMMM yyyy 'à' HH:mm";
			SimpleDateFormat simpleDateFormat =
			        new SimpleDateFormat(pattern, new Locale("fr", "FR"));

			sb.append(lineSeparator);
			sb.append(makeLengthedString(track.getInfo().title, 50, 1));
			sb.append(" - ");
			sb.append(makeLengthedString(getTimestamp(track.getInfo().length), 8, 0));
			sb.append(" - En cours ");
			
			int numberOfTitles = 1;
			
        	for (AudioTrack audioTrack : tracks) {

        		if (numberOfTitles < 10) {
					sb.append(lineSeparator);
					sb.append(makeLengthedString(audioTrack.getInfo().title, 50, 1));
					sb.append(" - ");
					sb.append(makeLengthedString(getTimestamp(audioTrack.getInfo().length), 8, 0));
					sb.append(" @ ");
					sb.append(simpleDateFormat.format(new Date(now + offsetTime)));
        		}
        		else if (numberOfTitles == 10) {
					sb.append(lineSeparator);
					sb.append(" ... ");
        		}
        		
				numberOfTitles++;

				offsetTime += audioTrack.getInfo().length;
					
//							lineSeparator = "\n";
			}
        	
			sb.append(lineSeparator);
			sb.append(makeLengthedString(numberOfTitles + " titres", 50, 1));
			sb.append(" - ");
			sb.append(makeLengthedString(getTimestamp(offsetTime), 8, 0));
			sb.append(" ➟ ");
			sb.append(simpleDateFormat.format(new Date(now + offsetTime)));

        	sb.append("\n```");
        	
        	channel.sendMessage(sb.toString()).complete();
        }
	}
	
	@Override
	public String getCommandHelp() {
		return "Donne la liste des pistes en cours.";
	}

	private AudioPlayer getPlayer(Guild guild) {
		return bot.getGuildAudioPlayer(guild).getPlayer();
	}

	private TrackScheduler getScheduler(Guild guild) {
		return bot.getGuildAudioPlayer(guild).getScheduler();
	}
}