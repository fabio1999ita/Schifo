package eu.darkbot.fabio.api;

import com.github.manolo8.darkbot.Main;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.darkbot.fabio.utils.DownloadUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Objects;

public class manageAPI {

    public static int random;
    public static boolean deleted;
    public static boolean loaded;
    public static boolean checked;

    public manageAPI() {
        File f = new File("lib\\SchifoAPI.dll");
        if (!f.exists())
            downloadAPI();

        random = (int) (Math.random() * 10000) + 1;
        Path source = Paths.get("lib\\SchifoAPI.dll");
        try {
            Files.copy(source, source.resolveSibling(System.getProperty("java.io.tmpdir") + "SchifoAPI" + random + ".dll"),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void checkApiVersion() {
        try {
            URL url = new URL("https://gist.github.com/fabio1999ita/d3c47965a1f2758a44dc6f6fdd2fccf9/raw/version.json");
            URLConnection request = url.openConnection();
            request.connect();


            JsonObject jsonObjectAlt = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent())).getAsJsonObject();
            String version = jsonObjectAlt.get("API_version").getAsString();

            if (!SchifoAPI.version().equals(version))
                downloadAPI();

            checked = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void downloadAPI() {
        /*try (BufferedInputStream in = new BufferedInputStream(new URL("https://gist.github.com/fabio1999ita/d3c47965a1f2758a44dc6f6fdd2fccf9/raw/SchifoAPI.dll").openStream()); FileOutputStream out = new FileOutputStream("lib\\SchifoAPI.dll")) {
            final byte[] data = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                out.write(data, 0, count);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        DownloadUtil a = new DownloadUtil("https://gist.github.com/fabio1999ita/d3c47965a1f2758a44dc6f6fdd2fccf9/raw/SchifoAPI.dll", "lib\\SchifoAPI.dll", "SchifoAPI");
        a.execute();
        while (!a.isDone()) {

        }
    }

    public static void deleteTmpFile() {
        File api = new File("lib\\SchifoAPI.dll");
        //if (api.exists())
        //api.delete();
        if (api.length() == 0)
            api.delete();

        //System.out.println("api lenght " + api.length());

        final File downloadDirectory = new File(System.getProperty("java.io.tmpdir"));
        final File[] files = downloadDirectory.listFiles((dir, name) -> name.matches("SchifoAPI.*?"));
        assert files != null;
        Arrays.asList(files).forEach(File::delete);
        deleted = true;
    }

    public static boolean addSeparator(Main main, String module) {
        if (Objects.requireNonNull(main.featureRegistry.getFeatureDefinition("eu.darkbot.fabio.modules.BackPage")).isEnabled()) {
            return module.equals("BackPage");
        } else if (Objects.requireNonNull(main.featureRegistry.getFeatureDefinition("eu.darkbot.fabio.modules.HangarView")).isEnabled()) {
            return module.equals("HangarView");
        } else if (Objects.requireNonNull(main.featureRegistry.getFeatureDefinition("eu.darkbot.fabio.modules.ShowAllLogs")).isEnabled()) {
            return module.equals("ShowAllLogs");
        }
        return false;
        /*BackPage
        HangarView
        ShowLogs*/
    }
}
