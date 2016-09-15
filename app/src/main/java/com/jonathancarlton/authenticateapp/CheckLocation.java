package com.jonathancarlton.authenticateapp;

import android.os.Bundle;

/**
 * Created by Jonathan on 15-Sep-16.
 */
public class CheckLocation {

    // 54.980690, -1.614147
    private static final double CLAREMONT_LAT = 54.980690;
    private static final double CLAREMONT_LNG = -1.614147;

    public CheckLocation() {}

    public double distance(double lat, double lng) {
        // earth's radius in miles, 6371 for km output
        double earthRadius = 3958.75;

        double dLat = Math.toRadians(lat - CLAREMONT_LAT);
        double dLng = Math.toRadians(lng - CLAREMONT_LNG);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(CLAREMONT_LAT)) * Math.cos(Math.toRadians(lat));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double dist = earthRadius * c;

        return dist;
    }

}
