package com.jonathancarlton.authenticateapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;

/**
 * <h1>Main Activity</h1>
 * The main activity for the application, the one in which
 * the user is greeted with as they log in.
 *
 * @author Jonathan Carlton
 */
public class MainActivity extends AppCompatActivity {

    private String twitterKey;
    private String twitterSecret;

    private TwitterLoginButton loginButton;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        twitterKey = getResources().getString(R.string.twitter_api_key);
        twitterSecret = getResources().getString(R.string.twitter_secret);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(twitterKey, twitterSecret);
        Fabric.with(this, new Twitter(authConfig));

        setContentView(R.layout.activity_main);

        setUpLoginButton();
    }

    /**
     * Setup the Twitter Login button.
     *
     * <b>Note:</b> The login button is taken from
     * the Fabric Twitter API.
     */
    private void setUpLoginButton() {
        // set up the Twitter login event
        loginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.i("TwitterKit", "Login with Twitter success");
                // just need to pass the id to the next activity.
                loadAuthActivity(result.data.getUserId(), result.data.getUserName());
            }
            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Login with Twitter failure", exception);
            }
        });

    }

    /**
     * Load the authentication activity to do the proper
     * work.
     *
     * @param userId        the requesting users Twitter ID
     * @param username      the requesting users Twitter username
     */
    private void loadAuthActivity(long userId, String username) {
        Intent intent = new Intent(this, AuthenticateActivity.class);
        intent.putExtra("twitter_user_id", userId);
        intent.putExtra("twitter_username", username);
        startActivity(intent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Ensure that the login button hears the result from any
        // activity that it triggered
        loginButton.onActivityResult(requestCode, resultCode, data);
    }
}
