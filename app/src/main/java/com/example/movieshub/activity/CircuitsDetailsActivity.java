package com.example.movieshub.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.example.movieshub.R;
import com.example.movieshub.util.Constant;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import okhttp3.HttpUrl;

public class CircuitsDetailsActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    protected LocationRequest mLocationRequest;

    private TextView textViewName, textViewAddress, textViewTel;
    private Button button;

    private String address;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circuits_details);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");
        Log.i("Bundle", bundle.getString("cinema"));
        Log.i("Bundle", bundle.getString("name"));
        Log.i("Bundle", bundle.getString("region"));
        Log.i("Bundle", bundle.getString("address"));
        Log.i("Bundle", bundle.getString("tel"));
        
        textViewName = findViewById(R.id.text_view_name);
        textViewAddress = findViewById(R.id.text_view_address);
        textViewTel = findViewById(R.id.text_view_tel);
        
        textViewName.setText(bundle.getString("name"));
        textViewAddress.setText(bundle.getString("address"));
        textViewTel.setText(bundle.getString("tel"));

        button = findViewById(R.id.btn_show_map);
        button.setOnClickListener(v -> {
            if (!mGoogleApiClient.isConnected())
                mGoogleApiClient.connect();

            if (mLastLocation != null) {
                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();

                HttpUrl.Builder builder = HttpUrl.parse(Constant.API_KEY_MAP_DIR).newBuilder();
                builder.addQueryParameter("api", String.valueOf(1))
                        .addQueryParameter("origin",latitude + "," + longitude)
                        .addQueryParameter("destination", textViewAddress.getText().toString());

                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(builder.build().toString()));
                i.setPackage("com.google.android.apps.maps");
                if (i.resolveActivity(getPackageManager())!= null)
                    startActivity(i);
            }
        });
        
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        
        
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(location -> {
                if (location != null)
                    mLastLocation = location;
            });
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

    @Override
    public void onLocationChanged(Location location) { mLastLocation = location; }
}
