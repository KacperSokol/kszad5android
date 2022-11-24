package com.example.sensorapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LocationActivity extends AppCompatActivity {

    private Button locationBtn;
    private Button addressBtn;
    private Location lastLocation;
    private TextView locationTextView;
    private TextView locationAddressTextView;
    private FusedLocationProviderClient fusedLocationProviderClient;

    final private int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        locationBtn = findViewById(R.id.location_button);
        locationTextView = findViewById(R.id.text_location);
        locationAddressTextView = findViewById(R.id.text_location_address);
        addressBtn = findViewById(R.id.address_button);

        addressBtn.setOnClickListener(v -> executeGeocoding());

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationBtn.setOnClickListener(v -> getLocation());

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode)
        {
            case REQUEST_LOCATION_PERMISSION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    getLocation();
                } else {
                    Toast.makeText(this, "denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void getLocation()
    {
        Log.d("sen", "0");
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
        else
        {
            Log.d("sen", "1");
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(
                    location -> {
                        Log.d("sen", "2");
                        if(location != null)
                        {
                            lastLocation = location;
                            locationTextView.setText(getString(R.string.location_text, location.getLatitude(), location.getLongitude(), location.getTime()));
                        }
                        else
                        {
                            locationTextView.setText("brak");
                        }
                    }
            );
        }
    }
    private String locationGeocoding(Context context, Location location)
    {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;

        String resultMessage = "";

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLatitude(), 1);
        } catch (IOException ioException)
        {
            resultMessage = "nie dziala";
        }

        if(addresses == null || addresses.isEmpty())
        {
            if(resultMessage.isEmpty())
            {
                resultMessage = "nie znaleziono";
            }
        }
        else
        {
            Address address = addresses.get(0);
            List<String> addressParts = new ArrayList<>();

            for(int i=0; i <= address.getMaxAddressLineIndex(); i++)
            {
                addressParts.add(address.getAddressLine(i));
            }
            resultMessage = TextUtils.join("\n", addressParts);
        }

        return resultMessage;
    }
    private void executeGeocoding()
    {
        if(lastLocation != null)
        {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<String> returnedAddress = executorService.submit(() -> locationGeocoding(getApplicationContext(), lastLocation));

            try {
                String result = returnedAddress.get();
                locationAddressTextView.setText(getString(R.string.address_text, result, System.currentTimeMillis()));


            } catch(ExecutionException | InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
    }
}