package fr.partipirate.discord.bots.congressus.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.partipirate.discord.bots.congressus.CongressusBot;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.role.RoleCreateEvent;
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;
import net.dv8tion.jda.core.events.role.update.RoleUpdateNameEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class RoleHandler extends ListenerAdapter {

	private static RoleHandler INSTANCE;

	public Map<Guild, List<Role>> guildRoles = new HashMap<Guild, List<Role>>();

	
	public RoleHandler(CongressusBot congressusBot) {
		INSTANCE = this;
	}

	public static RoleHandler getInstance() {
		return INSTANCE;
	}

	@Override
	public void onReady(ReadyEvent event) {
		List<Guild> guilds = event.getJDA().getGuilds();
		for(Guild guild : guilds) {
			System.out.println(guild);

			List<Role> roles = new ArrayList<Role>(guild.getRoles()); // no more unmutable

			guildRoles.put(guild, roles);
			dumpRoles(roles);
		}
	}
	
	@Override
	public void onRoleCreate(RoleCreateEvent event) {
		List<Role> roles = guildRoles.get(event.getGuild());
		roles.add(event.getRole());

		dumpRoles(roles);
	}
	
	@Override
	public void onRoleDelete(RoleDeleteEvent event) {
		List<Role> roles = guildRoles.get(event.getGuild());
		for (Iterator<Role> iterator = roles.iterator(); iterator.hasNext();) {
			Role role = iterator.next();
			if (role.getId().equals(event.getRole().getId())) {
				iterator.remove();
				break;
			}
		}

		dumpRoles(roles);
	}
	
	@Override
	public void onRoleUpdateName(RoleUpdateNameEvent event) {
		List<Role> roles = guildRoles.get(event.getGuild());
		for (Iterator<Role> iterator = roles.iterator(); iterator.hasNext();) {
			Role role = iterator.next();
			if (role.getId().equals(event.getRole().getId())) {
				iterator.remove();
				break;
			}
		}

		roles.add(event.getRole());
		
//		System.out.println("Now had " + roles.size() + " role");

		dumpRoles(roles);
	}
	
	private void dumpRoles(List<Role> roles) {
		for (Role role : roles) {
			System.out.println(role);
		}
	}
}