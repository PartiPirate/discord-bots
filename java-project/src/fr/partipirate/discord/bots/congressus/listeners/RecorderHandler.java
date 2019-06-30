package fr.partipirate.discord.bots.congressus.listeners;

import java.util.Iterator;
import java.util.Map;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import fr.partipirate.discord.bots.congressus.CongressusBot;
import fr.partipirate.discord.bots.congressus.GuildMusicManager;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * Add an handler for the recorder, emitting some silenced sound
 */
public class RecorderHandler extends ListenerAdapter {

	private static final long DELAY = 5000L;
	private static RecorderHandler INSTANCE;
	private CongressusBot congressusBot;

	public RecorderHandler(CongressusBot congressusBot) {
		this.congressusBot = congressusBot;
		
		launchRadioSupervisor();
		
		INSTANCE = this;
	}

	public static RecorderHandler getInstance() {
		return INSTANCE;
	}

/*
	public void resetGameTitle(List<Users> users) {
		
	}
*/

	private void launchRadioSupervisor() {
		Thread checkStatusThread = new Thread() {
			@Override
			public void run() {
				while(true) { // check the bot connection status
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

	        if (track == null) {
//	        	System.out.println("Aucune musique");
	        	getNext(manager);
	        } 
	        else {
				if (track.getInfo().length - track.getPosition() < DELAY) {
//					System.out.println("Reste moins de 10s : " + getTimestamp(track.getPosition()) + " / " + getTimestamp(track.getInfo().length));
					getNext(manager);
				}
				else {
//					System.out.println("Ca joue : " + getTimestamp(track.getPosition()) + " / " + getTimestamp(track.getInfo().length));
				}
			}
		}
	}

	private void getNext(GuildMusicManager manager) {
		loadUrl(manager, "https://www.youtube.com/watch?v=g4mHPeMGTJM");
//		congressusBot.setVolume(1);
	}

	private void loadUrl(GuildMusicManager manager, String trackUrl) {
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
			}
		});
	}
}