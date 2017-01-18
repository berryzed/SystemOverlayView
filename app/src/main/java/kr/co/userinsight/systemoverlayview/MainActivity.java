package kr.co.userinsight.systemoverlayview;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();
    private final int REQUEST_OVERLAY = 101;

    private Button mStartBtn;
    private Button mStopBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStartBtn = (Button) findViewById(R.id.main_start_btn);
        mStopBtn = (Button) findViewById(R.id.main_stop_btn);

        setButtonsEnabled(false);

        // Android 6.0이상의 버전에서 [다른 앱 위에 그리기]허용을 확인한다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_OVERLAY);
        } else {
            setButtonsEnabled(true);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OVERLAY) {
            if (Settings.canDrawOverlays(this)) {
                setButtonsEnabled(true);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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

    private void setButtonsEnabled(boolean enabled) {
        mStartBtn.setEnabled(enabled);
        mStopBtn.setEnabled(enabled);
    }
}
