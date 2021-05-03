package ru.boomearo.worldlister.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.boomearo.worldlister.WorldLister;

public class MessageManager {
	public static MessageManager get() {
		if (instance == null)
			instance = new MessageManager();
		return instance;
	}
	
	
	private static MessageManager instance = null;
	private MessageManager() {}
	
	
	private Map<String, String> msgs = new HashMap<String, String>();
	private Map<String, List<String>> msgsList = new HashMap<String, List<String>>();
	public String getMessage(String msg) {
		String s = msgs.get(msg);
		if (s != null) {
			return s;
		}
		else {
			return "Ошибка: Сообщение не найдено. Сообщите Буму что он что-то напортачил.";
		}
	}
	
	public List<String> getMessageList(String msg) {
		List<String> s = msgsList.get(msg);
		if (s != null) {
			return s;
		}
		else {
			return new ArrayList<String>(Arrays.asList("Ошибка: Сообщения не найдено. Сообщите Буму что он что-то напортачил."));
		}
	}
	
	public void loadMessages() {
		this.msgs.clear();
		this.msgsList.clear();
		for (String messages : WorldLister.getContext().getConfig().getConfigurationSection("Messages").getKeys(false)) {
			if (!WorldLister.getContext().getConfig().isList("Messages."+messages)) {
				 msgs.put(messages, WorldLister.getContext().getConfig().getString("Messages."+messages));
			}
			else {
				msgsList.put(messages, WorldLister.getContext().getConfig().getStringList("Messages."+messages));
			}
		}
	}
	
}
