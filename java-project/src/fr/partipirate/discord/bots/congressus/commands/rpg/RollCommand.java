package fr.partipirate.discord.bots.congressus.commands.rpg;

import java.awt.Color;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.partipirate.discord.bots.congressus.commands.ICommand;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;

public class RollCommand implements ICommand {

	public RollCommand() {
	}

	@Override
	public String getKeyWord() {
		return "roll";
	}
	
	@Override
	public void doCommand(User user, MessageChannel channel, Guild guild, String[] commandParts) {
		if (channel instanceof PrivateChannel) {
        	channel.sendMessage("*Commande à faire publiquement*").complete();
		}
		else if (!canDoCommand(user, guild)) {
        	channel.sendMessage("*Vous n'avez pas les droits suffisants*").complete();
		}
		else if (commandParts.length < 2) {
        	channel.sendMessage("Veuillez entrer un lancé de dé correct").complete();
		}
		else {
			StringBuilder diceCommand = new StringBuilder();
			for(int index = 1; index < commandParts.length; index++) {
				diceCommand.append(commandParts[index]);
			}

			int[] configuration = getFromPattern(diceCommand.toString());

			if (configuration == null) {
	        	channel.sendMessage("Veuillez entrer un lancé de dé correct").complete();
			}
			else {
				int[] rolls = roll(configuration[0], configuration[1], configuration[2]);

	            EmbedBuilder eb = new EmbedBuilder();
	            
	            eb.setColor(new Color(0x41D55F));
	            eb.setTitle(diceCommand.toString().toUpperCase());
	            eb.addField(new Field("Total", "" + rolls[0], true));

	            StringBuilder rollDescription = new StringBuilder();
	            String separator = "";
	            for (int i = 1; i < rolls.length; i++) {
					int roll = rolls[i];
					rollDescription.append(separator);
					rollDescription.append(roll);
					separator = ", ";
				}

	            eb.addField(new Field("Jet", rollDescription.toString(), true));

				channel.sendMessage(eb.build()).complete();
			}
		}
	}

	public static void main(String[] args) {
		String diceCommand = "6d6+3";
		
		int[] configuration;
		configuration = getFromPattern("6d6+3");
//		configuration = getFromPattern("6d6");
		
		int[] rolls;
		
		rolls = roll(configuration[0], configuration[1], configuration[2]);
		for (int i = 1; i < rolls.length; i++) {
			int j = rolls[i];
			System.out.println(j);
		}
		System.out.print(" => ");
		System.out.println(rolls[0]);

        EmbedBuilder eb = new EmbedBuilder();
        
        eb.setColor(new Color(0x41D55F));
        eb.setTitle(diceCommand.toString().toUpperCase());
        eb.addField(new Field("Total", "" + rolls[0], true));

        StringBuilder rollDescription = new StringBuilder();
        String separator = "";
        for (int i = 1; i < rolls.length; i++) {
			int roll = rolls[i];
			rollDescription.append(separator);
			rollDescription.append(roll);
			separator = ", ";
		}

        eb.addField(new Field("Jet", rollDescription.toString(), true));

		System.out.println(eb.build().toJSONObject());

	}

	public static int[] getFromPattern(String diceCommand) {
		Pattern pattern = Pattern.compile("([0-9]+)[dD]([0-9]+)[ +]*([0-9]*)");
		Matcher matcher = pattern.matcher(diceCommand);

		if (matcher.lookingAt()) {
			System.out.println(matcher.group(0));

			int[] configuration = new int[3];

			configuration[0] = Integer.parseInt(matcher.group(1));
			configuration[1] = Integer.parseInt(matcher.group(2));
			if (matcher.group(3).length() > 0) {
				configuration[2] = Integer.parseInt(matcher.group(3));
			}

			return configuration;
		}
		
		return null;
	}
	
	public static int[] roll(int numberOfDice, int numberOfFaces, int offset) {
		Random random = new Random(System.currentTimeMillis());

		int rolls[] = new int[numberOfDice + 1];
		
		for(int i = 0; i < numberOfDice; i++) {
			int roll = random.nextInt(numberOfFaces) + 1;
			rolls[0] += roll;
			rolls[i + 1] = roll;
		}

		rolls[0] += offset; 

		return rolls;
	}
	
	@Override
	public String getCommandHelp() {
		return "Lance les dés - aDb + c, où a est le nombre de dés, b le nombre de faces du dés, c le facteur fixe du jet";
	}

	@Override
	public boolean canDoCommand(User user, Guild guild) {
		return true;
	}

}