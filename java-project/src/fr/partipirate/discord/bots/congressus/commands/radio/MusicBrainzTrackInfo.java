package fr.partipirate.discord.bots.congressus.commands.radio;

public class MusicBrainzTrackInfo {

	private String recordingName, recordingID, recordingURL ;

	private String artistName, artistID, artistURL ;

	private String releaseName, releaseID ;

	private String coverURL ;

	// *** SETTERS

	// ** Recording setters **

	// Recording name setter
	public void setRecordingName(String value){
		this.recordingName = value ;
	}

	// Recording ID setter
	public void setRecordingID(String value){
		this.recordingID = value ;
	}

	// Recording URL setter
	public void setRecordingURL(String value){
		this.recordingURL = value ;
	}

	// ** Artist setters **

	// Artist name setter
	public void setArtistName(String value){
		this.artistName = value ;
	}

	// Artist ID setter
	public void setArtistID(String value){
		this.artistID = value ;
	}

	// Artist URL setter
	public void setArtistURL(String value){
		this.artistURL = value ;
	}

	// ** Release setters **

	// Release name setter
	public void setReleaseName(String value){
		this.releaseName = value ;
	}

	// Release ID setter
	public void setReleaseID(String value){
		this.releaseID = value ;
	}

	// ** Cover setter **

	public void setCoverURL(String value){
		this.coverURL = value ;
	}

	// *** GETTERS ***
	
	public String getRecordingName(){
		return this.recordingName ;
	}
	
	// Recording ID getter
	public String getRecordingID(){
		return this.recordingID ;
	}

	// Recording URL getter
	public String getRecordingURL(){
		return this.recordingURL ;
	}

	// ** Artist getter **

	// Artist name getter
	public String getArtistName(){
		return this.artistName ;
	}

	// Artist ID getter
	public String getArtistID(){
		return this.artistID ;
	}

	// Artist URL getter
	public String getArtistURL(){
		return this.artistURL ;
	}

	// ** Release getter **

	// Release name getter
	public String getReleaseName(){
		return this.releaseName ;
	}

	// Release ID getter
	public String getReleaseID(){
		return this.releaseID ;
	}

	// ** Cover getter **

	public String getCoverURL(){
		return this.coverURL ;
	}

}
