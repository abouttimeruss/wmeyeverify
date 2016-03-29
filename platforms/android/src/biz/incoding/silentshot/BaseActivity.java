package biz.incoding.silentshot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

public abstract class BaseActivity extends Activity {

    protected boolean hasPushedActivity;
    static Context _context;

    @Override
    protected void onStart() {

        super.onStart();

//        * @param enterAnim A resource ID of the animation resource to use for
//        *the incoming activity.  Use 0 for no animation.
//        * @param exitAnim A resource ID of the animation resource to use for
//        * the outgoing activity.  Use 0 for no animation.

        if (hasPushedActivity) {
            this.overridePendingTransition(getResources().getIdentifier("left_slide_out","anim",getPackageName()),
                    getResources().getIdentifier("left_slide_in","anim",getPackageName()));
        }
        hasPushedActivity = false;
    }

    protected boolean isAudioEnabled() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(SharedGlobals.SHARED_PREFERENCES, Activity.MODE_PRIVATE);
        boolean result = prefs.getBoolean("audio_enabled", Boolean.FALSE);
        return result;
    }

    protected void setIsAudioEnabled(boolean audio_enabled) {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(SharedGlobals.SHARED_PREFERENCES, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("audio_enabled", audio_enabled).apply();
    }

    protected void pushActivity(Intent intent) {

        hasPushedActivity = true;
        startActivityForResult(intent,0);

        overridePendingTransition(getResources().getIdentifier("right_slide_in","anim",getPackageName()),
                getResources().getIdentifier("right_slide_out","anim",getPackageName()));
    }
    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        _context = getApplicationContext();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getApplicationContext().getResources().getConfiguration().smallestScreenWidthDp < 800) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    public static Context getContext() {
        return _context;
    }


    public static String readLicenseCertificate() {
        return SharedGlobals.readLicenseCertificate(getContext());
    }

    public static void saveLicenseCertificate(String license_certificate) {
        SharedGlobals.saveLicenseCertificate(getContext(), license_certificate);
    }

    public static void deleteLicenseCertificate() {
        SharedGlobals.deleteLicenseCertificate(getContext());
    }
    @Override
    public void onBackPressed()
    {
    }
}
