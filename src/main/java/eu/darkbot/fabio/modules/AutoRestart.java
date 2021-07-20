package eu.darkbot.fabio.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.types.Editor;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.config.types.Options;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.core.itf.InstructionProvider;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.gui.tree.components.JListField;
import eu.darkbot.VerifierChecker.VerifierChecker;
import eu.darkbot.fabio.utils.ConfigsSupplier;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Properties;

@Feature(name = "AutoRestart", description = "Auto restart of Darkbot at time that you want", enabledByDefault = false)
public class AutoRestart implements Task, InstructionProvider, Configurable<AutoRestart.AutoStartConfig> {

    private Main main;
    private AutoStartConfig autoStartConfig;

    @Override
    public void install(Main main) {
        if (!Arrays.equals(VerifierChecker.class.getSigners(), getClass().getSigners())) return;
        if (!VerifierChecker.getAuthApi().requireDonor()) return;
        this.main = main;
        createFile();
    }

    @Override
    public void uninstall() {

    }

    @Override
    public void tickTask() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        if (ifFileNotEmpty() && autoStartConfig.Time.equals(formatter.format(date))) {
            try {
                if (autoStartConfig.autoHideApi)
                    Runtime.getRuntime().exec("javaw -jar DarkBot.jar -start -login file.properties -config " + autoStartConfig.configs + " -hide");
                else
                    Runtime.getRuntime().exec("javaw -jar DarkBot.jar -start -login file.properties -config " + autoStartConfig.configs);
                if (autoStartConfig.disableInRestart)
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
                    //main.featureRegistry.getFeatureDefinition(this).setStatus(false);
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
        @Option(value = "Restart time (HH:mm:ss)")
        public String Time = "05:35:00";

        @Option(value = "Select config")
        @Editor(JListField.class)
        @Options(ConfigsSupplier.class)
        public String configs = "config";

        @Option(value = "Disable after auto restart", description = "After auto restart the module will auto disable")
        public boolean disableInRestart = false;

        @Option(value = "Auto hide API", description = "After auto restart the API will auto hide")
        public boolean autoHideApi = false;
    }
}