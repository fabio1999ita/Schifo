package eu.darkbot.fabio.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.extensions.util.Version;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.darkbot.VerifierChecker.VerifierChecker;
import eu.darkbot.fabio.api.SchifoAPI;
import eu.darkbot.fabio.api.manageAPI;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

@Feature(name = "BotUpdater", description = "This module auto update Darkbot", enabledByDefault = true)
public class BotUpdater implements Task, Configurable<BotUpdater.UpdaterConfig> {

    private UpdaterConfig updaterConfig;

    @Override
    public void install(Main main) {
        if (!Arrays.equals(VerifierChecker.class.getSigners(), getClass().getSigners())) return;
        if (!VerifierChecker.getAuthApi().requireDonor()) return;

        if (!manageAPI.deleted)
            manageAPI.deleteTmpFile();
        if (!manageAPI.loaded)
            new manageAPI();
        if (!manageAPI.checked)
            manageAPI.checkApiVersion();


        File file = new File("DarkBot.exe");
        if (file.exists())
            if (file.delete()) {
                System.out.println("Deleted the file: " + file.getName());
            } else {
                System.out.println("Failed to delete the " + file.getName() + " file.");
            }

        try {
            URL url = new URL("https://gist.github.com/fabio1999ita/d3c47965a1f2758a44dc6f6fdd2fccf9/raw/version.json");
            URLConnection request = url.openConnection();
            request.connect();


            JsonObject jsonObjectAlt = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent())).getAsJsonObject();
            String version = jsonObjectAlt.get("version").getAsString();


            Version lastVersion = new Version(version);

            if (Main.VERSION.compareTo(lastVersion) < 0) {
                JButton cancel = new JButton("Cancel");
                JButton download = new JButton("Update");
                Popups.showMessageAsync("BotUpdater",
                        new JOptionPane("A new version of Darkbot is available\n" + Main.VERSION + " ➜ " + lastVersion,
                                JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION,
                                null, new Object[]{download, cancel}, download));

                cancel.addActionListener((event) -> SwingUtilities.getWindowAncestor(cancel).setVisible(false));

                download.addActionListener((event) -> {
                    updateLogic(main, lastVersion);
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void tick() {

    }

    private void updateLogic(Main main, Version newVersion) {
        main.featureRegistry.getFeatureDefinition(this).getIssues()
                .addWarning(Main.VERSION.toString(), newVersion.toString());
        try (BufferedInputStream in = new BufferedInputStream(new URL("https://gist.github.com/fabio1999ita/d3c47965a1f2758a44dc6f6fdd2fccf9/raw/DarkBot.jar").openStream()); FileOutputStream out = new FileOutputStream("DarkBot.jar")) {
            final byte[] data = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                out.write(data, 0, count);
            }

            if (updaterConfig.useFileProperties)
                if (updaterConfig.autoHideApi)
                    SchifoAPI.sendCommand("javaw -jar DarkBot.jar -start -login file.properties -hide");
                else
                    SchifoAPI.sendCommand("javaw -jar DarkBot.jar -start -login file.properties");
            else if (updaterConfig.autoHideApi)
                SchifoAPI.sendCommand("javaw -jar DarkBot.jar -hide");
            else
                SchifoAPI.sendCommand("javaw -jar DarkBot.jar");
            System.exit(10);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setConfig(BotUpdater.UpdaterConfig updaterConfig) {
        this.updaterConfig = updaterConfig;
    }

    public static class UpdaterConfig {
        @Option(value = "Use file.properties", description = "Use file.properties for auto login")
        public boolean useFileProperties = false;

        @Option(value = "Auto hide API", description = "After auto restart the API will auto hide")
        public boolean autoHideApi = false;
    }
}
