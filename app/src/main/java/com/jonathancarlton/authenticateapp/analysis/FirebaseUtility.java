package com.jonathancarlton.authenticateapp.analysis;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by Jonathan on 16-Sep-16.
 */
public class FirebaseUtility {

    private Context mContext;

    private FirebaseDatabase database;
    private DatabaseReference mDbRef;
    private List<User> allData;

    private User user;
    private User savedUser;

    public FirebaseUtility(User user, Context context) {
        this.database = FirebaseDatabase.getInstance();
        this.mDbRef = this.database.getReference("users");
        this.user = user;

        readUsers();


    }

    private void readUsers() {
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                try {
                    boolean run = new AsyncClass().execute(dataSnapshot).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }


//                Log.i("CHILDADDED", "snapshot: " + dataSnapshot.getChildren().toString());
//                allData = new ArrayList<>();
//                for (DataSnapshot child : dataSnapshot.getChildren()) {
//                    String key = child.getKey();
//                    if (key.equals("activity_" + user.getCurrentDate())) {
//                        long id = (long) child.child("user_id").getValue();
//                        String currentDate = (String) child.child("current_date").getValue();
//                        String lastChecked = (String) child.child("last_checked").getValue();
//
//                        List<String> timeLineListArr = (List<String>) child.child("timeline_since_last_checked").getValue();
//
//                        List<JSONObject> actualTopicsList = (List<JSONObject>) child.child("topics_posted").getValue();
//
//                        savedUser = new User(
//                                id,
//                                currentDate,
//                                timeLineListArr,
//                                lastChecked,
//                                actualTopicsList
//                        );
//
//                        allData.add(savedUser);
//
//                    }
//
//                    long id = (long) child.child("user_id").getValue();
//                    String currentDate = (String) child.child("current_date").getValue();
//                    String lastChecked = (String) child.child("last_checked").getValue();
//                    List<String> timeLineListArr = (List<String>) child.child("timeline_since_last_checked").getValue();
//                    List<JSONObject> actualTopicsList = (List<JSONObject>) child.child("topics_posted").getValue();
//
//                    User temp = new User(
//                            id,
//                            currentDate,
//                            timeLineListArr,
//                            lastChecked,
//                            actualTopicsList
//                    );
//                    allData.add(temp);

                }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d("FIREBASE_INFO", "onChildChanged:" + dataSnapshot.getKey());
                savedUser = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d("FIREBASE_INFO", "onChildRemoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d("FIREBASE_INFO", "onChildMoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Firebase Error", "postComments:onCancelled", databaseError.toException());
                Toast.makeText(mContext, "Failed to load comments.",
                        Toast.LENGTH_SHORT).show();
            }

            class AsyncClass extends AsyncTask<DataSnapshot, Void, Boolean> {


                @Override
                protected Boolean doInBackground(DataSnapshot... dataSnapshots) {
                    DataSnapshot snapshot = dataSnapshots[0];
                    allData = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String key = child.getKey();
                        if (key.equals("activity_" + user.getCurrentDate())) {
                            long id = (long) child.child("user_id").getValue();
                            String currentDate = (String) child.child("current_date").getValue();
                            String lastChecked = (String) child.child("last_checked").getValue();

                            List<String> timeLineListArr = (List<String>) child.child("timeline_since_last_checked").getValue();

                            List<JSONObject> actualTopicsList = (List<JSONObject>) child.child("topics_posted").getValue();

                            savedUser = new User(
                                    id,
                                    currentDate,
                                    timeLineListArr,
                                    lastChecked,
                                    actualTopicsList
                            );

                            allData.add(savedUser);

                        }

                        long id = (long) child.child("user_id").getValue();
                        String currentDate = (String) child.child("current_date").getValue();
                        String lastChecked = (String) child.child("last_checked").getValue();
                        List<String> timeLineListArr = (List<String>) child.child("timeline_since_last_checked").getValue();
                        List<JSONObject> actualTopicsList = (List<JSONObject>) child.child("topics_posted").getValue();

                        User temp = new User(
                                id,
                                currentDate,
                                timeLineListArr,
                                lastChecked,
                                actualTopicsList
                        );
                        allData.add(temp);

                    }
                    return true;
                }

                @Override
                protected void onPostExecute(Boolean bool) {
                    super.onPostExecute(bool);
                }
            }
        };
        mDbRef.addChildEventListener(childEventListener);
    }

    public void writeUser() {
        String key = mDbRef.child(String.valueOf(user.getUserId())).push().getKey();
        Map<String, Object> postValues = user.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + String.valueOf(user.getUserId()) + "/activity_" + user.getCurrentDate(), postValues);

        mDbRef.updateChildren(childUpdates);

    }

    public User fetchUser() {
        return savedUser;
    }

    public List<User> getAllData() {
        return allData;
    }
}
