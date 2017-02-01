package de.mvhs.android.zeiterfassung;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by eugen on 29.01.17.
 */

public class SensorsActivity extends AppCompatActivity {
    // GPS
    private ToggleButton _gpsCommand;
    private EditText _longitude;
    private EditText _latitude;
    private EditText _accuracy;

    // Beschleunigung
    private ToggleButton _accCommand;
    private EditText _accX;
    private EditText _accY;
    private EditText _accZ;

    // Bilder
    private Button _takePictureCommand;
    private Button _selectPictureCommand;
    private ImageView _imageContent;
    private TextView _resolutionValue;
    private String _tempFilePath;

    // Result Constants
    private final static int _SELECT_PICTURE = 100;
    private final static int _TAKE_PICTURE = 200;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);

        // GPS Views
        _gpsCommand = (ToggleButton) findViewById(R.id.GpsCommand);
        _longitude = (EditText) findViewById(R.id.GpsLongitudeValue);
        _latitude = (EditText) findViewById(R.id.GpsLatitudeValue);
        _accuracy = (EditText) findViewById(R.id.GpsAccuracyValue);
        _longitude.setKeyListener(null);
        _latitude.setKeyListener(null);
        _accuracy.setKeyListener(null);

        // Beschleunigung
        _accCommand = (ToggleButton) findViewById(R.id.AccelerationCommand);
        _accX = (EditText) findViewById(R.id.AccXValue);
        _accY = (EditText) findViewById(R.id.AccYValue);
        _accZ = (EditText) findViewById(R.id.AccZValue);
        _accX.setKeyListener(null);
        _accY.setKeyListener(null);
        _accZ.setKeyListener(null);

        // Bilder
        _takePictureCommand = (Button) findViewById(R.id.TakePictureCommand);
        _selectPictureCommand = (Button) findViewById(R.id.SelectPictureCommand);
        _imageContent = (ImageView) findViewById(R.id.ImageContent);
        _resolutionValue = (TextView) findViewById(R.id.ResolutionValue);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // GPS
        _gpsCommand.setOnCheckedChangeListener(new OnGpsSwitched());
        // Acceleration
        _accCommand.setOnCheckedChangeListener(new OnAccelerationSwitched());
        // Images
        _takePictureCommand.setOnClickListener(new OnTakePicture());
        _selectPictureCommand.setOnClickListener(new OnSelectPicture());
    }

    @Override
    protected void onStop() {
        super.onStop();
        _gpsCommand.setOnCheckedChangeListener(null);
        _accCommand.setOnCheckedChangeListener(null);
        _imageContent.setImageBitmap(null);
        _takePictureCommand.setOnClickListener(null);
        _selectPictureCommand.setOnClickListener(null);
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
        _accCommand.setChecked(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Vom Benutzer abgebrochen
        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case _SELECT_PICTURE:
                showGalleryPicture(data);
                break;

            case _TAKE_PICTURE:
                showCameraPicture(data);
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showCameraPicture(Intent data) {
        if (_tempFilePath == null) {
            // nur Vorschaubild
            Bundle extras = data.getExtras();
            Bitmap image = (Bitmap) extras.get("data");

            _imageContent.setImageBitmap(image);
            _resolutionValue.setText(String.format("%d x %d", image.getWidth(), image.getHeight()));
        } else {
            // Volle Auflösung
            Bitmap image = BitmapFactory.decodeFile(_tempFilePath);
            _imageContent.setImageBitmap(image);
            _resolutionValue.setText(String.format("%d x %d", image.getWidth(), image.getHeight()));
        }
    }

    private void showGalleryPicture(Intent data) {
        Uri imagePath = data.getData();
        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(imagePath);
            Bitmap image = BitmapFactory.decodeStream(imageStream);
            _imageContent.setImageBitmap(image);
            _resolutionValue.setText(String.format("%d x %d", image.getWidth(), image.getHeight()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (imageStream != null) {
                try {
                    imageStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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
                    _latitude.setText(String.valueOf(location.getLatitude()));
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
            _latitude.setText("");
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

    // Beschelunigung
    public class OnAccelerationSwitched implements CompoundButton.OnCheckedChangeListener, SensorEventListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                onAccOn();
            } else {
                onAccOff();
            }
        }

        private void onAccOn() {
            SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            Sensor accSensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            manager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        private void onAccOff() {
            SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            Sensor accSensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            manager.unregisterListener(this);

            _accX.setText("");
            _accY.setText("");
            _accZ.setText("");
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            // Beschleunigungssensor hat 3 Werte
            _accX.setText(String.valueOf(event.values[0]));
            _accY.setText(String.valueOf(event.values[1]));
            _accZ.setText(String.valueOf(event.values[2]));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Wenn die Egnauigkeit des Sensors sich ändert
        }
    }

    public class OnTakePicture implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            _tempFilePath = null;
            boolean fullResolution = true;
            if (fullResolution) {
                File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM), "Camera");

                // Temporäre Datei für das Bild
                // Berechtigung für das Schreiben NICHT vergessen vom Benutzer einzufordern
                try {
                    File imageFile = File.createTempFile("temp_", ".jpg", storageDir);

                    // Dateipfad der kamera-App bekannt geben (< API24)
                    //takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                    // Dateipfad der kamera-App bekannt geben (>= API24)
                    Uri fileUri = FileProvider.getUriForFile(SensorsActivity.this,
                            BuildConfig.APPLICATION_ID + ".fileprovider", imageFile);
                    takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

                    // Dateipfad vormerken
                    _tempFilePath = imageFile.getAbsolutePath();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (takeIntent.resolveActivity(getPackageManager()) != null) {
                // Originalbild
                startActivityForResult(takeIntent, _TAKE_PICTURE);
            }
        }
    }

    public class OnSelectPicture implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // Impliziter Intent für die Gallery
            Intent selectIntent = new Intent(Intent.ACTION_PICK);
            selectIntent.setType("image/*");

            // Prüfen, ob eine passende App installiert ist
            if (selectIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(selectIntent, _SELECT_PICTURE);
            }
        }
    }
}
