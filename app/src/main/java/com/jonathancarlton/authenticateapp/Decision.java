package com.jonathancarlton.authenticateapp;

import android.content.Context;
import android.util.Log;

import com.jonathancarlton.authenticateapp.analysis.LinkAnalysis;
import com.jonathancarlton.authenticateapp.analysis.TwitterSetup;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import twitter4j.Twitter;
import uk.ac.ncl.jcarlton.networkanalysis.util.MapSorter;

/**
 * <h1>Decision</h1>
 * The main class that decides, based on various parameters
 * from other classes if the user has past the tests in order
 * to be authenticated.
 *
 * <p>
 * The main usage of the class comes from the {@link #decide()}
 * method.
 *
 * @author Jonathan Carlton
 */
public class Decision {

    private Context applicationContext;

    private List<Long> staticUsers;
    private long requestingUser;
    private Date lastChecked;

    private Twitter instance = null;

    private boolean decision;

    /**
     * Object constructor.
     * <p>
     * Need to provide a requesting user id and a list
     * of the static user id's that form the keys
     * of the network.
     *
     * @param context
     * @param requestingUser the user attempting to authenticate
     * @param staticUsers    a list of user ids
     * @param lastChecked
     */
    public Decision(Context context, long requestingUser, List<Long> staticUsers, Date lastChecked) {
        this.applicationContext = context;
        this.requestingUser = requestingUser;
        this.staticUsers = staticUsers;
        this.lastChecked = lastChecked;
        this.instance = instanceSetup();
    }

    private Twitter instanceSetup() {
        return new TwitterSetup(applicationContext).getInstance();
    }

    /**
     * Reset option to re-try the authentication process.
     *
     * @param requestingUser the user attempting to authenticate
     * @param staticUsers    a list of user ids
     * @return a new Decision object
     */
    public Decision reset(Context context, long requestingUser, List<Long> staticUsers, Date lastChecked) {
        return new Decision(context, requestingUser, staticUsers, lastChecked);
    }


    public boolean decide() {
        // the process isn't going to work with null/empty/0'd variables
        if (staticUsers == null || staticUsers.isEmpty() || requestingUser == 0) {
            decision = false;
            return false;
        }

        LinkAnalysis link = new LinkAnalysis(
                applicationContext,
                requestingUser,
                instance,
                lastChecked
        );

        boolean follow, friend, activity = false;

        // check for the following link
        Map<Long, Boolean> followMap = link.checkForLinksFollowing(staticUsers);
        follow = checkMap(followMap);
        Log.i("DECISION_FOLLOW", "Result:" + follow);

        // check for the friends link
        Map<Long, Boolean> friendMap = link.checkForLinksFriends(staticUsers);
        friend = checkMap(friendMap);
        Log.i("DECISION_FRIEND", "Result: " + friend);

        try {
            activity = checkRecentActivity(link.recentActivity(staticUsers));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("DECISION_ACTIVITY", "Result: " + activity);

        // all true then set decision as true
        if (follow && friend && activity) decision = true;
        // else, if follow and activity is true then set decision as true
        else decision = follow && activity;

        return decision;
    }

    /**
     * Check that the recent activity of the requested
     * user is inline with the previously stored activity.
     * <p>
     * This will identify possible account breaches if
     * the activity isn't inline with previous attempts.
     *
     * @param recentActivity the stored json object of the users
     *                       recent activities
     * @return
     */
    private boolean checkRecentActivity(JSONObject recentActivity) {
        // unable to do anything with just one activity entry
        if (recentActivity.size() <= 1 || recentActivity.isEmpty())
            return false;

        // sort them into order (most recent first)
        Set<String> keySet = new TreeSet<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.compareTo(o1);
            }
        });
        keySet.addAll(recentActivity.keySet());

        return topicsChecked(recentActivity, keySet);
    }

    /**
     * Check the topics that are stored in the users recent
     * activity and compare them with the previous.
     *
     * @param recentActivity recent activity of the user
     * @param keySet         the keyset within the jsonObject
     * @return true if satisfactory, false if not
     */
    private boolean topicsChecked(JSONObject recentActivity, Set<String> keySet) {
        Map<String, Long> previousTopics = new HashMap<>();
        List<Boolean> topTopicCheckList = new ArrayList<>();
        for (String key : keySet) {
            // compare the topics
            JSONObject obj = (JSONObject) recentActivity.get(key);
            JSONArray topics = (JSONArray) obj.get("topics_posted");
            if (topics.isEmpty()) return false;

            if (previousTopics.isEmpty()) {
                for (Object t : topics) {
                    JSONObject innerTopicObject = (JSONObject) t;
                    previousTopics.put((String) innerTopicObject.get("topic"), (Long) innerTopicObject.get("frequency"));
                }
                previousTopics = MapSorter.valueDescending(previousTopics);
            } else {
                Map<String, Long> currentTopics = new HashMap<>();
                for (Object t : topics) {
                    JSONObject innerTopicObject = (JSONObject) t;
                    currentTopics.put((String) innerTopicObject.get("topic"), (Long) innerTopicObject.get("frequency"));
                }
                currentTopics = MapSorter.valueDescending(currentTopics);

                // the maps are completely equal
                if (previousTopics.equals(currentTopics))
                    return true;

                int loopCounter = 0;

                for (Map.Entry<String, Long> m : currentTopics.entrySet()) {
                    if (loopCounter != 2) {
                        topTopicCheckList.add(previousTopics.containsKey(m.getKey()));
                        loopCounter++;
                    } else {
                        break;
                    }
                }
                previousTopics = currentTopics;

            }

        }

        // is the list is empty then return false
        if (topTopicCheckList.isEmpty()) return false;

            // if the list contains no false instances, return true
        else if (!topTopicCheckList.contains(false)) return true;

            // else calculate the true/false count
        else {
            int trueCount = 0;
            int falseCount = 0;
            for (Boolean b : topTopicCheckList) {
                if (b) trueCount++;
                else falseCount++;
            }

            if (falseCount > trueCount) return false;
            else return falseCount <= trueCount;
        }
    }

    /**
     * Check the contents of the map and return the
     * result.
     * <p>
     * Used to check the friend and follower maps.
     *
     * @param map to be checked
     * @return true/false
     */
    private boolean checkMap(Map<Long, Boolean> map) {
        if (map.isEmpty()) return false; // empty map, return false!
        else if (!map.containsValue(false)) return true; // contains no false values
        else {
            int falseCount = 0;
            int trueCount = 0;
            for (Map.Entry<Long, Boolean> m : map.entrySet()) {
                if (m.getValue()) trueCount++; // if value = true then increment
                else falseCount++; // else must be false
            }

            // contains more false values than true
            if (falseCount > trueCount) return false;
            else return falseCount < trueCount;
        }
    }

    /**
     * Fetch the decision
     * @return  the decision.
     */
    public boolean isDecision() {
        return decision;
    }
}
