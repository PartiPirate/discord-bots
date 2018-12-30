package fr.partipirate.discord.bots.congressus.commands.dj;

import fr.partipirate.discord.bots.congressus.commands.ICommand;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

public abstract class ADJCommand implements ICommand {

	public static String DJ_PIRATE_ROLE = "dj pirate";
	
	protected Member getMember(User user, Guild guild) {
		if (guild == null) return null;
		
		return guild.getMember(user);
	}

	protected boolean isDJ(Member member) {
		if (member == null) return false;
		
		for (Role role : member.getRoles()) {
			if (role.getName().equalsIgnoreCase(DJ_PIRATE_ROLE)) return true;
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