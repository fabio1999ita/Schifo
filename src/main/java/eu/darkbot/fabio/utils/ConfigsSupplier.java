package eu.darkbot.fabio.utils;

import com.github.manolo8.darkbot.config.types.suppliers.OptionList;

import java.io.File;
import java.util.*;

public class ConfigsSupplier extends OptionList<String> {

    private final List<String> configs = new ArrayList<>();
    {
        configs.add(new File("config.json").getName().replace(".json", ""));
        File[] files = new File("configs").listFiles((dir, name) -> name.endsWith(".json"));

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    configs.add(file.getName().replace(".json", ""));
                }
            }
        }
    }

    @Override
    public String getValue(String s) {
        return s;
    }

    @Override
    public String getText(String s) {
        return s;
    }

    @Override
    public List<String> getOptions() {
        return configs;
    }
}
