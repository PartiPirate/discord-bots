package fr.partipirate.discord.bots.congressus.listeners;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import fr.partipirate.discord.bots.congressus.CongressusBot;
import net.dv8tion.jda.api.audio.hooks.ConnectionListener;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildMuteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceSelfDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceSelfMuteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
//import net.spy.memcached.MemcachedClient;

public class VocalChannelsHandler extends ListenerAdapter implements ConnectionListener {
//	static {
//		String[] serverlist = { "172.17.0.1:11211"};
//		SockIOPool pool = SockIOPool.getInstance();
//		pool.setServers(serverlist);
//		pool.initialize();
//	}

	private static Map<VoiceChannel, Map<Member, String>> CHANNELS = new HashMap<VoiceChannel, Map<Member, String>>();
	private static VocalChannelsHandler INSTANCE;
	/*
	private static MemcachedClient MEMCACHED_CLIENT;
	
	static {
		try {
			MEMCACHED_CLIENT = new MemcachedClient(new InetSocketAddress("192.168.0.100", 11211));
		}
		catch (Exception e) {
		}
	}
	*/

	private CongressusBot bot;

	public VocalChannelsHandler(CongressusBot congressusBot) {
		INSTANCE = this;
		this.bot = congressusBot;
	}

	public void checkAllVoiceChannels() {
		List<VoiceChannel> voiceChannels = bot.getJDA().getVoiceChannels();

		for (VoiceChannel voiceChannel : voiceChannels) {
			Map<Member, String> map = getChannelMap(voiceChannel);
			map.clear();

			List<Member> members = voiceChannel.getMembers();
			for (Member member : members) {
				map.put(member, "");
			}
		}
	}

	public static VocalChannelsHandler getInstance() {
		return INSTANCE;
	}

	@Override
	public void onGenericEvent(GenericEvent event) {
//		System.out.print("Event : ");
//		System.out.println(event.getClass().getName());
	}

	private Map<Member, String> getChannelMap(VoiceChannel channel) {
		Map<Member, String> map = CHANNELS.get(channel);

		if (map == null) {
			map = new HashMap<Member, String>();
			CHANNELS.put(channel, map);
		}

		return map;
	}

	public Set<Member> getChannelMembers(VoiceChannel channel) {
		return getChannelMap(channel).keySet();
	}

	public void putMemberInChannel(VoiceChannel channel, Member member) {
		Map<Member, String> map = getChannelMap(channel);
		map.put(member, "bidule");
	}

	public void removeMemberInChannel(VoiceChannel channel, Member member) {
		Map<Member, String> map = getChannelMap(channel);
		map.remove(member);
	}

	/* User leave, move, join */

	@Override
	public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
		if (event.getChannelJoined() != null) {
			putMemberInChannel(event.getChannelJoined().asVoiceChannel(), event.getMember());
			System.out.println(stringify(event.getChannelJoined().asVoiceChannel()));
		}
		if (event.getChannelLeft() != null) {
			removeMemberInChannel(event.getChannelLeft().asVoiceChannel(), event.getMember());
			System.out.println(stringify(event.getChannelLeft().asVoiceChannel()));
		}
		
	}

	/* User mute/deafen */

	@Override
	public void onGuildVoiceSelfDeafen(GuildVoiceSelfDeafenEvent event) {
		updateVoiceChannelsMembers(event.getMember());
	}

	@Override
	public void onGuildVoiceGuildMute(GuildVoiceGuildMuteEvent event) {
		updateVoiceChannelsMembers(event.getMember());
	}

	@Override
	public void onGuildVoiceSelfMute(GuildVoiceSelfMuteEvent event) {
		updateVoiceChannelsMembers(event.getMember());
	}

	@Override
	public void onGuildVoiceGuildDeafen(GuildVoiceGuildDeafenEvent event) {
		updateVoiceChannelsMembers(event.getMember());
	}

	private void updateVoiceChannelsMembers(Member eventMember) {
		for (Entry<VoiceChannel, Map<Member, String>> entry : CHANNELS.entrySet()) {
			for (Member member : entry.getValue().keySet()) {
				if (member.equals(eventMember)) {
//					System.out.println(entry.getKey().getName());
//					System.out.println(stringify(entry.getKey()));
					return;
				}
			}
		}
	}

//	onGuildVoice

	public String stringify(VoiceChannel voiceChannel) {
		JSONArray array = new JSONArray();

//		Map<Member, String> map = getChannelMap(voiceChannel);
		Set<Member> members = getChannelMembers(voiceChannel);
		for (Member member : members) {
			JSONObject object = new JSONObject();

			object.put("id", member.getUser().getId());
			object.put("nickname", member.getNickname());
			object.put("username", member.getUser().getName());
			object.put("userId", member.getUser().getId());
			object.put("isSelfDeafened", member.getVoiceState().isSelfDeafened());
			object.put("isGuildDeafened", member.getVoiceState().isGuildDeafened());
			object.put("isSelfMuted", member.getVoiceState().isSelfMuted());
			object.put("isGuildMuted", member.getVoiceState().isGuildMuted());

			array.put(object);
		}
		
		/*
		try {
			String key = "voice_channel_" + voiceChannel.getId();
			System.out.println("Set memcached key : " + key);
			if (MEMCACHED_CLIENT != null) MEMCACHED_CLIENT.set(key, 0, array.toString());
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		*/
		
		return array.toString();
	}

	@Override
	public void onPing(long ping) {
	}

	@Override
	public void onStatusChange(ConnectionStatus connectionStatus) {
	}

	@Override
	public void onUserSpeaking(User user, boolean isSpeaking) {
		// change speaping status of a user
	}
}
