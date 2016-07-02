package biz.incoding.silentshot;

import android.content.Intent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by home on 10.02.2016.
 */
public class IrisAccess  extends CordovaPlugin {
    public CallbackContext callbackContext;
    public static EVCaptureActivity evCaptureActivity;
    private static int scanType;
    private String userNameFromOptions;
    private String userKeyFromOptions;



    @Override
    public boolean execute(String action, JSONArray args,
                           CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;

        if (action.equalsIgnoreCase("GetIris")) {

            if(args.length() > 0)
            {
                JSONObject arguments = (JSONObject)args.get(0);
                if(arguments.has("scanType"))
                    scanType = arguments.getInt("scanType");

                if(arguments.has("userName"))
                    userNameFromOptions = arguments.getString("userName");
                else
                    userNameFromOptions = "";

                if(arguments.has("userKey"))
                    userKeyFromOptions = arguments.getString("userKey");
                else
                    userKeyFromOptions = "";
            }
            else
            {
                scanType = 1;
                userNameFromOptions = "sample";
                userKeyFromOptions = "1234fhshfsf678906867";
            }

            Intent intent = new Intent(this.cordova.getActivity(), biz.incoding.silentshot.EVCaptureActivity.class);

            intent.putExtra(EVCaptureActivity.IS_ENROLLMENT_KEY, scanType == 0 ? true:false);
            intent.putExtra(EVCaptureActivity.USER_ID_KEY, userNameFromOptions);
            intent.putExtra(EVCaptureActivity.USERKEY_KEY, userKeyFromOptions);
            //intent.putExtra(EVCaptureActivity.SERVICE_PACKAGE_KEY, servicePackageKey);

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //this.cordova.getActivity().startActivityForResult(intent,100);
            this.cordova.startActivityForResult(this,intent,100);
            //startActivityForResult(this, intent,100);


//            this.callbackContext.success("huihgyugyu");

//            PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
//            r.setKeepCallback(true);
//            callbackContext.sendPluginResult(r);
            return true;
        } else if(action.equalsIgnoreCase("ClearUI")){
            try {
                evCaptureActivity.finish();
            }catch (Exception e){}

        }
        return false;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case 100: //integer matching the integer suplied when starting the activity
                if(intent != null ){
                    if (resultCode == android.app.Activity.RESULT_OK) {
                        //in case of success return the string to javascript\
                        String result = intent.getStringExtra("result");
                        if(scanType == 0) {
                            this.callbackContext.success(result);
                        }else{
                            String str = "[{\"verified\":\"true\",\"userKey\":\"" + result + "\"}]";
                            try {
                                JSONArray jsonarray = new JSONArray(str);
                                PluginResult r = new PluginResult(PluginResult.Status.OK,jsonarray);
                                r.setKeepCallback(true);
                                callbackContext.sendPluginResult(r);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        String message = intent.getStringExtra("result");
                        PluginResult r = new PluginResult(PluginResult.Status.OK,message);
                        r.setKeepCallback(true);
                        callbackContext.sendPluginResult(r);
                    }
                }
                break;
            default:
                break;
        }
    }

}
