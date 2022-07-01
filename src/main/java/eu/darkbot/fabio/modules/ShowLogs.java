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
import javax.swing.text.Document;
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

@Feature(name = "ShowLogs", description = "Displays the log files in order of modification", enabledByDefault = false)
public class ShowLogs implements Task, InstructionProvider, ExtraMenuProvider {

    private JPanel panel;
    private JScrollPane scroll;
    private JTextArea textArea;

    private JTextField findField;

    private JButton findButton;

    private int pos = 0;

    @Override
    public void install(Main main) {
        if (!Arrays.equals(VerifierChecker.class.getSigners(), getClass().getSigners())) return;
        if (!VerifierChecker.getAuthApi().requireDonor()) return;

        panel = new JPanel(new MigLayout(""));
        findField = new JTextField(20);
        findButton = new JButton("Next");
        textArea = new JTextArea();
        scroll = new JScrollPane(textArea);
        panel.add(findField);
        panel.add(findButton, "wrap");
        panel.add(scroll, "height ::" + Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 1.3 +
                ", width ::" + Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 1.4 + ", span");


        findButton.addActionListener(e -> {
            String find = findField.getText().toLowerCase();
            textArea.requestFocusInWindow();
            if (find.length() > 0) {
                Document document = textArea.getDocument();
                int findLength = find.length();
                try {
                    boolean found = false;
                    if (pos + findLength > document.getLength()) {
                        pos = 0;
                    }
                    while (pos + findLength <= document.getLength()) {
                        String match = document.getText(pos, findLength).toLowerCase();
                        if (match.equals(find)) {
                            found = true;
                            break;
                        }
                        pos++;
                    }

                    if (found) {
                        Rectangle viewRect = textArea.modelToView(pos);
                        textArea.scrollRectToVisible(viewRect);
                        textArea.setCaretPosition(pos + findLength);
                        textArea.moveCaretPosition(pos);
                        pos += findLength;
                    }
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            }
        });
    }

    @Override
    public void tick() {

    }

    public Collection<JComponent> getExtraMenuItems(Main main) {
        if (manageAPI.addSeparator(main, "ShowLogs"))
            return Arrays.asList(createSeparator("Schifo"),
                    create("Show Logs", e -> logic()));
        else
            return Collections.singletonList(
                    create("Show Logs", e -> logic()));
    }

    private void logic() {
        if (!textArea.getText().trim().equals("")) {
            textArea.selectAll();
            textArea.replaceSelection("");
        }
        List<File> files = null;
        try {
            files = listFilesOldestFirst();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        assert files != null;
        for (File file : files) {
            if (file.isFile()) {
                BufferedReader inputStream = null;
                String line;
                try {
                    //inputStream = new BufferedReader(new FileReader(file)); //old with no utf8 encoding
                    inputStream = new BufferedReader(new InputStreamReader(
                            Files.newInputStream(file.toPath()), StandardCharsets.UTF_8));
                    List<String> tmp = new ArrayList<>();

                    while ((line = inputStream.readLine()) != null) {
                        tmp.add(line);
                        //textArea.append(line + "\n");
                    }

                    for (int i = tmp.size() - 1; i >= 0; i--) {
                        textArea.append(tmp.get(i) + "\n");
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
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        Popups.showMessageAsync("Darkbot Log", new Object[]{panel}, JPanel.UNDEFINED_CONDITION);
    }

    private List<File> listFilesOldestFirst() throws IOException {
        try (final Stream<Path> fileStream = Files.list(Paths.get("logs"))) {
            return fileStream
                    .map(Path::toFile)
                    .sorted(Comparator.comparing(File::lastModified).reversed())
                    .filter(path -> !path.getName().contains("deaths"))
                    .collect(Collectors.toList());
        }
    }

}
