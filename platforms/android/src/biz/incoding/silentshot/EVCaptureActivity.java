package biz.incoding.silentshot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eyeverify.evserviceinterface.aidl.data.EVServiceHelper;
import com.eyeverify.evserviceinterface.client.EVEnrollCompletion;
import com.eyeverify.evserviceinterface.client.EVRegisterCompletion;
import com.eyeverify.evserviceinterface.client.EVServiceBusyException;
import com.eyeverify.evserviceinterface.client.EVServiceClient;
import com.eyeverify.evserviceinterface.client.EVServiceException;
import com.eyeverify.evserviceinterface.client.EVServiceListener;
import com.eyeverify.evserviceinterface.client.EVVerifyCompletion;
import com.eyeverify.evserviceinterface.client.base.EVServiceProperties;
import com.eyeverify.evserviceinterface.client.event.EVEyeRegionsChangedEvent;
import com.eyeverify.evserviceinterface.client.event.EVEyeRegionsChangedListener;
import com.eyeverify.evserviceinterface.client.event.EVEyeStatusChangedEvent;
import com.eyeverify.evserviceinterface.client.event.EVEyeStatusChangedListener;
import com.eyeverify.evserviceinterface.constants.EVEnums;
import com.eyeverify.evserviceinterface.constants.EVEvents;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

//import android.widget.Toast;

public class EVCaptureActivity extends BaseActivity {

    public static final String TAG = EVCaptureActivity.class.getSimpleName();

    public static final String SERVICE_PACKAGE_KEY = "SERVICE_PACKAGE";
    public static final String IS_ENROLLMENT_KEY = "IS_ENROLLMENT";
    public static final String USER_ID_KEY = "USER_ID";
    public static final String USERKEY_KEY = "USER_KEY";

    private enum MESSAGE_STATE {ALERT, NEW_SESSION, ERROR, ABORT}

    private static final String SHARED_PREFERENCES = "EVServiceSampleActivity-SharedPreferences";
    private static final String PUBLIC_KEY_PREFERENCE = "publicKey";

    private EVServiceClient mServiceClient;
    private byte[] mNonce;
    boolean isEnrollment = false;
    String userID;
    String userKey;

    boolean isMidSession;
    boolean hasLaunched;

    private ViewGroup service_window;

    ProgressBar enroll_progress;

    ViewGroup service_overlay;

    int overlay_width;
    int overlay_height;
    int overlay_top;
    int overlay_left;


    TargetRectangle target_box;
    TextView counter_text;
    TextView capture_notification_text;

    String servicePackageName;

    int mStep, mTotalSteps, mCounter, killSwitchTimeOut;

    Handler mIdleTimer;
    Runnable mIdleRunnable;

    private EVEnums.EyeStatus currentEyeStatus = EVEnums.EyeStatus.None;

    private int currentResult = RESULT_CANCELED;
    private String currentResultString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IrisAccess.evCaptureActivity = this;

        setContentView(getResources().getIdentifier("activity_capture", "layout", getPackageName()));

        findViewById(android.R.id.content).getRootView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                //event.setLocation(event.getX());
                IrisAccess._cordova.findViewById(android.R.id.content).getRootView().dispatchTouchEvent(event);
                return true;
            }
        });


        service_window = (ViewGroup) findViewById(getResources().getIdentifier("capture_window", "id", getPackageName()));
        mServiceClient = new EVServiceClient(mListener, new EVServiceProperties("1DBRJYSHENYXWOK0"));
        //mServiceClient = new EVServiceClient(mListener, new EVServiceProperties(BaseActivity.readLicenseCertificate()));

        Intent intent = getIntent();


        servicePackageName = intent.getStringExtra(SERVICE_PACKAGE_KEY);
        if (servicePackageName == null) {
            servicePackageName = getBaseContext().getPackageName();
        }
        isEnrollment = intent.getBooleanExtra(IS_ENROLLMENT_KEY, false);
        userID = intent.getStringExtra(USER_ID_KEY);
        userKey = intent.getStringExtra(USERKEY_KEY);

        mIdleRunnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.putExtra("result", currentResultString);
                setResult(currentResult, intent);
                finish();
            }
        };

        enroll_progress = (ProgressBar) findViewById(getResources().getIdentifier("capture_enroll_progress", "id", getPackageName()));


//        <item android:id="@android:id/background">
//        <shape>
//        <gradient
//        android:startColor="#ddd"
//        android:endColor="#ddd"
//        android:angle="0"
//                />
//        </shape>
//        </item>
//
//        android:id="@android:id/progress">
//        <clip>
//        <shape>
//        <gradient
//        android:startColor="#84B533"
//        android:endColor="#84B533"
//        android:centerY="0.25"
//        android:angle="0" />
//        </shape>
//        </clip>
//        </item>

        Drawable background = new ColorDrawable(0xFFDDDDDD);
        Drawable progress = new ColorDrawable(0xFF84B533);
        ClipDrawable clipProgress = new ClipDrawable(progress, Gravity.LEFT,
                ClipDrawable.HORIZONTAL);

        LayerDrawable layerlist = new LayerDrawable(new Drawable[]{
                background, clipProgress});
        layerlist.setId(0, android.R.id.background);
        layerlist.setId(1, android.R.id.progress);

        enroll_progress.setProgressDrawable(layerlist);

        service_overlay = (ViewGroup) findViewById(getResources().getIdentifier("capture_overlay", "id", getPackageName()));

        target_box = (TargetRectangle) findViewById(getResources().getIdentifier("capture_target_box", "id", getPackageName()));
        counter_text = (TextView) findViewById(getResources().getIdentifier("capture_counter_text", "id", getPackageName()));

        capture_notification_text = (TextView) findViewById(getResources().getIdentifier("capture_notification_text", "id", getPackageName()));
    }

    @Override
    protected void onResume() {
        super.onResume();

        configureProgressBar(0, 10, 0);

        if (isMidSession) {
            Intent intent = new Intent();
            intent.putExtra("result", "Incomplete process, you will have to retry.");
            setResult(currentResult, intent);
            finish();
        } else if (!hasLaunched) {
            //must occur after window is available
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        hasLaunched = true;
                        mServiceClient.connect(servicePackageName, getBaseContext(), service_window);

                    } catch (Throwable ex) {
                        String msg = "Failed to connect.";
                        Log.e(TAG, msg, ex);
                    }
                }
            }, 100);
        } else {
            Intent intent = new Intent();
            intent.putExtra("result", "Failed to connect.");
            setResult(currentResult, intent);
            finish();//launched and finished, but maybe a phone call afterwards while sitting on finish screen

        }
    }

    private void doAuth() {
        try {
            Log.d(TAG, "EVCaptureActivity.doAuth");

            if (isEnrollment) {
                mServiceClient.enrollUser(userID, userKey.getBytes());
            } else {
                mNonce = EVServiceHelper.generate(8).getBytes();
                mServiceClient.verifyUser(userID, mNonce);
            }

            killSwitchTimeOut = mServiceClient.getSetting("kill_switch_abort_timeout", Integer.class);

        } catch (EVServiceException e) {
            //Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            currentResultString = e.getMessage();
            Intent intent = new Intent();
            intent.putExtra("result", currentResultString);
            setResult(currentResult, intent);
            finish();
            e.printStackTrace();
        } catch (EVServiceBusyException e) {
            //Toast.makeText(getApplicationContext(), "Cannot continue, currently busy", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.putExtra("result", "Cannot continue, currently busy");
            setResult(currentResult, intent);
            finish();
        }
    }

    private void startInactivityTimer() {

        mIdleTimer = new Handler();
        mIdleTimer.postDelayed(mIdleRunnable, 1000 * (killSwitchTimeOut != 0 ? killSwitchTimeOut : 180));
    }

    private void cancelInactivityTimer() {
        if (mIdleTimer != null) {
            mIdleTimer.removeCallbacks(mIdleRunnable);
        }
    }

    protected void onPause() {
        removeVideoOverlays();

        cancelInactivityTimer();

        try {
            mServiceClient.releaseCamera();
            mServiceClient.disconnect();
        } catch (Throwable ex) {
            String msg = "Failed to disconnect.";
            Log.e(TAG, msg, ex);
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        removeVideoOverlays();
        super.onDestroy();
    }

    private synchronized void addVideoOverlays() {

//        View parent = (View) service_overlay.getParent();
        if (0 == overlay_width) {
            overlay_width = service_overlay.getWidth();
            overlay_height = service_overlay.getHeight();
            overlay_top = service_overlay.getTop();
            overlay_left = service_overlay.getLeft();

            ((ViewGroup) service_overlay.getParent()).removeView(service_overlay);
        }
        DisplayMetrics metrics = new DisplayMetrics();
//        ViewGroup.LayoutParams p = parent.getLayoutParams();
//        p.height = (int) (120*metrics.density);

//        parent.setLayoutParams(p);


        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        overlay_top = (int) (100 * metrics.density);
        WindowManager windowManager = (WindowManager) getBaseContext().getSystemService(Context.WINDOW_SERVICE);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(overlay_width, overlay_height, overlay_left, overlay_top, WindowManager.LayoutParams.TYPE_TOAST, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.RGBA_8888);
        params.gravity = Gravity.TOP | Gravity.CENTER;

        try {
            windowManager.addView(service_overlay, params);
        } catch (Exception e) {
            //already added
        }


        service_overlay.setVisibility(View.VISIBLE);


        target_box.setTargetSuccess(false);
        target_box.startScanning();
    }

    private synchronized void removeVideoOverlays() {

        WindowManager windowManager = (WindowManager) getBaseContext().getSystemService(Context.WINDOW_SERVICE);

        try {
            windowManager.removeViewImmediate(service_overlay);
        } catch (Exception e) {
        }
    }


    void reconfigureProgressBar() {
        configureProgressBar(mStep, mTotalSteps, mCounter);
    }

    private void configureProgressBar(int step, int totalSteps, int counter) {

        mStep = step;
        mTotalSteps = totalSteps;
        mCounter = counter;

        enroll_progress.setMax(100);

        int value = 0;

        if (step <= 0) {
            value = 0;
        } else if (step >= totalSteps) {
            value = enroll_progress.getMax();
        } else {
            value = step * enroll_progress.getMax() / totalSteps;
        }

        enroll_progress.setProgress(value);

        if (value == enroll_progress.getMax()) {

            capture_notification_text.setTextColor(Color.parseColor("#FF555555"));
        } else {
            capture_notification_text.setTextColor(Color.parseColor("#FF555555"));
        }

        if (counter < 1 || value == enroll_progress.getMax()) {
            counter_text.setVisibility(View.GONE);
        } else {
            counter_text.setVisibility(View.VISIBLE);
            counter_text.setText("" + counter);
        }
    }

    private void clearAllTexts() {
        capture_notification_text.setText("");
    }

    private void resumeAuth() {
        clearAllTexts();
        target_box.startScanning();
        counter_text.setVisibility(View.VISIBLE);
    }

    private class EVListener implements EVServiceListener, EVEyeRegionsChangedListener, EVEyeStatusChangedListener {

        @Override
        public void registrationCompleted(EVRegisterCompletion completion) {

        }

        @Override
        public void onServiceAvailable() {
            doAuth();
        }

        @Override
        public void onWindowAdded() {
            addVideoOverlays();
        }

        @Override
        public void onWindowRemoved() {
            //do something probably
            removeVideoOverlays();
        }

        @Override
        public void onWindowFailure() {
            Intent intent = new Intent();
            intent.putExtra("result", "Camera unavailable. Please restart the device.");
            setResult(currentResult, intent);
            finish();
        }

        @Override
        public void handleEvent(EVEyeRegionsChangedEvent event) {
            // Log.d(TAG, "Handle EVEyeRegionsChangedEvent: previewLeftX=" + event.getOriginalPreviewLeftX() + "; previewLeftY=" + event.getOriginalPreviewLeftY() + "; previewLeftW=" + event.getOriginalPreviewLeftW() + "; previewLeftH=" + event.getOriginalPreviewLeftH());

            if (currentEyeStatus == EVEnums.EyeStatus.None) {
                return;
            }


            Integer leftX = event.getPreviewLeftX();
            Integer leftY = event.getPreviewLeftY();
            Integer leftW = event.getPreviewLeftW();
            Integer leftH = event.getPreviewLeftH();

            Integer rightX = event.getPreviewRightX();
            Integer rightY = event.getPreviewRightY();
            Integer rightW = event.getPreviewRightW();
            Integer rightH = event.getPreviewRightH();

            if (leftH != null && leftW != null && leftX != null && leftY != null
                    && rightX != null && rightY != null && rightW != null && rightH != null) {

                int screenW = service_window.getWidth();
                int screenH = service_window.getHeight();

                RelativeLayout.LayoutParams leftLayoutParams = new RelativeLayout.LayoutParams(service_window.getLayoutParams());
                leftLayoutParams.setMargins(leftX, leftY, screenW - (leftX + leftW), screenH - (leftY + leftH));

                RelativeLayout.LayoutParams rightLayoutParams = new RelativeLayout.LayoutParams(service_window.getLayoutParams());
                rightLayoutParams.setMargins(rightX, rightY, screenW - (rightX + rightW), screenH - (rightY + rightH));
            }
        }

        @Override
        public void handleEvent(EVEyeStatusChangedEvent event) {
            currentEyeStatus = event.getEyeStatus();
            Log.d(TAG, "Handle EVEyeStatusChangedEvent: status=" + currentEyeStatus);

            if (currentEyeStatus == null) return;

            target_box.setTargetSuccess(false);

            switch (currentEyeStatus) {
                case Okay:
                    target_box.setTargetSuccess(true);
                    capture_notification_text.setText(null);
                    currentResult = RESULT_OK;
                    break;
                case NoEye:
                    capture_notification_text.setText("Position your eyes in the window");
                    break;
                case TooClose:
                case TooFarAway:
                    capture_notification_text.setText("Move device closer");
                    break;
                case NoGaze:
                    break;
                default:
                    break;
            }
            cancelInactivityTimer();
        }

        @Override
        public void handleEvent(int eventCode, Map<String, String> params) {

            switch (eventCode) {
                case EVEvents.DispatchEvent_EnrollmentStarted: {
                    clearAllTexts();

                    isMidSession = true;
                    break;
                }
                case EVEvents.DispatchEvent_EnrollmentSessionStarted: {
                    int counter = EVEvents.getParameter(EVEvents.kEnrollmentCounter, params, Integer.class);
                    counter_text.setText(counter + "");

                    resumeAuth();

                    cancelInactivityTimer();
                    break;
                }
                case EVEvents.DispatchEvent_EnrollmentSessionCompleted: {

                    boolean enrollmentEnding = EVEvents.getParameter(EVEvents.kEnrollmentEnding, params, Boolean.class);

                    if (!enrollmentEnding) {
                        changeMessageState(MESSAGE_STATE.NEW_SESSION);
                    }

                    counter_text.setVisibility(View.GONE);

                    target_box.setTargetSuccess(false);
                    target_box.stopScanning();

                    startInactivityTimer();
                    break;
                }
                case EVEvents.DispatchEvent_EnrollmentStepCompleted: {

                    int step = EVEvents.getParameter(EVEvents.kEnrollmentStepCompleted, params, Integer.class);
                    int totalSteps = EVEvents.getParameter(EVEvents.kEnrollmentTotalSteps, params, Integer.class);
                    int counter = EVEvents.getParameter(EVEvents.kEnrollmentCounter, params, Integer.class);

                    configureProgressBar(step, totalSteps, counter);

                    break;
                }
                case EVEvents.DispatchEvent_EnrollmentCompleted: {
                    counter_text.setVisibility(View.GONE);
                    isMidSession = false;

                    cancelInactivityTimer();
                    break;
                }
                case EVEvents.DispatchEvent_VerificationStarted: {
                    clearAllTexts();
                    isMidSession = true;
                    configureProgressBar(0, 10, 0);

                    cancelInactivityTimer();
                    break;
                }
                case EVEvents.DispatchEvent_VerificationCompleted: {
                    target_box.stopScanning();
                    isMidSession = false;

                    startInactivityTimer();
                    break;
                }
                default:

            }
        }

        @Override
        public void enrollmentCompleted(EVEnrollCompletion completion) {
            try {


                Log.d(TAG, "Starting enrollmentCompleted: success=" + completion.isSuccess() + "; user_key=" + new String(completion.getUserKey()));

                if (completion.isSuccess()) {
                    currentResult = RESULT_OK;
                    currentResultString = EVServiceHelper.data2string(completion.getEncodedPublicKey());
                    Log.d(TAG, "Storing public key: encodedPublicKey=" + EVServiceHelper.data2string(completion.getEncodedPublicKey()));

                    SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES, Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    if (completion.getEncodedPublicKey() != null && completion.getEncodedPublicKey().length > 0) {
                        editor.putString(PUBLIC_KEY_PREFERENCE, EVServiceHelper.data2string(completion.getEncodedPublicKey()));
                    } else {
                        editor.remove(PUBLIC_KEY_PREFERENCE);
                    }
                    editor.apply();

                    capture_notification_text.setText("Great, we got it.");
                    Intent intent = new Intent();
                    intent.putExtra("result", currentResultString);
                    setResult(currentResult, intent);
                    finish();
                } else if (completion.isIncomplete()) {
                    capture_notification_text.setText("We could not enroll you");
                    currentResultString = "We could not enroll you";
                    currentResult = RESULT_CANCELED;
                    Intent intent = new Intent();
                    intent.putExtra("result", currentResultString);
                    setResult(currentResult, intent);
                    finish();
                } else if (completion.wasAborted()) {
                    if (SharedGlobals.isLicensingError(completion.getAbortResult())) {
                        BaseActivity.deleteLicenseCertificate();

                        Intent intent = new Intent();
                        intent.putExtra("result", "LICENSE ERROR");
                        setResult(currentResult, intent);

                        finish();
                    }

                    showAbortMessages(completion.getAbortResult());
                    return;
                }

                switch (completion.getEnrollmentResult()) {
                    case bad_quality:
                        showError(SharedGlobals.ERROR_TYPE.QUALITY);
                        break;
                    case error:
                        showError(SharedGlobals.ERROR_TYPE.SYSTEM);
                        break;
                    case http_error:
                        showError(SharedGlobals.ERROR_TYPE.INTERNET);
                        break;
                    case bad_match:
                        showError(SharedGlobals.ERROR_TYPE.ENROLLMENT_MATCH);
                        break;
                    case zero_images:
                    case no_eyes:
                        showError(SharedGlobals.ERROR_TYPE.NO_EYE);
                        break;
                    case low_lighting:
                        showError(SharedGlobals.ERROR_TYPE.LOW_LIGHTING);
                        break;
                    default:
                        showError(SharedGlobals.ERROR_TYPE.NOT_ENROLLED); //show default error message
                        break;
                }

                Log.d(TAG, "Finished enrollmentCompleted: success=" + completion.isSuccess());

            } catch (Throwable ex) {
                Log.e(TAG, "Failed to complete enrollment.", ex);
            }
        }

        @Override
        public void verificationCompleted(EVVerifyCompletion completion) {
            try {

                Log.d(TAG, "Starting verificationCompleted: success=" + completion.isSuccess() + "; user_key=" + new String(completion.getUserKey()));

                boolean signatureVerify = false;
                if (completion.isSuccess()) {

                    if (completion.getSignedNonce() != null && completion.getSignedNonce().length > 0) {
                        Log.d(TAG, "Verifying signature: mNonce=" + EVServiceHelper.data2string(mNonce) + "; signedNonce=" + EVServiceHelper.data2string(completion.getSignedNonce()));

                        SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES, Activity.MODE_PRIVATE);
                        byte[] encodedPublicKey = EVServiceHelper.string2data(prefs.getString(PUBLIC_KEY_PREFERENCE, null));
                        if (encodedPublicKey == null || encodedPublicKey.length == 0) {
                            Log.w(TAG, "Missing public key. Please register first.");
                            return;
                        }

                        X509EncodedKeySpec spec = new X509EncodedKeySpec(encodedPublicKey);
                        KeyFactory kf = KeyFactory.getInstance("RSA");
                        PublicKey publicKey = kf.generatePublic(spec);

                        Signature instance = Signature.getInstance("SHA1withRSA");
                        instance.initVerify(publicKey);
                        instance.update(mNonce);

                        signatureVerify = instance.verify(completion.getSignedNonce());
                    } else {
                        Log.d(TAG, "Skipped signature verify because registrtaion is disabled on the service side.");
                    }

                    capture_notification_text.setText("Verified");
                } else if (completion.wasAborted()) {
                    showAbortMessages(completion.getAbortResult());
                    currentResult = RESULT_CANCELED;
                    Intent intent = new Intent();
                    intent.putExtra("result", completion.getAbortResult());
                    setResult(currentResult, intent);
                    finish();

                    return;
                } else {

                    switch (completion.getVerificationResult()) {
                        case bad_quality:
                            showError(SharedGlobals.ERROR_TYPE.QUALITY);
                            break;
                        case error:
                            showError(SharedGlobals.ERROR_TYPE.SYSTEM);
                            break;
                        case http_error:
                            showError(SharedGlobals.ERROR_TYPE.INTERNET);
                            break;
                        case zero_images:
                            showError(SharedGlobals.ERROR_TYPE.NO_EYE);
                            break;
                        default:
                            showError(SharedGlobals.ERROR_TYPE.NOT_VERIFIED); //show default error message
                            break;
                    }

                }


                Log.d(TAG, "Finished verifyCompleted: success=" + completion.isSuccess() + "; signatureVerify=" + signatureVerify);
                if (completion.isSuccess()) {
                    currentResult = RESULT_OK;
                    currentResultString = EVServiceHelper.data2string(completion.getSignedNonce());
                    Intent intent = new Intent();
                    intent.putExtra("result", currentResultString);
                    setResult(currentResult, intent);
                    finish();
                }
            } catch (Throwable ex) {
                Log.e(TAG, "Failed to complete verification.", ex);
            }
        }
    }

    ;
    private EVServiceListener mListener = new EVListener();

    private void showError(SharedGlobals.ERROR_TYPE theError) {

        MESSAGE_STATE message_state = MESSAGE_STATE.ERROR;

        currentResult = RESULT_CANCELED;
        switch (theError) {
            case NOT_ENROLLED:
                currentResultString = "Please keep still while scanning";
                break;
            case DISTANCE:
                currentResultString = "We don’t have the distance right";
                break;
            case NO_EYE:
                currentResultString = "We are not seeing your eyes";
                break;
            case QUALITY:
                currentResultString = "Keep Scanning";
                break;
            case SYSTEM:
                currentResultString = "System error";
                break;
            case LICENSE_INVALID:
                message_state = MESSAGE_STATE.ABORT;
                currentResultString = getString(getResources().getIdentifier("capture_license_invalid_suggestion", "string", getPackageName()));
                break;
            case LICENSE_EXPIRED:
                message_state = MESSAGE_STATE.ABORT;
                currentResultString = getString(getResources().getIdentifier("capture_license_expired_suggestion", "string", getPackageName()));
                break;
            case LICENSE_LIMITED:
                message_state = MESSAGE_STATE.ABORT;
                currentResultString = getString(getResources().getIdentifier("capture_license_limited_suggestion", "string", getPackageName()));
                break;
            case INTERNET:
                message_state = MESSAGE_STATE.ABORT;
                currentResultString = "Unable to access the internet";
                break;
            case ENROLLMENT_MATCH:
                currentResultString = "Keep scanning";
                break;
            case CHAFF:
                currentResultString = "Server data not found";
                break;
            case APP_BACKGROUND:
                currentResultString = "Scan not completed";
                break;
            case LOW_LIGHTING:
                currentResultString = "The lighting is insufficient for the current operation";
                break;
            case NOT_SUPPORTED:
                message_state = MESSAGE_STATE.ABORT;
                currentResultString = "EyeVerify does not currently support this device";
                break;
            default:
                currentResultString = mServiceClient.isEnrollment() ?
                        "Please keep still while scanning" :
                        "Not Verified";
                break;
        }

        changeMessageState(message_state);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent();
        intent.putExtra("result", currentResultString);
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void changeMessageState(MESSAGE_STATE messageState) {

        counter_text.setVisibility(View.GONE);

        //only change what differs from defaults above
        switch (messageState) {
            case ALERT:

                break;
            case NEW_SESSION:

                try {
                    reconfigureProgressBar();
                    mServiceClient.continueAuth();
                    resumeAuth();
                } catch (Throwable ex) {
                    String msg = "Failed to continue.";
                    Log.e(TAG, msg, ex);
                    currentResultString = msg;
                }
                break;
            case ERROR:

                break;
            case ABORT:

                break;
            default:
        }
    }

    private void showAbortMessages(EVEnums.abort_reason abortReason) {

        switch (abortReason) {
            case license_invalid:
                showError(SharedGlobals.ERROR_TYPE.LICENSE_INVALID);
                break;
            case license_expired:
                showError(SharedGlobals.ERROR_TYPE.LICENSE_EXPIRED);
                break;
            case license_limited:
                showError(SharedGlobals.ERROR_TYPE.LICENSE_LIMITED);
                break;
            case internet_required:
                showError(SharedGlobals.ERROR_TYPE.INTERNET);
                break;
            case app_background:
                showError(SharedGlobals.ERROR_TYPE.APP_BACKGROUND);
                break;
            case abort_low_lighting:
                showError(SharedGlobals.ERROR_TYPE.LOW_LIGHTING);
                break;
            case unsupported_device:
                showError(SharedGlobals.ERROR_TYPE.NOT_SUPPORTED);
                break;
            case system_timeout:
                showError(SharedGlobals.ERROR_TYPE.NO_EYE);
                break;
            default: {
                switch (currentEyeStatus) {
                    case NoEye:
                        showError(SharedGlobals.ERROR_TYPE.NO_EYE);
                        break;
                    case TooClose:
                    case TooFarAway:
                        showError(SharedGlobals.ERROR_TYPE.DISTANCE);
                        break;
                    default:
                        showError(mServiceClient.isEnrollment() ? SharedGlobals.ERROR_TYPE.NOT_ENROLLED : SharedGlobals.ERROR_TYPE.NOT_VERIFIED);
                        break;
                }
                break;
            }
        }
    }
}
