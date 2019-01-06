package fr.partipirate.discord.bots.congressus.listeners;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import fr.partipirate.discord.bots.congressus.Configuration;

import net.dv8tion.jda.core.audio.AudioReceiveHandler;
import net.dv8tion.jda.core.audio.CombinedAudio;
import net.dv8tion.jda.core.audio.UserAudio;

public class AudioRecorderHandler implements AudioReceiveHandler {

	private static int seed = 0;
	private int id;
	private long start;
	private boolean recording = false;
	private FileOutputStream fis = null;
	private String filename = null;
	
	
	public String getRecorderDir() {
		String recorderDir = (Configuration.getInstance().OPTIONS.get("recorder") != null) ? (Configuration.getInstance().OPTIONS.get("recorder").get("audio") != null ? Configuration.getInstance().OPTIONS.get("recorder").get("audio") : "")  : "";

		System.out.println(recorderDir);

		return recorderDir;
	}

	public String getRelativeFilePath(String extension) {
		File file = new File(getFilename(extension));
		String filename = file.getName();
		
		return getRecorderDir() + filename;
	}

	public String getFilename(String extension) {
		return filename + "." + extension;
	}
	
	public boolean isRecording() {
		return recording;
	}

	public void startRecording() {
		this.id = ++seed;
		this.start= System.currentTimeMillis();
		this.recording = true;

		// open outputstream
		try {
			File file = new File(getRecorderDir() + this.id + "-" + System.currentTimeMillis());
			filename = file.getAbsolutePath();
			fis = new FileOutputStream(getFilename("pcm"));
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("Start recording");
	}
	
	public long endRecording() {
		long end = System.currentTimeMillis();

		this.recording = false;

		// close outputstream
		try {
			fis.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Stop recording");

		return end - start;
	}
	
	@Override
	public boolean canReceiveCombined() {
		System.out.println("Recording :" + this.recording);

		return this.recording;
	}

	@Override
	public boolean canReceiveUser() {
		return false;
	}

	@Override
	public void handleCombinedAudio(CombinedAudio combinedAudio) {
		byte[] data = combinedAudio.getAudioData(1.0);
		System.out.println(combinedAudio.getUsers());
		System.out.println(data.length);
		System.out.println(data[0] + " " + data[1]);
		
		// close outputstream
		try {
			fis.write(data);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handleUserAudio(UserAudio userAudio) {
		// DO NOTHING
	}
}
