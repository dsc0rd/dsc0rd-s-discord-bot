package main.dsc0rd.bot;

import main.dsc0rd.Launcher;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import java.io.IOException;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;

public class Bot {

    private String prefix = "";

    public CommandManager executor;

    public Bot(String prefix) {
        this.prefix = prefix;
        executor = new CommandManager();
    }

    @SubscribeEvent
    public void onEvent(MessageReceivedEvent event) {

        String[] preArgs = event.getMessage().getContentRaw().split(" ");
        ArrayList<String> args = new ArrayList<>(Arrays.asList(preArgs));

        if (args.get(0).startsWith(this.prefix)) {
            String cmd = args.get(0).replace(this.prefix, "");
            if (Launcher.config.isCommandAllowed(cmd)) {
                args.remove(0);
                executor.execute(cmd, event, args);
            }
        }

    }

    @SubscribeEvent
    public void shutdown(ShutdownEvent event) {
        try {
            Launcher.logfile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        Launcher.jda.removeEventListener(this);
        Bot bot = new Bot(this.prefix);
        Launcher.jda.addEventListener(bot);
    }

    public void shutdown() {
        Launcher.jda.shutdown();
        System.exit(1);
    }
}
