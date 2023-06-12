package fr.partipirate.discord.bots.congressus.commands.personae;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import fr.partipirate.discord.bots.congressus.commands.ICommand;
import fr.partipirate.discord.bots.congressus.listeners.OnConnectionHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.User;

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

				Set<Member> members = new HashSet<>();
				members.addAll(guild.getMembersByEffectiveName(search, true));
				members.addAll(guild.getMembersByName(search, true));
				members.addAll(guild.getMembersByNickname(search, true));

				// looking for accounts from ID
				if (search.matches("<@\\d+>")) {
				    search = search.substring(2, search.length()-1);
				    System.out.println("Search for id "+search);
				    members.add(guild.getMemberById(search));
				}

				System.out.println("Found : " + members);

				if (members.size() == 0) {
				    System.out.println("Try another way...");

				    final String consumerSearch = search;
				    guild.pruneMemberCache();
				    
				    guild.loadMembers().onSuccess(new Consumer<List<Member>>() {
					@Override
					public void accept(List<Member> members) {
					    for(Member member : guild.getMembers()) {
						if ((member.getEffectiveName() != null && member.getEffectiveName().toLowerCase().contains(consumerSearch.toLowerCase())) || (member.getNickname() != null && member.getNickname().toLowerCase().contains(consumerSearch.toLowerCase()))) {
						    OnConnectionHandler.getInstance().updateMember(member, false);
						}
					    }
					}
				    });
				}
				else {
				    System.out.println("Found : " + members);

				    for (Member member : members) {
					OnConnectionHandler.getInstance().updateMember(member, false);
				    }
				}
			}
		}
	}

	@Override
	public String getCommandHelp() {
		return "Permet de remettre à jour ses droits ou ceux d'autres personnes en les nommant.";
	}
}
