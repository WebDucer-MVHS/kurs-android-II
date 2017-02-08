package de.mvhs.android.zeiterfassung;

import android.content.ContentUris;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.text.ParseException;
import java.util.Calendar;

import de.mvhs.android.zeiterfassung.databinding.ActivityEditBindableBinding;
import de.mvhs.android.zeiterfassung.db.TimeDataContract;
import de.mvhs.android.zeiterfassung.models.TimeTrackData;

/**
 * Created by eugen on 06.02.17.
 */

public class BindableEditActivity extends AppCompatActivity {
    public final static String ID_KEY = "TimeTrackingId";
    private TimeTrackData _viewModel = null;
    private long _id = -1;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ID aus der Nachricht extrahieren
        _id = getIntent().getLongExtra(ID_KEY, -1);

        // Binding erzeugen
        ActivityEditBindableBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_bindable);
        _viewModel = new TimeTrackData(this);
        binding.setTimeData(_viewModel);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (_id == -1){
            // Neuer Datensatz
            _viewModel.setStartTime(Calendar.getInstance());
            _viewModel.setEndTime(Calendar.getInstance());
            setTitle(R.string.NewDataActivityTitle);
        } else {
            // Datensatz laden
            loadData();
        }
    }

    private void loadData() {
        Uri dataUri =
                ContentUris.withAppendedId(TimeDataContract.TimeData.CONTENT_URI, _id);

        Cursor data = getContentResolver().query(dataUri, null, null, null, null);
        try {
            _viewModel.init(data);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
