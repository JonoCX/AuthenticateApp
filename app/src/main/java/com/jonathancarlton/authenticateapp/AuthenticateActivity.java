package com.jonathancarlton.authenticateapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.jonathancarlton.authenticateapp.util.CheckLocation;
import com.jonathancarlton.authenticateapp.util.DatabaseHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * <h1>Authenticate Activity</h1>
 * The activity in which the user is greeted with the
 * button to authenticate themselves - if successful they
 * are able to do a range of things (constructs that aren't
 * built into this program) i.e. access a room.
 */
public class AuthenticateActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Button authenticateButton;
    private long twitterID;
    private String twitterUsername;

    private boolean authDecision = false;

    private GoogleApiClient googleApiClient = null;
    private Location lastLocation;
    private double lastLat;
    private double lastLng;
    private static final int REQUEST_LOCATION = 2;
    private boolean locationAuth = false;

    private Thread thread;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenicate);

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


        // automatically set to false.
        authDecision = false;

        authenticateButton = (Button) findViewById(R.id.auth_btn);

        Intent passedIntent = getIntent();
        twitterID = (long) passedIntent.getExtras().get("twitter_user_id");
        twitterUsername = (String) passedIntent.getExtras().get("twitter_username");

        setUpAuthButton();


    }

    /**
     * A method to setup the authentication button that the
     * user is presented with
     */
    private void setUpAuthButton() {
        authenticateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Long> staticUsers = new ArrayList<Long>();

                // pull the static user ids from the resources
                staticUsers.add(Long.parseLong(getResources().getString(R.string.static_user_1)));
                staticUsers.add(Long.parseLong(getResources().getString(R.string.static_user_2)));
                staticUsers.add(Long.parseLong(getResources().getString(R.string.static_user_3)));

                try {
                    authDecision = new DecideAuth().execute(staticUsers).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

                finalDecision();

            }
        });

    }

    /**
     * Make the final decision as to whether or not the
     * user will be authenticated based on the results of
     * the processing of their Twitter feed and location.
     */
    private void finalDecision() {
        CheckLocation locationCheck = new CheckLocation();
        // if distance < 0.1 miles (10 miles for testing)
        locationAuth = locationCheck.distance(lastLat, lastLng) < 10;
        if (authDecision && locationAuth) {
            Toast.makeText(getApplicationContext(), "SUCCESS", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(getApplicationContext(), "FAILURE", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(
                    this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION
            );
        } else {
            // get the last known location from the GPS
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastLocation != null) {
                lastLat = lastLocation.getLatitude();
                lastLng = lastLocation.getLongitude();
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // noinspection ResourceType
                Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                if (lastLocation != null) {
                    lastLat = lastLocation.getLatitude();
                    lastLng = lastLocation.getLongitude();

                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConnectionSuspended(int i) {}

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}


    /**
     * <h2>Decide Authentication</h2>
     * Inner asynchronous which runs along side the main UI thread
     * when called.
     * <p>
     * The class calls the relevant objects to perform the analysis
     * on the requesting user, the topic detection and the relationships
     * that the user has which the static users.
     */
    private class DecideAuth extends AsyncTask<List<Long>, Void, Boolean> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected Boolean doInBackground(List<Long>... lists) {
            DatabaseHelper helper = new DatabaseHelper(getApplicationContext());

            boolean choice = false;
            boolean inserted = false;

            // fetch the stored date
            String date = helper.getDate(twitterID);

            // if no date is stored
            if (date.equals("No records stored")) {
                // create and insert a new one into the SQLite DB
                date = new SimpleDateFormat("dd-MM-yyyy-HH:mm-ss").format(Calendar.getInstance().getTime());
                if (helper.insertDate(twitterID, date)) inserted = true;
            } else {
                inserted = true;
            }

            if (inserted) {
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss");
                    Date formattedDate = simpleDateFormat.parse(date);
                    Decision decision = new Decision(
                            getApplicationContext(),
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

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            finalDecision();
        }
    }
}
