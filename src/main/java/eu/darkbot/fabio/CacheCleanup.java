package eu.darkbot.fabio;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Behaviour;
import com.github.manolo8.darkbot.extensions.features.Feature;

import java.io.IOException;

@Feature(name = "CacheCleanup", description = "Auto cache cleanup for Darkbot", enabledByDefault = false)
public class CacheCleanup implements Behaviour {

    @Override
    public void install(Main main) {
        try {
            Runtime.getRuntime().exec("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 2");
            Runtime.getRuntime().exec("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 8");
            Runtime.getRuntime().exec("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 16");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void tick() {

    }
}
