package com.alejandrolai.sunset;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Alejandro on 4/1/16.
 */
public class FetchAddressIntentService extends IntentService {

    private static final String TAG = "FetchAddressIS";

    /**
     * The receiver where results are forwarded from this service.
     */
    protected ResultReceiver mReceiver;

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public FetchAddressIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";

        mReceiver = intent.getParcelableExtra("receiver");

        // Check if receiver was properly registered.
        if (mReceiver == null) {
            return;
        }

        // Get the location passed to this service through an extra.
        LatLng location = intent.getParcelableExtra("location");

        // Make sure that the location data was really sent over through an extra. If it wasn't,
        // send an error error message and return.
        if (location == null) {
            errorMessage = "no data provided";
            deliverResultToReceiver(1, errorMessage);
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // Address found using the Geocoder.
        List<Address> addresses = null;

        try {
            // Using getFromLocation() returns an array of Addresses for the area immediately
            // surrounding the given latitude and longitude. The results are a best guess and are
            // not guaranteed to be accurate.
            addresses = geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = "Service not available";
            Log.e("FetchAIS",ioException.toString());
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = "Invalid latlng";
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "no address found";
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(1, errorMessage);
        } else {
            Address address = addresses.get(0);
            String city = address.getLocality();
            String state = address.getAdminArea();
            deliverResultToReceiver(0, city + ", " + state);
        }
    }

    /**
     * Sends a resultCode and message to the receiver.
     */
    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString("result", message);
        mReceiver.send(resultCode, bundle);
    }
}
