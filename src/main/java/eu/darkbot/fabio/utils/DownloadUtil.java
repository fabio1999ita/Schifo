package eu.darkbot.fabio.utils;

import com.github.manolo8.darkbot.gui.utils.Popups;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.TimerTask;

public class DownloadUtil extends SwingWorker<Void, Void> {

    private final String downloadUrl;

    private final String pathDownload;

    private final String fileName;

    private final JProgressBar progressBar;


    public DownloadUtil(String downloadUrl, String pathDownload, String fileName) {
        this.downloadUrl = downloadUrl;
        this.pathDownload = pathDownload;
        this.fileName = fileName;

        JPanel panel = new JPanel();
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        panel.add(progressBar);

        addPropertyChangeListener(evt -> {
            if ("progress".equalsIgnoreCase(evt.getPropertyName())) {
                progressBar.setValue(getProgress());
            }
        });

        //Popups.showMessageSync("Schifo download", new JOptionPane(panel));
        Popups.showMessageAsync("Schifo: downloading " + fileName, new Object[]{panel}, JPanel.UNDEFINED_CONDITION);
    }

    @Override
    protected void done() {
        new java.util.Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (Window w : JDialog.getWindows()) {
                    if (w instanceof JDialog && ((JDialog) w).getTitle().equals("Schifo: downloading " + fileName)) {
                        w.setVisible(false);
                        w.dispose();
                    }
                }
            }
        }, 3000);
    }

    @Override
    protected Void doInBackground() {
        URL url;
        int fileLength;
        try {
            /*url = new URL(downloadUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();*/
            url = new URL(downloadUrl);
            URLConnection connection = url.openConnection();
            connection.connect();

            fileLength = connection.getContentLength();

            try (BufferedInputStream in = new BufferedInputStream(url.openStream()); FileOutputStream out = new FileOutputStream(pathDownload)) {
                final byte[] data = new byte[1024];
                int count;
                long total = 0;

                while ((count = in.read(data, 0, 1024)) != -1) {
                    total += count;
                    out.write(data, 0, count);
                    setProgress((int) ((total * 100) / fileLength));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
