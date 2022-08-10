package eu.darkbot.fabio.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.features.Feature;
import eu.darkbot.VerifierChecker.VerifierChecker;
import eu.darkbot.fabio.api.SchifoAPI;
import eu.darkbot.fabio.api.manageAPI;
import eu.darkbot.util.Timer;

import java.time.LocalTime;
import java.util.Arrays;

@Feature(name = "AntiLoadingStuck [BETA]", description = "Avoid stuck in loading screen", enabledByDefault = true)
public class AntiLoadingStuck implements Task {

    Timer timer = Timer.get(1_000);
    boolean checkCalendar;
    boolean isRefreshing;
    private Main main;
    private boolean checkHook;
    private boolean checkUnHook;

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

        this.main = main;

        SchifoAPI.hook();
        checkHook = true;
        checkUnHook = false;
    }

    @Override
    public void backgroundTick() {
        if (main != null && !main.config.BOT_SETTINGS.API_CONFIG.BROWSER_API.name().contains("NO_OP_API")
                && !main.config.BOT_SETTINGS.API_CONFIG.BROWSER_API.name().contains("DARK_MEM_API")
                && !(LocalTime.now().isAfter(LocalTime.parse("05:28:00")) && LocalTime.now().isBefore(LocalTime.parse("05:39:00")))) {
            if (main.hero.map.id == -1) {

                if (!checkCalendar) {
                    timer.tryActivate();
                    checkCalendar = true;
                }
                if (!checkHook) {
                    SchifoAPI.hook();
                    checkHook = true;
                    checkUnHook = false;
                    isRefreshing = true;
                }

                if (timer.isArmed() && timer.isInactive()) {
                    if (SchifoAPI.checkIfIsStuck() && isRefreshing) {
                        isRefreshing = false;
                        SchifoAPI.sendCommand("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 2");
                        SchifoAPI.sendCommand("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 8");
                        SchifoAPI.sendCommand("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 16");

                        Main.API.handleRefresh();
                    }
                    timer.disarm();
                    checkCalendar = false;
                }

                int a = 0;
            } else {
                if (!checkUnHook) {
                    isRefreshing = false;
                    SchifoAPI.unHook();
                    checkUnHook = true;
                    checkHook = false;
                }
            }
        }
    }

    @Override
    public void tick() {

    }
}