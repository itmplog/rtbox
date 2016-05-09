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
    int timeout = RTBox.DefaultCommandTimeout;
    Shell shell = null;

    /**
     * Constructs
     *
     * @param command
     */
    public Command(String... command) {
        this.command = command;
    }

    /**
     * Constructs
     * @param timeout
     * @param command
     */
    public Command(int timeout, String... command) {
        this.timeout = timeout;
        this.command = command;
    }

    /**
     * This is called from Shell after adding it
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

    /**
     * This is called from shell when writing to the outputStream of shell
     * @param out
     * @throws IOException
     */
    public void writeCommand(OutputStream out) throws IOException {
        out.write(getCommand().getBytes());
    }

    /**
     * Called from shell when process Output
     * @param line
     */
    public void processOutput(String line) {
        Log.d(RTBox.TAG, "ID: " + id + ", Output: " + line);

        // now execute specific output parsing
        output(id, line);
    }

    /**
     * Called from shell when output id and output lines
     * @param id
     * @param line
     */
    public abstract void output(int id, String line);

    /**
     * This will execute after command execution
     * @param exitCode
     */
    private void processAfterExecution(int exitCode) {
        Log.d(RTBox.TAG, "ID: " + id + ", ExitCode: " + exitCode);

        afterExecution(id, exitCode);
    }

    /**
     * Called after execution
     * @param id
     * @param exitCode
     */
    public abstract void afterExecution(int id, int exitCode);

    /**
     * called when command Finished
     * @param id
     */
    private void commandFinished(int id) {
        Log.d(RTBox.TAG, "Command " + id + " finished.");
    }

    /**
     * synchronized set Command exitCode
     * @param code
     */
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


    /**
     * Called after terminate
     * @param reason
     */
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

    /**
     * Waits for this command to finish and setTimeout
     * @param timeout
     * @throws TimeoutException
     */
    public synchronized void waitForFinish(int timeout) throws TimeoutException{
        this.timeout = timeout;
        waitForFinish();
    }
}
