package eu.darkbot.fabio.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.features.Feature;
import eu.darkbot.VerifierChecker.VerifierChecker;
import eu.darkbot.util.Timer;

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
    }

    @Override
    public void backgroundTick() {
        /*if (main != null && !main.config.BOT_SETTINGS.API_CONFIG.BROWSER_API.name().contains("NO_OP_API")
                && !main.config.BOT_SETTINGS.API_CONFIG.BROWSER_API.name().contains("DARK_MEM_API")
                && !(LocalTime.now().isAfter(LocalTime.parse("05:28:00")) && LocalTime.now().isBefore(LocalTime.parse("05:39:00")))) {
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
        }*/
    }

    @Override
    public void tick() {

    }
}
