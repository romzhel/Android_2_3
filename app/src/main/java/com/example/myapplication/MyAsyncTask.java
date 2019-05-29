package com.example.myapplication;

import android.os.AsyncTask;

import java.util.concurrent.TimeUnit;

public class MyAsyncTask extends AsyncTask<Integer, Integer, Integer> {
    private final int COUNT_AMOUNT = 100;
    private final int RAISE_DEGREE = 2;
    private int count;
    private int result;
    private OnMyAsyncTaskListener listener;

    public MyAsyncTask(OnMyAsyncTaskListener listener) {
        this.listener = listener;
    }

    @Override
    protected Integer doInBackground(Integer... integers) {
        for (count = 0; count < COUNT_AMOUNT; count++) {

            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            result += Math.pow(integers[0] + count, RAISE_DEGREE);

            publishProgress(++count);
        }

        return result;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        listener.onComplete(integer);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        listener.onStatusProgress(count);
    }

    public interface OnMyAsyncTaskListener {
        void onStatusProgress(int count);

        void onComplete(int result);
    }

    public void setListener(OnMyAsyncTaskListener listener) {
        this.listener = listener;
    }

    public int getResult() {
        return result;
    }
}
