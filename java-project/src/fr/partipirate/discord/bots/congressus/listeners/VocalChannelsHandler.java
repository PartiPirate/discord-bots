package fr.partipirate.discord.bots.congressus.listeners;

import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class VocalChannelsHandler extends ListenerAdapter {

	@Override
	public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
/*		
		System.out.print("Member : ");
		System.out.print(event.getMember().getNickname());
		System.out.print(", Event : ");
		System.out.print(event.getClass().getName());
		System.out.print(", Etat : ");
		System.out.print(event.getVoiceState());
		System.out.print(", Channel : ");
		System.out.println(event.getChannelLeft().getName());
*/		
	}
	
	@Override
	public void onGenericEvent(Event event) {
		System.out.print("Event : ");
		System.out.println(event.getClass().getName());
	}
}