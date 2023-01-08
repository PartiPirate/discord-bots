package fr.partipirate.discord.bots.congressus;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import fr.partipirate.discord.bots.congressus.commands.ICommand;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Configuration {
	private static final Configuration INSTANCE = new Configuration();

	public static Configuration getInstance() {
		return INSTANCE;
	}

	public String NAME = null;
	public String TOKEN = "";
	public String VOCAL_CHANNEL_NAME = null;
	public int VOLUME = 50;
	public String PLAYLIST = null;
	public String PREFIX = "!";
	public boolean SPEAKING_AWARE = false;

	@SuppressWarnings("rawtypes")
	public List<Class> COMMAND_PLUGINS = new ArrayList<>();
	@SuppressWarnings("rawtypes")
	public List<Class> LISTENER_PLUGINS = new ArrayList<>();
	public Map<String, Map<String, String>> OPTIONS = new HashMap<String, Map<String,String>>();

	@SuppressWarnings("rawtypes")
	public void readConfiguration(String configurationFile) {
		try {
			FileInputStream fis = new FileInputStream(configurationFile);
			JSONObject jsonObject = (JSONObject) new JSONTokener(fis).nextValue();
			
			if (jsonObject.has("name")) {
				NAME = jsonObject.getString("name");
			}
			TOKEN = jsonObject.getString("token");

			JSONArray plugins = jsonObject.getJSONArray("plugins");
			System.out.println("Plugins : " + plugins.length());
			for(int index = 0; index < plugins.length(); ++index) {
				String pluginClassName = plugins.getString(index);
				System.out.println("Search for " + pluginClassName);
				try {
					Class pluginClass = Class.forName(pluginClassName);
					
					System.out.println(pluginClass);
					
					if (hasPluginInterface(pluginClass, ICommand.class)) {
						COMMAND_PLUGINS.add(pluginClass);
						System.out.println("Add command " + pluginClassName);
					}
					else if (hasPluginInterface(pluginClass, EventListener.class) || hasPluginSuperClass(pluginClass, ListenerAdapter.class)) {
						LISTENER_PLUGINS.add(pluginClass);
						System.out.println("Add listener " + pluginClassName);
					}
					
				} catch (Exception e) {
					System.out.println(pluginClassName + " plugin not found");
				}
			}
			
			System.out.println("COMMANDS : " + COMMAND_PLUGINS.size());
			System.out.println("LISTENERS : " + LISTENER_PLUGINS.size());

			if (jsonObject.has("vocal")) {
				VOCAL_CHANNEL_NAME = jsonObject.getString("vocal");
				if (jsonObject.has("volume")) {
					VOLUME = jsonObject.getInt("volume");
				}
				PLAYLIST = jsonObject.getString("playlist");
			}

			if (jsonObject.has("prefix")) {
				PREFIX = jsonObject.getString("prefix");
			}

			if (jsonObject.has("speakingAware")) {
				SPEAKING_AWARE = jsonObject.getBoolean("speakingAware");
			}
			
			if (jsonObject.has("options")) {
				JSONObject options = jsonObject.getJSONObject("options");
				
				for (String optionKey : options.keySet()) {
					JSONObject option = options.getJSONObject(optionKey);
					Map<String, String> finalOption = new HashMap<>();
					
					OPTIONS.put(optionKey, finalOption);
					for (String key : option.keySet()) {
						finalOption.put(key, option.getString(key));
					}
				}
			}

			fis.close();
		}
		catch (IOException e) {
		}
	}

	@SuppressWarnings("rawtypes")
	private static boolean hasPluginInterface(Class pluginClass, Class clazz) {
		for (Class interfaceClass : pluginClass.getInterfaces()) {
			if (interfaceClass == clazz) return true;
		}
		
		return false;
	}

	@SuppressWarnings("rawtypes")
	private static boolean hasPluginSuperClass(Class pluginClass, Class clazz) {
		if (pluginClass.getSuperclass() == clazz) return true;
		
		return false;
	}


	@SuppressWarnings("rawtypes")
	public EventListener getHandler(Class pluginClass, CongressusBot bot) {

		try {
			for (Constructor constructor : pluginClass.getConstructors()) {
				if (constructor.getParameters().length == 0) {
					return (EventListener) constructor.newInstance(new Object[] {});
				}
				else if (constructor.getParameters().length == 1 && constructor.getParameters()[0].getParameterizedType().equals(CongressusBot.class)) {
					return (EventListener) constructor.newInstance(new Object[] {bot});
				}
			}
		}
		catch (Exception e) {
		}
		
		return null;
	}

	@SuppressWarnings("rawtypes")
	public ICommand getCommand(Class pluginClass, CongressusBot bot) {

		try {
			for (Constructor constructor : pluginClass.getConstructors()) {
				if (constructor.getParameters().length == 0) {
					return (ICommand) constructor.newInstance(new Object[] {});
				}
				else if (constructor.getParameters().length == 1 && constructor.getParameters()[0].getParameterizedType().equals(CongressusBot.class)) {
					return (ICommand) constructor.newInstance(new Object[] {bot});
				}
				else if (constructor.getParameters().length == 1 && constructor.getParameters()[0].getParameterizedType().equals(List.class)) {
					return (ICommand) constructor.newInstance(new Object[] {bot.commands});
				}
				else {
					return (ICommand) constructor.newInstance(new Object[] {bot.commands});
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
}