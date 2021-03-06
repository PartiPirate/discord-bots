package fr.partipirate.discord.bots.congressus.commands.personae;

import java.util.ArrayList;
import java.util.List;

import fr.partipirate.discord.bots.congressus.commands.ICommand;
import fr.partipirate.discord.bots.congressus.listeners.OnConnectionHandler;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class RenewRightsCommand implements ICommand {

	@Override
	public String getKeyWord() {
		return "renew";
	}

	@Override
	public boolean canDoCommand(User user, Guild guild) {
		return true;
	}

	@Override
	public void doCommand(User user, MessageChannel channel, Guild guild, String[] commandParts) {
		if (guild == null) return;
		
		Member userMember = guild.getMember(user);
		
		if (commandParts.length == 1 && userMember != null) {
			OnConnectionHandler.getInstance().updateMember(userMember, false);
		}
		else if (commandParts.length > 1) {
			for(int index = 1; index < commandParts.length; index++) {
				String search = commandParts[index];
				if (search.startsWith("@")) {
					search = search.substring(1);
				}
				if (search.contains("#")) {
					search = search.substring(0, search.indexOf("#"));
				}

				System.out.println("Will search for : " + search);

				List<Member> members = new ArrayList<>();
				members.addAll(guild.getMembersByEffectiveName(search, true));
				members.addAll(guild.getMembersByName(search, true));
				
				System.out.println("Found : " + members);
				
				for (Member member : members) {
					OnConnectionHandler.getInstance().updateMember(member, false);
				}
			}
		}
	}

	@Override
	public String getCommandHelp() {
		return "Permet de remettre à jour ses droits ou ceux d'autres personnes en les nommant.";
	}
}