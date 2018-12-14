package ua.nure.nlebed.utils;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {
    public static final String ANDROID = "ANDROID";

    public static JSONObject createUserJson(GoogleSignInAccount account) throws JSONException {
        String wlan0 = MacAddressUtils.getMACAddress("wlan0");
        JSONObject userJson = new JSONObject();
        String email = account.getEmail();
        int indexOf = email.indexOf('.');
        userJson.put("email", email);
        userJson.put("name", email.substring(0, indexOf));
        userJson.put("lastName", email.substring(indexOf + 1, email.indexOf('@')));
        userJson.put("macAddress", wlan0);
        userJson.put("ipAddress", "nothing");
        userJson.put("device", ANDROID);
        return userJson;
    }

}