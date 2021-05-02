package eu.darkbot.fabio;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.extensions.plugins.Plugin;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Feature(name = "AutoRestart", description = "Auto restart bot when you want", enabledByDefault = false)
public class AutoRestart implements Task, Configurable<AutoRestart.AutoStartConfig> {

    private Main main;
    private AutoStartConfig autoStartConfig;

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
                p.load(reader);

                if (!p.getProperty("username").isEmpty() && !p.getProperty("password").isEmpty()) {
                    if (!main.featureRegistry.getFeatureDefinition(this).getIssues().getIssues().isEmpty())
                        main.featureRegistry.getFeatureDefinition(this).sendUpdate();
                    return true;
                } else {
                    main.featureRegistry.getFeatureDefinition(this).getIssues().addWarning("Schifo: dio can", "ghe sboro");
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

    public static class AutoStartConfig {

        @Option(value = "Time")
        public String Time = "05:35";
    }
}
