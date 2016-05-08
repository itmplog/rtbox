package top.itmp.rtbox;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import top.itmp.rtbox.command.Command;
import top.itmp.rtbox.utils.Log;
import top.itmp.rtbox.utils.OnRootAccessDenied;
import top.itmp.rtbox.utils.RootAccessDeniedException;
import top.itmp.rtbox.utils.Utils;

/**
 * Created by hz on 2016/5/2.
 */
public class Shell implements Closeable {
    private final Process process;
    private final BufferedReader stdOutErr;
    private final DataOutputStream outputStream;
    private final List<Command> commands = new ArrayList<>();
    private OnRootAccessDenied onRootAccessDenied;
    private boolean isRootAccessGranted = false;
    private boolean close = false;

    private static final String LD_LIBRARY_PATH = System.getenv("LD_LIBRARY_PATH");
    private static final String token = "F*D^W@#FGF";

    /**
     * Start root shell
     *
     * @param customEnv
     * @param baseDirectory
     * @return
     * @throws IOException
     */
    public static Shell startRootShell(ArrayList<String> customEnv, String baseDirectory)
            throws IOException {
        Log.d(RTBox.TAG, "Starting Root Shell!");

        // On some versions of Android (ICS) LD_LIBRARY_PATH is unset when using su
        // We need to pass LD_LIBRARY_PATH over su for some commands to work correctly.
        if (customEnv == null) {
            customEnv = new ArrayList<String>();
        }
        customEnv.add("LD_LIBRARY_PATH=" + LD_LIBRARY_PATH);

        Shell shell = new Shell(Utils.getSuPath(), customEnv, baseDirectory);

        return shell;
    }


    /**
     * Start root shell without customEnv abd baseDirectory
     *
     * @return
     * @throws IOException
     */
    public static Shell startRootShell() throws IOException {
        return startRootShell(null, null);
    }

    /**
     * Start root shell without customEnv abd baseDirectory
     * Add OnRootAccessDenied Interface
     *
     * @param onAccess
     * @return
     * @throws IOException
     */
    public static Shell startRootShell(OnRootAccessDenied onAccess) throws IOException {
        Shell shell = new Shell(Utils.getSuPath(), null, null, onAccess);
        return shell;
    }

    /**
     * Start normal sh shell
     *
     * @param customEnv
     * @param baseDirectory
     * @return
     * @throws IOException
     */
    public static Shell startShell(ArrayList<String> customEnv, String baseDirectory)
            throws IOException {
        Log.d(RTBox.TAG, "Starting Shell!");
        Shell shell = new Shell("sh", customEnv, baseDirectory);
        return shell;
    }

    /**
     * Start normal sh shell without customEnv abd baseDirectory
     *
     * @return
     * @throws IOException
     */
    public static Shell startShell() throws IOException {
        return startShell(null, null);
    }

    /**
     * Start custom shell defined by shellPath
     *
     * @param shellPath
     * @param customEnv
     * @param baseDirectory
     * @return
     * @throws IOException
     */
    public static Shell startCustomShell(String shellPath, ArrayList<String> customEnv,
                                         String baseDirectory) throws IOException {
        Log.d(RTBox.TAG, "Starting Custom Shell!");
        Shell shell = new Shell(shellPath, customEnv, baseDirectory);

        return shell;
    }

    /**
     * Start custom shell defined by shellPath without custom environment and base directory
     *
     * @param shellPath
     * @return
     * @throws IOException
     */
    public static Shell startCustomShell(String shellPath) throws IOException {
        return startCustomShell(shellPath, null, null);
    }

    private Shell(String shell, ArrayList<String> customEnv, String baseDirectory, OnRootAccessDenied onRootAccessDenied) throws IOException {
        this.onRootAccessDenied = onRootAccessDenied;

        Log.d(RTBox.TAG, "Starting shell: " + shell);

        process = Utils.runWithEnv(shell, customEnv, baseDirectory);

        stdOutErr = new BufferedReader(new InputStreamReader(process.getInputStream()));
        outputStream = new DataOutputStream(process.getOutputStream());

        outputStream.write("echo Started\n".getBytes());
        outputStream.flush();

        while (true) {
            String line = stdOutErr.readLine();
            if (line == null) {
                if (onRootAccessDenied != null)
                    onRootAccessDenied.onDenied();
                throw new RootAccessDeniedException(
                        "stdout line is null! Access was denied or this executeable is not a shell!");
            }
            if ("".equals(line))
                continue;
            if ("Started".equals(line))
                break;

            destroyShellProcess();
            throw new IOException("Unable to start shell, unexpected output \"" + line + "\"");
        }

        if (Utils.getSuPath().equals(shell)) {
            isRootAccessGranted = true;
        }

        new Thread(inputRunnable, "Shell Input").start();
        new Thread(outputRunnable, "Shell Output").start();
    }

    private Shell(String shell, ArrayList<String> customEnv, String baseDirectory) throws IOException {
        Log.d(RTBox.TAG, "Starting shell: " + shell);

        process = Utils.runWithEnv(shell, customEnv, baseDirectory);

        stdOutErr = new BufferedReader(new InputStreamReader(process.getInputStream()));
        outputStream = new DataOutputStream(process.getOutputStream());

        outputStream.write("echo Started\n".getBytes());
        outputStream.flush();

        while (true) {
            String line = stdOutErr.readLine();
            if (line == null) {
                if (onRootAccessDenied != null)
                    onRootAccessDenied.onDenied();
                throw new RootAccessDeniedException(
                        "stdout line is null! Access was denied or this executeable is not a shell!");
            }
            if ("".equals(line))
                continue;
            if ("Started".equals(line))
                break;

            destroyShellProcess();
            throw new IOException("Unable to start shell, unexpected output \"" + line + "\"");
        }

        if (Utils.getSuPath().equals(shell)) {
            isRootAccessGranted = true;
        }

        new Thread(inputRunnable, "Shell Input").start();
        new Thread(outputRunnable, "Shell Output").start();
    }

    private Runnable inputRunnable = new Runnable() {
        public void run() {
            try {
                writeCommands();
            } catch (IOException e) {
                Log.e(RTBox.TAG, "IO Exception", e);
            }
        }
    };

    private Runnable outputRunnable = new Runnable() {
        public void run() {
            try {
                readOutput();
            } catch (IOException e) {
                Log.e(RTBox.TAG, "IOException", e);
            } catch (InterruptedException e) {
                Log.e(RTBox.TAG, "InterruptedException", e);
            }
        }
    };

    private void writeCommands() throws IOException {
        try {
            int commandIndex = 0;
            while (true) {
                DataOutputStream out;
                synchronized (commands) {
                    while (!close && commandIndex >= commands.size()) {
                        commands.wait();
                    }
                    out = this.outputStream;
                }
                if (commandIndex < commands.size()) {
                    Command next = commands.get(commandIndex);
                    next.writeCommand(out);
                    String line = "\necho " + token + " " + commandIndex + " $?\n";
                    out.write(line.getBytes());
                    out.flush();
                    commandIndex++;
                } else if (close) {
                    out.write("\nexit 0\n".getBytes());
                    out.flush();
                    Log.d(RTBox.TAG, "Closing shell");
                    process.waitFor();
                    out.close();
                    return;
                } else {
                    Thread.sleep(50);
                }
            }
        } catch (InterruptedException e) {
            Log.e(RTBox.TAG, "interrupted while writing command", e);
        }
    }

    /**
     * Reads output line by line, seperated by token written after every command
     *
     * @throws IOException
     * @throws InterruptedException
     */
    private void readOutput() throws IOException, InterruptedException {
        Command command = null;

        // index of current command
        int commandIndex = 0;
        while (true) {
            String lineStdOut = stdOutErr.readLine();

            // terminate on EOF
            if (lineStdOut == null)
                break;

            if (command == null) {

                // break on close after last command
                if (commandIndex >= commands.size()) {
                    if (close)
                        break;
                    continue;
                }

                // get current command
                command = commands.get(commandIndex);
            }

            int pos = lineStdOut.indexOf(token);
            if (pos > 0) {
                command.processOutput(lineStdOut.substring(0, pos));
            }
            if (pos >= 0) {
                lineStdOut = lineStdOut.substring(pos);
                String fields[] = lineStdOut.split(" ");
                int id = Integer.parseInt(fields[1]);
                if (id == commandIndex) {
                    command.setExitCode(Integer.parseInt(fields[2]));

                    // go to next command
                    commandIndex++;
                    command = null;
                    continue;
                }
            }
            command.processOutput(lineStdOut);
        }
        Log.d(RTBox.TAG, "Read all output");
        process.waitFor();
        stdOutErr.close();
        destroyShellProcess();

        while (commandIndex < commands.size()) {
            if (command == null) {
                command = commands.get(commandIndex);
            }
            command.terminated("Unexpected Termination!");
            commandIndex++;
            command = null;
        }
    }

    private void destroyShellProcess() {
        try {
            // Yes, this really is the way to check if the process is
            // still running.
            process.exitValue();
        } catch (IllegalThreadStateException e) {
            // Only call destroy() if the process is still running;
            // Calling it for a terminated process will not crash, but
            // (starting with at least ICS/4.0) spam the log with INFO
            // messages ala "Failed to destroy process" and "kill
            // failed: ESRCH (No such process)".
            process.destroy();
        }

        Log.d(RTBox.TAG, "Shell destroyed");
    }

    /**
     * Add command to shell queue
     *
     * @param command
     * @return
     * @throws IOException
     */
    public Command add(Command command) throws IOException {
        if (close)
            throw new IOException("Unable to add commands to a closed shell");
        synchronized (commands) {
            commands.add(command);
            // set shell on the command object, to know where the command is running on
            command.addToShell(this, (commands.size() - 1));
            commands.notifyAll();
        }

        return command;
    }

    /**
     * return true if root access granted
     *
     * @return
     */
    public boolean isRootAccessGranted() {
        return isRootAccessGranted;
    }

    public int getCommandsSize() {
        return commands.size();
    }

    /**
     * Close shell
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        synchronized (commands) {
            this.close = true;
            commands.notifyAll();
        }
    }
}
