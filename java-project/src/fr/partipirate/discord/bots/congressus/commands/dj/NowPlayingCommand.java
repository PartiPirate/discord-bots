package fr.partipirate.discord.bots.congressus.commands.dj;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import fr.partipirate.discord.bots.congressus.CongressusBot;
import fr.partipirate.discord.bots.congressus.commands.ICommand;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;

public class NowPlayingCommand extends ADJCommand implements ICommand {
    private static final String CD = "\uD83D\uDCBF";
//    private static final String DVD = "\uD83D\uDCC0";
    private static final String MIC = "\uD83C\uDFA4 **|>** ";

//    private static final String QUEUE_TITLE = "__%s has added %d new track%s to the Queue:__";
    private static final String QUEUE_DESCRIPTION = "%s **|>**  %s\n%s\n%s %s";
//    private static final String QUEUE_INFO = "Info about the Queue: (Size - %d)";
//    private static final String ERROR = "Error while loading \"%s\"";

	private CongressusBot bot;

	public NowPlayingCommand(CongressusBot bot) {
		this.bot = bot;
	}

	@Override
	public String getKeyWord() {
		return "nowplaying";
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
            channel.sendMessage(String.format(QUEUE_DESCRIPTION, CD, getOrNull(track.getInfo().title),
                    "\u23F1 **|>** `[ " + getTimestamp(track.getPosition()) + " / " + getTimestamp(track.getInfo().length) + " ]`",
                    "" + MIC, getOrNull(track.getInfo().author))).complete();
        }
	}

	@Override
	public String getCommandHelp() {
		return "Donne les informations de la piste courante, s'il y en a une.";
	}

	private AudioPlayer getPlayer(Guild guild) {
		return bot.getGuildAudioPlayer(guild).getPlayer();
	}
}