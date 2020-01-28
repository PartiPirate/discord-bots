package fr.partipirate.discord.bots.congressus.commands.radio;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
		} catch (Exception e) {
		}

		return null;
	}

	public static boolean deleteTrack(String trackUrl) {
		return true;
		/*
		 * try { Properties parameters = new Properties(); parameters.setProperty("url",
		 * trackUrl);
		 * 
		 * JSONObject object = call(getUrl("do_deleteTrack", parameters));
		 * 
		 * if (object.has("status")) { return object.getBoolean("status"); } } catch
		 * (Exception e) { }
		 * 
		 * return true;
		 */
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
		} catch (Exception e) {
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
		} catch (Exception e) {
		}

		return true;
	}

	public static MusicBrainzTrackInfo searchTrackInfo(String trackName, String artistName) {
		
		MusicBrainzTrackInfo mbTrackInfo = new MusicBrainzTrackInfo() ;
		
		String formatedTrackName = trackName ;

		formatedTrackName = formatedTrackName.toLowerCase() ;
		formatedTrackName = formatedTrackName.replaceAll(" ", "+") ;
		formatedTrackName = formatedTrackName.replaceAll("&", "%26") ;
		
		String formatedArtistName = artistName ;
		
		formatedArtistName = formatedArtistName.toLowerCase() ;
		formatedArtistName = formatedArtistName.replaceAll(" ", "+") ;
		formatedArtistName = formatedArtistName.replaceAll("&", "%26") ;
		
		String searchQuery = formatedTrackName + "+AND+artist:" + formatedArtistName ;
		String searchURL = "https://musicbrainz.org/ws/2/recording?query=" + searchQuery + "&fmt=json";
		
		System.out.println("Search Query : " + searchQuery);
		System.out.println("Search Query url: " + searchURL);

		JSONObject musicBrainsReply;

		try {
			musicBrainsReply = call(searchURL);
		} catch (Exception e) {
			return null;
		}

		if (musicBrainsReply.has("recordings")) {
			JSONArray recordingsArray = musicBrainsReply.getJSONArray("recordings");

			boolean isFind = false;
			int recordingsArrayIndex = 0;

			// Search artist name in the recording list.
			while (!isFind && recordingsArrayIndex < recordingsArray.length()) {

				if (recordingsArray.getJSONObject(recordingsArrayIndex).has("artist-credit")) {

					JSONArray artistArray = recordingsArray.getJSONObject(recordingsArrayIndex)
							.getJSONArray("artist-credit");

					boolean haveArtist = false;
					int artistArrayIndex = 0;

					// System.out.println("Artist Length : " + artistArray.length());

					// Search artist name in artists list
					while (!haveArtist && artistArrayIndex < artistArray.length()) {

						JSONObject artistObject = artistArray.getJSONObject(artistArrayIndex);

						/*
						if (artistObject.has("name")) {
							System.out.println("Compare " + artistObject.getString("name") + " vs " + artistName);
						}
						*/

						if (artistObject.has("name") && artistObject.getString("name").equalsIgnoreCase(artistName)) {
							isFind = true;
							haveArtist = true;

							if (artistObject.has("artist") && artistObject.getJSONObject("artist").has("id")) {
								mbTrackInfo.setArtistID(artistObject.getJSONObject("artist").getString("id"));
								mbTrackInfo.setArtistName(artistObject.getString("name"));
								String artistURL = "https://musicbrainz.org/artist/" + mbTrackInfo.getArtistID();
								mbTrackInfo.setArtistURL(artistURL);
							}
						}

						artistArrayIndex++;
					}

					if (haveArtist) {
						if (recordingsArray.getJSONObject(recordingsArrayIndex).has("releases")) {
							JSONArray releasesArray = recordingsArray.getJSONObject(recordingsArrayIndex)
									.getJSONArray("releases");

							if (releasesArray.length() > 0) {
								
								Date minDate = null ;
								int minDateIndex = 0 ;
								
								
								for(int releaseIndex = 0 ; releaseIndex < releasesArray.length() ; releaseIndex++) {
									
									JSONObject tempReleaseObject = releasesArray.getJSONObject(releaseIndex);
									Date releaseDate ;
									
									if (tempReleaseObject.has("date")) {
										System.out.println(releaseIndex+" Release date : "+tempReleaseObject.getString("date"));
										
										try {
											
											String dateString = tempReleaseObject.getString("date") ;
											
											System.out.println("Date string size : "+dateString.length()) ;
											
											if (dateString.length() == 10) {
												releaseDate = new SimpleDateFormat("yyyy-MM-dd").parse(tempReleaseObject.getString("date"));
											}
											else if (dateString.length() == 4) {
												releaseDate = new SimpleDateFormat("yyyy").parse(tempReleaseObject.getString("date"));
											}
											else {
												releaseDate = null ;
											}
											
											
										} catch (JSONException | ParseException e) {
											// TODO Auto-generated catch block
											releaseDate = null ;
											System.out.println("Date Parse ERROR") ;
										}
										
										if (minDate == null && releaseDate != null) {
											minDate = releaseDate ;
											minDateIndex = releaseIndex ;
										}
										else if(releaseDate != null && releaseDate.before(minDate)) {
											minDate = releaseDate ;
											minDateIndex = releaseIndex ;
										}
									}
								}
								
								System.out.println("Min Date : "+minDate);
								
								JSONObject releaseObject = releasesArray.getJSONObject(minDateIndex); ;
								
								
								if (releaseObject.has("id")) {
									mbTrackInfo.setReleaseID(releaseObject.getString("id"));
								}

								if (releaseObject.has("title")) {
									mbTrackInfo.setReleaseName(releaseObject.getString("title"));
								}
							}
						}

						if (recordingsArray.getJSONObject(recordingsArrayIndex).has("id")) {
							mbTrackInfo.setRecordingID(
									recordingsArray.getJSONObject(recordingsArrayIndex).getString("id"));
							String artistURL = "https://musicbrainz.org/recording/" + mbTrackInfo.getRecordingID();
							mbTrackInfo.setRecordingURL(artistURL);

						}

						if (recordingsArray.getJSONObject(recordingsArrayIndex).has("title")) {
							mbTrackInfo.setRecordingName(
									recordingsArray.getJSONObject(recordingsArrayIndex).getString("title"));
						}

						System.out.println("Release ID : " + mbTrackInfo.getReleaseID());
						String coverURL = getCoverURLInCoverArtArchive(mbTrackInfo.getReleaseID());
						System.out.println("Cover url : " + coverURL);

						try {
							String imageRedirection = getFinalUrl(coverURL);
							System.out.println(imageRedirection);
							coverURL = imageRedirection;
						}
						catch (IOException e) {
						}

						mbTrackInfo.setCoverURL(coverURL);

						return mbTrackInfo;
					}
				}

				recordingsArrayIndex++;
			}
		}

		return null;
	}

	private static String getCoverURLInCoverArtArchive(String id) {

		String searchURL = "https://ia801900.us.archive.org/13/items/mbid-" + id + "/index.json";
//		String searchURL = "https://archive.org/download/mbid-" + id + "/index.json";

		System.out.println("Search Query url: " + searchURL);

		JSONObject coverArchiveReply;

		try {
			coverArchiveReply = call(searchURL);
		} catch (Exception e) {
			return null;
		}

		if (coverArchiveReply.has("images")) {

			JSONArray imagesArray = coverArchiveReply.getJSONArray("images");

			if (imagesArray.length() > 0) {
				JSONObject imageObject = imagesArray.getJSONObject(0);

				if (imageObject.has("thumbnails")) {
					if (imageObject.getJSONObject("thumbnails").has("small")) {
						return imageObject.getJSONObject("thumbnails").getString("small");
					} else {
						Iterator<String> keyList = imageObject.getJSONObject("thumbnails").keys();

						if (keyList.hasNext()) {
							return imageObject.getJSONObject("thumbnails").getString(keyList.next());
						}
					}
				}
				if (imageObject.has("image")) {
					String imageUrl = imageObject.getString("image");
					
					return imageUrl;
				}
			}
		}

		return null;
	}

	private static String getFinalUrl(String contentUrl) throws IOException {
		URL url = new URL(contentUrl);
		URLConnection connection = url.openConnection();

		((HttpURLConnection)connection).getHeaderFields();

		return ((HttpURLConnection)connection).getURL().toString();
	}
	
	private static String getContent(String contentUrl) throws IOException {
		URL url = new URL(contentUrl);
		URLConnection connection = url.openConnection();

/*		
//		((HttpURLConnection)connection).setInstanceFollowRedirects(false);

		Map<String, List<String>> fields = ((HttpURLConnection)connection).getHeaderFields();
		for (String key : fields.keySet()) {
			System.out.print(key);
			System.out.print(" => ");
			System.out.println(fields.get(key));
		}
		
		System.out.println(((HttpURLConnection)connection).getResponseCode());
		System.out.println(((HttpURLConnection)connection).getResponseMessage());
		System.out.println(((HttpURLConnection)connection).getURL());
*/

		InputStreamReader sr = new InputStreamReader(connection.getInputStream());
		StringWriter sw = new StringWriter();

		char[] buffer = new char[8192];
		int nbRead;

		while ((nbRead = sr.read(buffer)) != -1) {
			sw.write(buffer, 0, nbRead);
		}

		sr.close();
		sw.close();

		return sw.toString();
	}
	
	private static JSONObject call(String apiCallUrl) throws IOException {
		String json = getContent(apiCallUrl);

		JSONObject object = (JSONObject) new JSONTokener(json).nextValue();

		return object;
	}
}