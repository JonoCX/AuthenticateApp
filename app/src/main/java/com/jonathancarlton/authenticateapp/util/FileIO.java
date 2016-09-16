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
 * @author Jonathan Carlton
 */
public class FileIO {

    private Context context;

    public FileIO(Context context) {
        this.context = context;
    }

    public JSONObject readInJSON(String filename) throws IOException {
        File directory = context.getDir("json", Context.MODE_PRIVATE);
        File jsonFile = new File(directory, filename + ".json");
        FileInputStream inputStream = new FileInputStream(jsonFile);
        BufferedReader reader;
        JSONObject result = null;

        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(reader);
            result = (JSONObject) obj;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void writeJSON(JSONObject jsonObject, String fileName) throws IOException {
        String date = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss").format(Calendar.getInstance().getTime());
        File directory = context.getDir("json", Context.MODE_PRIVATE);
        File file = new File(directory, fileName + ".json");
        FileWriter writer;

        if (!file.createNewFile()) {
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
