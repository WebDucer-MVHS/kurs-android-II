package de.mvhs.android.zeiterfassung;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import de.mvhs.android.zeiterfassung.db.ZeitContentProvider;
import de.mvhs.android.zeiterfassung.db.ZeitTabelle;

public class EditActivity extends Activity {
	/* Klassenvariablen */
	public final static String KEY_ID = "key_id";
	private long _ID = -1;
	private Date _StartDate = null;
	private Date _EndDate = null;
	private DateFormat _DF = DateFormat.getDateInstance(SimpleDateFormat.SHORT);
	private DateFormat _TF = DateFormat.getTimeInstance(SimpleDateFormat.SHORT);
	private EditText _StartDatum;
	private EditText _StartZeit;
	private EditText _EndDatum;
	private EditText _EndZeit;
	private EditText _KontaktName;
	private Button _KontaktSuche;

	/* Listener definitionen */
	private OnLongClickListener _StartDateListener = new OnLongClickListener() {

		public boolean onLongClick(View v) {
			return onStartDateClicked();
		}
	};
	private OnLongClickListener _StartTimeListener = new OnLongClickListener() {

		public boolean onLongClick(View v) {
			return onStartTimeClicked();
		}
	};
	private OnLongClickListener _EndDateListener = new OnLongClickListener() {

		public boolean onLongClick(View v) {
			return onEndDateClicked();
		}
	};
	private OnLongClickListener _EndTimeListener = new OnLongClickListener() {

		public boolean onLongClick(View v) {
			return onEndTimeClicked();
		}
	};
	private OnDateSetListener _StartDateSet = new OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			_StartDate = new Date(year + 1900, monthOfYear, dayOfMonth,
					_StartDate.getHours(), _StartDate.getMinutes());
			_StartDatum.setText(_DF.format(_StartDate));
		}
	};
	private OnTimeSetListener _StartTimeSet = new OnTimeSetListener() {

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			_StartDate = new Date(_StartDate.getYear(), _StartDate.getMonth(),
					_StartDate.getDate(), hourOfDay, minute);
			_StartZeit.setText(_TF.format(_StartDate));
		}
	};
	private OnDateSetListener _EndDateSet = new OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			_EndDate = new Date(year + 1900, monthOfYear, dayOfMonth,
					_EndDate.getHours(), _EndDate.getMinutes());
			_EndDatum.setText(_DF.format(_EndDate));
		}
	};
	private OnTimeSetListener _EndTimeSet = new OnTimeSetListener() {

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			_EndDate = new Date(_EndDate.getYear(), _EndDate.getMonth(),
					_EndDate.getDate(), hourOfDay, minute);
			_EndZeit.setText(_TF.format(_EndDate));
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);

		if (getIntent().getExtras() != null
				&& getIntent().getExtras().containsKey(KEY_ID)) {
			_ID = getIntent().getLongExtra(KEY_ID, -1);
		}

		_StartDatum = (EditText) findViewById(R.id.startDate);
		_StartZeit = (EditText) findViewById(R.id.startTime);
		_EndDatum = (EditText) findViewById(R.id.endDate);
		_EndZeit = (EditText) findViewById(R.id.endTime);
		_KontaktName = (EditText) findViewById(R.id.kontaktName);
		_KontaktSuche = (Button) findViewById(R.id.kontaktSuche);
	}

	@Override
	protected void onStart() {
		// Initialisierung der Listener
		// -- Direkte Eingabe verhindern
		_StartDatum.setKeyListener(null);
		_StartZeit.setKeyListener(null);
		_EndDatum.setKeyListener(null);
		_EndZeit.setKeyListener(null);

		// -- Auf LongClick reagieren
		_StartDatum.setOnLongClickListener(_StartDateListener);
		_StartZeit.setOnLongClickListener(_StartTimeListener);
		_EndDatum.setOnLongClickListener(_EndDateListener);
		_EndZeit.setOnLongClickListener(_EndTimeListener);

		// -- Klick Listener registrieren
		_KontaktSuche.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				runSearch();
			}
		});

		// Daten laden
		loadData();

		super.onStart();
	}

	@Override
	protected void onStop() {
		// Listener deaktivieren
		_StartDatum.setOnLongClickListener(null);
		_StartZeit.setOnLongClickListener(null);
		_EndDatum.setOnLongClickListener(null);
		_EndZeit.setOnLongClickListener(null);

		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.menu_edit, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.opt_save:
			ContentValues values = new ContentValues();
			values.put(ZeitTabelle.STARTZEIT,
					ZeitTabelle.SDF.format(_StartDate));
			values.put(ZeitTabelle.ENDZEIT, ZeitTabelle.SDF.format(_EndDate));
			values.put(ZeitTabelle.KONTAKT, _KontaktName.getText().toString());
			getContentResolver().update(
					Uri.withAppendedPath(ZeitContentProvider.CONTENT_URI,
							String.valueOf(_ID)), values, null, null);
			this.finish();

			break;
		case R.id.opt_delete:
			// Abfrage, ob wirklich gelöscht werden soll
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setTitle(R.string.title_delete)
					.setMessage(R.string.delete_confirmation)
					.setNegativeButton(R.string.cmd_no,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
									EditActivity.this.finish();
								}
							})
					.setPositiveButton(R.string.cmd_yes,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									getContentResolver()
											.delete(Uri
													.withAppendedPath(
															ZeitContentProvider.CONTENT_URI,
															String.valueOf(_ID)),
													null, null);

									EditActivity.this.finish();
								}
							});

			builder.create().show();

			break;
		case R.id.opt_cancel:
			this.finish();

			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Setzen der Werte
	 */
	private void loadData() {
		if (_ID > 0) {
			Cursor data = getContentResolver().query(
					Uri.withAppendedPath(ZeitContentProvider.CONTENT_URI,
							String.valueOf(_ID)), null, null, null, null);
			if (data.moveToFirst()) {
				try {
					_StartDate = ZeitTabelle.SDF.parse(data.getString(data
							.getColumnIndex(ZeitTabelle.STARTZEIT)));

					_EndDate = null;
					if (!data.isNull(data.getColumnIndex(ZeitTabelle.ENDZEIT))) {
						_EndDate = ZeitTabelle.SDF.parse(data.getString(data
								.getColumnIndex(ZeitTabelle.ENDZEIT)));
					}

					// Setzen der Startzeit, wenn nicht leer
					if (_StartDate != null) {
						_StartDatum.setText(_DF.format(_StartDate));
						_StartZeit.setText(_TF.format(_StartDate));
					} else {
						_StartDatum.setText("");
						_StartZeit.setText("");
					}
					// Setzen der Endzeit, wenn nicht leer
					if (_EndDate != null) {
						_EndDatum.setText(_DF.format(_EndDate));
						_EndZeit.setText(_TF.format(_EndDate));
					} else {
						_EndDatum.setText("");
						_EndZeit.setText("");
					}
					// Kontakt aus der Datenbak auslesen
					if (!data.isNull(data.getColumnIndex(ZeitTabelle.KONTAKT))) {
						_KontaktName.setText(data.getString(data
								.getColumnIndex(ZeitTabelle.KONTAKT)));
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Starten des DatePicker Dialoges für die Eingabe des Startdatums
	 * 
	 * @return
	 */
	private boolean onStartDateClicked() {
		DatePickerDialog dpd = new DatePickerDialog(this, _StartDateSet,
				_StartDate.getYear() + 1900, _StartDate.getMonth(),
				_StartDate.getDate());
		dpd.show();
		return true;
	}

	/**
	 * Starten des TimePicker Dialoges für die Eingabe der Startzeit
	 * 
	 * @return
	 */
	private boolean onStartTimeClicked() {
		TimePickerDialog tpd = new TimePickerDialog(this, _StartTimeSet,
				_StartDate.getHours(), _StartDate.getMinutes(),
				android.text.format.DateFormat.is24HourFormat(this));
		tpd.show();
		return true;
	}

	/**
	 * Starten des DatePicker Dialoges für die Eingabe des Enddatums
	 * 
	 * @return
	 */
	private boolean onEndDateClicked() {
		DatePickerDialog dpd = new DatePickerDialog(this, _EndDateSet,
				_EndDate.getYear() + 1900, _EndDate.getMonth(),
				_EndDate.getDate());
		dpd.show();
		return true;
	}

	/**
	 * Starten des TimePicker Dialoges für die Eingabe der Endzeit
	 * 
	 * @return
	 */
	private boolean onEndTimeClicked() {
		TimePickerDialog tpd = new TimePickerDialog(this, _EndTimeSet,
				_EndDate.getHours(), _EndDate.getMinutes(),
				android.text.format.DateFormat.is24HourFormat(this));
		tpd.show();
		return true;
	}

	/**
	 * Suche der Kontakte in der Peopele / Kontakte-App Datenbank
	 */
	private void runSearch() {
		String[] projection = new String[] { ContactsContract.Contacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME };
		String where = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '1'";
		String[] whereArgs = null;
		String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
				+ " COLLATE LOCALIZED ASC";

		final Cursor data = getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI, projection, where,
				whereArgs, sortOrder);

		// Dialog mit Auflistung erzeugen
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Bitte Konakt auswählen!").setSingleChoiceItems(data,
				-1, ContactsContract.Contacts.DISPLAY_NAME,
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						if (data != null
								&& data.moveToPosition(which)
								&& !data.isNull(data
										.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))) {
							_KontaktName.setText(data.getString(data
									.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
						}
						dialog.dismiss();
					}
				});

		// Dialog anzeigen
		builder.create().show();
	}

}
