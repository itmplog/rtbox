package top.itmp.rtbox;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.StatFs;
import android.os.SystemClock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import top.itmp.rtbox.command.Command;
import top.itmp.rtbox.command.SimpleCommand;
import top.itmp.rtbox.utils.FS;
import top.itmp.rtbox.utils.Log;

/**
 * Created by hz on 2016/5/2.
 */
public class RtBox {
    public static boolean DebugMode = BuildConfig.DEBUG;
    public static final String TAG = "rtbox";
    public static final String VERSION = "rtbox" + " v" + BuildConfig.VERSION_NAME + '.' + BuildConfig.VERSION_CODE;
    public static int DefaultCommandTimeout = 10000;

    private Shell shell;

    public static enum LogLevel {
        VERBOSE,
        ERROR,
        DEBUG,
        WARN
    }

    /**
     * Get a RtBox
     *
     * @param shell to exec commands on
     */
    public RtBox(Shell shell){
        super();
        this.shell = shell;
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

    /**
     * This command class gets all pids to a given process name
     */
    private class PsCommand extends Command {
        private String processName;
        private ArrayList<String> pids;
        private String psRegex;
        private Pattern psPattern;

        public PsCommand(String processName) {
            super("ps");
            this.processName = processName;
            pids = new ArrayList<String>();

            /**
             * regex to get pid out of ps line, example:
             *
             * <pre>
             *  root    24736    1   12140  584   ffffffff 40010d14 S /data/data/org.adaway/files/blank_webserver
             * ^\\S \\s ([0-9]+)                          .*                                      processName    $
             * </pre>
             */
            psRegex = "^\\S+\\s+([0-9]+).*" + Pattern.quote(processName) + "$";
            psPattern = Pattern.compile(psRegex);
        }

        public ArrayList<String> getPids() {
            return pids;
        }

        public String getPidsString() {
            StringBuilder sb = new StringBuilder();
            for (String s : pids) {
                sb.append(s);
                sb.append(" ");
            }

            return sb.toString();
        }

        @Override
        public void output(int id, String line) {
            // general check if line contains processName
            if (line.contains(processName)) {
                Matcher psMatcher = psPattern.matcher(line);

                // try to match line exactly
                try {
                    if (psMatcher.find()) {
                        String pid = psMatcher.group(1);
                        // add to pids list
                        pids.add(pid);
                        Log.d(RtBox.TAG, "Found pid: " + pid);
                    } else {
                        Log.d(RtBox.TAG, "Matching in ps command failed!");
                    }
                } catch (Exception e) {
                    Log.e(RtBox.TAG, "Error with regex!", e);
                }
            }
        }

        @Override
        public void afterExecution(int id, int exitCode) {
        }

    }

    /**
     * This method can be used to kill a running process
     *
     * (commands: ps, kill)
     *
     * @param processName
     *            name of process to kill
     * @return <code>true</code> if process was found and killed successfully
     * @throws IOException
     * @throws TimeoutException
     */
    public boolean killAll(String processName) throws TimeoutException,
            IOException {
        Log.d(RtBox.TAG, "Killing process " + processName);

        PsCommand psCommand = new PsCommand(processName);
        shell.add(psCommand).waitForFinish();

        // kill processes
        if (!psCommand.getPids().isEmpty()) {
            // example: kill -9 1234 1222 5343
            SimpleCommand killCommand = new SimpleCommand("kill -9 "
                    + psCommand.getPidsString());
            shell.add(killCommand).waitForFinish();

            if (killCommand.getExitCode() == 0) {
                return true;
            } else {
                return false;
            }
        } else {
            Log.d(RtBox.TAG, "No pid found! Nothing was killed!");
            return false;
        }
    }

    /**
     * Kill a running executable
     *
     * See README for more information how to use your own executables!
     *
     * @param executableName
     * @return
     * @throws TimeoutException
     * @throws IOException
     *
    public boolean killAllExecutable(String executableName) throws TimeoutException, IOException {
        return killAll(ExecutableCommand.EXECUTABLE_PREFIX + executableName + ExecutableCommand.EXECUTABLE_SUFFIX);
    }*/

    /**
     * This method can be used to to check if a process is running
     *
     * @param processName
     *            name of process to check
     * @return <code>true</code> if process was found
     * @throws IOException
     * @throws TimeoutException
     *             (Could not determine if the process is running)
     */
    public boolean isProcessRunning(String processName) throws TimeoutException, IOException {
        PsCommand psCommand = new PsCommand(processName);
        shell.add(psCommand).waitForFinish();

        // if pids are available process is running!
        if (!psCommand.getPids().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if binary is running
     *
     * @param binaryName
     * @return
     * @throws TimeoutException
     * @throws IOException
     *
    public boolean isBinaryRunning(String binaryName) throws TimeoutException, IOException {
        return isProcessRunning(ExecutableCommand.EXECUTABLE_PREFIX + binaryName
                + ExecutableCommand.EXECUTABLE_SUFFIX);
    }*/

    /**
     * Ls command to get permissions or symlinks
     */
    private class LsCommand extends Command {
        private String fileName;
        private String permissionRegex;
        private Pattern permissionPattern;
        private String symlinkRegex;
        private Pattern symlinkPattern;

        private String symlink;
        private String permissions;

        public String getSymlink() {
            return symlink;
        }

        public String getPermissions() {
            return permissions;
        }

        public LsCommand(String file) {
            super("ls -l " + file);

            // get only filename:
            this.fileName = (new File(file)).getName();
            Log.d(RtBox.TAG, "fileName: " + fileName);

            /**
             * regex to get pid out of ps line, example:
             *
             * <pre>
             * with busybox:
             *     lrwxrwxrwx     1 root root            15 Aug 13 12:14 dev/stdin -> /proc/self/fd/0
             *
             * with toolbox:
             *     lrwxrwxrwx root root            15 Aug 13 12:14 stdin -> /proc/self/fd/0
             *
             * Regex:
             * ^.*?(\\S{10})                     .*                                                  $
             * </pre>
             */
            permissionRegex = "^.*?(\\S{10}).*$";
            permissionPattern = Pattern.compile(permissionRegex);

            /**
             * regex to get symlink
             *
             * <pre>
             *     ->           /proc/self/fd/0
             * ^.*?\\-\\> \\s+  (.*)           $
             * </pre>
             */
            symlinkRegex = "^.*?\\-\\>\\s+(.*)$";
            symlinkPattern = Pattern.compile(symlinkRegex);
        }

        /**
         * Converts permission string from ls command to numerical value. Example: -rwxrwxrwx gets
         * to 777
         *
         * @param permissions
         * @return
         */
        private String convertPermissions(String permissions) {
            int owner = getGroupPermission(permissions.substring(1, 4));
            int group = getGroupPermission(permissions.substring(4, 7));
            int world = getGroupPermission(permissions.substring(7, 10));

            return "" + owner + group + world;
        }

        /**
         * Calculates permission for one group
         *
         * @param permission
         * @return value of permission string
         */
        private int getGroupPermission(String permission) {
            int value = 0;

            if (permission.charAt(0) == 'r') {
                value += 4;
            }
            if (permission.charAt(1) == 'w') {
                value += 2;
            }
            if (permission.charAt(2) == 'x') {
                value += 1;
            }

            return value;
        }

        @Override
        public void output(int id, String line) {
            // general check if line contains file
            if (line.contains(fileName)) {

                // try to match line exactly
                try {
                    Matcher permissionMatcher = permissionPattern.matcher(line);
                    if (permissionMatcher.find()) {
                        permissions = convertPermissions(permissionMatcher.group(1));

                        Log.d(RtBox.TAG, "Found permissions: " + permissions);
                    } else {
                        Log.d(RtBox.TAG, "Permissions were not found in ls command!");
                    }

                    // try to parse for symlink
                    Matcher symlinkMatcher = symlinkPattern.matcher(line);
                    if (symlinkMatcher.find()) {
                        /*
                         * TODO: If symlink points to a file in the same directory the path is not
                         * absolute!!!
                         */
                        symlink = symlinkMatcher.group(1);
                        Log.d(RtBox.TAG, "Symlink found: " + symlink);
                    } else {
                        Log.d(RtBox.TAG, "No symlink found!");
                    }
                } catch (Exception e) {
                    Log.e(RtBox.TAG, "Error with regex!", e);
                }
            }
        }

        @Override
        public void afterExecution(int id, int exitCode) {
        }

    }

    /**
     * @param file
     *            String that represent the file, including the full path to the file and its name.
     * @return File permissions as String, for example: 777, returns null on error
     * @throws IOException
     * @throws TimeoutException
     *
     */
    public String getFilePermissions(String file) throws TimeoutException, IOException {
        Log.d(RtBox.TAG, "Checking permissions for " + file);

        String permissions = null;

        if (fileExists(file)) {
            Log.d(RtBox.TAG, file + " was found.");

            LsCommand lsCommand = new LsCommand(file);
            shell.add(lsCommand).waitForFinish();

            permissions = lsCommand.getPermissions();
        }

        return permissions;
    }

    /**
     * Sets permission of file
     *
     * @param file
     *            absolute path to file
     * @param permissions
     *            String like 777
     * @return true if command worked
     * @throws TimeoutException
     * @throws IOException
     */
    public boolean setFilePermissions(String file, String permissions)
            throws TimeoutException, IOException {
        Log.d(RtBox.TAG, "Set permissions of " + file + " to " + permissions);

        SimpleCommand chmodCommand = new SimpleCommand("chmod " + permissions + " " + file);
        shell.add(chmodCommand).waitForFinish();

        if (chmodCommand.getExitCode() == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This will return a String that represent the symlink for a specified file.
     *
     * @param file
     *            The path to the file to get the Symlink for. (must have absolute path)
     *
     * @return A String that represent the symlink for a specified file or null if no symlink
     *         exists.
     * @throws IOException
     * @throws TimeoutException
     */
    public String getSymlink(String file) throws TimeoutException,
            IOException {
        Log.d(RtBox.TAG, "Find symlink for " + file);

        String symlink = null;

        LsCommand lsCommand = new LsCommand(file);
        shell.add(lsCommand).waitForFinish();

        symlink = lsCommand.getSymlink();

        return symlink;
    }

    /**
     * Copys a file to a destination. Because cp is not available on all android devices, we use dd
     * or cat.
     *
     * @param source
     *            example: /data/data/org.adaway/files/hosts
     * @param destination
     *            example: /system/etc/hosts
     * @param remountAsRw
     *            remounts the destination as read/write before writing to it
     *            tries to copy file attributes from source to destination, if only cat is available
     *            only permissions are preserved
     * @return true if it was successfully copied
     * @throws IOException
     * @throws TimeoutException
     */
    public boolean copyFile(String source, String destination, boolean remountAsRw,
                            boolean preservePermissions) throws IOException, TimeoutException {

        /*
         * dd can only copy files, but we can not check if the source is a file without invoking
         * shell commands, because from Java we probably have no read access, thus we only check if
         * they are ending with trailing slashes
         */
        if (source.endsWith("/") || destination.endsWith("/")) {
            throw new FileNotFoundException("dd can only copy files!");
        }

        // remount destination as read/write before copying to it
        if (remountAsRw) {
            if (!remount(destination, "RW")) {
                Log.d(RtBox.TAG,
                        "Remounting failed! There is probably no need to remount this partition!");
            }
        }

        // get permissions of source before overwriting
        String permissions = null;
        if (preservePermissions) {
            permissions = getFilePermissions(source);
        }

        boolean commandSuccess = false;

        SimpleCommand ddCommand = new SimpleCommand("dd if=" + source + " of="
                + destination);
        shell.add(ddCommand).waitForFinish();

        if (ddCommand.getExitCode() == 0) {
            commandSuccess = true;
        } else {
            // try cat if dd fails
            SimpleCommand catCommand = new SimpleCommand("cat " + source + " > "
                    + destination);
            shell.add(catCommand).waitForFinish();

            if (catCommand.getExitCode() == 0) {
                commandSuccess = true;
            }
        }

        // set back permissions from source to destination
        if (preservePermissions) {
            setFilePermissions(destination, permissions);
        }

        // remount destination back to read only
        if (remountAsRw) {
            if (!remount(destination, "RO")) {
                Log.d(RtBox.TAG,
                        "Remounting failed! There is probably no need to remount this partition!");
            }
        }

        return commandSuccess;
    }

    public static final int REBOOT_SOFT = 1;
    public static final int REBOOT_REBOOT = 2;
    public static final int REBOOT_SHUTDOWN = 3;
    public static final int REBOOT_RECOVERY = 4;
    public static final int REBOOT_BOOTLOADER = 5;
    public static final int REBOOT_DOWNLOAD = 6; // .


    /**
     * Shutdown or reboot device. Possible actions are REBOOT_SOFT, REBOOT_REBOOT,
     * REBOOT_SHUTDOWN, REBOOT_RECOVERY, REBOOT_BOOTLOADER,
     * REBOOT_DOWNLOAD: Not all devices have this option. You can try this, but risk is yours
     *
     * @param action
     * @throws IOException
     * @throws TimeoutException
     */
    public void reboot(int action) throws TimeoutException, IOException {
        if (action == REBOOT_SOFT) {
            killAll("system_server");
            // or: killAll("zygote");
        } else {
            String command;
            switch (action) {
                case REBOOT_REBOOT:
                    command = "reboot";
                    break;
                case REBOOT_SHUTDOWN:
                    command = "reboot -p";
                    break;
                case REBOOT_RECOVERY:
                    command = "reboot recovery";
                    break;
                case REBOOT_BOOTLOADER:
                    command = "reboot bootloader";
                    break;
                case REBOOT_DOWNLOAD:
                    command = "reboot download";
                    break;
                default:
                    command = "reboot";
                    break;
            }

            SimpleCommand rebootCommand = new SimpleCommand(command);
            shell.add(rebootCommand).waitForFinish();

            if (rebootCommand.getExitCode() == -1) {
                Log.e(RtBox.TAG, "Reboot failed!");
            }
        }
    }

    /**
     * This command checks if a file exists
     */
    private class FileExistsCommand extends Command {
        private String file;
        private boolean fileExists = false;

        public FileExistsCommand(String file) {
            super("ls " + file);
            this.file = file;
        }

        public boolean isFileExists() {
            return fileExists;
        }

        @Override
        public void output(int id, String line) {
            if (line.trim().equals(file)) {
                fileExists = true;
            }
        }

        @Override
        public void afterExecution(int id, int exitCode) {
        }

    }

    /**
     * Use this to check whether or not a file exists on the filesystem.
     *
     * @param file
     *            String that represent the file, including the full path to the file and its name.
     *
     * @return a boolean that will indicate whether or not the file exists.
     * @throws IOException
     * @throws TimeoutException
     *
     */
    public boolean fileExists(String file) throws TimeoutException, IOException {
        FileExistsCommand fileExistsCommand = new FileExistsCommand(file);
        shell.add(fileExistsCommand).waitForFinish();

        if (fileExistsCommand.isFileExists()) {
            return true;
        } else {
            return false;
        }
    }

    public abstract class WithPermissions {
        abstract void whileHavingPermissions();
    }

    /**
     * Execute user defined Java code while having temporary permissions on a file
     *
     * @param file
     * @param withPermissions
     * @throws TimeoutException
     * @throws IOException
     */
    public void withPermission(String file, String permission, WithPermissions withPermissions)
            throws TimeoutException, IOException {
        String oldPermissions = getFilePermissions(file);

        // set permissions (If set to 666, then Dalvik VM can also write to that file!)
        setFilePermissions(file, permission);

        // execute user defined code
        withPermissions.whileHavingPermissions();

        // set back to old permissions
        setFilePermissions(file, oldPermissions);
    }

    /**
     * Execute user defined Java code while having temporary write permissions on a file using chmod
     * 666
     *
     * @param file
     * @param withWritePermissions
     * @throws TimeoutException
     * @throws IOException
     */
    public void withWritePermissions(String file, WithPermissions withWritePermissions)
            throws TimeoutException, IOException {
        withPermission(file, "666", withWritePermissions);
    }

    /**
     * Sets system clock using /dev/alarm
     *
     * @param millis
     * @throws TimeoutException
     * @throws IOException
     */
    public void setSystemClock(final long millis) throws TimeoutException, IOException {
        withWritePermissions("/dev/alarm", new WithPermissions() {

            @Override
            void whileHavingPermissions() {
                SystemClock.setCurrentTimeMillis(millis);
            }
        });
    }

    /**
     * Adjust system clock by offset using /dev/alarm
     *
     * @param offset
     * @throws TimeoutException
     * @throws IOException
     */
    public void adjustSystemClock(final long offset) throws TimeoutException, IOException {
        withWritePermissions("/dev/alarm", new WithPermissions() {

            @Override
            void whileHavingPermissions() {
                SystemClock.setCurrentTimeMillis(System.currentTimeMillis() + offset);
            }
        });
    }

    /**
     * This will take a path, which can contain the file name as well, and attempt to remount the
     * underlying partition.
     *
     * For example, passing in the following string:
     * "/system/bin/some/directory/that/really/would/never/exist" will result in /system ultimately
     * being remounted. However, keep in mind that the longer the path you supply, the more work
     * this has to do, and the slower it will run.
     *
     * @param file
     *            file path
     * @param mountType
     *            mount type: pass in RO (Read only) or RW (Read Write)
     * @return a <code>boolean</code> which indicates whether or not the partition has been
     *         remounted as specified.
     */
    public boolean remount(String file, String mountType) {
        // Recieved a request, get an instance of Remounter
        FS fs = new FS(shell);
        // send the request
        return (fs.remount(file, mountType));
    }

    /**
     * This will tell you how the specified mount is mounted. rw, ro, etc...
     *
     * @param path The mount you want to check
     *
     * @return <code>String</code> What the mount is mounted as.
     * @throws Exception
     *             if we cannot determine how the mount is mounted.
     */
    public String getMountedAs(String path) throws Exception {
        ArrayList<FS.Mount> mounts = FS.getMounts();
        if (mounts != null) {
            for (FS.Mount mount : mounts) {
                if (path.contains(mount.getMountPoint().getAbsolutePath())) {
                    Log.d(RtBox.TAG, (String) mount.getFlags().toArray()[0]);
                    return (String) mount.getFlags().toArray()[0];
                }
            }

            throw new Exception();
        } else {
            throw new Exception();
        }
    }

    /**
     * Check if there is enough space on partition where target is located
     *
     * @param size
     *            size of file to put on partition
     * @param target
     *            path where to put the file
     *
     * @return true if it will fit on partition of target, false if it will not fit.
     */
    @SuppressWarnings("deprecation")
    public boolean hasEnoughSpaceOnPartition(String target, long size) {
        try {
            // new File(target).getFreeSpace() (API 9) is not working on data partition

            // get directory without file
            String directory = new File(target).getParent().toString();

            StatFs stat = new StatFs(directory);

            long blockSize;
            long availableBlocks;

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = stat.getBlockSizeLong();
                availableBlocks = stat.getAvailableBlocksLong();
            }else {
                blockSize = stat.getBlockSize();
                availableBlocks = stat.getAvailableBlocks();
            }
            long availableSpace = availableBlocks * blockSize;

            Log.i(RtBox.TAG, "Checking for enough space: Target: " + target
                    + ", directory: " + directory + " size: " + size + ", availableSpace: "
                    + availableSpace);

            if (size < availableSpace) {
                return true;
            } else {
                Log.e(RtBox.TAG, "Not enough space on partition!");
                return false;
            }
        } catch (Exception e) {
            // if new StatFs(directory) fails catch IllegalArgumentException and just return true as
            // workaround
            Log.e(RtBox.TAG, "Problem while getting available space on partition!", e);
            return true;
        }
    }

    /**
     * TODO: Not tested!
     *
     * @param toggle
     * @throws IOException
     * @throws TimeoutException
     */
    public void toggleAdbDaemon(boolean toggle) throws TimeoutException, IOException {
        SimpleCommand disableAdb = new SimpleCommand("setprop persist.service.adb.enable 0",
                "stop adbd");
        SimpleCommand enableAdb = new SimpleCommand("setprop persist.service.adb.enable 1",
                "stop adbd", "sleep 1", "start adbd");

        if (toggle) {
            shell.add(enableAdb).waitForFinish();
        } else {
            shell.add(disableAdb).waitForFinish();
        }
    }
}
