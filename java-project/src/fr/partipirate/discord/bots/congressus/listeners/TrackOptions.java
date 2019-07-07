package fr.partipirate.discord.bots.congressus.listeners;

public class TrackOptions {
	public Double startTime;
	public Double finishTime;

	@Override
	public String toString() {
		return super.toString() + "[startTime: " + startTime + ", finishTime: " + finishTime + "]";
	}
}