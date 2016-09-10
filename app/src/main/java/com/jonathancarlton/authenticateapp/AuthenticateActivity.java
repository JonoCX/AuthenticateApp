package com.jonathancarlton.authenticateapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import uk.ac.ncl.jcarlton.networkanalysis.Decision;

public class AuthenticateActivity extends AppCompatActivity {

    private Button authenticateButton;
    private long twitterID;
    private String twitterUsername;

    private boolean authDecision = false;

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
                List<Long> staticUsers = new ArrayList<Long>();
                staticUsers.add(Long.parseLong(getResources().getString(R.string.static_user_1)));
                staticUsers.add(Long.parseLong(getResources().getString(R.string.static_user_2)));
                staticUsers.add(Long.parseLong(getResources().getString(R.string.static_user_3)));

                try {
                    authDecision = new DecideAuth().execute(staticUsers).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

                if (authDecision)
                    Toast.makeText(getApplicationContext(), "SUCCESS", Toast.LENGTH_LONG);
                else
                    Toast.makeText(getApplicationContext(), "FAILURE", Toast.LENGTH_LONG);
            }
        });
    }

    private class DecideAuth extends AsyncTask<List<Long>, Void, Boolean> {


        @Override
        protected Boolean doInBackground(List<Long>... lists) {
            DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
            boolean choice = false;
            boolean inserted = false;
            String date = helper.getDate(twitterID);
            if (date.equals("No records stored")) {
                date = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss").format(Calendar.getInstance().getTime());
                if (helper.insertDate(twitterID, date))
                    inserted = true;
            } else
                inserted = true;

            if (inserted) {
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyy-HH:mm:ss");
                    Date formattedDate = simpleDateFormat.parse(date);
                    Decision decision = new Decision (
                            twitterID,
                            lists[0],
                            formattedDate
                    );

                    choice = decision.decide();
                } catch (ParseException pe) {
                    pe.printStackTrace();
                }
            } else {
                return false;
            }

            return choice;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }
    }
}
