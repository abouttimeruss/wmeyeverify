package biz.incoding.silentshot;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.eyeverify.evserviceinterface.aidl.data.EVServiceHelper;
import com.eyeverify.evserviceinterface.client.EVEnrollCompletion;
import com.eyeverify.evserviceinterface.client.EVRegisterCompletion;
import com.eyeverify.evserviceinterface.client.EVServiceClient;
import com.eyeverify.evserviceinterface.client.EVServiceListener;
import com.eyeverify.evserviceinterface.client.EVVerifyCompletion;
import com.eyeverify.evserviceinterface.client.base.EVServiceProperties;
import com.eyeverify.evserviceinterface.client.event.EVEyeRegionsChangedEvent;
import com.eyeverify.evserviceinterface.client.event.EVEyeRegionsChangedListener;
import com.eyeverify.evserviceinterface.client.event.EVEyeStatusChangedEvent;
import com.eyeverify.evserviceinterface.client.event.EVEyeStatusChangedListener;
import com.eyeverify.evserviceinterface.constants.EVEnums;
import com.eyeverify.evserviceinterface.constants.EVEvents;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

/**
 * Created by home on 10.02.2016.
 */
public class IrisAccess  extends CordovaPlugin {
    public CallbackContext callbackContext;
    private EVServiceClient mServiceClient;
    private EVServiceListener mListener = new EVListener();
    String userID;
    String userKey;

    @Override
    public boolean execute(String action, JSONArray args,
                           CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;

        if (action.equalsIgnoreCase("GetIris")) {


//            -(void)setDefaults {
//                evLoader = [[EyeVerifyLoader alloc] init];
//                [evLoader loadEyeVerifyWithLicense:@"1DBRJYSHENYXWOK0"];
//
//                EyeVerify *ev = [EyeVerifyLoader getEyeVerifyInstance];
//                ev.userName = userNameFromOptions;
//
//                [ev setCaptureView:[[UIView alloc] initWithFrame:CGRectMake(0, 100, 320, 100)]];
//
//
//            }





            mServiceClient = new EVServiceClient(mListener, new EVServiceProperties("1DBRJYSHENYXWOK0"));



            if(args.length() > 0)
            {
                NSDictionary *arguments = args.;

                if(arguments[@"scanType"])
                {
                    NSInteger dest = [arguments[@"scanType"] integerValue];
                    scanType = dest;
                }
                if(arguments[@"userName"])
                {
                    userNameFromOptions = arguments[@"userName"];

                }
                if(arguments[@"userKey"])
                {
                    userKeyFromOptions = arguments[@"userKey"];

                }
                NSLog(@"userName: %@   userKey: %@   scanType: %li", userNameFromOptions, userKeyFromOptions, scanType);

            }
            else
            {
                scanType = 1;
                userNameFromOptions = @"sample";
                userKeyFromOptions = @"1234fhshfsf678906867";
            }





            this.callbackContext.success("huihgyugyu");

//            PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
//            r.setKeepCallback(true);
//            callbackContext.sendPluginResult(r);
            return true;
        }
        return false;

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
            Toast.makeText(getApplicationContext(), "Camera unavailable. Please restart the device.", Toast.LENGTH_LONG).show();
            finish();
        }

        @Override
        public void handleEvent(EVEyeRegionsChangedEvent event) {
            // Log.d(TAG, "Handle EVEyeRegionsChangedEvent: previewLeftX=" + event.getOriginalPreviewLeftX() + "; previewLeftY=" + event.getOriginalPreviewLeftY() + "; previewLeftW=" + event.getOriginalPreviewLeftW() + "; previewLeftH=" + event.getOriginalPreviewLeftH());

            if (currentEyeStatus== EVEnums.EyeStatus.None || leftEyeBox==null || rightEyeBox==null) {
                return;
            }

            leftEyeBox.setVisibility(View.VISIBLE);
            rightEyeBox.setVisibility(View.VISIBLE);

            Integer leftX = event.getPreviewLeftX();
            Integer leftY = event.getPreviewLeftY();
            Integer leftW = event.getPreviewLeftW();
            Integer leftH = event.getPreviewLeftH();

            Integer rightX = event.getPreviewRightX();
            Integer rightY = event.getPreviewRightY();
            Integer rightW = event.getPreviewRightW();
            Integer rightH = event.getPreviewRightH();

            if (leftH!=null && leftW!=null && leftX!=null && leftY!=null
                    && rightX!=null && rightY!=null && rightW!=null && rightH!=null) {

                int screenW = service_window.getWidth();
                int screenH = service_window.getHeight();

                RelativeLayout.LayoutParams leftLayoutParams = new RelativeLayout.LayoutParams(service_window.getLayoutParams());
                leftLayoutParams.setMargins(leftX, leftY, screenW - (leftX + leftW), screenH - (leftY + leftH));
                leftEyeBox.setLayoutParams(leftLayoutParams);

                RelativeLayout.LayoutParams rightLayoutParams = new RelativeLayout.LayoutParams(service_window.getLayoutParams());
                rightLayoutParams.setMargins(rightX, rightY, screenW - (rightX + rightW), screenH - (rightY + rightH));
                rightEyeBox.setLayoutParams(rightLayoutParams);
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
                    break;
                case NoEye:
                    capture_notification_text.setText(getString(R.string.capture_message_align));
                    break;
                case TooClose:
                case TooFarAway:
                    capture_notification_text.setText(getString(R.string.capture_message_distance));
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

                scan_again_button.setVisibility(View.GONE);
                cancel_button.setVisibility(View.GONE);
                continue_button.setVisibility(View.VISIBLE);

                Log.d(TAG, "Starting enrollmentCompleted: success=" + completion.isSuccess() + "; user_key=" + new String(completion.getUserKey()));

                //Toast.makeText(getApplicationContext(), completion.isSuccess() ? R.string.main_enrollment_success : R.string.main_enrollment_failure, Toast.LENGTH_LONG).show();

                if (completion.isSuccess()) {
                    Log.d(TAG, "Storing public key: encodedPublicKey=" + EVServiceHelper.data2string(completion.getEncodedPublicKey()));

                    SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES, Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    if (completion.getEncodedPublicKey() != null && completion.getEncodedPublicKey().length > 0) {
                        editor.putString(PUBLIC_KEY_PREFERENCE, EVServiceHelper.data2string(completion.getEncodedPublicKey()));
                    } else {
                        editor.remove(PUBLIC_KEY_PREFERENCE);
                    }
                    editor.apply();

                    capture_complete_checkmark.setVisibility(View.VISIBLE);
                }

                if (completion.isSuccess()) {
                    capture_notification_text.setText(getString(R.string.enroll_completed_message));
                    sub_notification_text.setText(getString(R.string.enroll_done_message));
                    large_notification_text.setVisibility(View.GONE);
                    sub_notification_text.setVisibility(View.GONE);
                    return;
                }
                else if (completion.isIncomplete()) {
                    capture_notification_text.setText(getString(R.string.enroll_incomplete_message));
                    sub_notification_text.setText(getString(R.string.enroll_retry_message));
                    return;
                }
                else if (completion.wasAborted()) {
                    if (SharedGlobals.isLicensingError(completion.getAbortResult())) {
                        BaseActivity.deleteLicenseCertificate();
                        Intent groupIdActivity = new Intent(EVCaptureActivity.this, GroupIdActivity.class);
                        groupIdActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        groupIdActivity.putExtra(SharedGlobals.LICENSE_ERROR, completion.getAbortResult());
                        startActivity(groupIdActivity);
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

                Log.d(TAG, "Finished enrollmentCompleted: success="+ completion.isSuccess());
            } catch (Throwable ex) {
                Log.e(TAG, "Failed to complete enrollment.", ex);
            }
        }

        @Override
        public void verificationCompleted(EVVerifyCompletion completion) {
            try {

                scan_again_button.setVisibility(View.GONE);
                cancel_button.setVisibility(View.GONE);
                continue_button.setVisibility(View.VISIBLE);

                Log.d(TAG, "Starting verificationCompleted: success=" + completion.isSuccess() + "; user_key=" + new String(completion.getUserKey()));

                //Toast.makeText(getApplicationContext(), completion.isSuccess() ? R.string.main_verify_success : R.string.main_verify_failure, Toast.LENGTH_LONG).show();

                boolean signatureVerify = false;
                if (completion.isSuccess()) {
                    if (isAudioEnabled()) {
                        MediaPlayer verifiedPromptMediaPlayer = MediaPlayer.create(getBaseContext(), R.raw.verified_prompt);
                        verifiedPromptMediaPlayer.start();
                    }

                    if (completion.getSignedNonce() != null && completion.getSignedNonce().length>0) {
                        Log.d(TAG, "Verifying signature: mNonce="+ EVServiceHelper.data2string(mNonce)+"; signedNonce=" + EVServiceHelper.data2string(completion.getSignedNonce()));

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

                    capture_notification_text.setText(getString(R.string.main_verify_success));
                    large_notification_text.setVisibility(View.GONE);
                    sub_notification_text.setVisibility(View.GONE);
                    capture_complete_checkmark.setVisibility(View.VISIBLE);
                }
                else if (completion.wasAborted()) {
                    showAbortMessages(completion.getAbortResult());
                    return;
                }
                else {

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


                Log.d(TAG, "Finished verifyCompleted: success="+ completion.isSuccess()+"; signatureVerify="+signatureVerify);
            } catch (Throwable ex) {
                Log.e(TAG, "Failed to complete verification.", ex);
            }
        }
    };
}
