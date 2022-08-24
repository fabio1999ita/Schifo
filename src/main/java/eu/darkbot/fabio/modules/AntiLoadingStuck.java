package eu.darkbot.fabio.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.features.Feature;
import eu.darkbot.VerifierChecker.VerifierChecker;
import eu.darkbot.fabio.api.SchifoAPI;
import eu.darkbot.fabio.api.manageAPI;
import eu.darkbot.util.Timer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Feature(name = "AntiLoadingStuck", description = "Avoid stuck in loading screen & clear cache if need", enabledByDefault = true)
public class AntiLoadingStuck implements Task {

    final long time = System.currentTimeMillis();
    final long maxDiff = TimeUnit.DAYS.toMillis(3);
    Timer timer = Timer.get(1_000);
    boolean checkCalendar;
    boolean isRefreshing;
    Timer postTimer = Timer.get(TimeUnit.MINUTES.toMillis(7));
    private Main main;
    private boolean isHook;
    private boolean isUnHook;

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
        Objects.requireNonNull(main.featureRegistry.getFeatureDefinition(this)).addStatusListener(feature -> {
            if (feature.isEnabled()) {
                SchifoAPI.hook();
                isHook = true;
                isUnHook = false;
                isRefreshing = true;
            } else {
                checkCalendar = false;
                isRefreshing = false;
                SchifoAPI.unHook();
                isUnHook = true;
                isHook = false;
                postTimer.disarm();
            }
        });
        try {
            Files.newDirectoryStream(Paths.get("."), p -> (time - p.toFile().lastModified()) > maxDiff
                            && (p.toFile().getName().contains("hs_err_pid")))
                    .forEach(file -> {
                        try {
                            Files.delete(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void uninstall() {
        checkCalendar = false;
        isRefreshing = false;
        SchifoAPI.unHook();
        isUnHook = true;
        isHook = false;
        postTimer.disarm();
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
                if (!isHook) {
                    SchifoAPI.hook();
                    isHook = true;
                    isUnHook = false;
                    isRefreshing = true;
                }

                if ((timer.isArmed() && timer.isInactive()) || (postTimer.isArmed() && postTimer.isInactive())) {
                    if (SchifoAPI.checkIfIsStuck() && isRefreshing) {
                        SchifoAPI.sendCommand("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 2");
                        SchifoAPI.sendCommand("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 8");
                        SchifoAPI.sendCommand("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 16");

                        Main.API.handleRefresh();

                        timer.disarm();
                        postTimer.tryActivate();
                    }
                }
            } else {
                if (!isUnHook) {
                    checkCalendar = false;
                    isRefreshing = false;
                    SchifoAPI.unHook();
                    isUnHook = true;
                    isHook = false;
                    postTimer.disarm();
                }
            }
        }
    }

    @Override
    public void tick() {

    }
}