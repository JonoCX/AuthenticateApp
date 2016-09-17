package com.jonathancarlton.authenticateapp.analysis;

import android.content.Context;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.jonathancarlton.authenticateapp.util.Utility;

/**
 * <h1>Twitter Setup</h1>
 * Sets up a Twitter4J instance to be used in the
 * analysis of the users Twitter profile.
 *
 * @author Jonathan Carlton
 */
public class TwitterSetup {

    // application context
    private Context applicationContext;

    private String consumerKey;
    private String secretKey;
    private String accessToken;
    private String accessTokenSecret;

    /**
     * Object constructor
     *
     * @param context   the context of the application
     */
    public TwitterSetup (Context context) {
        this.applicationContext = context;
        setup();
    }

    /**
     * Builds an instance of Twitter to used to make
     * API calls.
     *
     * @return      an authenticated Twitter instance.
     */
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

    /**
     * Fetches the API keys in order to correctly
     * authenticate the Twitter instance.
     *
     * <b>Note:</b> If you were to use this class
     * you would need to change this method to correctly
     * point to the place in which the API keys are
     * stored.
     */
    private void setup() {
        Utility utility = new Utility(applicationContext);
        String[] arr = utility.getTokens("twitter", 4);
        consumerKey = arr[0];
        secretKey = arr[1];
        accessToken = arr[2];
        accessTokenSecret = arr[3];
    }


}
