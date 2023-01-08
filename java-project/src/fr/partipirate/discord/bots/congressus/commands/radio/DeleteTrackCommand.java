package fr.partipirate.discord.bots.congressus.commands.radio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import fr.partipirate.discord.bots.congressus.CongressusBot;
import fr.partipirate.discord.bots.congressus.GuildMusicManager;
import fr.partipirate.discord.bots.congressus.commands.ICommand;
import fr.partipirate.discord.bots.congressus.commands.dj.ADJCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class DeleteTrackCommand extends ADJCommand implements ICommand {

	private CongressusBot bot;

	public DeleteTrackCommand(CongressusBot bot) {
		this.bot = bot;
	}

	@Override
	public String getKeyWord() {
		return "deleteTrack";
	}

	@Override
	public boolean canDoCommand(User user, Guild guild) {
		return isDJ(getMember(user, guild));
	}

	@Override
	public void doCommand(User user, MessageChannel channel, Guild guild, String[] commandParts) {
		// if (channel instanceof PrivateChannel) {
		// channel.sendMessage("*Commande à faire publiquement*").complete();
		// }
		// else
		if (!isDJ(getMember(user, guild))) {
			channel.sendMessage("*Vous n'avez pas les droits suffisants*").complete();
		} 
		else if (commandParts.length == 1) {
			channel.sendMessage("*Veuillez indiquer au moins une URL*").complete();
		} 
		else {
			if (commandParts.length == 2 && commandParts[1].equalsIgnoreCase("current" )) {
				AudioTrack track = bot.getGuildAudioPlayer(guild).getPlayer().getPlayingTrack();
				if (track != null) {
					RadioHelper.deleteTrack(track.getInfo().uri);
					channel.sendMessage("Suppression de **" + track.getInfo().title + "** *" + track.getInfo().author + "* - "
							+ getTimestamp(track.getInfo().length)).complete();
					bot.skipTrack((TextChannel) channel);
				}
				else {
					channel.sendMessage("Suppression de la piste courante impossible, le bot ne joue pas de musique**").complete();
				}
			}
			else {
				for (int index = 1; index < commandParts.length; ++index) {
					deleteTrack(channel, commandParts[index]);
				}
			}
		}
	}

	private void deleteTrack(MessageChannel channel, String trackUrl) {
		GuildMusicManager manager = bot.getMusicManagers().values().iterator().next();

		bot.getPlayerManager().loadItemOrdered(manager, trackUrl, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(AudioTrack track) {
				RadioHelper.deleteTrack(track.getInfo().uri);
				channel.sendMessage("Suppression de **" + track.getInfo().title + "** *" + track.getInfo().author + "* - "
						+ getTimestamp(track.getInfo().length)).complete();
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				for (AudioTrack track : playlist.getTracks()) {
					RadioHelper.deleteTrack(track.getInfo().uri);
					channel.sendMessage("Suppression de **" + track.getInfo().title + "** *" + track.getInfo().author + "* - "
							+ getTimestamp(track.getInfo().length)).complete();
				}
			}

			@Override
			public void noMatches() {
			}

			@Override
			public void loadFailed(FriendlyException exception) {
			}
		});
	}

	@Override
	public String getCommandHelp() {
		return "Supprime une piste dans la base de donnée [url|current]";
	}
}