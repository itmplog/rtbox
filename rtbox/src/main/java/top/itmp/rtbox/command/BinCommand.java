package top.itmp.rtbox.command;

import android.content.Context;
import android.os.Build;

import java.io.File;

/**
 * Created by hz on 16/5/14.
 */
public abstract class BinCommand extends Command {
    public static String commandLine;
    public static final String BIN_PREFIX = "lib";
    public static final String BIN_SUFFIX = "_bin.so";


    /**
     * This class provides a way to use your own binaries!
     * <p>
     * * Include your own executables, renamed from * to lib*_exec.so, in your libs folder under the
     * architecture directories. Now they will be deployed by Android the same way libraries are
     * deployed!
     * <p>
     * See README for more information how to use your own executables!
     *
     * @param context
     * @param binName
     * @param parameters
     */
    public BinCommand(Context context, String binName, String parameters) {
        super(getLibPATH(context) + File.separator + BIN_PREFIX + binName
                + BIN_SUFFIX + " " + parameters);
        commandLine = BIN_PREFIX + binName + BIN_SUFFIX;
    }

    /**
     * This class provides a way to use your own binaries!
     * <p>
     * * Include your own executables, renamed from * to lib*_exec.so, in your libs folder under the
     * architecture directories. Now they will be deployed by Android the same way libraries are
     * deployed!
     * <p>
     * See README for more information how to use your own executables!
     *
     * @param context
     * @param binName
     */
    public BinCommand(Context context, String binName) {
        super(getLibPATH(context) + File.separator + BIN_PREFIX + binName
                + BIN_SUFFIX);
        commandLine = BIN_PREFIX + binName + BIN_SUFFIX;
    }


    /**
     * Get full path to lib directory of app
     *
     * @param context
     * @return dir as String
     */
    private static String getLibPATH(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return context.getApplicationInfo().nativeLibraryDir;
        } else {
            return context.getApplicationInfo().dataDir + File.pathSeparator + "lib";
        }
    }


    public String getCommandLine() {
        return commandLine;
    }

    public abstract void output(int id, String line);

    public abstract void afterExecution(int id, int exitCode);
}
