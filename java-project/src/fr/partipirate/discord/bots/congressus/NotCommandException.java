package fr.partipirate.discord.bots.congressus;

public class NotCommandException extends Exception {
	private static final long serialVersionUID = -6992804275369124831L;

	public NotCommandException(String message) {
		super(message);
	}
}