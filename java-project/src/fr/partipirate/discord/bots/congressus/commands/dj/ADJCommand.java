package fr.partipirate.discord.bots.congressus.commands.dj;

import fr.partipirate.discord.bots.congressus.commands.ICommand;
import fr.partipirate.discord.bots.congressus.Configuration;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public abstract class ADJCommand implements ICommand {

	public static String DJ_PIRATE_ROLE = "dj pirate";
	protected String djRoleName = null;
	
	protected Member getMember(User user, Guild guild) {
		if (guild == null) return null;
		
		return guild.getMember(user);
	}

	protected String getDJRoleName() {
		if (djRoleName == null) {
			djRoleName = (Configuration.getInstance().OPTIONS.get("radio") != null) ? Configuration.getInstance().OPTIONS.get("radio").get("djRoleName") : null;
		}
		if (djRoleName == null) {
			djRoleName = DJ_PIRATE_ROLE;
		}

//		System.out.println("DJ Role name : " + djRoleName);

		return djRoleName;
	}

	protected boolean isDJ(Member member) {
		if (member == null) return false;
		
		for (Role role : member.getRoles()) {
//			System.out.println("Actual Role name : " + role.getName());
			if (role.getName().equalsIgnoreCase(getDJRoleName())) return true;
		}
		
		return false;
	}

    protected String getTimestamp(long milis) {
        long seconds = milis / 1000;
        long hours = Math.floorDiv(seconds, 3600);
        seconds = seconds - (hours * 3600);
        long mins = Math.floorDiv(seconds, 60);
        seconds = seconds - (mins * 60);
        return (hours == 0 ? "" : hours + ":") + String.format("%02d", mins) + ":" + String.format("%02d", seconds);
    }

    protected String getOrNull(String s) {
        return s.isEmpty() ? "N/A" : s;
    }

	protected String makeLengthedString(String value, int length, int direction) {
		if (value.length() == length) return value;
		
		if (value.length() > length) return value.substring(0, length - 3) + "...";

		while(value.length() < length) {
			if (direction == 0 || (direction == 2 && (value.length() % 2 == 0))) {
				value = " " + value;
			}
			else {
				value = value + " ";
			}
		}
		
		return value;
	}
}