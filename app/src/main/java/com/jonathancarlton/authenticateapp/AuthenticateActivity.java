package com.jonathancarlton.authenticateapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.concurrent.ExecutionException;

import uk.ac.ncl.jcarlton.networkanalysis.Decision;

public class AuthenticateActivity extends AppCompatActivity {

    private Button authenticateButton;
    private long twitterID;
    private String twitterUsername;

    private boolean authDecision;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenicate);

        // automatically set to false.
        authDecision = false;

        authenticateButton = (Button) findViewById(R.id.auth_btn);

        Intent passedIntent = getIntent();
        twitterID = (long) passedIntent.getExtras().get("twitter_user_id");
        twitterUsername = (String) passedIntent.getExtras().get("twitter_username");

        setUpAuthButton();
    }

    private void setUpAuthButton() {
        authenticateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Long[] staticUsers = new Long[] {
                        Long.parseLong(getResources().getString(R.string.static_user_1)),
                        Long.parseLong(getResources().getString(R.string.static_user_2)),
                        Long.parseLong(getResources().getString(R.string.static_user_3))
                };

                try {
                    authDecision = new DecideAuth().execute(staticUsers).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private class DecideAuth extends AsyncTask<Long, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Long... staticUsers) {
            //Decision decision = new Decision(twitterID, staticUsers, null);


            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }
    }
}
