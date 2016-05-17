package top.itmp.rtbox.example;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import top.itmp.rtbox.RtBox;
import top.itmp.rtbox.Shell;
import top.itmp.rtbox.command.SimpleCommand;

/**
 * Created by hz on 16/5/11.
 */
public class FramentNew extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout rootView = new LinearLayout(container.getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        rootView.setLayoutParams(layoutParams);
        rootView.setOrientation(LinearLayout.VERTICAL);

        Button button = new Button(rootView.getContext());
        button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        button.setText("exec test");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RtBox.Debug = true;
                Shell shell = Shell.startShell();
                SimpleCommand simpleCommand = new SimpleCommand("echo aaaa");
                SimpleCommand simpleCommand1 = new SimpleCommand("echo bbbb");
                SimpleCommand simpleCommand2 = new SimpleCommand("echo cccc");
                try {
                    shell.add(simpleCommand);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                shell.run(simpleCommand1);
                try {
                    simpleCommand.waitForFinish();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }
                simpleCommand2.exec(shell);

                new AlertDialog.Builder(getActivity())
                        .setTitle("Output")
                        .setMessage(simpleCommand.getCommand() + ": " + simpleCommand.getOutput() + "\n" +
                                simpleCommand1.getCommand() + ": " + simpleCommand1.getOutput() + "\n" +
                                simpleCommand2.getCommand() + ": " + simpleCommand2.getOutput() + "\n")
                        .show();
            }
        });

        rootView.addView(button);

        return rootView;
    }
}
