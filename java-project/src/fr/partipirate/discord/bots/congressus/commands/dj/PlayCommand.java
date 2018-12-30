package fr.partipirate.discord.bots.congressus.commands.dj;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import fr.partipirate.discord.bots.congressus.CongressusBot;
import fr.partipirate.discord.bots.congressus.commands.ICommand;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class PlayCommand extends ADJCommand implements ICommand {

	private CongressusBot bot;

	public PlayCommand(CongressusBot bot) {
		this.bot = bot;
	}
	
	@Override
	public String getKeyWord() {
		return "play";
	}

	@Override
	public boolean canDoCommand(User user, Guild guild) {
		return isDJ(getMember(user, guild));
	}

	@Override
	public void doCommand(User user, MessageChannel channel, Guild guild, String[] commandParts) {
		if (channel instanceof PrivateChannel) {
        	channel.sendMessage("*Commande à faire publiquement*").complete();
		}
		else if (!isDJ(getMember(user, guild))) {
        	channel.sendMessage("*Vous n'avez pas les droits suffisants*").complete();
		}
		else if (getPlayer(guild).isPaused() && commandParts.length == 1) {
			getPlayer(guild).setPaused(false);
		}
		else if (commandParts.length > 1) {
			for (int index = 1; index < commandParts.length; ++index) {
				bot.loadAndPlay((TextChannel)channel, commandParts[index]);
			}
		}
	}
	
	@Override
	public String getCommandHelp() {
		return "Remet en lecture la musique, ou si une donnée est ajoutée chercher la piste et l'ajoute.";
	}

	private AudioPlayer getPlayer(Guild guild) {
		return bot.getGuildAudioPlayer(guild).getPlayer();
	}
}