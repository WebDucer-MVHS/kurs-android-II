package de.mvhs.android.zeiterfassung;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

/**
 * Created by eugen on 29.01.17.
 */

public class SensorsActivity extends AppCompatActivity {
    private ToggleButton _gpsCommand;
    private EditText _longitude;
    private EditText _lattitude;
    private EditText _accuracy;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);

        // GPS Views
        _gpsCommand = (ToggleButton) findViewById(R.id.GpsCommand);
        _longitude = (EditText) findViewById(R.id.GpsLongitudeValue);
        _lattitude = (EditText) findViewById(R.id.GpsLatitudeValue);
        _accuracy = (EditText) findViewById(R.id.GpsAccuracyValue);
        _longitude.setKeyListener(null);
        _lattitude.setKeyListener(null);
        _accuracy.setKeyListener(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // GPS
        _gpsCommand.setOnCheckedChangeListener(new OnGpsSwitched());
    }

    @Override
    protected void onStop() {
        super.onStop();
        _gpsCommand.setOnCheckedChangeListener(null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // GPS
    }

    @Override
    protected void onPause() {
        super.onPause();
        // GPS ausschalten
        _gpsCommand.setChecked(false);
    }

    // Innere Klassen
    // GPS
    public class OnGpsSwitched implements CompoundButton.OnCheckedChangeListener {
        // Einstellungen
        final long _timeDelta = 1000; // 1 Sekunde
        final float _distanceDelta = 50; // 50 Meter
        LocationListener _locationListener;

        public OnGpsSwitched() {
            _locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    _longitude.setText(String.valueOf(location.getLongitude()));
                    _lattitude.setText(String.valueOf(location.getLatitude()));
                    _accuracy.setText(String.valueOf(location.getAccuracy()));
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                turnGpsOn();
            } else {
                turnGpsOff();
            }
        }

        private void turnGpsOff() {
            LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);

            if (ContextCompat.checkSelfPermission(SensorsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(SensorsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        SensorsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        100);
                return;
            }
            manager.removeUpdates(_locationListener);

            _longitude.setText("");
            _lattitude.setText("");
            _accuracy.setText("");
        }

        private void turnGpsOn() {
            LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);

            if (ContextCompat.checkSelfPermission(SensorsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(SensorsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        SensorsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        100);
                return;
            }
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, _timeDelta, _distanceDelta, _locationListener);
        }
    }
}
