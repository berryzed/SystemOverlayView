package kr.co.userinsight.systemoverlayview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onStartClick(View v) {
        if (startService(new Intent(this, OverlayService.class)) != null) {
            Log.i(TAG, "-----Started service!-----");
        }
    }

    public void onStopClick(View v) {
        if (stopService(new Intent(this, OverlayService.class))) {
            Log.i(TAG, "-----Stopped service!-----");
        }
    }
}
