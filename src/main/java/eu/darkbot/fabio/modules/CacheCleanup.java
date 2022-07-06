package eu.darkbot.fabio.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.modules.utils.LegacyFlashPatcher;
import eu.darkbot.VerifierChecker.VerifierChecker;
import eu.darkbot.fabio.api.SchifoAPI;
import eu.darkbot.util.Timer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Feature(name = "CacheCleanup", description = "Auto cache cleanup for Darkbot", enabledByDefault = true)
public class CacheCleanup implements Task {

    final long time = System.currentTimeMillis();
    final long maxDiff = TimeUnit.DAYS.toMillis(3);
    private final Timer calendarTimer = Timer.get(59_000);
    private Main main;
    private boolean checkCalendar;
    private boolean isRefreshing;

    @Override
    public void install(Main main) {
        if (!Arrays.equals(VerifierChecker.class.getSigners(), getClass().getSigners())) return;
        if (!VerifierChecker.getAuthApi().requireDonor()) return;

        this.main = main;

        try {
            Files.newDirectoryStream(Paths.get("."), p -> (time - p.toFile().lastModified()) > maxDiff
                            && (p.toFile().getName().contains("hs_err_pid")))
                    .forEach(file -> {
                        try {
                            Files.delete(file);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void backgroundTick() {
        if (main != null && !main.config.BOT_SETTINGS.API_CONFIG.BROWSER_API.name().contains("NO_OP_API")
                && !main.config.BOT_SETTINGS.API_CONFIG.BROWSER_API.name().contains("DARK_MEM_API")
                && !(LocalTime.now().isAfter(LocalTime.parse("05:28:00")) && LocalTime.now().isBefore(LocalTime.parse("05:37:00")))) {
            if (main.hero.map.id == -1) {
                if (!checkCalendar) {
                    calendarTimer.tryActivate();
                    checkCalendar = true;
                    isRefreshing = false;
                }
            } else {
                calendarTimer.disarm();
                if (calendarTimer.isInactive()) {
                    checkCalendar = false;
                }
            }

            if (!isRefreshing && calendarTimer.isArmed() && calendarTimer.isInactive()) {
                isRefreshing = true;

                if (Main.VERSION.getBeta() >= 109) {
                    new LegacyFlashPatcher() {{
                        cleanupCache();
                    }};
                } else {
                    SchifoAPI.sendCommand("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 2");
                    SchifoAPI.sendCommand("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 8");
                    SchifoAPI.sendCommand("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 16");
                }
                Main.API.handleRefresh();
            }
        }
    }

    @Override
    public void tick() {

    }
}
