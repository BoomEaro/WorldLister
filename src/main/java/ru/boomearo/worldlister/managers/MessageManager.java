package ru.boomearo.worldlister.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import ru.boomearo.worldlister.WorldLister;

public class MessageManager {

    private static MessageManager instance = null;

    private ConcurrentMap<String, String> msgs = new ConcurrentHashMap<>();
    private ConcurrentMap<String, List<String>> msgsList = new ConcurrentHashMap<>();

    private static final String errorString = "Ошибка: Сообщение не найдено. Сообщите Буму что он что-то напортачил.";

    public static MessageManager get() {
        if (instance == null) {
            instance = new MessageManager();
        }
        return instance;
    }

    private MessageManager() {

    }

    public String getMessage(String msg) {
        String s = msgs.get(msg);
        if (s != null) {
            return s;
        }
        else {
            return errorString;
        }
    }

    public List<String> getMessageList(String msg) {
        List<String> s = msgsList.get(msg);
        if (s != null) {
            return s;
        }
        else {
            return Collections.singletonList(errorString);
        }
    }

    public void loadMessages() {
        ConcurrentMap<String, String> tmpMsgs = new ConcurrentHashMap<>();
        ConcurrentMap<String, List<String>> tmpMsgsList = new ConcurrentHashMap<>();

        Configuration config = WorldLister.getInstance().getConfig();
        ConfigurationSection section = config.getConfigurationSection("Messages");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                if (!section.isList(key)) {
                    tmpMsgs.put(key, section.getString(key).replace("&", "§"));
                }
                else {
                    List<String> originalList = section.getStringList(key);
                    List<String> tmp = new ArrayList<>();
                    for (String tmpMsg : originalList) {
                        tmp.add(tmpMsg.replace("&", "§"));
                    }
                    tmpMsgsList.put(key, Collections.unmodifiableList(tmp));
                }
            }
        }

        this.msgs = tmpMsgs;
        this.msgsList = tmpMsgsList;
    }

}
