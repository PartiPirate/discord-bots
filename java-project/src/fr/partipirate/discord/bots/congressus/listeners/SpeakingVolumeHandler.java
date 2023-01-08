package fr.partipirate.discord.bots.congressus.listeners;

import fr.partipirate.discord.bots.congressus.CongressusBot;
import net.dv8tion.jda.api.audio.hooks.ConnectionListener;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.managers.AudioManager;

public class SpeakingVolumeHandler implements ConnectionListener {
	private AudioManager audioManager;
	private CongressusBot congressusBot;
	int numberOfSpeaking = 0;

	public SpeakingVolumeHandler(AudioManager audioManager, CongressusBot congressusBot) {
		this.audioManager = audioManager;
		this.congressusBot = congressusBot;
	}
	
	@Override
	public void onUserSpeaking(User user, boolean speaking) {
		if (!congressusBot.isSpeakingAware()) return;
		
		System.out.println("User : " + user + ", speaking : " + speaking);
		if (speaking) {
			numberOfSpeaking++;
			if (numberOfSpeaking == 1) {
				congressusBot.lowVolume(audioManager.getGuild());
			}
		}
		else {
			numberOfSpeaking--;
			if (numberOfSpeaking == 0) {
				congressusBot.unmute(audioManager.getGuild());
			}
			else if (numberOfSpeaking < 0) {
				numberOfSpeaking = 0;
			}
		}
	}

	@Override
	public void onStatusChange(ConnectionStatus arg0) {
	}

	@Override
	public void onPing(long ping) {
	}
}