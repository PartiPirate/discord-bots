package fr.partipirate.discord.bots.congressus;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONWriter;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

/**
 * This class schedules tracks for the audio player. It contains the queue of
 * tracks.
 */
public class TrackScheduler extends AudioEventAdapter {
	private final AudioPlayer player;
	private final BlockingQueue<AudioTrack> queue;
	private static final String PLAYLISTS_FILE = "radio.json";

	/**
	 * @param player
	 *            The audio player this scheduler uses
	 */
	public TrackScheduler(AudioPlayer player) {
		this.player = player;
		this.queue = new LinkedBlockingQueue<>();
	}

	/**
	 * Add the next track to queue or play right away if nothing is in the
	 * queue.
	 *
	 * @param track
	 *            The track to play or add to queue.
	 */
	public void queue(AudioTrack track) {
		// Calling startTrack with the noInterrupt set to true will start the
		// track only if nothing is currently playing. If
		// something is playing, it returns false and does nothing. In that case
		// the player was already playing so this
		// track goes to the queue instead.
		if (!player.startTrack(track, true)) {
			queue.offer(track);
		}

		writeConfiguration();
	}

	/**
	 * Start the next track, stopping the current one if it is playing.
	 */
	public void nextTrack() {
		// Start the next track, regardless of if something is already playing
		// or not. In case queue was empty, we are
		// giving null to startTrack, which is a valid argument and will simply
		// stop the player.
		player.startTrack(queue.poll(), false);
		
		writeConfiguration();
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		// Only start the next track if the end reason is suitable for it
		// (FINISHED or LOAD_FAILED)
		if (endReason.mayStartNext) {
			nextTrack();
		}
	}
	
	public AudioTrack[] getTracks() {
		AudioTrack[] tracks = queue.toArray(new AudioTrack[queue.size()]);
		
		return tracks;
	}
	

	public void readConfiguration(GuildMusicManager guildAudioPlayer, String playlistFile) {
		try {
			FileInputStream fis = new FileInputStream((playlistFile == null) ? PLAYLISTS_FILE : playlistFile);
			JSONObject jsonObject = (JSONObject) new JSONTokener(fis).nextValue();
			
			JSONArray currentPlaylist = jsonObject.getJSONArray("current");
			List<String> uris = new ArrayList<>(); 
			for(int index = 0; index < currentPlaylist.length(); ++index) {
				String uri = currentPlaylist.getString(index);
				uris.add(uri);
			}
			
			fis.close();
			
			for (String uri : uris) {
				guildAudioPlayer.getManager().loadItemOrdered(guildAudioPlayer, uri, new AudioLoadResultHandler() {
					@Override
					public void trackLoaded(AudioTrack track) {
						queue(track);
					}

					@Override
					public void playlistLoaded(AudioPlaylist playlist) {
					}

					@Override
					public void noMatches() {
					}

					@Override
					public void loadFailed(FriendlyException exception) {
					}
				});
			}
		}
		catch (IOException e) {
		}
	}
	
	public void writeConfiguration() {
		try {
			FileWriter fw = new FileWriter(PLAYLISTS_FILE);
			JSONWriter writer = new JSONWriter(fw);
			
			writer.object();
			writer.key("current").array();
			
			AudioTrack playingTrack = player.getPlayingTrack();
			if (playingTrack != null) {
				writer.value(playingTrack.getInfo().uri);
			}
			
			for (AudioTrack track : getTracks()) {
				writer.value(track.getInfo().uri);
			}
			
			writer.endArray();
			writer.endObject();
			
			fw.flush();
			fw.close();
		}		
		catch (IOException e) {
		}
	}

	public void shuffle() {
		List<AudioTrack> audioTracks = new ArrayList<>();
		
		AudioTrack playingTrack = player.getPlayingTrack();
		if (playingTrack != null) {
			audioTracks.add(playingTrack);
		}
		
		for (AudioTrack track : getTracks()) {
			audioTracks.add(track);
		}
		
		Collections.shuffle(audioTracks);
		
		queue.clear();
		
		for (AudioTrack audioTrack : audioTracks) {
			queue.offer(audioTrack);
		}
		
		nextTrack();
	}

	public AudioTrack forgetTrack(int trackNumber) {
		List<AudioTrack> audioTracks = new ArrayList<>();
		for (AudioTrack track : getTracks()) {
			audioTracks.add(track);
		}

		queue.clear();
		
		AudioTrack forgottenTrack = null;
		
		for(int index = 0; index < audioTracks.size(); ++index) {
			if ((index + 2) != trackNumber) {
				queue.offer(audioTracks.get(index));
			}
			else {
				forgottenTrack = audioTracks.get(index);
			}
		}
		
		return forgottenTrack;
	}
	
}