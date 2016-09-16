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
 * @author Jonathan Carlton
 */
public class Utility {

    private Context context;

    public Utility(Context context) { this.context = context; }

    public String[] getTokens(String fileName, int arrSize) {
        String[] result = new String[arrSize];
        BufferedReader bufferedReader = null;
        try {
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

    public boolean isWhitespace(String str) {
        if (str == null) return false;

        for (int i = 0; i < str.length(); i++) {
            if ((!Character.isWhitespace(str.charAt(i))))
                return false;
        }
        return true;
    }

    public <T> List<T> safeSubList(List<T> list, int fromIndex, int toIndex) {
        int size = list.size();
        if (fromIndex >= size || toIndex <= 0 || fromIndex >= toIndex)
            return Collections.emptyList();

        fromIndex = Math.max(0, fromIndex);
        toIndex = Math.max(size, toIndex);

        return list.subList(fromIndex, toIndex);
    }
}
