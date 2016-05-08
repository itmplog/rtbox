package top.itmp.rtbox.command;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeoutException;

import top.itmp.rtbox.RTBox;
import top.itmp.rtbox.Shell;
import top.itmp.rtbox.utils.Log;

/**
 * Created by hz on 2016/5/2.
 */
public abstract class Command {
    final String command[];
    boolean finished = false;
    int exitCode;
    int id;
    int timeout = RTBox.defaultCommandTimeout;
    Shell shell = null;

    public Command(String... command) {
        this.command = command;
    }

    public Command(int timeout, String... command) {
        this.timeout = timeout;
        this.command = command;
    }

    /**
     * This is called from Shell after adding it
     *
     * @param shell
     * @param id
     */
    public void addToShell(Shell shell, int id) {
        this.shell = shell;
        this.id = id;
    }

    /**
     * Gets command string executed on the shell
     *
     * @return
     */
    public String getCommand() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < command.length; i++) {
            sb.append(command[i]);
            sb.append(' ');
            sb.append("2>&1");
            sb.append('\n');
        }
        Log.d(RTBox.TAG, "Sending command(s): " + sb.toString());
        return sb.toString();
    }

    public void writeCommand(OutputStream out) throws IOException {
        out.write(getCommand().getBytes());
    }

    public void processOutput(String line) {
        Log.d(RTBox.TAG, "ID: " + id + ", Output: " + line);

        // now execute specific output parsing
        output(id, line);
    }

    public abstract void output(int id, String line);

    public void processAfterExecution(int exitCode) {
        Log.d(RTBox.TAG, "ID: " + id + ", ExitCode: " + exitCode);

        afterExecution(id, exitCode);
    }

    public abstract void afterExecution(int id, int exitCode);

    public void commandFinished(int id) {
        Log.d(RTBox.TAG, "Command " + id + " finished.");
    }

    public synchronized void setExitCode(int code) {
        exitCode = code;
        finished = true;
        commandFinished(id);
        this.notifyAll();
    }

    /**
     * Terminate the shell cause reason
     *
     * @param reason
     */
    public void terminate(String reason) {
        try {
            shell.close();
            Log.d(RTBox.TAG, "Terminating the shell.");
            terminated(reason);
        } catch (IOException e) {
        }
    }

    public void terminated(String reason) {
        setExitCode(-1);
        Log.d(RTBox.TAG, "Command " + id + " did not finish, because of " + reason);
    }

    /**
     * Waits for this command to finish and forwards exitCode into afterExecution method
     *
     * @throws TimeoutException
     */
    public synchronized void waitForFinish() throws TimeoutException {
        while (!finished) {
            try {
                this.wait(timeout);
            } catch (InterruptedException e) {
                Log.e(RTBox.TAG, "InterruptedException in waitForFinish()", e);
            }

            if (!finished) {
                finished = true;
                terminate("Timeout");
                throw new TimeoutException("Timeout has occurred.");
            }
        }

        processAfterExecution(exitCode);
    }

}
