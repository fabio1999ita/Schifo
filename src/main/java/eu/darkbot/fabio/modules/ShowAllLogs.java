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
import javax.swing.text.DefaultCaret;
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
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Feature(name = "ShowAllLogs", description = "Displays the log files in order of modification", enabledByDefault = false)
public class ShowAllLogs implements Task, InstructionProvider, ExtraMenuProvider {

    List<String> logsList = new ArrayList<>();
    List<String> logsListFile = new ArrayList<>();
    List<String> logsListException = new ArrayList<>();
    List<String> deathsList = new ArrayList<>();

    private JTabbedPane tabbed;

    private JTextArea textLogsPane;
    private JTextArea textDeathsPane;
    private JTextArea textExceptionsPane;

    private JScrollPane logsScroll;
    private JScrollPane deathsScroll;
    private JScrollPane exceptionsScroll;

    @Override
    public void install(Main main) {
        if (!Arrays.equals(VerifierChecker.class.getSigners(), getClass().getSigners())) return;
        if (!VerifierChecker.getAuthApi().requireDonor()) return;

        tabbed = new JTabbedPane();
        JPanel panel = new JPanel(new MigLayout(""));
        JPanel logsPanel = new JPanel(new MigLayout(""));
        JPanel deathsPanel = new JPanel(new MigLayout(""));
        JPanel exceptionsPanel = new JPanel(new MigLayout(""));

        textLogsPane = new JTextArea();
        logsScroll = new JScrollPane(textLogsPane);
        logsScroll.getVerticalScrollBar().setUnitIncrement(16);
        logsPanel.add(logsScroll, "height :" + Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 +
                ":" + Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 1.3 +
                ", width :" + Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 3 +
                ":" + Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 1.4);

        textDeathsPane = new JTextArea();
        deathsScroll = new JScrollPane(textDeathsPane);
        deathsScroll.getVerticalScrollBar().setUnitIncrement(16);
        deathsPanel.add(deathsScroll, "height :" + Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 +
                ":" + Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 1.3 +
                ", width :" + Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 3 +
                ":" + Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 1.4);

        textExceptionsPane = new JTextArea();
        exceptionsScroll = new JScrollPane(textExceptionsPane);
        exceptionsScroll.getVerticalScrollBar().setUnitIncrement(16);
        exceptionsPanel.add(exceptionsScroll, "height :" + Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 +
                ":" + Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 1.3 +
                ", width :" + Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 3 +
                ":" + Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 1.4);

        tabbed.add(logsPanel, "Bot logs");
        tabbed.add(deathsPanel, "Bot deaths");
        tabbed.add(exceptionsPanel, "Bot exceptions");
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
        DefaultCaret caretLogs = (DefaultCaret) textLogsPane.getCaret();
        caretLogs.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        DefaultCaret caretDeaths = (DefaultCaret) textDeathsPane.getCaret();
        caretDeaths.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        DefaultCaret caretExceptions = (DefaultCaret) textExceptionsPane.getCaret();
        caretExceptions.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        new ShowAllLogsSwing(textLogsPane, logsList, logsListException, logsListFile, textExceptionsPane).execute();
        new ShowAllDeathsSwing(textDeathsPane, deathsList).execute();
        printDeaths();

        Popups.showMessageAsync("Darkbot Log", new Object[]{tabbed}, JPanel.UNDEFINED_CONDITION);
    }

    private void printDeaths() {

    }
}

class ShowAllLogsSwing extends SwingWorker<List<String>, String> {

    JTextArea textLogsPane;
    List<String> logsList;
    List<String> logsListException;
    List<String> logsListFile;
    JTextArea textExceptionsPane;

    public ShowAllLogsSwing(JTextArea textLogsPane, List<String> logsList, List<String> logsListException, List<String> logsListFile, JTextArea textExceptionsPane) {
        this.textLogsPane = textLogsPane;
        this.logsList = logsList;
        this.logsListException = logsListException;
        this.logsListFile = logsListFile;
        this.textExceptionsPane = textExceptionsPane;
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

    @Override
    protected void process(List<String> chunks) {
        for (String text : chunks) {
            textLogsPane.append(text);
        }
    }

    @Override
    protected void done() {
        try {
            get();
            SwingUtilities.invokeLater(() -> {
                //logsScroll.getViewport().setViewPosition(new Point(0, 0));
                logsList.clear();
                System.gc();
            });
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected List<String> doInBackground() {
        textLogsPane.setText("");

        int lineCount = 0;
        List<String> logsListFileTemp = new ArrayList<>();
        List<String> logsListExceptionTemp = new ArrayList<>();
        List<File> files = null;
        try {
            files = logsListFilesOldestFirst();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (files == null) return null;

        String utf8CharITES = "éúáíìèòàù£€¡";
        String utf8CharTR = "çığşöü";
        String utf8CharPL = "ąęłżźńó";
        String utf8CharDE = "äöüß";
        String utf8CharHU = "áéóöőúüű";
        String utf8CharCZ = "áčďěňóřšťúůýž";
        String utf8CharRO = "ăâîșț";

        String utf8Char = utf8CharITES + utf8CharTR + utf8CharPL + utf8CharDE + utf8CharHU + utf8CharCZ + utf8CharRO;

        //remove duplicate from utf8Char
        utf8Char = Arrays.stream(utf8Char.split(""))
                .distinct()
                .collect(Collectors.joining());

        String newUtf8Char = "";

        String resultFile = "";

        for (File file : files) {
            if (file.isFile() && file.length() != 0) {
                BufferedReader inputStream = null;
                try {
                    inputStream = new BufferedReader(new InputStreamReader(
                            Files.newInputStream(file.toPath()), StandardCharsets.UTF_8));
                    resultFile += inputStream.lines().parallel().collect(Collectors.joining("\n"));

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
        for (char c : utf8Char.toCharArray()) {
            if (resultFile.toLowerCase().contains(String.valueOf(c)))
                newUtf8Char += c;
        }

        for (File file : files) {
            if (file.isFile() && file.length() != 0) {
                BufferedReader inputStream = null;
                String line;
                try {
                    inputStream = new BufferedReader(new InputStreamReader(
                            Files.newInputStream(file.toPath()), StandardCharsets.UTF_8));

                    while ((line = inputStream.readLine()) != null) {
                        if (line.contains("Exception")) {
                            while (line != null && line.contains("Exception")) {
                                logsListException.add(new String(line.getBytes(), StandardCharsets.US_ASCII));
                                while ((line = inputStream.readLine()) != null) {
                                    if (line.contains("\tat") || line.contains("..."))
                                        logsListException.add(new String(line.getBytes(), StandardCharsets.US_ASCII));
                                    else break;
                                }
                                Collections.reverse(logsListException);
                                logsListFileTemp.addAll(logsListException);
                                Collections.reverse(logsListException);
                                logsListExceptionTemp.addAll(logsListException);

                                logsListException.clear();
                            }
                        }

                        if (line != null) {
                            if (line.length() > 25 && !line.contains("Fatal Error")) {
                                line = line.replace(line.substring(24, 25), "");
                                line = line.replace(line.substring(20, 24), "");
                                line = line.replace(line.substring(0, 6), "");
                                line = line.replace("-=-", "");
                                line = line.replace("-=[", "");
                                line = line.replace("]=-", "");
                            }

                            boolean check = false;
                            String tempLine;

                            String start = null;
                            String end = null;

                            String t0, t1;

                            tempLine = line;

                            for (int i = 15; i < tempLine.length(); i++) { //here i start from 15 for avoid first data char
                                char a = tempLine.charAt(i);
                                for (char c : newUtf8Char.toCharArray()) {
                                    if (tempLine.toLowerCase().contains(String.valueOf(c))) {
                                        if (Character.toLowerCase(a) == c) {
                                            check = true;
                                            String[] test = new String[2];
                                            if (start == null)
                                                test = tempLine.split(String.valueOf(a), 2);
                                            else {
                                                if (end != null)
                                                    test = end.split(String.valueOf(a), 2);
                                            }

                                            try {
                                                t0 = test[0];
                                            } catch (ArrayIndexOutOfBoundsException e) {
                                                t0 = "";
                                            }
                                            try {
                                                t1 = test[1];
                                            } catch (ArrayIndexOutOfBoundsException e) {
                                                t1 = "";
                                            }


                                            start = t0 + "`";
                                            logsListFileTemp.add(new String(start.getBytes(), StandardCharsets.US_ASCII));
                                            logsListFileTemp.add("`" + a);

                                            end = t1;
                                            break;
                                        }
                                    }
                                }
                                if (i == tempLine.length() - 1 && end != null) {
                                    String eEnd = "``" + end;
                                    logsListFileTemp.add(new String(eEnd.getBytes(), StandardCharsets.US_ASCII));
                                    start = null;
                                }
                            }
                            if (!check)
                                logsListFileTemp.add(new String(line.getBytes(), StandardCharsets.US_ASCII));

                            lineCount++;
                        }
                    }
                    if (lineCount > 1_100_000) //stop after 1+ million line
                        break;

                    for (int i = 0; i < logsListFileTemp.size(); i++) {
                        String list = logsListFileTemp.get(i);
                        if (list != null) {
                            if (list.contains("`") && !list.contains("``")) {
                                list = list.replace("`", "");
                                if (i == 0) {
                                    logsListFile.add(list);
                                } else {
                                    logsListFile.set(logsListFile.size() - 1, logsListFile.get(logsListFile.size() - 1) + list);
                                }
                            } else {
                                if (list.contains("``")) {
                                    list = list.replace("`", "");
                                    logsListFile.set(logsListFile.size() - 1, logsListFile.get(logsListFile.size() - 1) + list);
                                    logsListFile.add("");
                                } else {
                                    logsListFile.add(list);
                                    logsListFile.add("");
                                }
                            }
                        } else
                            logsListFileTemp.remove(i);
                    }
                    for (int i = 0; i < logsListFile.size(); i++) {
                        if (logsListFile.get(i).equals(""))
                            logsListFile.remove(i);
                    }

                    for (String s : logsListExceptionTemp) {
                        textExceptionsPane.append(s + "\n");
                    }
                    logsListExceptionTemp.clear();
                    logsListFileTemp.clear();
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

        StringBuilder stringBuffer = new StringBuilder();
        for (String s : logsList) {
            if (s != null)
                stringBuffer.append(s).append("\n");
        }

        publish(stringBuffer.toString());

        return logsList;
    }
}

class ShowAllDeathsSwing extends SwingWorker<List<String>, String> {

    JTextArea textDeathsPane;
    List<String> deathsList;

    public ShowAllDeathsSwing(JTextArea textDeathsPane, List<String> deathsList) {
        this.textDeathsPane = textDeathsPane;
        this.deathsList = deathsList;
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

    @Override
    protected void process(List<String> chunks) {
        for (String text : chunks) {
            textDeathsPane.append(text + "\n");
        }
    }

    @Override
    protected void done() {
        try {
            get();
            SwingUtilities.invokeLater(() -> {
                //logsScroll.getViewport().setViewPosition(new Point(0, 0));
                deathsList.clear();
            });
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected List<String> doInBackground() {
        textDeathsPane.setText("");

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
        for (int i = deathsList.size() - 1; i >= 0; i--) {
            publish(deathsList.get(i));
        }

        return deathsList;
    }

}