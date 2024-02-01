package main.dsc0rd;

import com.google.gson.JsonObject;
import main.dsc0rd.bot.Bot;
import main.dsc0rd.bot.Config;
import me.ihaq.imguruploader.ImgurUploader;
import me.ihaq.imguruploader.exception.ImgurUploaderException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Calendar;

import javax.security.auth.login.LoginException;

public class Launcher {

    public static JDA jda;
    public static boolean local = true;
    public static FileWriter logfile;
    static String path = "";
    public static Config config;

    public static void main(String[] args) throws LoginException {
        try {
            config = new Config();
            jda = JDABuilder.createDefault(config.getString("token")).enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .setEventManager(new AnnotatedEventManager())
                    .addEventListeners(new Bot(config.getString("prefix")))
                    .build().awaitReady();
            path = local ? System.getProperty("user.dir") + "\\log.log" : "/home/admin/discord/log.log";
            logfile = new FileWriter(path, true);
        } catch (InterruptedException | IOException e) {
            logError(e.getLocalizedMessage());
        } finally {
            log("Ready! @ " + Calendar.getInstance().getTime().toString());
        }
    }

    public static void log(String log) {
        System.out.println(log);
        BufferedWriter writer = new BufferedWriter(logfile);
        try {
            writer.write("[" + LocalDateTime.now() + "]" + ":" + log);
            writer.newLine();
            writer.close();
            logfile = new FileWriter(path, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logError(String log) {
        log("[ERROR]" + log);
    }

    public static String uploadToImgur(String clientID, File image) {
        ImgurUploader helper = new ImgurUploader(clientID);
        final String[] result = { "" };
        try {
            JsonObject jsonObject = helper.uploadSync(image);
            result[0] = jsonObject.get("link").getAsString(); // getting the link of the uploaded image
        } catch (IOException | ImgurUploaderException e) {
            e.printStackTrace();
        }
        return result[0];
    }
}
