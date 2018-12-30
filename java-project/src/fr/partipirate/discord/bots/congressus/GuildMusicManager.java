package fr.partipirate.discord.bots.congressus;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import fr.partipirate.discord.bots.congressus.listeners.AudioPlayerSendHandler;

/**
 * Holder for both the player and a track scheduler for one guild.
 */
public class GuildMusicManager {
	/**
	 * Audio player for the guild.
	 */
	public final AudioPlayer player;
	/**
	 * Track scheduler for the player.
	 */
	public final TrackScheduler scheduler;
	
	private AudioPlayerManager manager;

	/**
	 * Creates a player and a track scheduler.
	 * 
	 * @param manager
	 *            Audio player manager to use for creating the player.
	 */
	public GuildMusicManager(AudioPlayerManager manager) {
		this.manager = manager;
		player = manager.createPlayer();
		scheduler = new TrackScheduler(player);
		player.addListener(scheduler);
	}

	/**
	 * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
	 */
	public AudioPlayerSendHandler getSendHandler() {
		return new AudioPlayerSendHandler(player);
	}

	public AudioPlayer getPlayer() {
		return player;
	}

	public TrackScheduler getScheduler() {
		return scheduler;
	}
	
	public AudioPlayerManager getManager() {
		return manager;
	}
}