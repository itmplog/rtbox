package top.itmp.rtbox.example;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import top.itmp.rtbox.RtBox;
import top.itmp.rtbox.Shell;
import top.itmp.rtbox.command.SimpleCommand;
import top.itmp.rtbox.utils.OnRootAccessDenied;

public class FragmentNormal extends Fragment {

    private ScrollView scrollView;
    private TextView content;
    private EditText execContent;
    private Button exec;
    private Button superExec;
    private Button checkRootAccess;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frament_normal, container, false);
        scrollView = (ScrollView)rootView.findViewById(R.id.scrollView);
        content = (TextView)rootView.findViewById(R.id.textView);
        execContent = (EditText)rootView.findViewById(R.id.execContent);
        exec = (Button)rootView.findViewById(R.id.exec);
        superExec = (Button)rootView.findViewById(R.id.execSuper);
        checkRootAccess = (Button)rootView.findViewById(R.id.checkRootAccess);

        RtBox.DebugMode = true;
        RtBox.DefaultCommandTimeout = 5000;

        exec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String execString = execContent.getText().toString();

                if(TextUtils.isEmpty(execString)){
                    Toast.makeText(getActivity(), "Must not be empty", Toast.LENGTH_SHORT).show();
                }else{
                    try {
                        Shell shell = Shell.startShell();
                        SimpleCommand simpleCommand = new SimpleCommand(execString);
                        shell.add(simpleCommand).waitForFinish();

                        content.append(simpleCommand.getOutput() +"return: " + simpleCommand.getExitCode());

                        scrollView.post(new Runnable() {
                            @Override
                            public void run() {
                                scrollView.fullScroll(View.FOCUS_DOWN);
                            }
                        });

                        shell.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }catch (TimeoutException e){
                        e.printStackTrace();
                    }
                }

            }
        });

        superExec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String execString = execContent.getText().toString();

                if (TextUtils.isEmpty(execString)) {
                    Toast.makeText(getActivity(), "Must not be empty", Toast.LENGTH_SHORT).show();
                } else {
                    boolean rootAccess = false;
                    try {

                        Shell shell = Shell.startRootShell(new OnRootAccessDenied(){
                            @Override
                            public void onDenied() {
                                new AlertDialog.Builder(getActivity())
                                        .setTitle("Root Access")
                                        .setMessage("Root Access Has Been Denied!!")
                                        .setPositiveButton(android.R.string.ok, null)
                                        .show();
                            }
                        });

                        rootAccess = shell.isRootAccessGranted();

                        //Shell shell = Shell.startRootShell();

                        SimpleCommand simpleCommand = new SimpleCommand(execString);

                        shell.add(simpleCommand).waitForFinish();

                        content.setText(simpleCommand.getOutput());

                        shell.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }catch (TimeoutException e){
                        e.printStackTrace();
                    }
                    Log.v(RtBox.TAG, "root: " + rootAccess);
                    if(!rootAccess){
                        Toast.makeText(getActivity(), "No Root Access Granted", Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            }
        });

        checkRootAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setTitle("Root Access")
                        .setPositiveButton(android.R.string.ok, null);
                if(RtBox.isRootAccessGranted()){
                    builder.setMessage("Granted")
                            .show();
                }else{
                    builder.setMessage("Not Granted")
                            .show();
                }
            }
        });

        return rootView;
    }
}
