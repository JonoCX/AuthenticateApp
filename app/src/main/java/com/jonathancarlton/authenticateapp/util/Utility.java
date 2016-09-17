package com.jonathancarlton.authenticateapp.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

/**
 * <h1>Utility</h1>
 * A general utility class for random methods that are used
 * throughout the application.
 *
 * @author Jonathan Carlton
 */
public class Utility {

    private Context context;

    /**
     * Object constructor
     * @param context       the applications context
     */
    public Utility(Context context) { this.context = context; }

    /**
     * Fetches the authentication tokens for the various
     * APIs that are used throughout the project.
     *
     * <b>Note:</b> The assets folder in Android projects
     * is a read-only folder, so the json information in the
     * FileIO class cannot be stored there.
     *
     * @param fileName      the name of the API.
     *
     * @param arrSize       the expected number of authentication
     *                      tokens to be returned.
     *
     * @return              an array consisting of the authentication
     *                      tokens.
     */
    public String[] getTokens(String fileName, int arrSize) {
        String[] result = new String[arrSize];
        BufferedReader bufferedReader = null;
        try {
            // open the assets folder and read from.
            bufferedReader = new BufferedReader(new InputStreamReader(context.getAssets().open("tokens/" + fileName + ".txt")));

            String line;
            int i = 0;
            while ((line = bufferedReader.readLine()) != null) {
                result[i] = line;
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) bufferedReader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Check to see if a String is just made up of
     * whitespace.
     *
     * <b>Note:</b> An empty String, tested with .isEmpty()
     * will return true if the String representation is "". However,
     * it will return false if the String is made up of whitespace:
     * "  ".
     *
     * @param str       the String to be checked.
     *
     * @return          true if the String is made up purely
     *                  of whitespace and false if it is not.
     */
    public boolean isWhitespace(String str) {
        if (str == null) return false;

        for (int i = 0; i < str.length(); i++) {
            if ((!Character.isWhitespace(str.charAt(i))))
                return false;
        }
        return true;
    }

    /**
     * Generic method for creating a sub list safely.
     *
     * @param list          the list to be reduced in size.
     *
     * @param fromIndex     the starting index.
     *
     * @param toIndex       the finishing point.
     *
     * @param <T>           generic type of the list.
     *
     * @return              the reduced list from the fromIndex
     *                      to the toIndex.
     */
    public <T> List<T> safeSubList(List<T> list, int fromIndex, int toIndex) {
        int size = list.size();
        if (fromIndex >= size || toIndex <= 0 || fromIndex >= toIndex)
            return Collections.emptyList();

        fromIndex = Math.max(0, fromIndex);
        toIndex = Math.max(size, toIndex);

        return list.subList(fromIndex, toIndex);
    }
}
