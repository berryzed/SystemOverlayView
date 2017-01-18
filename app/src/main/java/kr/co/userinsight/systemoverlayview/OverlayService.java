package kr.co.userinsight.systemoverlayview;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;

public class OverlayService extends Service implements
        View.OnTouchListener, View.OnClickListener, View.OnLongClickListener {

    private final int SVC_NOTI_ID = 101;
    private final String ACTION_STOP = "stop";

    private Context mContext;
    private WindowManager mWindowManager;
    private View mView;
    private WindowManager.LayoutParams mViewParams;
    private FrameLayout mBackgroundLayout;
    private WindowManager.LayoutParams mBackgroundParams;

    private Vibrator mVibrator;
    private Notification mNotification;
    private NotificationCompat.Builder mNotiBuilder;

    private boolean mTouchEventEnabled;

    private float mInitialTouchX;
    private float mInitialTouchY;
    private int mInitialX;
    private int mInitialY;

    @Override
    public void onCreate() {
        super.onCreate();

        this.mContext = getApplicationContext();

        // onStartCommand에서의 중복된 뷰 생성 방지를 위해 onCreate에서 구현
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // View setting
        mView = inflater.inflate(R.layout.view_overlay, null);

        Button callBtn = (Button) mView.findViewById(R.id.emer_call_btn);
        callBtn.setOnClickListener(this);
        callBtn.setOnLongClickListener(this);
        callBtn.setOnTouchListener(this);

        Button endBtn = (Button) mView.findViewById(R.id.end_btn);
        endBtn.setOnClickListener(this);

        LinearLayout containerLayout = (LinearLayout) mView.findViewById(R.id.container_layout);
        containerLayout.setOnLongClickListener(this);
        containerLayout.setOnTouchListener(this);

        mViewParams = new WindowManager.LayoutParams(
                WRAP_CONTENT,
                WRAP_CONTENT,
                TYPE_SYSTEM_ERROR,
                FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        // TODO 메인 뷰가 WRAP_CONTENT로 설정되어 있으면 중앙 하단에 위치 시킬 수 있는 방법 연구
        mViewParams.gravity = Gravity.TOP | Gravity.START;

        mWindowManager.addView(mView, mViewParams);

        // 롱 클릭 시 버튼 뒷 배경을 검게한다.
        mBackgroundLayout = new FrameLayout(mContext);
        mBackgroundLayout.setBackgroundColor(Color.argb(150, 0, 0, 0));
        mBackgroundParams = new WindowManager.LayoutParams(
                MATCH_PARENT,
                MATCH_PARENT,
                TYPE_SYSTEM_OVERLAY,
                FLAG_NOT_FOCUSABLE | FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        startNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_STOP:
                    Toast.makeText(mContext, "The service has been stopped.", Toast.LENGTH_SHORT).show();
                    stopForeground(true);
                    stopSelf();
                    break;
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        mWindowManager.removeView(mView);
    }

    private void startNotification() {
        Intent stopIntent = new Intent(this, OverlayService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, 0);

        mNotiBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("The service is running.")
                .setTicker("The service has been started.")
                .setSmallIcon(R.drawable.ic_directions_walk)
                .setOngoing(true)
                .addAction(R.drawable.ic_close, "Stop", stopPendingIntent);

        mNotification = mNotiBuilder.build();

        startForeground(SVC_NOTI_ID, mNotification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.emer_call_btn:
                Toast.makeText(mContext, "Call!", Toast.LENGTH_SHORT).show();
                mNotiBuilder.setTicker(null);
                mNotiBuilder.setSmallIcon(R.drawable.ic_close);
                mNotification = mNotiBuilder.build();
                startForeground(SVC_NOTI_ID, mNotification);
                break;

            case R.id.end_btn:
                Toast.makeText(mContext, "Stop!", Toast.LENGTH_SHORT).show();
                stopForeground(true);
                stopSelf();
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        // 단말기 설정에서 햅틱반응이 Off 일 경우 동작하지 않음
        // v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);

        mVibrator.vibrate(50);
        mWindowManager.addView(mBackgroundLayout, mBackgroundParams);
        mTouchEventEnabled = true;
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInitialX = mViewParams.x;
                mInitialY = mViewParams.y;
                mInitialTouchX = event.getRawX();
                mInitialTouchY = event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                if (mTouchEventEnabled) {
                    mViewParams.x = (mInitialX + (int) (event.getRawX() - mInitialTouchX));
                    mViewParams.y = (mInitialY + (int) (event.getRawY() - mInitialTouchY));
                    mWindowManager.updateViewLayout(mView, mViewParams);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mTouchEventEnabled) {
                    mWindowManager.removeView(mBackgroundLayout);
                    mTouchEventEnabled = false;
                    return true;
                }
                break;
        }
        return false;
    }
}
