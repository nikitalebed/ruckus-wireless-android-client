package ua.nure.nlebed.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import ua.nure.nlebed.R;
import ua.nure.nlebed.utils.JsonUtils;

import java.io.IOException;
import java.util.Objects;

public class SignInActivity extends AppCompatActivity
        implements View.OnClickListener {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private static final String NURE_UA_DOMAIN = "@nure.ua";
    public static final int SCHEDULE_TIME_MILLISECONDS = 100;

    private GoogleSignInClient mGoogleSignInClient;
    private TextView mStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStatusTextView = findViewById(R.id.status);

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(f -> finish());

        GoogleSignInOptions signInClient = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, signInClient);

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setColorScheme(SignInButton.COLOR_LIGHT);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUI(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String email = account.getEmail();
            if ((Objects.requireNonNull(email)).endsWith(NURE_UA_DOMAIN) || checkEmailExists(email)) {
                sendCreateUserHttpRequest(account);
                updateUI(account);
            } else {
                updateUI(null);
            }
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkEmailExists(String email) throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("https://nure-ruckus-users-control.herokuapp.com//rest/user/checkExist/" + email);
//        HttpPost httpPost = new HttpPost("https://nure-ruckus-users-control.herokuapp.com/rest/user");
        HttpEntity entity = httpclient.execute(httpGet).getEntity();
        return Boolean.parseBoolean(EntityUtils.toString(entity, "UTF-8"));
    }

    private void sendCreateUserHttpRequest(GoogleSignInAccount account) throws Exception {
        HttpClient httpclient = new DefaultHttpClient();
//        HttpPost httpPost = new HttpPost("http://10.0.2.2:8080/rest/user");
        HttpPost httpPost = new HttpPost("https://nure-ruckus-users-control.herokuapp.com/rest/user");
        JSONObject userJson = JsonUtils.createUserJson(account);
        httpPost.setEntity(new StringEntity(userJson.toString()));
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
        httpclient.execute(httpPost);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, task -> updateUI(null));
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account != null) {
            mStatusTextView.setText(getString(R.string.signed_in_fmt, account.getEmail()));
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText(R.string.sign_in_with_nure_google_domain);
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out).setVisibility(View.GONE);
            mGoogleSignInClient.signOut();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
        }
    }
}
