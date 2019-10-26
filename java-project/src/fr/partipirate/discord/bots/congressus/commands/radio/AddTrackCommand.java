package fr.partipirate.discord.bots.congressus.commands.radio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import fr.partipirate.discord.bots.congressus.CongressusBot;
import fr.partipirate.discord.bots.congressus.GuildMusicManager;
import fr.partipirate.discord.bots.congressus.commands.ICommand;
import fr.partipirate.discord.bots.congressus.commands.dj.ADJCommand;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class AddTrackCommand extends ADJCommand implements ICommand {

	private CongressusBot bot;

	public AddTrackCommand(CongressusBot bot) {
		this.bot = bot;
	}

	@Override
	public String getKeyWord() {
		return "addTrack";
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
			for (int index = 1; index < commandParts.length; ++index) {
				addTrack(channel, commandParts[index]);
			}
		}
	}

	private void addTrack(MessageChannel channel, String trackUrl) {
		GuildMusicManager manager = bot.getMusicManagers().values().iterator().next();

		System.out.println("Add track : " + trackUrl);
		
		bot.getPlayerManager().loadItemOrdered(manager, trackUrl, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(AudioTrack track) {
				if (RadioHelper.hasTrack(track.getInfo().uri)) {
					channel.sendMessage("Ajout de **" + track.getInfo().uri + "** refusée, déjà présente").complete();
					return;
				}

				RadioHelper.addTrack(track);
				channel.sendMessage("Ajout de **" + track.getInfo().title + "** *" + track.getInfo().author + "* - "
						+ getTimestamp(track.getInfo().length)).complete();
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				for (AudioTrack track : playlist.getTracks()) {
					if (RadioHelper.hasTrack(track.getInfo().uri)) {
						channel.sendMessage("Ajout de **" + track.getInfo().uri + "** refusée, déjà présente").complete();
						continue;
					}

					RadioHelper.addTrack(track);
					channel.sendMessage("Ajout de **" + track.getInfo().title + "** *" + track.getInfo().author + "* - "
							+ getTimestamp(track.getInfo().length)).complete();
				}
			}

			@Override
			public void noMatches() {
				System.out.println("Found nothing");
			}

			@Override
			public void loadFailed(FriendlyException exception) {
				System.out.println("Load fail");
				exception.printStackTrace();
			}
		});
	}

	@Override
	public String getCommandHelp() {
		return "Ajoute une piste dans la base de donnée";
	}
}