package eu.darkbot.fabio;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.core.itf.InstructionProvider;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.extensions.plugins.Plugin;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Feature(name = "AutoRestart", description = "Auto restart bot when you want", enabledByDefault = false)
public class AutoRestart implements Task, InstructionProvider, Configurable<AutoRestart.AutoStartConfig> {

    private Main main;
    private AutoStartConfig autoStartConfig;
    private int newsize;
    private int oldsize;

    @Override
    public void install(Main main) {
        this.main = main;
        createFile();
    }

    @Override
    public void uninstall() {

    }

    @Override
    public void tickTask() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        if (oldsize != newsize)
        if (ifFileNotEmpty() && autoStartConfig.Time.equals(formatter.format(date))) {
            try {
                Runtime.getRuntime().exec("javaw -jar DarkBot.jar -start -login file.properties");
                main.featureRegistry.getFeatureDefinition(this).setStatus(false);
                System.exit(20);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setConfig(AutoStartConfig autoStartConfig) {
        this.autoStartConfig = autoStartConfig;
    }

    @Override
    public void tick() {

    }

    @Override
    public void tickStopped() {

    }

    private void createFile() {
        File f = new File("file.properties");
        if (!f.exists())
            try (OutputStream outputStream = new FileOutputStream("file.properties")) {
                LinkedHashMap<String, String> map = new LinkedHashMap<>();

                Properties properties = new Properties();

                map.put("username", "");
                map.put("password", "");
                map.put("master_password", "");
                properties.putAll(map);

                properties.store(outputStream, "Write data after '='");

            } catch (IOException e) {
                e.printStackTrace();
            }
        ifFileNotEmpty();
    }

    private boolean ifFileNotEmpty() {
        try {
            FileReader reader;
            try {
                Properties p = new Properties();
                reader = new FileReader("file.properties");
                oldsize = reader.read();
                p.load(reader);

                if (!p.getProperty("username").isEmpty() && !p.getProperty("password").isEmpty()) {
                    if (!main.featureRegistry.getFeatureDefinition(this).getIssues().getIssues().isEmpty()) {
                        main.featureRegistry.getFeatureDefinition(this).getIssues().getIssues().clear();
                        main.pluginUpdater.checkUpdates();
                    }
                    return true;
                } else {
                    main.featureRegistry.getFeatureDefinition(this).getIssues()
                            .addWarning("Warning: user data are empty, click on gear and open file then write credential", "");
                    main.featureRegistry.getFeatureDefinition(this).setStatus(false);
                    return false;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public JComponent beforeConfig() {
        JButton openfile = new JButton("Open 'file.properties' file");
        openfile.addActionListener(e -> {
            if (Desktop.isDesktopSupported()) {
                File file = new File("file.properties");
                Desktop desktop = Desktop.getDesktop();
                try {
                    if (!file.exists()) {
                        createFile();
                    }
                    desktop.open(file);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        return openfile;
    }

    public static class AutoStartConfig {
        @Option(value = "Time")
        public String Time = "05:35";
    }
}
