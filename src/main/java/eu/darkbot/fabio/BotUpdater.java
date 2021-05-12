package eu.darkbot.fabio;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.extensions.util.Version;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

@Feature(name = "BotUpdater", description = "This module auto update Darkbot", enabledByDefault = false)
public class BotUpdater implements Task {

    @Override
    public void install(Main main) {
        try {
            URL url = new URL("https://gist.githubusercontent.com/fabio1999ita/c8d2f16fcd6924a2e77f52b41969a7a6/raw/version.json");
            URLConnection request = url.openConnection();
            request.connect();

            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
            JsonObject rootobj = root.getAsJsonObject();
            String version = rootobj.get("version").getAsString();

            Version newVersion = new Version(version);

            if (Main.VERSION.compareTo(newVersion) < 0) {
                JButton cancel = new JButton("Cancel");
                JButton download = new JButton("Update");
                Popups.showMessageAsync("BotUpdater",
                        new JOptionPane("A new version of Darkbot is available\n" + Main.VERSION + " ➜ " + newVersion,
                                JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION,
                                null, new Object[]{download, cancel}, download));

                cancel.addActionListener((event) -> SwingUtilities.getWindowAncestor(cancel).setVisible(false));

                download.addActionListener((event) -> {
                    main.featureRegistry.getFeatureDefinition(this).getIssues()
                            .addWarning(Main.VERSION.toString(), newVersion.toString());
                    try (BufferedInputStream in = new BufferedInputStream(new URL("https://gist.github.com/fabio1999ita/c8d2f16fcd6924a2e77f52b41969a7a6/raw/DarkBot.jar").openStream()); FileOutputStream out = new FileOutputStream("DarkBot.jar")) {
                        final byte[] data = new byte[1024];
                        int count;
                        while ((count = in.read(data, 0, 1024)) != -1) {
                            out.write(data, 0, count);
                        }
                        Runtime.getRuntime().exec("javaw -jar DarkBot.jar");
                        System.exit(10);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void tick() {

    }
}
