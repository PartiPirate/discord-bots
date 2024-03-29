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
import fr.partipirate.discord.bots.congressus.listeners.VocalChannelsHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.audio.hooks.ConnectionListener;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class CongressusBot extends ListenerAdapter implements EventListener {

    public static final int LOW_VOLUME = 10;

    @SuppressWarnings("rawtypes")
    public static void main(String[] args)
	    throws LoginException, IllegalArgumentException, InterruptedException, RateLimitedException {
	Configuration.getInstance().readConfiguration(args[0]);

	CongressusBot congressusBot = new CongressusBot();
	congressusBot.setSpeakingAware(Configuration.getInstance().SPEAKING_AWARE);

	JDABuilder jdaBuilder = JDABuilder.createDefault(Configuration.getInstance().TOKEN);
	jdaBuilder.addEventListeners(congressusBot);

	for (Class pluginClass : Configuration.getInstance().LISTENER_PLUGINS) {
	    EventListener eventListener = Configuration.getInstance().getHandler(pluginClass, congressusBot);
	    System.out.println(eventListener.getClass().getName() + " listener found");
	    jdaBuilder.addEventListeners(eventListener);
	}
	jdaBuilder.addEventListeners(new VocalChannelsHandler(congressusBot));

	jdaBuilder.setBulkDeleteSplittingEnabled(false);

	jdaBuilder.setChunkingFilter(ChunkingFilter.ALL); // enable member chunking for all guilds
	jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);

	jdaBuilder.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS,
		GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.SCHEDULED_EVENTS,
		GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS);

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

	if (VocalChannelsHandler.getInstance() != null) {
	    VocalChannelsHandler.getInstance().checkAllVoiceChannels();
	}
    }

    public void setBotNickname(String nickname) {
	if (this.jda == null) return; // Not yet ready

	Member selfMember = this.jda.getGuilds().get(0).getMember(this.jda.getSelfUser());
	this.jda.getGuilds().get(0).modifyNickname(selfMember, nickname).complete();
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
		 * AudioTrack firstTrack = playlist.getSelectedTrack();
		 * 
		 * if (firstTrack == null) { firstTrack = playlist.getTracks().get(0); }
		 * 
		 * channel.sendMessage("Ajout à la liste de *" + firstTrack.getInfo().title +
		 * "* (première piste de la liste *" + playlist.getName() + "*)").queue();
		 * 
		 * play(channel.getGuild(), musicManager, firstTrack);
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

    public int getNumberOfTracks(TextChannel channel) {
	GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
	return musicManager.scheduler.getNumberOfTracks();
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
	if (!audioManager.isConnected()) {
	    audioManager.setConnectionListener(new SpeakingVolumeHandler(audioManager, this));
	    audioManager.setConnectionListener(new ConnectionListener() {

		@Override
		public void onUserSpeaking(User arg0, boolean arg1) {
		    // TODO Auto-generated method stub

		}

		@Override
		public void onStatusChange(ConnectionStatus arg0) {
		    // TODO Auto-generated method stub

		}

		@Override
		public void onPing(long arg0) {
			System.out.println("On Ping : " + arg0);
		}
	    });

	    VoiceChannel choosedVoiceChannel = null;
	    VoiceChannel foundVoiceChannel = null;
	    VoiceChannel firstVoiceChannel = null;

	    for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
		if (firstVoiceChannel == null) {
		    firstVoiceChannel = voiceChannel;
		}

		if (voiceChannel.getName().equalsIgnoreCase(channelName)) {
		    foundVoiceChannel = voiceChannel;
		    break;
		}

		if (channelName.startsWith("~")
			&& voiceChannel.getName().toLowerCase().contains(channelName.toLowerCase().substring(1))) {
		    foundVoiceChannel = voiceChannel;
		    break;
		}
	    }

	    if (foundVoiceChannel != null) {
		audioManager.setAutoReconnect(true);
		audioManager.openAudioConnection(foundVoiceChannel);
		choosedVoiceChannel = foundVoiceChannel;
	    }
	    else if (firstVoiceChannel != null) {
		audioManager.setAutoReconnect(true);
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
