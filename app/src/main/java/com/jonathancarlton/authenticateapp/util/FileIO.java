package com.jonathancarlton.authenticateapp.util;

import android.content.Context;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * <h1>File Input/Output</h1>
 * The class provides the functionality to both
 * read and write JSON objects to the internal
 * memory of the device which the app is running
 * on.
 * <p>
 * If the app were to be deleted from the phone
 * then the internal memory, reserved for the
 * app, is flushed and the files written/read
 * here will be lost.
 *
 * @author Jonathan Carlton
 */
public class FileIO {

    // the application context, to access directory's
    private Context context;

    /**
     * Object constructor.
     *
     * @param context   takes the application context as a parameter
     *                  so the interal directory's and files can be
     *                  accessed.
     */
    public FileIO(Context context) {
        this.context = context;
    }

    /**
     * Read in the stored JSON from the internal memory, given
     * a particular file name.
     *
     * @param filename          the name of the file, for the stored
     *                          json it will be the requesting users
     *                          id.
     *
     * @return                  the stored information as a JSON object,
     *                          all information stored.
     *
     * @throws IOException      thrown when the file can't be found
     *                          or read just to corruption, etc.
     */
    public JSONObject readInJSON(String filename) throws IOException {
        // get the /json/ directory and private mode is to signal internal memory
        File directory = context.getDir("json", Context.MODE_PRIVATE);
        File jsonFile = new File(directory, filename + ".json");
        FileInputStream inputStream = new FileInputStream(jsonFile);
        BufferedReader reader = null;
        JSONObject result = null;

        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            JSONParser parser = new JSONParser();

            // simple.json parser is able to take a Reader as a parameter.
            Object obj = parser.parse(reader);
            result = (JSONObject) obj;
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) reader.close();
        }

        return result;
    }

    /**
     * Write a JSON object to the internal memory of the device
     * that the application is running on.
     *
     * @param jsonObject        the object to be written
     *
     * @param fileName          the name of the file - usually the
     *                          user's id
     *
     * @throws IOException      thrown if the file cannot be written
     *                          to.
     */
    public void writeJSON(JSONObject jsonObject, String fileName) throws IOException {
        String date = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss").format(Calendar.getInstance().getTime());
        File directory = context.getDir("json", Context.MODE_PRIVATE);
        File file = new File(directory, fileName + ".json");
        FileWriter writer;

        // if the file already exists
        if (!file.createNewFile()) {
            // read in the previously stored JSON via the read in json method.
            JSONObject previous = readInJSON(fileName);
            previous.put("activity_" + date, jsonObject);
            writer = new FileWriter(file);
            writer.write(previous.toJSONString());
        } else {
            writer = new FileWriter(file);
            JSONObject first = new JSONObject();
            first.put("activity_" + date, jsonObject);
            writer.write(first.toJSONString());
        }
        writer.flush();
        writer.close();
    }
}
