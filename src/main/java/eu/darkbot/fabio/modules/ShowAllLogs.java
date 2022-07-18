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

    List<String> logsList = new ArrayList<>();
    List<String> logsListFile = new ArrayList<>();
    List<String> logsListException = new ArrayList<>();
    private JTabbedPane tabbed;
    private JTextArea textLogsPane;
    private JTextArea textDeathsPane;

    @Override
    public void install(Main main) {
        if (!Arrays.equals(VerifierChecker.class.getSigners(), getClass().getSigners())) return;
        if (!VerifierChecker.getAuthApi().requireDonor()) return;

        tabbed = new JTabbedPane();
        JPanel panel = new JPanel(new MigLayout(""));
        JPanel logsPanel = new JPanel(new MigLayout(""));
        JPanel deathsPanel = new JPanel(new MigLayout(""));

        textLogsPane = new JTextArea();
        JScrollPane logsScroll = new JScrollPane(textLogsPane);
        logsScroll.getVerticalScrollBar().setUnitIncrement(16);
        logsPanel.add(logsScroll, "height :" + Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2.5 +
                ":" + Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 1.3 +
                ", width :" + Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 3.5 +
                ":" + Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 1.4);

        textDeathsPane = new JTextArea();
        JScrollPane deathsScroll = new JScrollPane(textDeathsPane);
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
        textLogsPane.setText("");

        boolean checkException = false;

        int lineCount = 0;

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
                        lineCount++;
                        if (lineCount > 18_000)
                            break;

                        if (line.contains("Exception")) {
                            while(line.contains("Exception")){
                                logsListException.add(line);
                                while ((line = inputStream.readLine()).contains("\tat")){
                                    logsListException.add(line);
                                }
                                Collections.reverse(logsListException);
                                logsListFile.addAll(logsListException);
                                logsListException.clear();
                            }
                        }

                        if (line.length() > 25 && !line.contains("Fatal Error")) {
                            line = line.replace(line.substring(24, 25), "");
                            line = line.replace(line.substring(20, 24), "");
                            line = line.replace(line.substring(0, 6), "");
                            //line = line.replace("[2022/", "");
                            //line = line.replace("]", "");
                            line = line.replace("-=-", "");
                            line = line.replace("-=[", "");
                            line = line.replace("]=-", "");
                        }

                        logsListFile.add(line);
                    }
                    Collections.reverse(logsListFile);
                    logsList.addAll(logsListFile);
                    logsListFile.clear();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        for (String s : logsList) {
            textLogsPane.append(s + "\n");
        }
        logsList.clear();
    }

    private void printDeaths() {
        textDeathsPane.setText("");

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
                        textDeathsPane.append(deathsList.get(i));
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
