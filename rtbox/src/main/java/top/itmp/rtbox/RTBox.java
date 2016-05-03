package top.itmp.rtbox;

import java.io.IOException;

import top.itmp.rtbox.utils.Log;

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

    public static boolean isRootAccessGranted() {
        boolean rootAccess = false;
        try {
            Shell rootShell = Shell.startRootShell();
            rootAccess = rootShell.isRootAccessGranted();
            rootShell.close();
        }catch (IOException e){
            Log.w(TAG, "Root Access Not Granted!!", e);
        }

        return rootAccess;
    }

}
