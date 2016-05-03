package top.itmp.rtbox;

/**
 * Created by hz on 2016/5/2.
 */
public class RTBox {
    public static boolean DebugMode = false;
    public static final String TAG = "rtbox";
    public static final String version = "rtbox v0.1";

    public static int defaultCommandTimeout = 10000;


    public static enum LogLevel {
        VERBOSE,
        ERROR,
        DEBUG,
        WARN
    }

}
