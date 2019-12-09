package fr.partipirate.discord.bots.congressus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import fr.partipirate.discord.bots.congressus.commands.ICommand;
import fr.partipirate.discord.bots.congressus.listeners.SpeakingVolumeHandler;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;

public class CongressusBot extends ListenerAdapter implements EventListener {

	public static final int LOW_VOLUME = 10;

	@SuppressWarnings("rawtypes")
	public static void main(String[] args)
			throws LoginException, IllegalArgumentException, InterruptedException, RateLimitedException {
		Configuration.getInstance().readConfiguration(args[0]);

		CongressusBot congressusBot = new CongressusBot();
		congressusBot.setSpeakingAware(Configuration.getInstance().SPEAKING_AWARE);

		JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT).setToken(Configuration.getInstance().TOKEN);
		jdaBuilder.addEventListener(congressusBot);
		jdaBuilder.addEventListener(new EventListener() {
			@Override
			public void onEvent(Event event) {
//				System.out.println(event);
			}
		});

		for (Class pluginClass : Configuration.getInstance().LISTENER_PLUGINS) {
			EventListener eventListener = Configuration.getInstance().getHandler(pluginClass, congressusBot);
			System.out.println(eventListener.getClass().getName() + " listener found");
			jdaBuilder.addEventListener(eventListener);
		}

		jdaBuilder.setBulkDeleteSplittingEnabled(false);

		jdaBuilder.build().awaitReady();
	}

	private AudioPlayerManager playerManager;
	private Map<Long, GuildMusicManager> musicManagers;
	List<ICommand> commands;
	private boolean isSpeakingAware = false;
	private JDA jda;

	@SuppressWarnings("rawtypes")
	public CongressusBot() {
		this.commands = new ArrayList<ICommand>();

	    this.musicManagers = new HashMap<>();
	    
	    this.playerManager = new DefaultAudioPlayerManager();
	    AudioSourceManagers.registerRemoteSources(playerManager);
	    AudioSourceManagers.registerLocalSource(playerManager);

		for (Class pluginClass : Configuration.getInstance().COMMAND_PLUGINS) {
			ICommand command = Configuration.getInstance().getCommand(pluginClass, this);
			if (command != null) {
				commands.add(command);
				System.out.println("Command " + command.getKeyWord() + " found");
			}
			else {
				System.err.println("Command not found");
			}
		}
	}

	public JDA getJDA() {
		return this.jda;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.isFromType(ChannelType.PRIVATE)) {
//			System.out.printf("[PM] %s: %s\n", event.getAuthor().getName(), event.getMessage().getContent());
		} 
		else {
//			System.out.printf("[%s][%s] %s: %s\n", event.getGuild().getName(), event.getTextChannel().getName(),
//					event.getMember().getEffectiveName(), event.getMessage().getContent());
		}
	}

	@Override
	public void onReady(ReadyEvent event) {
		System.out.println("API is ready!");

		if (Configuration.getInstance().VOCAL_CHANNEL_NAME != null) {
			Guild guild = event.getJDA().getGuilds().get(0);
			
			GuildMusicManager guildAudioPlayer = getGuildAudioPlayer(guild);
			connectToVoiceChannel(guild.getAudioManager(), Configuration.getInstance().VOCAL_CHANNEL_NAME);
			guildAudioPlayer.getScheduler().readConfiguration(guildAudioPlayer, Configuration.getInstance().PLAYLIST);
		}
		
		this.jda = event.getJDA();
		
		if (Configuration.getInstance().NAME != null) {
			setBotNickname(Configuration.getInstance().NAME);
		}
	}
	
	public void setBotNickname(String nickname) {
		if (this.jda == null) return; // Not yet ready

		Member selfMember = this.jda.getGuilds().get(0).getMember(this.jda.getSelfUser());
		this.jda.getGuilds().get(0).getController().setNickname(selfMember, nickname).complete();
	}

	public synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
		long guildId = Long.parseLong(guild.getId());
		GuildMusicManager musicManager = musicManagers.get(guildId);

		if (musicManager == null) {
			musicManager = new GuildMusicManager(playerManager);
			musicManagers.put(guildId, musicManager);
		}

		guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

		return musicManager;
	}

	public void loadAndPlay(final TextChannel channel, final String trackUrl) {
		GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

		playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(AudioTrack track) {
				channel.sendMessage("Ajout à la liste de *" + track.getInfo().title + "*").queue();

				play(channel.getGuild(), musicManager, track);
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				for (AudioTrack track : playlist.getTracks()) {
					play(channel.getGuild(), musicManager, track);
				}
/*				
				AudioTrack firstTrack = playlist.getSelectedTrack();

				if (firstTrack == null) {
					firstTrack = playlist.getTracks().get(0);
				}

				channel.sendMessage("Ajout à la liste de *" + firstTrack.getInfo().title + "* (première piste de la liste *"
						+ playlist.getName() + "*)").queue();

				play(channel.getGuild(), musicManager, firstTrack);
*/				
			}

			@Override
			public void noMatches() {
				channel.sendMessage("Rien de trouvé à *" + trackUrl + "*").queue();
			}

			@Override
			public void loadFailed(FriendlyException exception) {
				exception.printStackTrace();
				channel.sendMessage("Je n'arrive pas à jouer : " + exception.getMessage()).queue();
			}
		});
	}

	private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
		connectToVoiceChannel(guild.getAudioManager(), Configuration.getInstance().VOCAL_CHANNEL_NAME);

		musicManager.scheduler.queue(track);
	}

	public void skipTrack(TextChannel channel) {
		GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
		musicManager.scheduler.nextTrack();

		if (channel != null) {
			channel.sendMessage("On passe à la suite").queue();
		}
	}

	public void skipTrack(TextChannel channel, int trackNumber) {
		GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
		AudioTrack track = musicManager.scheduler.forgetTrack(trackNumber);

		channel.sendMessage("On oublie la piste *" + track.getInfo().title + "*").queue();

		if (channel != null) {
			channel.sendMessage("On passe à la suite").queue();
		}
	}

	public void emptyTracks(TextChannel channel) {
		GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
		musicManager.scheduler.clear();

		if (channel != null) {
			channel.sendMessage("Hop là, plus rien à jouer").queue();
		}
	}
	
	public void shuffle(TextChannel channel) {
		GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
		musicManager.scheduler.shuffle();

		channel.sendMessage("J'ai fait tomber la pile de titres, je relance comme je peux :innocent:").queue();
	}	

	public void disconnect(AudioManager audioManager) {
		try {
			audioManager.closeAudioConnection();
		}
		catch (Exception e) {
			// We force it !!
		}
	}
	
	public VoiceChannel connectToVoiceChannel(AudioManager audioManager, String channelName) {
		if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {

			audioManager.setConnectionListener(new SpeakingVolumeHandler(audioManager, this));

			VoiceChannel choosedVoiceChannel = null;
			VoiceChannel foundVoiceChannel = null;
			VoiceChannel firstVoiceChannel = null;

			for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
				if (firstVoiceChannel == null)
					firstVoiceChannel = voiceChannel;
				if (voiceChannel.getName().equalsIgnoreCase(channelName)) {
					foundVoiceChannel = voiceChannel;
					break;
				}
				if (channelName.startsWith("~") && voiceChannel.getName().toLowerCase().contains(channelName.toLowerCase().substring(1))) {
					foundVoiceChannel = voiceChannel;
					break;
				}
			}

			if (foundVoiceChannel != null) {
				audioManager.setAutoReconnect(false);
				audioManager.openAudioConnection(foundVoiceChannel);
				choosedVoiceChannel = foundVoiceChannel;
			} 
			else if (firstVoiceChannel != null) {
				audioManager.setAutoReconnect(false);
				audioManager.openAudioConnection(firstVoiceChannel);
				choosedVoiceChannel = firstVoiceChannel;
			}

			System.out.println("Volume " + Configuration.getInstance().VOLUME);
			getPlayer(audioManager.getGuild()).setVolume(Configuration.getInstance().VOLUME);

			return choosedVoiceChannel;
		}

		return null;
	}

	public int previousVolume;

	private AudioPlayer getPlayer(Guild guild) {
		return this.getGuildAudioPlayer(guild).getPlayer();
	}

	public void unmute(Guild guild) {
		if (previousVolume != 0) {
			getPlayer(guild).setVolume(previousVolume);
			previousVolume = 0;
		}
	}

	public void mute(Guild guild) {
		if (previousVolume == 0) {
			previousVolume = getPlayer(guild).getVolume(); 
			getPlayer(guild).setVolume(0);
		}
	}
	
	public void lowVolume(Guild guild) {
		if (previousVolume == 0 && getPlayer(guild).getVolume() > LOW_VOLUME) {
			previousVolume = getPlayer(guild).getVolume(); 
			getPlayer(guild).setVolume(LOW_VOLUME);
		}
	}
	
	public List<ICommand> getCommands() {
		return commands;
	}
	
	public Map<Long, GuildMusicManager> getMusicManagers() {
		return musicManagers;
	}
	
	public AudioPlayerManager getPlayerManager() {
		return playerManager;
	}

	public boolean isSpeakingAware() {
		return isSpeakingAware;
	}

	public void setSpeakingAware(boolean isSpeakingAware) {
		this.isSpeakingAware = isSpeakingAware;
	}
}