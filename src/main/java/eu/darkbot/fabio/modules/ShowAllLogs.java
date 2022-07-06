package eu.darkbot.fabio.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.ExtraMenuProvider;
import com.github.manolo8.darkbot.core.itf.InstructionProvider;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.gui.utils.Popups;
import eu.darkbot.VerifierChecker.VerifierChecker;
import eu.darkbot.fabio.api.manageAPI;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Feature(name = "ShowAllLogs", description = "Displays the log files in order of modification", enabledByDefault = false)
public class ShowAllLogs implements Task, InstructionProvider, ExtraMenuProvider {

    private JTabbedPane tabbed;

    private Box logsBox;

    private Box deathsBox;

    @Override
    public void install(Main main) {
        if (!Arrays.equals(VerifierChecker.class.getSigners(), getClass().getSigners())) return;
        if (!VerifierChecker.getAuthApi().requireDonor()) return;

        tabbed = new JTabbedPane();
        JPanel panel = new JPanel(new MigLayout(""));
        JPanel logsPanel = new JPanel(new MigLayout(""));
        JPanel deathsPanel = new JPanel(new MigLayout(""));

        logsBox = Box.createVerticalBox();
        JScrollPane logsScroll = new JScrollPane(logsBox);
        logsScroll.getVerticalScrollBar().setUnitIncrement(16);
        logsPanel.add(logsScroll, "height :" + Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2.5 +
                ":" + Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 1.3 +
                ", width :" + Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 3.5 +
                ":" + Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 1.4);

        deathsBox = Box.createVerticalBox();
        JScrollPane deathsScroll = new JScrollPane(deathsBox);
        deathsScroll.getVerticalScrollBar().setUnitIncrement(16);
        deathsPanel.add(deathsScroll, "height :" + Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2.5 +
                ":" + Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 1.3 +
                ", width :" + Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 3.5 +
                ":" + Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 1.4);
        tabbed.add(logsPanel, "Bot logs");
        tabbed.add(deathsPanel, "Bot deaths");

        panel.add(tabbed, "span");
    }

    @Override
    public void tick() {

    }

    public Collection<JComponent> getExtraMenuItems(Main main) {
        if (manageAPI.addSeparator(main, "ShowAllLogs"))
            return Arrays.asList(createSeparator("Schifo"),
                    create("Show All Logs", e -> logic()));
        else
            return Collections.singletonList(
                    create("Show All Logs", e -> logic()));
    }

    private void logic() {
        printLogs();
        printDeaths();

        Popups.showMessageAsync("Darkbot Log", new Object[]{tabbed}, JPanel.UNDEFINED_CONDITION);
    }

    private void printLogs() {
        logsBox.removeAll();
        boolean checkException = false;
        List<String> logsList = new ArrayList<>();
        List<String> logsListException = new ArrayList<>();

        List<File> files = null;
        try {
            files = logsListFilesOldestFirst();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        assert files != null;
        for (File file : files) {
            if (file.isFile()) {
                BufferedReader inputStream = null;
                String line;
                try {
                    inputStream = new BufferedReader(new InputStreamReader(
                            Files.newInputStream(file.toPath()), StandardCharsets.UTF_8));

                    while ((line = inputStream.readLine()) != null) {
                        if (line.contains("Exception") || line.contains("\tat")) {
                            checkException = true;
                            if (line.startsWith("\tat"))
                                logsListException.add("         " + line);
                            else
                                logsListException.add(line);
                        } else {
                            if (checkException) {
                                checkException = false;
                                Collections.reverse(logsListException);
                                logsList.addAll(logsListException);
                                logsListException.clear();
                            }
                            logsList.add(line);
                        }
                    }
                    for (int i = logsList.size() - 1; i >= 0; i--) {
                        JTextField textField = new JTextField(logsList.get(i));
                        textField.setEditable(false);
                        textField.setBorder(null);
                        textField.setForeground(UIManager.getColor("Label.foreground"));
                        textField.setFont(UIManager.getFont("Label.font"));
                        if (textField.getText().contains("Exception") || textField.getText().contains("\tat"))
                            textField.setForeground(Color.red);
                        logsBox.add(textField);
                    }
                    logsList.clear();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private void printDeaths() {
        deathsBox.removeAll();
        List<String> deathsList = new ArrayList<>();

        List<File> files = null;
        try {
            files = deathsListFilesOldestFirst();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        assert files != null;
        for (File file : files) {
            if (file.isFile()) {
                BufferedReader inputStream = null;
                String line;
                try {
                    inputStream = new BufferedReader(new InputStreamReader(
                            Files.newInputStream(file.toPath()), StandardCharsets.UTF_8));

                    while ((line = inputStream.readLine()) != null) {
                        deathsList.add(line);
                    }

                    for (int i = deathsList.size() - 1; i >= 0; i--) {
                        JLabel label = new JLabel(deathsList.get(i) + "\n");
                        deathsBox.add(label);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private List<File> logsListFilesOldestFirst() throws IOException {
        try (final Stream<Path> fileStream = Files.list(Paths.get("logs"))) {
            return fileStream
                    .map(Path::toFile)
                    .sorted(Comparator.comparing(File::lastModified).reversed())
                    .filter(path -> !path.getName().contains("deaths"))
                    .collect(Collectors.toList());
        }
    }

    private List<File> deathsListFilesOldestFirst() throws IOException {
        try (final Stream<Path> fileStream = Files.list(Paths.get("logs"))) {
            return fileStream
                    .map(Path::toFile)
                    .sorted(Comparator.comparing(File::lastModified).reversed())
                    .filter(path -> path.getName().contains("deaths"))
                    .collect(Collectors.toList());
        }
    }
}
