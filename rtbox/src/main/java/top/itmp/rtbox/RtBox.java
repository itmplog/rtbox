package top.itmp.rtbox;

import java.io.IOException;

import top.itmp.rtbox.utils.Log;

/**
 * Created by hz on 2016/5/2.
 */
public class RtBox {
    public static boolean DebugMode = false;
    public static final String TAG = "rtbox";
    public static final String VERSION = "rtbox" + " v" + BuildConfig.VERSION_NAME + '.' + BuildConfig.VERSION_CODE;

    public static int DefaultCommandTimeout = 10000;

    public static enum LogLevel {
        VERBOSE,
        ERROR,
        DEBUG,
        WARN
    }

    /**
     * General methord to check if user has su binary and accepts root access!
     *
     * @return true if root worked
     */
    public static boolean isRootAccessGranted() {
        boolean rootAccess = false;
        try {
            Shell rootShell = Shell.startRootShell();
            rootAccess = rootShell.isRootAccessGranted();
            rootShell.close();
        } catch (IOException e) {
            Log.w(TAG, "Root Access Not Granted!!", e);
        }

        return rootAccess;
    }

}
