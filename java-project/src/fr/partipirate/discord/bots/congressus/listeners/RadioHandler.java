package fr.partipirate.discord.bots.congressus.listeners;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import fr.partipirate.discord.bots.congressus.CongressusBot;
import fr.partipirate.discord.bots.congressus.GuildMusicManager;
import fr.partipirate.discord.bots.congressus.commands.radio.RadioHelper;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Game.GameType;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class RadioHandler extends ListenerAdapter {

	private static final long DELAY = 5000L;
	private static RadioHandler INSTANCE;
	private CongressusBot congressusBot;
//	private AudioTrack playingTrack = null;
	private boolean isTitleAware = true;

	public Map<String, TrackOptions> trackOptions = new HashMap<String, TrackOptions>();

	public RadioHandler(CongressusBot congressusBot) {
		this.congressusBot = congressusBot;

		launchRadioSupervisors();

		INSTANCE = this;
	}

	public static RadioHandler getInstance() {
		return INSTANCE;
	}

	public void resetGameTitle(AudioTrack track) {
		if (track == null) {
			this.congressusBot.getJDA().getPresence().setGame(null);
			System.out.println("Show no title");
//			this.congressusBot.getJDA().getPresence().setGame(Game.of(GameType.DEFAULT, null));
		} 
		else {

			String musicInfo = track.getInfo().title + " - " + track.getInfo().author ;

			this.congressusBot.getJDA().getPresence().setGame(Game.of(GameType.LISTENING, musicInfo));
			System.out.println("Show " + track.getInfo().title);
			System.out.println("Duration : " + getTimestamp(track.getInfo().length));
		}
	}

	private void launchRadioSupervisors() {
		Thread checkStatusThread = new Thread() {
			@Override
			public void run() {
				while (true) { // check the bot connection status
					synchronized (this) {
						try {
//							System.out.println("Check radio status");
							checkRadioStatus();
							this.wait(DELAY);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		};
		checkStatusThread.start();
	}

	public void checkRadioStatus() {
		Map<Long, GuildMusicManager> managers = congressusBot.getMusicManagers();

		for (Iterator<GuildMusicManager> iterator = managers.values().iterator(); iterator.hasNext();) {
			GuildMusicManager manager = iterator.next();

			AudioTrack track = manager.getPlayer().getPlayingTrack();
//			this.playingTrack = track;

			if (track == null) {
				System.out.println("Aucune musique");
				getNext(manager);
			} 
			else {
				TrackOptions trackOptions = this.trackOptions.get(track.getInfo().uri);
				long trackLength = (trackOptions != null && trackOptions.finishTime != null) ? Math.round(trackOptions.finishTime * 1000) : track.getInfo().length; 

				if ((trackLength - track.getPosition()) < DELAY) {
//					System.out.println("Reste moins de 10s : " + getTimestamp(track.getPosition()) + " / "
//							+ getTimestamp(track.getInfo().length));
					getNext(manager);
				} 
				else {
//					System.out.println("Ca joue : " + getTimestamp(track.getPosition()) + " / "
//							+ getTimestamp(track.getInfo().length));
				}
			}
		}
	}

	public void getTrack(GuildMusicManager manager, String id) {
		try {
			JSONObject object = RadioHelper.getTrack(id);
			
			if (object.has("track")) {
				JSONObject track = object.getJSONObject("track");
				String trackUrl = track.getString("tra_url");

				loadUrl(manager, trackUrl);
				System.out.println("Url  : " + trackUrl);

				TrackOptions currentTrackOptions = new TrackOptions();
				currentTrackOptions.startTime = (track.has("tra_start_time") && !track.isNull("tra_start_time")) ? track.getDouble("tra_start_time") : null;
				currentTrackOptions.finishTime = (track.has("tra_finish_time") && !track.isNull("tra_finish_time")) ? track.getDouble("tra_finish_time") : null;

				trackOptions.put(trackUrl, currentTrackOptions);
				System.out.println("Add " + currentTrackOptions + " to " + trackUrl);
			}

			if (object.has("numberOfTracks")) {
//				System.out.println("Number of tracks : " + object.getInt("numberOfTracks"));
			}
			if (object.has("durationOfTracks")) {
//				System.out.println("Durations of tracks : " + getTimestamp(object.getInt("durationOfTracks") * 1000L));
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getJingle(GuildMusicManager manager, String id) {
		try {
			JSONObject object = RadioHelper.getJingle(id);
			
			if (object.has("track")) {
				JSONObject track = object.getJSONObject("track");
				String trackUrl = track.getString("tra_url");

				loadUrl(manager, trackUrl);
				System.out.println("Url  : " + trackUrl);

				TrackOptions currentTrackOptions = new TrackOptions();
				currentTrackOptions.startTime = (track.has("tra_start_time") && !track.isNull("tra_start_time")) ? track.getDouble("tra_start_time") : null;
				currentTrackOptions.finishTime = (track.has("tra_finish_time") && !track.isNull("tra_finish_time")) ? track.getDouble("tra_finish_time") : null;

				trackOptions.put(trackUrl, currentTrackOptions);
				System.out.println("Add " + currentTrackOptions + " to " + trackUrl);
			}

			if (object.has("numberOfTracks")) {
//				System.out.println("Number of tracks : " + object.getInt("numberOfTracks"));
			}
			if (object.has("durationOfTracks")) {
//				System.out.println("Durations of tracks : " + getTimestamp(object.getInt("durationOfTracks") * 1000L));
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getNext(GuildMusicManager manager) {
		// If there are some items, it's not necessary to ask for some new item
		if (manager.scheduler.getNumberOfTracks() > 0) return;

		try {
			JSONObject object = RadioHelper.getNext();
			
			if (object.has("track")) {
				JSONObject track = object.getJSONObject("track");
				String trackUrl = track.getString("tra_url");

				loadUrl(manager, trackUrl);
				System.out.println("Url  : " + trackUrl);

				TrackOptions currentTrackOptions = new TrackOptions();
				currentTrackOptions.startTime = (track.has("tra_start_time") && !track.isNull("tra_start_time")) ? track.getDouble("tra_start_time") : null;
				currentTrackOptions.finishTime = (track.has("tra_finish_time") && !track.isNull("tra_finish_time")) ? track.getDouble("tra_finish_time") : null;

				trackOptions.put(trackUrl, currentTrackOptions);
				System.out.println("Add " + currentTrackOptions + " to " + trackUrl);
			}

			if (object.has("numberOfTracks")) {
//				System.out.println("Number of tracks : " + object.getInt("numberOfTracks"));
			}
			if (object.has("durationOfTracks")) {
//				System.out.println("Durations of tracks : " + getTimestamp(object.getInt("durationOfTracks") * 1000L));
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadUrl(GuildMusicManager manager, String trackUrl) {
		congressusBot.getPlayerManager().loadItemOrdered(manager, trackUrl, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(AudioTrack track) {
				manager.scheduler.queue(track);
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				AudioTrack firstTrack = playlist.getSelectedTrack();

				if (firstTrack == null) {
					firstTrack = playlist.getTracks().get(0);
				}

				manager.scheduler.clear();
				manager.scheduler.queue(firstTrack);
			}
			
			@Override
			public void noMatches() {
				System.err.println("No match for " + trackUrl);
			}

			@Override
			public void loadFailed(FriendlyException exception) {
				System.err.println("Exception for " + trackUrl);
				exception.printStackTrace();

				// No more usable
				RadioHelper.deleteTrack(trackUrl);
				
				// Error, so next right now
				getNext(manager);
			}
		});
	}

	protected String getTimestamp(long milis) {
		long seconds = milis / 1000;
		long hours = Math.floorDiv(seconds, 3600);
		seconds = seconds - (hours * 3600);
		long mins = Math.floorDiv(seconds, 60);
		seconds = seconds - (mins * 60);
		return (hours == 0 ? "" : hours + ":") + String.format("%02d", mins) + ":" + String.format("%02d", seconds);
	}

	public void setTitleAware(boolean isTitleAware) {
		this.isTitleAware = isTitleAware;
	}

	public boolean getTitleAware() {
		return this.isTitleAware;
	}
}