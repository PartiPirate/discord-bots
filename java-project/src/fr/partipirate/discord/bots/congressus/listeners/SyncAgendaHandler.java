package fr.partipirate.discord.bots.congressus.listeners;

import fr.partipirate.discord.bots.congressus.Configuration;
import fr.partipirate.discord.bots.congressus.CongressusBot;
import fr.partipirate.discord.bots.congressus.common.congressus.SyncAgenda;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SyncAgendaHandler extends ListenerAdapter {
    private static long DELAY = 3600000L; // 60mn, by default
    private static SyncAgendaHandler INSTANCE;
    private CongressusBot congressusBot;

    public SyncAgendaHandler(CongressusBot congressusBot) {
	this.congressusBot = congressusBot;

	if (Configuration.getInstance().OPTIONS.get("syncAgenda") != null) {
	    String delayString = Configuration.getInstance().OPTIONS.get("syncAgenda").get("delay");
	    if (delayString != null) {
		DELAY = Integer.parseInt(delayString);
	    }
	}
	System.out.println("Scheduling SyncAgenda every " + (DELAY / 1000) + " seconds");

	launchSchedulerThread();

	INSTANCE = this;
    }

    public static SyncAgendaHandler getInstance() {
	return INSTANCE;
    }

    private void launchSchedulerThread() {
	Thread syncAgendaThread = new Thread() {
	    @Override
	    public void run() {
		while (true) { // check the bot connection status
		    synchronized (this) {
			try {
			    syncAgenda();
			    this.wait(DELAY);
			}
			catch (InterruptedException e) {
			    e.printStackTrace();
			}
		    }
		}
	    }
	};
	syncAgendaThread.start();
    }

    private void syncAgenda() {
	if (congressusBot.getJDA() != null) {
	    // needed to check if bot is initialized
	    System.out.println("Syncing agenda");
	    for (Guild guild : congressusBot.getJDA().getGuilds())
		SyncAgenda.syncAgenda(guild);
	}
    }
}
