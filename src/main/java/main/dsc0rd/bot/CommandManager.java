package main.dsc0rd.bot;

import main.dsc0rd.Launcher;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.script.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class CommandManager {

    ArrayList<String> commandNameList;
    JSONObject cmds;
    ScriptEngineManager factory;
    ScriptEngine engine;

    public CommandManager() {
        reload();
    }

    void reload() {
        commandNameList = new ArrayList<>();
        initJSONFile();
        populateCommandNameList();
        factory = new ScriptEngineManager();
        engine = factory.getEngineByName("nashorn");
    }

    void saveCommandJSON() {
        File f;
        if (Launcher.local)
            f = new File("commands.json");
        else
            f = new File(Launcher.config.getString("commandFile"));

        try {
            cmds.write(new FileWriter(f)).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void initJSONFile() {
        File f;
        if (Launcher.local)
            f = new File("commands.json");
        else
            f = new File(Launcher.config.getString("commandFile"));
        if (f.exists()) {
            InputStream is;
            try {
                is = new FileInputStream(f);
                String jsonTxt = IOUtils.toString(is, StandardCharsets.UTF_8);
                cmds = new JSONObject(jsonTxt);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void populateCommandNameList() {
        commandNameList.addAll(cmds.keySet());
    }

    public void execute(String cmd, MessageReceivedEvent event, ArrayList<String> args) {
        Message message = event.getMessage();
        Launcher.log(cmd.toString().concat(message.toString()));
        JSONObject exec = new JSONObject();
        if (cmds.has(cmd))
            exec = cmds.getJSONObject(cmd);
        EmbedBuilder builder = new EmbedBuilder().setAuthor(String.format("%#s", message.getAuthor()),
                message.getAuthor().getAvatarUrl(), message.getAuthor().getAvatarUrl());

        if (cmd.equalsIgnoreCase("cmd")) {
            if (args.size() > 0) {
                switch (args.get(0)) {
                    case "add":
                        if (args.get(1).equalsIgnoreCase("cmd")) {
                            Launcher.log(String.format("%#s", message.getAuthor()) + " tried to break me @ "
                                    + Calendar.getInstance().getTime().toString());
                            builder.addField(":x: FORBIDDEN", "YOU CANNOT DO THAT", false);
                            break;
                        }
                        if (cmds.has(args.get(1))) {
                            builder.addField(":x: FORBIDDEN", "Already have this command, remove it first", false);
                            break;
                        }
                        addCommand(args.get(1));
                        builder.addField(":white_check_mark: Sucksass",
                                "Successfully added " + args.get(1) + " to command list", true);
                        break;
                    case "remove":
                        if (cmds.has(args.get(1))) {
                            removeCommand(args.get(1));
                            builder.addField(":white_check_mark: Sucksass",
                                    "Successfully removed " + args.get(1) + " from command list", true);
                            break;
                        }
                        builder.addField(":x: Failure", "No such command: " + args.get(1), true);
                        break;
                    case "change":
                        switch (args.get(1)) {
                            case "title":
                                args.get(1);
                                break;
                            case "prefab":
                                args.get(1);
                                break;
                            case "responses":
                                break;
                        }
                        break;
                    case "list":
                        for (String s : cmds.keySet()) {

                            JSONObject _cmd = cmds.getJSONObject(s);
                            builder.addField(":desktop: " + s, "", false);
                            if (_cmd.has("runnable")) {
                                builder.addField("runnable?", "true", true);
                                if (_cmd.has("function"))
                                    builder.addField("function", _cmd.getString("function"), true);
                            }
                            if (_cmd.has("title"))
                                builder.addField("title", "" + _cmd.getJSONArray("title").toString(), true);
                            if (_cmd.has("prefab"))
                                builder.addField("prefab", "" + _cmd.getJSONArray("prefab").toString(), true);
                            if (_cmd.has("responses"))
                                builder.addField("response", "" + _cmd.getJSONArray("responses").toString(), true);
                            if (_cmd.has("splitter"))
                                builder.addField("splitter", "" + _cmd.getString("splitter"), true);
                        }
                        break;
                }
            } else {

                for (String s : cmds.keySet()) {

                    JSONObject _cmd = cmds.getJSONObject(s);
                    builder.addField(":desktop: " + s, "", false);
                    if (_cmd.has("runnable")) {
                        builder.addField("runnable?", "true", true);
                        if (_cmd.has("function"))
                            builder.addField("function", _cmd.getString("function"), true);
                    }
                    if (_cmd.has("title"))
                        builder.addField("title", "" + _cmd.getJSONArray("title").toString(), true);
                    if (_cmd.has("prefab"))
                        builder.addField("prefab", "" + _cmd.getJSONArray("prefab").toString(), true);
                    if (_cmd.has("responses"))
                        builder.addField("response", "" + _cmd.getJSONArray("responses").toString(), true);
                    if (_cmd.has("splitter"))
                        builder.addField("splitter", "" + _cmd.getString("splitter"), true);
                }

            }
        } else if (exec.has("runnable")) {
            String func = exec.getString("function");
            for (int i = 0; i < args.size(); i++)
                func = func.replace("%a" + (i + 1), args.get(i));
            try {
                engine.eval(func);
            } catch (ScriptException e) {
                e.printStackTrace();
            }

            String result = (String) engine.get("result");

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!result.equalsIgnoreCase("none"))
                switch (exec.getString("type")) {
                    default:
                    case "image":
                        builder.setImage(result);
                        break;
                    case "video":
                        break;

                }
            else {
                String error = exec.getString("error");
                for (int i = 0; i < args.size(); i++)
                    error = error.replace("%a" + (i + 1), args.get(i));
                builder.addField(":x: Error from " + cmd, error, false);
            }
        } else {
            int responseArrayLength = exec.getJSONArray("responses").length(),
                    prefabArrayLength = exec.getJSONArray("prefab").length();
            String finalResponseTitle = "", finalResponse, response;
            if (prefabArrayLength > 1) {
                finalResponseTitle = exec.getJSONArray("prefab").getString(0);
                finalResponse = exec.getJSONArray("prefab").getString(1);
            } else {
                finalResponse = exec.getJSONArray("prefab").getString(0);
            }
            if (responseArrayLength > 1)
                response = exec.getJSONArray("responses").getString(0);
            else
                response = exec.getJSONArray("responses")
                        .getString(ThreadLocalRandom.current().nextInt(responseArrayLength));

            if (exec.has("title")) {
                String title = "";

                if (exec.getJSONArray("title").length() > 1) {
                    title = exec.getJSONArray("title").getString(1)
                            .replace("%u", String.format("%#s", message.getAuthor()))
                            .replace("%m", ArrayListToString(args));
                    builder.addField(exec.getJSONArray("title").getString(0), title, false);
                } else {
                    title = exec.getJSONArray("title").getString(0);
                    title = title.replace("%u", message.getAuthor().getName()).replace("%m", ArrayListToString(args));
                    finalResponseTitle = title;
                }
            }

            finalResponseTitle = prepareString(finalResponseTitle, response, message, args);
            finalResponse = prepareString(finalResponse, response, message, args);

            if (exec.has("splitter")) {
                String[] rsmp = ArrayListToString(args).split(exec.getString("splitter"));
                finalResponse = finalResponse.replace("%rsmp", rsmp[ThreadLocalRandom.current().nextInt(rsmp.length)]);
            }
            builder.addField(finalResponseTitle, finalResponse, false);

            Launcher.log(String.format("%#s", message.getAuthor()) + " called " + cmd + " with message: "
                    + ArrayListToString(args) + " at " + Calendar.getInstance().getTime().toString());
        }

        event.getChannel().asTextChannel().sendMessageEmbeds(builder.build()).queue();

    }

    public String ArrayListToString(ArrayList<String> ob) {
        StringBuilder result = new StringBuilder();
        for (String s : ob) {
            result.append(s);
            result.append(" ");
        }
        return result.toString();
    }

    public String prepareString(String original, String response, Message message, ArrayList<String> args) {
        return original.replace("%u", String.format("%#s", message.getAuthor())).replace("%s", response).replace("%m",
                ArrayListToString(args));
    }

    public void addCommand(String name) {
        JSONObject result = new JSONObject();
        cmds.putOnce(name, result);
        saveCommandJSON();
        reload();
    }

    void changeTitle(String name, String[] title) {

    }

    public void addCommand(String name, String[] title, String[] prefab, String[] responses, Optional<String> splitter,
            Optional<String> function, boolean runnable) {
        JSONArray _title = new JSONArray(title), _prefab = new JSONArray(prefab), _responses = new JSONArray(responses);
        JSONObject result = new JSONObject();

        if (runnable && function.isPresent()) {
            result.put("runnable", "");
            result.put("function", function.get());
        }
        result.put("title", _title);
        result.put("prefab", _prefab);
        result.put("responses", _responses);

        splitter.ifPresent(s -> result.put("splitter", s));

        cmds.putOnce(name, result);
        saveCommandJSON();
        reload();

    }

    public void removeCommand(String name) {
        cmds.remove(name);
        saveCommandJSON();
        reload();
    }

}
