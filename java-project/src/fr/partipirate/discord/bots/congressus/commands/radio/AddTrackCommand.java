package fr.partipirate.discord.bots.congressus.commands.radio;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.json.JSONObject;
import org.json.JSONTokener;

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

		bot.getPlayerManager().loadItemOrdered(manager, trackUrl, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(AudioTrack track) {
				if (hasTrack(track.getInfo().uri)) {
					channel.sendMessage("Ajout de **" + track.getInfo().uri + "** refusée, déjà présente").complete();
					return;
				}

				addTrack(track);
				channel.sendMessage("Ajout de **" + track.getInfo().title + "** *" + track.getInfo().author + "* - "
						+ getTimestamp(track.getInfo().length)).complete();
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				for (AudioTrack track : playlist.getTracks()) {
					if (hasTrack(track.getInfo().uri)) {
						channel.sendMessage("Ajout de **" + track.getInfo().uri + "** refusée, déjà présente").complete();
						continue;
					}

					addTrack(track);
					channel.sendMessage("Ajout de **" + track.getInfo().title + "** *" + track.getInfo().author + "* - "
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

	private boolean addTrack(AudioTrack track) {
		try {
			StringBuilder apiCallUrlBuilder = new StringBuilder();
			apiCallUrlBuilder.append(RadioHelper.getUrl("do_addTrack"));
			apiCallUrlBuilder.append("&url=");
			apiCallUrlBuilder.append(URLEncoder.encode(track.getInfo().uri, "UTF-8"));
			apiCallUrlBuilder.append("&title=");
			apiCallUrlBuilder.append(URLEncoder.encode(track.getInfo().title, "UTF-8"));
			apiCallUrlBuilder.append("&author=");
			apiCallUrlBuilder.append(URLEncoder.encode(track.getInfo().author, "UTF-8"));
			apiCallUrlBuilder.append("&duration=");
			apiCallUrlBuilder.append(URLEncoder.encode(String.valueOf(track.getInfo().length / 1000), "UTF-8"));

			String apiCallUrl = apiCallUrlBuilder.toString();
			System.out.println(apiCallUrl);

			URL url = new URL(apiCallUrl);
			URLConnection connection = url.openConnection();
			InputStreamReader sr = new InputStreamReader(connection.getInputStream());
			StringWriter sw = new StringWriter();

			char[] buffer = new char[8192];
			int nbRead;

			while ((nbRead = sr.read(buffer)) != -1) {
				sw.write(buffer, 0, nbRead);
			}

			sr.close();
			sw.close();

			String json = sw.toString();

			JSONObject object = (JSONObject) new JSONTokener(json).nextValue();

			if (object.has("status")) {
				return object.getBoolean("status");
			}
		} 
		catch (Exception e) {
		}

		return true;
	}

	private boolean hasTrack(String trackUrl) {
		try {
			String apiCallUrl = RadioHelper.getUrl("do_hasTrack") + "&url=" + URLEncoder.encode(trackUrl, "UTF-8");
			System.out.println(apiCallUrl);

			URL url = new URL(apiCallUrl);
			URLConnection connection = url.openConnection();
			InputStreamReader sr = new InputStreamReader(connection.getInputStream());
			StringWriter sw = new StringWriter();

			char[] buffer = new char[8192];
			int nbRead;

			while ((nbRead = sr.read(buffer)) != -1) {
				sw.write(buffer, 0, nbRead);
			}

			sr.close();
			sw.close();

			String json = sw.toString();

			JSONObject object = (JSONObject) new JSONTokener(json).nextValue();

			if (object.has("status")) {
				return object.getBoolean("status");
			}
		} 
		catch (Exception e) {
		}

		return true;
	}

	@Override
	public String getCommandHelp() {
		return "Ajoute une piste dans la base de donnée";
	}
}