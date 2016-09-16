package com.jonathancarlton.authenticateapp.analysis;

import android.util.Log;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.json.simple.JSONObject;

import uk.ac.ncl.jcarlton.networkanalysis.util.MapSorter;

/**
 * Created by Jonathan on 16-Sep-16.
 */
@IgnoreExtraProperties
public class User {

    private long userId;
    private String currentDate;
    private List<String> timelineSinceLastChecked;
    private String lastChecked;
    private List<JSONObject> topicsPosted;
    private List<Map<String, Object>> topicsPostedMap;

    public User() {}

    public User(long userId, String currentDate, List<String> timelineSinceLastChecked, String lastChecked, List<JSONObject> topicsPosted) {
        this.userId = userId;
        this.currentDate = currentDate;
        this.timelineSinceLastChecked = timelineSinceLastChecked;
        this.lastChecked = lastChecked;
        this.topicsPosted = topicsPosted;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("user_id", userId);
        result.put("current_date", currentDate);
        result.put("timeline_since_last_checked", timelineSinceLastChecked);
        result.put("last_checked", lastChecked);
        result.put("topics_posted", topicsPosted);

        return result;
    }

    @Exclude
    private List<Map<String, Object>> convertTopicsPosted() {
        Map<String, Object> map = new HashMap<>();

        for (JSONObject obj : topicsPosted) {
            map.put("frequency", obj.get("frequency"));
            map.put("topic", obj.get("topic"));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        result.add(map);
        return result;
    }

    @Exclude
    public JSONObject toJSONObject() {
        JSONObject result = new JSONObject();
        return result;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public List<String> getTimelineSinceLastChecked() {
        return timelineSinceLastChecked;
    }

    public void setTimelineSinceLastChecked(List<String> timelineSinceLastChecked) {
        this.timelineSinceLastChecked = timelineSinceLastChecked;
    }

    public String getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(String lastChecked) {
        this.lastChecked = lastChecked;
    }

    public List<JSONObject> getTopicsPosted() {
        return topicsPosted;
    }

    public void setTopicsPosted(List<JSONObject> topicsPosted) {
        this.topicsPosted = topicsPosted;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", currentDate='" + currentDate + '\'' +
                ", timelineSinceLastChecked=" + timelineSinceLastChecked +
                ", lastChecked='" + lastChecked + '\'' +
                ", topicsPosted=" + topicsPosted +
                '}';
    }
}
