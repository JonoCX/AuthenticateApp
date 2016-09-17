package com.jonathancarlton.authenticateapp.analysis;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.jonathancarlton.authenticateapp.util.FileIO;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import twitter4j.IDs;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import uk.ac.ncl.jcarlton.networkanalysis.util.MapSorter;

/**
 * <h1>Link Analysis</h1>
 * Perform link analysis on a Twitter based data
 * set.
 * <p>
 * Both the users that the given user is following {@link #checkForLinksFollowing(List)}
 * and the users that are following (friends) the given user {@link #checkForLinksFriends(List)}
 * are processed for consumption.
 *
 * @author Jonathan Carlton
 * @version 1.0
 */
public class LinkAnalysis {

    private Context applicationContext;

    private long userId;
    private String username;
    private Twitter twitterInstance;
    private Date since;
    private List<String> feed;

    /**
     * Create an object using a user id and an pre-authenticated
     * instance of the Twitter (Twitter4j) API.
     *
     * @param userId          the id of a user from Twitter.
     * @param twitterInstance pre-authenticated instance of the Twitter4j
     *                        Twitter API.
     */
    public LinkAnalysis(Context applicationContext, long userId, Twitter twitterInstance, Date since) {
        this.applicationContext = applicationContext;
        this.userId = userId;
        this.username = null;
        this.twitterInstance = twitterInstance;
        this.since = since;
        setupFeed();
    }

    /**
     * Fetch the requesting users Twitter feed
     * in order for it to be processed.
     */
    private void setupFeed() {
        List<Status> rawFeed;
        feed = new ArrayList<>();
        if (userId != 0) {
            try {
                username = twitterInstance.getScreenName();
                rawFeed = getTweets(userId);
                for (Status s : rawFeed) {
                    feed.add(s.getText());
                }
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        } else {
            try {
                userId = twitterInstance.getId();
                rawFeed = getTweets(userId);
                for (Status s : rawFeed)
                    feed.add(s.getText());
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Given a list of user ids, check to see if there are links
     * between the user passed in the creation of the object and the
     * ids in the list.
     *
     * @param users list of user ids (longs)
     * @return id mapped too true if there is a link, false if not
     */
    public Map<Long, Boolean> checkForLinksFollowing(List<Long> users) {
        Map<Long, Boolean> result = new HashMap<>();
        try {
            for (long l : users)
                result.put(l, false);

            IDs ids = getFollowers();
            for (long u : users) {
                for (long anId : ids.getIDs()) {
                    if (u == anId) {
                        result.put(anId, true);
                        break;
                    }
                }
            }

        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Fetch the followers of the given user (passed when the object
     * was created).
     *
     * @return the IDs of the followers.
     * @throws TwitterException passed from the {@link #twitterInstance} API.
     */
    private IDs getFollowers() throws TwitterException {
        IDs ids;
        long cursor = -1;

        do {
            if (userId == 0)
                ids = twitterInstance.getFollowersIDs(username, cursor);
            else
                ids = twitterInstance.getFollowersIDs(userId, cursor);
            System.out.println("IDS (Followers): " + ids);
        } while ((cursor = ids.getNextCursor()) != 0);

        return ids;
    }

    /**
     * Given a list of users, check to see if there are any established
     * links between them and the user passed at the creation of the object.
     * <p>
     * A friend, in terms of Twitter, is a user who has a following
     * relationship with another user.
     * user1 follows user2 (user1 is a friend of user2)
     *
     * @param users list of user ids (long)
     * @return id mapped too true if there is a link, false if not
     */
    public Map<Long, Boolean> checkForLinksFriends(List<Long> users) {
        Map<Long, Boolean> result = new HashMap<>();
        try {
            for (long l : users)
                result.put(l, false);

            IDs ids = getFriends();

            for (long u : users) {
                for (long anId : ids.getIDs()) {
                    if (u == anId) {
                        result.put(anId, true);
                        break;
                    }
                }
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Fetch the friends of a given user (passed when the
     * object was created).
     *
     * @return ids of the friends.
     * @throws TwitterException passed from the {@link #twitterInstance} API.
     */
    private IDs getFriends() throws TwitterException {
        IDs ids;
        long cursor = -1;

        do {
            if (userId == 0)
                ids = twitterInstance.getFriendsIDs(username, cursor);
            else
                ids = twitterInstance.getFriendsIDs(userId, cursor);
            System.out.println("IDS (Friends): " + ids);
        } while ((cursor = ids.getNextCursor()) != 0);

        return ids;
    }

    /**
     * Package the recent activity by the user in question, ready to
     * be processed and stored.
     * <p>
     * This 'recent activity' includes all social media activity
     * such as; the tweets they've liked, an up-to-date snapshot
     * of their timeline, the topics that they've posted about
     * since being last checked and the static users that they've
     * interacted with in the meantime.
     * </p>
     * @param users         the static users.
     *
     * @return              JSONObject of all the most recent social
     *                      media activity from the user in question.
     *
     * @throws IOException  thrown from the FileIO object.
     */
    public JSONObject recentActivity(List<Long> users) throws IOException {
        String currentDate = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss").format(Calendar.getInstance().getTime());
        String lastChecked = "";

        // if the date is null, then it hasn't been checked before so set it to the current date
        if (since == null)
            lastChecked = currentDate;
        else
            lastChecked = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss").format(since);

        JSONArray topicsPosted = topicsPosted(feed);

        JSONObject inner = new JSONObject();
        inner.put("user_id", userId);
        inner.put("current_date", currentDate);
        inner.put("last_checked", lastChecked);
        inner.put("timeline_since_last_checked", feed);
        inner.put("topics_posted", topicsPosted);

        FileIO io = new FileIO(applicationContext);
        io.writeJSON(inner, Long.toString(userId));


        return io.readInJSON(Long.toString(userId));

    }


    /**
     * Process the topics that the user has posted about
     * and return them in a JSON array ready to be added
     * to the recent activity json file.
     *
     * @param feed      the feed to be analysed.
     *
     * @return          the topics in which the user has
     *                  be talking about.
     */
    private JSONArray topicsPosted(List<String> feed) {
        TopicDetection detection = new TopicDetection(applicationContext, feed);
        Map<String, JSONArray> response = detection.detectTopicsAll();


        Map<String, Integer> countMap = new HashMap<>();
        for (Map.Entry<String, JSONArray> m : response.entrySet()) {
            JSONArray current = m.getValue();
            double probability = 0.000;
            String label = "";
            for (int i = 0; i < current.size(); i++) {
                JSONObject inner = (JSONObject) current.get(i);
                double currentProbability = (double) inner.get("probability");

                // pick the label that has the highest probability
                if (currentProbability > probability) {
                    probability = currentProbability;
                    label = (String) inner.get("label");
                }
            }

            // if the label is an empty string then it could not be determined
            if (label.isEmpty())
                Log.i("TOPICS_POSTED", "Topic could not be determinded for the string : " + m.getKey());
            else {
                if (countMap.containsKey(label))
                    countMap.put(label, countMap.get(label) + 1);
                else
                    countMap.put(label, 1);
            }
        }


        Map<String, Integer> sortedMap;
        if (countMap.size() <= 1)
            sortedMap = countMap;
        else
            sortedMap = MapSorter.valueDescending(countMap);

        // convert to JSON Array
        JSONArray result = new JSONArray();
        for (Map.Entry<String, Integer> m : sortedMap.entrySet()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("topic", m.getKey());
            jsonObject.put("frequency", m.getValue());
            result.add(jsonObject);
        }

        return result;
    }

    /**
     * Fetch and process the tweets from the requeting users
     * timeline.
     *
     * @param userId                 the request users id.
     *
     * @return                       a list of the text content of each
     *                               tweet on the users timeline.
     *
     * @throws TwitterException      thrown from the Twitter4J library, specifically
     *                               the getUserTimeline method call.
     */
    private List<Status> getTweets(long userId) throws TwitterException {
        List<Status> list = new ArrayList<>();

        /*
            Paging is a method to count the rate limit issues with the
            Twitter4J library. You're only able to gather a set amount
            of information from the Twitter API, so by incremementally
            increasing the paging is reduces this limit being met.
         */
        Paging paging = new Paging(1);
        List<Status> temp;
        outerLoop:
        do {
            temp = twitterInstance.getUserTimeline(userId, paging);

            for (Status s : temp) {
                // if the current status has been created after the since day
                if (s.getCreatedAt().after(since)) {
                    list.add(s);
                } else if (s.getCreatedAt().before(since)) { // else before then break.
                    break outerLoop;
                }
            }

            // clear to remove the previously sourced statuses
            temp.clear();
            paging.setPage(paging.getPage() + 1);
        } while (list.size() > 0);

        return list;
    }

    /**
     * Much like the {@link #getTweets(long)} method, this method will
     * retrieve the favourites of the requesting user.
     *
     * @return                      a list of the favourites of the user.
     *
     * @throws TwitterException     thrown from the Twitter4J library, specifically
     *                              the getFavourites method.
     */
    private List<Status> getFavourites() throws TwitterException {
        List<Status> result = new ArrayList<>();
        Paging paging = new Paging(1);
        List<Status> temp;
        outerloop:
        do {
            if (userId == 0)
                temp = twitterInstance.getFavorites(username, paging);
            else
                temp = twitterInstance.getFavorites(userId, paging);

            for (Status s : temp) {
                if ((s.getCreatedAt()).after(since))
                    result.add(s);
                else if (s.getCreatedAt().before(since))
                    break outerloop;
            }

            temp.clear();
            paging.setPage(paging.getPage() + 1);
        } while (temp.size() > 0);

        return result;
    }
}
