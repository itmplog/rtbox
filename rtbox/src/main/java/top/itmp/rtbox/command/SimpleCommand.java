package top.itmp.rtbox.command;

import top.itmp.rtbox.RtBox;
import top.itmp.rtbox.utils.Log;

/**
 * Created by hz on 2016/5/3.
 */
public class SimpleCommand extends Command {

    private StringBuilder sb = new StringBuilder();

    public SimpleCommand(String ... command){
        super(command);
    }

    @Override
    public void output(int id, String line) {
        Log.v(RtBox.TAG, line + "    " + id);
        sb.append(line).append('\n');
    }

    @Override
    public void afterExecution(int id, int exitCode) {

    }

    public String getOutput(){
        return sb.toString();
    }

    public int getExitCode(){
        return exitCode;
    }
}
