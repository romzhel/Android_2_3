package com.example.myapplication;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private final int CALCULATING_PARAMETER = 50;
    private final int TASK_PROGRESS_FINISHED = 100;
    private final String TASK_RESULT = "taskResult";
    private int taskResult = -1;
    private ProgressBar progressBar;
    private TextView statusTextView;
    private TextView resultTextView;
    private TextView serviceTextView;
    private MyAsyncTask myAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        checkExistingTask();
    }

    private void initViews() {
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        statusTextView = (TextView) findViewById(R.id.status_text);
        resultTextView = (TextView) findViewById(R.id.result_text);
        serviceTextView = (TextView) findViewById(R.id.service_value);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runAsyncTask();
            }
        });

        //------------------------------------------------------------------------------------
        findViewById(R.id.btn_run_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(MainActivity.this, MyService.class));

            }
        });
        findViewById(R.id.btn_stop_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, MyService.class));
            }
        });

        findViewById(R.id.btn_run_single_task).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bound) myService.makeJob();
            }
        });
    }

    private void checkExistingTask() {
        MyAsyncTask existingTask = (MyAsyncTask) getLastCustomNonConfigurationInstance();
        if (existingTask != null) {
            if (existingTask.getStatus() == AsyncTask.Status.RUNNING) {

                myAsyncTask = existingTask;
                myAsyncTask.setListener(initMyAsyncTaskListener());
                setTaskResult(getResources().getString(R.string.result_caption_calculating));
                setTaskStatus(getResources().getString(R.string.task_started));

            } else if (existingTask.getStatus() == AsyncTask.Status.FINISHED) {

                setTaskProgress(TASK_PROGRESS_FINISHED);
                setTaskStatus(getResources().getString(R.string.task_finished));

            }
        }
    }

    private void runAsyncTask() {
        if (myAsyncTask == null) {

            taskResult = -1;

            myAsyncTask = new MyAsyncTask(initMyAsyncTaskListener());
            myAsyncTask.execute(CALCULATING_PARAMETER);

            setTaskStatus(getResources().getString(R.string.task_started));
            setTaskResult(getResources().getString(R.string.result_caption_calculating));

        } else {

            Toast.makeText(this, getResources().getString(R.string.task_running),
                    Toast.LENGTH_SHORT).show();

        }
    }

    private MyAsyncTask.OnMyAsyncTaskListener initMyAsyncTaskListener() {
        MyAsyncTask.OnMyAsyncTaskListener myAsyncTaskListener = new MyAsyncTask.OnMyAsyncTaskListener() {
            @Override
            public void onStatusProgress(int count) {
                setTaskProgress(count);
            }

            @Override
            public void onComplete(int result) {
                setTaskResult(result);
                setTaskStatus(getResources().getString(R.string.task_finished));
                taskResult = result;
            }
        };

        return myAsyncTaskListener;
    }


    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        if (myAsyncTask != null) return myAsyncTask;

        return super.onRetainCustomNonConfigurationInstance();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(TASK_RESULT, taskResult);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            taskResult = savedInstanceState.getInt(TASK_RESULT);

            if (taskResult > 0) {
                setTaskStatus(getResources().getString(R.string.task_finished));
                setTaskResult(taskResult);
            }
        }
    }

    private void setTaskProgress(int count) {
        if (progressBar != null) {
            progressBar.setProgress(count);
        }
    }

    private void setTaskStatus(String status) {
        if (statusTextView != null) {
            statusTextView.setText(status);
        }
    }

    private void setTaskResult(int result) {
        if (resultTextView != null) {
            resultTextView.setText(String.format(getResources().getString(R.string.result_caption), result));
        }
    }

    private void setTaskResult(String text) {
        if (resultTextView != null) {
            resultTextView.setText(text);
        }
    }

    //service part ------------------------------------------------------------------------------
    private MyService myService;
    private boolean bound;

    private ServiceConnection sConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService = ((MyService.MyBinder) service).getService();
            bound = true;

            myService.setOnValueChange(new MyService.OnValueChange() {
                @Override
                public void onChange(int value) {

                    final int fValue = value;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (serviceTextView != null) {
                                serviceTextView.setText(String.valueOf(fValue));
                            }
                        }
                    });
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        bindService(new Intent(MainActivity.this, MyService.class), sConn, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unbindService(sConn);
    }
}
