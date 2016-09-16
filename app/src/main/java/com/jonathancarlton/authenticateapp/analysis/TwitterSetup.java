package com.jonathancarlton.authenticateapp.analysis;

import android.content.Context;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.jonathancarlton.authenticateapp.util.Utility;

/**
 * @author Jonathan Carlton
 */
public class TwitterSetup {

    private Context applicationContext;

    private String consumerKey;
    private String secretKey;
    private String accessToken;
    private String accessTokenSecret;

    public TwitterSetup (Context context) {
        this.applicationContext = context;
        setup();
    }

    public Twitter getInstance() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setDebugEnabled(true)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(secretKey)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret);
        TwitterFactory factory = new TwitterFactory(builder.build());
        return factory.getInstance();
    }

    private void setup() {
        Utility utility = new Utility(applicationContext);
        String[] arr = utility.getTokens("twitter", 4);
        consumerKey = arr[0];
        secretKey = arr[1];
        accessToken = arr[2];
        accessTokenSecret = arr[3];
    }


}
