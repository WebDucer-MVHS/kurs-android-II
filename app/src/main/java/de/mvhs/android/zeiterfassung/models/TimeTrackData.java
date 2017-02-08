package de.mvhs.android.zeiterfassung.models;

import android.content.Context;
import android.database.Cursor;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.InverseBindingAdapter;
import android.databinding.adapters.Converters;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

import de.mvhs.android.zeiterfassung.BR;
import de.mvhs.android.zeiterfassung.db.TimeDataContract;

/**
 * Created by eugen on 06.02.17.
 */

public class TimeTrackData extends BaseObservable {
    private DateFormat _dateFormatter;
    private DateFormat _timeFormatter;
    private Context _context;

    private Calendar _startTime = null;
    private Calendar _endTime = null;
    private int _pause = 0;
    private String _comment = null;

    public TimeTrackData(Context context) {
        // Init android formatter
        _dateFormatter = android.text.format.DateFormat.getDateFormat(context);
        _timeFormatter = android.text.format.DateFormat.getTimeFormat(context);
        _context = context;
    }

    public TimeTrackData(Context context, Cursor initData) throws ParseException {
        this(context);
        init(initData);
    }

    public void init(Cursor initData) throws ParseException {
        if (initData == null || !initData.moveToFirst()) {
            // keine Daten übergeben
            throw new IllegalArgumentException("No data found");
        }

        int index = initData.getColumnIndex(TimeDataContract.TimeData.Columns.START);
        if (index == -1) {
            throw new IllegalArgumentException("Column '" + TimeDataContract.TimeData.Columns.START + "' not found");
        }
        String data = initData.getString(index);
        setStartTime(TimeDataContract.Converter.parseFromDb(data));

        index = initData.getColumnIndex(TimeDataContract.TimeData.Columns.END);
        if (index == -1) {
            throw new IllegalArgumentException("Column '" + TimeDataContract.TimeData.Columns.END + "' not found");
        }
        if (!initData.isNull(index)) {
            setEndTime(TimeDataContract.Converter.parseFromDb(initData.getString(index)));
        }

        index = initData.getColumnIndex(TimeDataContract.TimeData.Columns.PAUSE);
        if (index == -1) {
            throw new IllegalArgumentException("Column '" + TimeDataContract.TimeData.Columns.PAUSE + "' not found");
        }
        setPause(initData.getInt(index));

        index = initData.getColumnIndex(TimeDataContract.TimeData.Columns.COMMENT);
        if (index == -1) {
            throw new IllegalArgumentException("Column '" + TimeDataContract.TimeData.Columns.COMMENT + "' not found");
        }
        if (!initData.isNull(index)) {
            setComment(initData.getString(index));
        }
    }

    @Bindable
    public String getDisplayStartDate() {
        return _startTime == null ? null : _dateFormatter.format(_startTime.getTime());
    }

    @Bindable
    public String getDisplayStartTime() {
        return _startTime == null ? null : _timeFormatter.format(_startTime.getTime());
    }

    @Bindable
    public Calendar getStartTime() {
        return _startTime;
    }

    public void setStartTime(Calendar startTime) {
        if (startTime != _startTime) {
            _startTime = startTime;
            notifyPropertyChanged(BR.startTime);
            notifyPropertyChanged(BR.displayStartDate);
            notifyPropertyChanged(BR.displayStartTime);
        }
    }

    @Bindable
    public String getDisplayEndDate() {
        return _endTime == null ? null : _dateFormatter.format(_endTime.getTime());
    }

    @Bindable
    public String getDisplayEndTime() {
        return _endTime == null ? null : _timeFormatter.format(_endTime.getTime());
    }

    @Bindable
    public Calendar getEndTime() {
        return _endTime;
    }

    public void setEndTime(Calendar endTime) {
        if (_endTime != endTime) {
            _endTime = endTime;
            notifyPropertyChanged(BR.endTime);
            notifyPropertyChanged(BR.displayEndDate);
            notifyPropertyChanged(BR.displayEndTime);
        }
    }

    @Bindable
    public int getPause() {
        return _pause;
    }

    public void setPause(int pause) {
        if (_pause != pause) {
            _pause = pause;
            notifyPropertyChanged(BR.pause);
        }
    }

    @Bindable
    public String getComment() {
        return _comment;
    }

    public void setComment(String comment) {
        if (_comment != comment) {
            _comment = comment;
            notifyPropertyChanged(BR.comment);
        }
    }

    public void onChangeStartDate(View view){
        Toast.makeText(_context, "Test", Toast.LENGTH_LONG).show();
    }

    @BindingAdapter("android:text")
    public static void setText(TextView view, int value){
        if(Integer.toString(value).equals(view.getText().toString())){
            return;
        }
        view.setText(value == 0 ? "" : Integer.toString(value));
    }

    @InverseBindingAdapter(attribute = "android:text")
    public static int getText(TextView view){
        String value = view.getText().toString();
        if(value == null || value.isEmpty()){
            return 0;
        }

        return Integer.parseInt(value);
    }
}
