package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MyService extends Service {
    private final int CYCLES_COUNT = 10;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private MyBinder myBinder = new MyBinder();
    private OnValueChange onValueChange;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        executorService.submit(new Runnable() {
            @Override
            public void run() {

                Random random = new Random();
                int randomValue;
                int cycles = CYCLES_COUNT;

                while (cycles-- > 0) {
                    random.nextInt();
                    randomValue = random.nextInt(100);

                    onValueChange.onChange(randomValue);

                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                stopSelf();
            }
        });

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class MyBinder extends Binder {
        public MyService getService() {
            return MyService.this;
        }
    }

    public void makeJob() {
        Toast.makeText(this, getResources().getString(R.string.service_method_called),
                Toast.LENGTH_SHORT).show();
    }

    public interface OnValueChange {
        void onChange(int value);
    }

    public void setOnValueChange(OnValueChange onValueChange) {
        this.onValueChange = onValueChange;
    }
}
