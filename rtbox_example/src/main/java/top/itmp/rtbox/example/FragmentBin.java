package top.itmp.rtbox.example;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import top.itmp.rtbox.RtBox;
import top.itmp.rtbox.Shell;
import top.itmp.rtbox.command.SimpleBinCommand;

/**
 * Created by hz on 16/5/14.
 */
public class FragmentBin extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout rootView = new LinearLayout(container.getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        rootView.setLayoutParams(layoutParams);
        rootView.setOrientation(LinearLayout.VERTICAL);
        final TextView textView = new TextView(getActivity());
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        RtBox.DebugMode = true;
        final Shell shell = Shell.startShell();
        final RtBox rtBox = new RtBox(shell);
        final SimpleBinCommand simpleBinCommand = new SimpleBinCommand(getActivity(), "hello", null);

        Button execBin = new Button(getActivity());
        execBin.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        execBin.setText("execbin");
        execBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shell.run(simpleBinCommand);

                try {
                    boolean isRunning = rtBox.isProcessRunning(simpleBinCommand.getCommand());
                    textView.setText(simpleBinCommand.getCommand() + (isRunning ? "running: \n": "not running\n"));

                    if(isRunning){
                        ArrayList<String> pids = rtBox.getPids(simpleBinCommand.getCommand());
                        for(String pid: pids){
                            textView.append(pid);
                        }
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }catch (TimeoutException e){
                    e.printStackTrace();
                }

            }
        });

        Button checkBin = new Button(getActivity());
        checkBin.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        checkBin.setText("killBin");
        checkBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(rtBox.isProcessRunning(simpleBinCommand.getCommand())){
                       textView.setText(rtBox.killAll(simpleBinCommand.getCommand()) ? "killed" : "kill failed");
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }catch (TimeoutException e){
                    e.printStackTrace();
                }
            }
        });

        rootView.addView(textView);
        rootView.addView(execBin);
        rootView.addView(checkBin);
        return rootView;
    }
}
