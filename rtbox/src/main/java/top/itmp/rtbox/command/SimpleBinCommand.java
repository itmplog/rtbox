package top.itmp.rtbox.command;

import android.content.Context;

/**
 * Created by hz on 16/5/14.
 */
public class SimpleBinCommand extends BinCommand {
    private StringBuilder sb = new StringBuilder();

    public SimpleBinCommand(Context context, String binName, String parameters) {
        super(context, binName, parameters);
    }

    public SimpleBinCommand(Context context, String binName) {
        super(context, binName);
    }

    @Override
    public void output(int id, String line) {
        sb.append(line).append('\n');
    }

    @Override
    public void afterExecution(int id, int exitCode) {

    }

    public String getOutPut() {
        return sb.toString();
    }

    public int getExitCode() {
        return exitCode;
    }
}
