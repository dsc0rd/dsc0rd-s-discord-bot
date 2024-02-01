package main.dsc0rd.bot;

import main.dsc0rd.Launcher;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Config {

    private JSONObject config;

    public Config() {
        File f = new File("./config.json");
        if (!f.isFile() && !f.exists()) {
            Launcher.local = true;
            f = new File("./config.json");
        }
        if (f.exists()) {
            InputStream is;
            try {
                is = new FileInputStream(f);
                String jsonTxt = IOUtils.toString(is, StandardCharsets.UTF_8);
                config = new JSONObject(jsonTxt);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getString(String key) {
        return config.getString(key);
    }

    public int getInt(String key) {
        return config.getInt(key);
    }

    public JSONArray getArray(String key) {
        return config.getJSONArray(key);
    }

    public boolean isCommandAllowed(String cmd){
        return getArray("allowedCommands").toList().contains(cmd);
    }
}
