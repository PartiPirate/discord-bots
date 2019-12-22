package fr.partipirate.discord.bots.congressus.commands.radio;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Iterator;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import fr.partipirate.discord.bots.congressus.Configuration;
import fr.partipirate.discord.bots.congressus.commands.radio.MusicBrainzTrackInfo;

public class RadioHelper {

	public static String getUrl() {
		StringBuilder sb = new StringBuilder();

		sb.append(Configuration.getInstance().OPTIONS.get("radio").get("url"));

		return sb.toString();
	}

	public static String getUrl(String method) throws UnsupportedEncodingException {
		return getUrl(method, new Properties());
	}

	public static String getUrl(String method, Properties parameters) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();

		sb.append(getUrl());
		sb.append("api.php?method=");
		sb.append(method);

		sb.append("&token=");
		sb.append(Configuration.getInstance().OPTIONS.get("radio").get("token"));

		sb.append("&secret=");
		sb.append(Configuration.getInstance().OPTIONS.get("radio").get("secret"));

		for (Entry<Object, Object> property : parameters.entrySet()) {
			sb.append("&");
			sb.append(property.getKey().toString());
			sb.append("=");
			sb.append(URLEncoder.encode(property.getValue().toString(), "UTF-8"));
		}

		return sb.toString();
	}

	public static JSONObject getNext() {
		try {
			JSONObject object = call(getUrl("do_getNext"));

			return object;
		} 
		catch (Exception e) {
		}

		return null;
	}

	public static boolean deleteTrack(String trackUrl) {
		try {
			Properties parameters = new Properties();
			parameters.setProperty("url", trackUrl);

			JSONObject object = call(getUrl("do_deleteTrack", parameters));

			if (object.has("status")) {
				return object.getBoolean("status");
			}
		} 
		catch (Exception e) {
		}

		return true;
	}

	public static boolean addTrack(AudioTrack track) {
		try {
			Properties parameters = new Properties();
			parameters.setProperty("url", track.getInfo().uri);
			parameters.setProperty("title", track.getInfo().title);
			parameters.setProperty("author", track.getInfo().author);
			parameters.setProperty("duration", String.valueOf(track.getInfo().length / 1000));

			JSONObject object = call(getUrl("do_addTrack", parameters));
			
			if (object.has("status")) {
				return object.getBoolean("status");
			}
		} 
		catch (Exception e) {
		}

		return true;
	}

	public static boolean hasTrack(String trackUrl) {
		try {
			Properties parameters = new Properties();
			parameters.setProperty("url", trackUrl);

			JSONObject object = call(getUrl("do_hasTrack", parameters));

			if (object.has("status")) {
				return object.getBoolean("status");
			}
		} 
		catch (Exception e) {
		}

		return true;
	}

	public static MusicBrainzTrackInfo searchTrackInfo(String trackName, String artistName){

		MusicBrainzTrackInfo mbTrackInfo = new MusicBrainzTrackInfo() ;

		String searchQuery = trackName ;

		searchQuery.toLowerCase​() ;
		searchQuery.replaceAll​(" ", "+") ;
		searchQuery.replaceAll​("&", "%26") ;

		String searchURL = "https://musicbrainz.org/ws/2/recording?query=" + searchQuery + "&fmt=json" ;

		try {

			JSONObject musicBrainsReply = call(searchURL) ;

			if (musicBrainsReply.has("recordings")) {

				JSONArray recordingsArray = musicBrainsReply.getJSONArray("recordings") ;

				boolean isFind = false ;
				int i = 0 ;


				// Search artist name in the recording list.
				while( !isFind && i < recordingsArray.length()){

					if (recordingsArray.getJSONObject(i).has("artist-credit")) {
						
						JSONArray artistArray = recordingsArray.getJSONObject(i).getJSONArray("artist-credit") ;

						boolean haveArtist = false ;
						int j = 0 ;

						// Search artist name in artists list
						while(!haveArtist && j < artistArray.length()){

							JSONObject artistObject = artistArray.getJSONObject(i) ;

							if (artistObject.has("name") && artistObject.getString("name").equalsIgnoreCase​(artistName)) {
							 		
							 		isFind = true ;

							 		if (artistObject.has("artist") && artistObject.getJSONObject("artist").has("id")) {

							 			mbTrackInfo.setArtistID(artistObject.getJSONObject("artist").getString("id")) ;

							 			mbTrackInfo.setArtistName(artistObject.getString("name")) ;

							 			String artistURL = "https://musicbrainz.org/artist/" + mbTrackInfo.getArtistID() ;

							 			mbTrackInfo.setArtistURL(artistURL) ;

							 		}

							}

							j++ ; 
						}

						if (haveArtist) {

							if (recordingsArray.getJSONObject(i).has("releases")) {
								JSONArray releasesArray = recordingsArray.getJSONObject(i).getJSONArray("releases") ;

								if (releasesArray.length() > 0) {
									
									JSONObject releaseObject = releasesArray.getJSONObject(0) ;

									if (releaseObject.has("id")) {
										mbTrackInfo.setReleaseID(releaseObject.getString("id")) ;
									}

									if (releaseObject.has("title")) {
										mbTrackInfo.setReleaseName(releaseObject.getString("title")) ;
										
									}

								}
							}

							if (recordingsArray.getJSONObject(i).has("id")) {

								mbTrackInfo.setRecordingID(recordingsArray.getJSONObject(i).getString("id")) ;

								String artistURL = "https://musicbrainz.org/recording/" + mbTrackInfo.getArtistID() ;

							 	mbTrackInfo.setRecordingURL(artistURL) ;

							}

							if (recordingsArray.getJSONObject(i).has("title")) {

								mbTrackInfo.setRecordingName(recordingsArray.getJSONObject(i).getString("title")) ;

							}

							String coverURL = getCoverURLInCoverArtArchive(mbTrackInfo.getReleaseID()) ;

							mbTrackInfo.setCoverURL(coverURL) ;

							return mbTrackInfo ;
							
						}

					}


					i++ ;
				}

			}

		}
		catch (Exception e) {
		}

		return null ;

	}

	private static String getCoverURLInCoverArtArchive(String id){

		String searchURL = "https://ia801900.us.archive.org/13/items/mbid-" + id + "/index.json" ;

		try {

			JSONObject coverArchiveReply = call(searchURL) ;

			if (coverArchiveReply.has("images")) {

				JSONArray imagesArray = coverArchiveReply.getJSONArray("images") ;

				if (imagesArray.length() > 0) {

					JSONObject imageObject = imagesArray.getJSONObject(0) ;

					if (imageObject.has("thumbnails")) {

						if (imageObject.getJSONObject("thumbnails").has("small")) {

							return imageObject.getJSONObject("thumbnails").getString("small") ;

						}
						else{

							Iterator<String> keyList = imageObject.getJSONObject("thumbnails").keys() ;

							if (keyList.hasNext()) {

								return imageObject.getJSONObject("thumbnails").getString(keyList.next()) ;
								
							}

						}
						
					}
					if (imageObject.has("image")) {

						return imageObject.getString("image") ;

					}
					
				}
				
			}

		}
		catch (Exception e) {
		}

		return null ;
	}

	private static JSONObject call(String apiCallUrl) throws IOException {
		URL url = new URL(apiCallUrl);
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
		
		return object;
	}
	
}