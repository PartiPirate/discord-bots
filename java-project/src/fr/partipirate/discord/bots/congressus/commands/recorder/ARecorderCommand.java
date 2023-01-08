package fr.partipirate.discord.bots.congressus.commands.recorder;

import fr.partipirate.discord.bots.congressus.Configuration;
import fr.partipirate.discord.bots.congressus.commands.ICommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public abstract class ARecorderCommand implements ICommand {

	private String[] recorderRoles;

	/**
	 * @return String[] the permitted roles for recording
	 */
	protected String[] getRecorderRoles() {
		if (recorderRoles == null) {
			recorderRoles = (Configuration.getInstance().OPTIONS.get("recorder") != null) ? Configuration.getInstance().OPTIONS.get("recorder").get("roles").split(",") : null;
		}

//		System.out.println("DJ Role name : " + djRoleName);

		return recorderRoles;
	}

	@Override
	public boolean canDoCommand(User user, Guild guild) {
		Member member = guild.getMember(user);

		for (Role role : member.getRoles()) {
			for (String recorderRole : getRecorderRoles()) {
				if (role.getName().equalsIgnoreCase(recorderRole)) return true;
			}
		}

		return false;
	}
}