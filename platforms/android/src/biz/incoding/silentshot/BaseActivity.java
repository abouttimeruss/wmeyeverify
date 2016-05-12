package biz.incoding.silentshot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

public abstract class BaseActivity extends Activity {

    static Context _context;

    @Override
    protected void onStart() {

        super.onStart();

//        * @param enterAnim A resource ID of the animation resource to use for
//        *the incoming activity.  Use 0 for no animation.
//        * @param exitAnim A resource ID of the animation resource to use for
//        * the outgoing activity.  Use 0 for no animation.

    }

    protected void pushActivity(Intent intent) {

        startActivityForResult(intent,0);

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
