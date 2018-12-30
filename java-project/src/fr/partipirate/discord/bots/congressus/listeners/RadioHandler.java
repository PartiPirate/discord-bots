package fr.partipirate.discord.bots.congressus.listeners;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import fr.partipirate.discord.bots.congressus.Configuration;
import fr.partipirate.discord.bots.congressus.CongressusBot;
import fr.partipirate.discord.bots.congressus.GuildMusicManager;
import fr.partipirate.discord.bots.congressus.commands.radio.RadioHelper;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class RadioHandler extends ListenerAdapter {

	private static final long DELAY = 5000L;
	private static RadioHandler INSTANCE;
	private CongressusBot congressusBot;
	private AudioTrack playingTrack = null;
	private boolean isTitleAware = true;

	public RadioHandler(CongressusBot congressusBot) {
		this.congressusBot = congressusBot;
		
		launchRadioSupervisor();
		
		INSTANCE = this;
	}

	public static RadioHandler getInstance() {
		return INSTANCE;
	}

	private void launchRadioSupervisor() {
		Thread checkStatusThread = new Thread() {
			@Override
			public void run() {
				while(true) { // check the bot connection status
					synchronized (this) {
						try {
							System.out.println("Check radio status");
							checkRadioStatus();
							this.wait(DELAY);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		};
		checkStatusThread.start();
		
		Thread showNameThread = new Thread() {
			@Override
			public void run() {
				int position = 0;
				
				while(true) { // check the bot connection status
					synchronized (this) {
						try {
							StringBuilder nicknameBuilder = new StringBuilder(Configuration.getInstance().NAME);
							String nickname = nicknameBuilder.toString();

							if (isTitleAware) {
								AudioTrack track = playingTrack;

						        if (track == null) {
						        }
						        else {
						        	nicknameBuilder.append(" - ");
						        	nicknameBuilder.append(track.getInfo().title);
/*						        	
							        	nickBuilder.append(" - ");
							        	nickBuilder.append(getTimestamp(track.getPosition()));
							        	nickBuilder.append(" / ");
							        	nickBuilder.append(getTimestamp(track.getInfo().length));
*/						        	
						        	nicknameBuilder.append("            ");
						        }

								nickname = nicknameBuilder.toString();

								int beginIndex = position;
								int endIndex = beginIndex + 32;
	
								if (endIndex > nickname.length() && beginIndex != 0) {
									position = 0;
	
									beginIndex = position;
									endIndex = beginIndex + 32;
	
									if (endIndex > nickname.length()) {
										endIndex = nickname.length();
									}
								}
								else if (endIndex > nickname.length() && beginIndex == 0) {
									endIndex = nickname.length();
								}

								nickname = nickname.substring(beginIndex, endIndex);
								position += 2;
							}

//							System.out.println("Start change : " + new Date());
//							System.out.println("Change nickname to : " + nickName);

							congressusBot.setBotNickname(nickname);
//							System.out.println("End change : " + new Date());
						}
						catch(Exception e) {
							e.printStackTrace();
						}

						try {
							this.wait(100L); // Just in case, the nickname setting has a delay 
						} 
						catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
			
		};
		
		if (Configuration.getInstance().NAME != null) {
			showNameThread.start();
		}
	}
	
	public void checkRadioStatus() {
		Map<Long, GuildMusicManager> managers = congressusBot.getMusicManagers();
		
		for (Iterator<GuildMusicManager> iterator = managers.values().iterator(); iterator.hasNext();) {
			GuildMusicManager manager = iterator.next();
			
	        AudioTrack track = manager.getPlayer().getPlayingTrack();
	        this.playingTrack  = track;
	        
	        if (track == null) {
//	        	System.out.println("Aucune musique");
	        	getNext(manager);
	        } 
	        else {
				if (track.getInfo().length - track.getPosition() < DELAY) {
//					System.out.println("Reste moins de 10s : " + getTimestamp(track.getPosition()) + " / " + getTimestamp(track.getInfo().length));
					getNext(manager);
				}
				else {
//					System.out.println("Ca joue : " + getTimestamp(track.getPosition()) + " / " + getTimestamp(track.getInfo().length));
				}
			}
		}
	}

	private void getNext(GuildMusicManager manager) {
		String nextCall = RadioHelper.getUrl("do_getNext");
//		String nextCall = "https://radio.partipirate.org/api.php?method=do_getNext";
		System.out.println(nextCall);
		
		try {
			URL url = new URL(nextCall);
			URLConnection connection = url.openConnection();
			InputStreamReader sr = new InputStreamReader(connection.getInputStream());
			StringWriter sw = new StringWriter();
			
			char[] buffer = new char[8192];
			int nbRead;
			
			while ((nbRead = sr.read(buffer)) != -1) {
				sw.write(buffer, 0, nbRead);
			}

			sr.close();
			sw.close();

			String json = sw.toString();

			JSONObject object = (JSONObject) new JSONTokener(json).nextValue();

			if (object.has("track")) {
				JSONObject track = object.getJSONObject("track");
				String trackUrl = track.getString("tra_url");
				
				loadUrl(manager, trackUrl);
				System.out.println("Url  : " + trackUrl);
			}
			
			System.out.println("Number of tracks : " + object.getInt("numberOfTracks"));
			System.out.println("Durations of tracks : " + getTimestamp(object.getInt("durationOfTracks") * 1000L));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void loadUrl(GuildMusicManager manager, String trackUrl) {
		congressusBot.getPlayerManager().loadItemOrdered(manager, trackUrl, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(AudioTrack track) {
				manager.scheduler.queue(track);
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				AudioTrack firstTrack = playlist.getSelectedTrack();

				if (firstTrack == null) {
					firstTrack = playlist.getTracks().get(0);
				}

				manager.scheduler.queue(firstTrack);
			}

			@Override
			public void noMatches() {
				System.err.println("No match for " + trackUrl);
			}

			@Override
			public void loadFailed(FriendlyException exception) {
				System.err.println("Exception for " + trackUrl);
				exception.printStackTrace();
			}
		});
	}

    protected String getTimestamp(long milis) {
        long seconds = milis / 1000;
        long hours = Math.floorDiv(seconds, 3600);
        seconds = seconds - (hours * 3600);
        long mins = Math.floorDiv(seconds, 60);
        seconds = seconds - (mins * 60);
        return (hours == 0 ? "" : hours + ":") + String.format("%02d", mins) + ":" + String.format("%02d", seconds);
    }

	public void setTitleAware(boolean isTitleAware) {
		this.isTitleAware  = isTitleAware;
	}

	public boolean getTitleAware() {
		return this.isTitleAware;
	}
}