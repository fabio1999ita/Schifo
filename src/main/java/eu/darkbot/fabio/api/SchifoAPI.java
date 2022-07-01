package eu.darkbot.fabio.api;

public class SchifoAPI {

    static {
        System.load(System.getProperty("java.io.tmpdir") + "SchifoAPI" + manageAPI.random + ".dll");
        manageAPI.loaded = true;
    }

    //public static native void getAddress(Object o);
    //SchifoAPI schifoAPI = new SchifoAPI();
    //SchifoAPI.getAddress(main.guiManager);

    public static native void sendCommand(String c);

    public static native String version();

    public static native void showHangar(String instance, String sid, String flashEmbed);

    public static native void BackPage(String instance, String sid);
}
