package eu.darkbot.fabio.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.ExtraMenuProvider;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.utils.http.Method;
import eu.darkbot.VerifierChecker.VerifierChecker;
import eu.darkbot.fabio.api.SchifoAPI;
import eu.darkbot.fabio.api.manageAPI;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Feature(name = "HangarView", description = "Open graphic hangar in backpage", enabledByDefault = false)
public class HangarView implements Task, ExtraMenuProvider {

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
    }

    public Collection<JComponent> getExtraMenuItems(Main main) {
        if (manageAPI.addSeparator(main, "HangarView"))
            return Arrays.asList(createSeparator("Schifo"),
                    create("Show Hangar", e -> logic(main)));
        else
            return Collections.singletonList(
                    create("Show Hangar", e -> logic(main)));
    }

    @Override
    public void tick() {

    }

    private void logic(Main main) {
        if (!main.backpage.sidStatus().contains("KO")) {
            String flashEmbed = null;
            try {
                flashEmbed = main.backpage.getConnection("indexInternal.es?action=internalDock", Method.GET)
                        .consumeInputStream(inputStream -> new BufferedReader(new InputStreamReader(inputStream))
                                .lines()
                                .filter(l -> l.contains("flashembed(\"equipment_container\""))
                                .findFirst().orElse(null))
                        .split("}, \\{")[1]
                        .replaceAll(",", "&")
                        .replaceAll(": ", "=")
                        .replaceAll("\"", "")
                        .replaceAll("}\\);", "");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            SchifoAPI.showHangar(main.statsManager.instance, main.statsManager.sid, flashEmbed);
        } else {
            Popups.showMessageAsync("Error",
                    "Your SID must be OK to see the hangar.\n" +
                            "Try a manual reload or restart the bot.", JOptionPane.ERROR_MESSAGE);
        }
    }
}
