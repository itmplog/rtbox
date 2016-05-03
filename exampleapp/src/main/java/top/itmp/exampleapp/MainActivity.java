package top.itmp.exampleapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import top.itmp.rtbox.RTBox;
import top.itmp.rtbox.Shell;
import top.itmp.rtbox.command.SimpleCommand;

public class MainActivity extends AppCompatActivity {

    private ScrollView scrollView;
    private TextView content;
    private EditText execContent;
    private Button exec;
    private Button superExec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scrollView = (ScrollView)findViewById(R.id.scrollView);
        content = (TextView)findViewById(R.id.textView);
        execContent = (EditText)findViewById(R.id.execContent);
        exec = (Button)findViewById(R.id.exec);
        superExec = (Button)findViewById(R.id.execSuper);

        RTBox.DebugMode = true;

        exec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String execString = execContent.getText().toString();

                if(TextUtils.isEmpty(execString)){
                    Toast.makeText(getApplicationContext(), "Must not be empty", Toast.LENGTH_SHORT).show();
                }else{
                    try {
                        Shell shell = Shell.startShell();
                        SimpleCommand simpleCommand = new SimpleCommand(execString);
                        shell.add(simpleCommand).waitForFinish();

                        content.append(simpleCommand.getOutput() + simpleCommand.getExitCode());

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

    }
}
