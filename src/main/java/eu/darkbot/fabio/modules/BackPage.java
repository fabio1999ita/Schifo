package eu.darkbot.fabio.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.ExtraMenuProvider;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.gui.utils.Popups;
import eu.darkbot.VerifierChecker.VerifierChecker;
import eu.darkbot.fabio.api.SchifoAPI;
import eu.darkbot.fabio.api.manageAPI;

import javax.swing.*;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Feature(name = "BackPage", description = "BackPage for homepage of darkorbit with flash integrated", enabledByDefault = false)
public class BackPage implements Task, ExtraMenuProvider {

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

    @Override
    public void tick() {

    }

    @Override
    public Collection<JComponent> getExtraMenuItems(Main main) {
        if (manageAPI.addSeparator(main, "BackPage"))
            return Arrays.asList(createSeparator("Schifo"),
                    create("Open BackPage", e -> logic(main, main.statsManager.instance, main.statsManager.sid)));
        else
            return Collections.singletonList(
                    create("Open BackPage", e -> logic(main, main.statsManager.instance, main.statsManager.sid)));
    }

    private void logic(Main main, String instance, String sid) {
        File backPage = new File("lib\\BackPage.jar");
        if (!backPage.exists())
            try (BufferedInputStream in = new BufferedInputStream(new URL("https://host.darkbot.eu/uploads/Fabio/BackPage.jar").openStream()); FileOutputStream out = new FileOutputStream("lib\\BackPage.jar")) {
                final byte[] data = new byte[1024];
                int count;
                while ((count = in.read(data, 0, 1024)) != -1) {
                    out.write(data, 0, count);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        File flash = new File("lib\\pepflashplayer.dll");
        if (!flash.exists())
            try (BufferedInputStream in = new BufferedInputStream(new URL("https://host.darkbot.eu/uploads/Fabio/pepflashplayer.dll").openStream()); FileOutputStream out = new FileOutputStream("lib\\pepflashplayer.dll")) {
                final byte[] data = new byte[1024];
                int count;
                while ((count = in.read(data, 0, 1024)) != -1) {
                    out.write(data, 0, count);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        if (!main.backpage.sidStatus().contains("KO")) {
            SchifoAPI.BackPage(instance, sid);
        } else {
            Popups.showMessageAsync("Error",
                    "Your SID must be OK to see the hangar.\n" +
                            "Try a manual reload or restart the bot.", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getMD5() {
        MessageDigest md = null;
        byte[] digest = new byte[0];
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(Files.readAllBytes(Paths.get("lib\\BackPage.jar")));
            digest = md.digest();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return DatatypeConverter.printHexBinary(digest).toUpperCase();
    }
}