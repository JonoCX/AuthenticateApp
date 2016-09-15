package com.jonathancarlton.authenticateapp;

import android.*;
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
import com.google.android.gms.location.*;
import com.google.android.gms.location.LocationServices;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import uk.ac.ncl.jcarlton.networkanalysis.Decision;

public class AuthenticateActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Button authenticateButton;
    private long twitterID;
    private String twitterUsername;

    private boolean authDecision = false;

    private GoogleApiClient mGoogleApiClient = null;
    private Location mLastLocation;
    private double mLastLat;
    private double mLastLng;
    private static final int REQUEST_LOCATION = 2;
    private boolean permissionGranted = false;
    private boolean locationAuth = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenicate);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
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

                CheckLocation locationCheck = new CheckLocation();
                // if distance < 0.1 miles
                locationAuth = locationCheck.distance(mLastLat, mLastLng) < 0.1;

                if (authDecision && locationAuth)
                    Toast.makeText(getApplicationContext(), "SUCCESS", Toast.LENGTH_LONG);
                else
                    Toast.makeText(getApplicationContext(), "FAILURE", Toast.LENGTH_LONG);
            }
        });
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

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
            Log.i("CURRENT LAT", String.valueOf(mLastLat));
            Log.i("CURRENT LONG", String.valueOf(mLastLng));
        } else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                mLastLat = mLastLocation.getLatitude();
                mLastLng = mLastLocation.getLongitude();
                permissionGranted = true;
                Log.i("CURRENT LAT", String.valueOf(mLastLat));
                Log.i("CURRENT LONG", String.valueOf(mLastLng));
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // noinspection ResourceType
                Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (lastLocation != null) {
                    mLastLat = lastLocation.getLatitude();
                    mLastLng = lastLocation.getLongitude();
                    permissionGranted = true;
                }
            } else {
                // Permission denied
                permissionGranted = false;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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
                    Decision decision = new Decision(
                            twitterID,
                            lists[0],
                            formattedDate,
                            getResources().getString(R.string.t4j_consumer_key),
                            getResources().getString(R.string.t4j_secret_key),
                            getResources().getString(R.string.t4j_access_token),
                            getResources().getString(R.string.t4j_access_token_secret),
                            getResources().getString(R.string.ml_api_key)
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
