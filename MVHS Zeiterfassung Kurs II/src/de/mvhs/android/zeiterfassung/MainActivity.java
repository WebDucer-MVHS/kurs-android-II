package de.mvhs.android.zeiterfassung;

import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import de.mvhs.android.zeiterfassung.db.WorktimeTable;

public class MainActivity extends Activity {
  private static final DateFormat _TFmedium = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // Referenzierung der Buttons aus dem Layout
    Button start = (Button) findViewById(R.id.cmd_start);
    Button end = (Button) findViewById(R.id.cmd_end);

    // Zuweisung der Click-Events zu den Buttons
    start.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        runStartClick();
      }
    });
    end.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        runEndClick();
      }
    });
  }

  @Override
  protected void onStart() {
    super.onStart();

    Button startButton = (Button) findViewById(R.id.cmd_start);
    Button endButton = (Button) findViewById(R.id.cmd_end);

    // Prüfen, ob ein offener Eintrag vorhanden ist
    WorktimeTable table = new WorktimeTable(this);
    long id = table.getOpenWorktime();
    // Wenn ein Eintrag vorhanden ist
    if (id > 0) {
      startButton.setEnabled(false);
      endButton.setEnabled(true);
      Date start = table.getStartDate(id);
      EditText startTime = (EditText) findViewById(R.id.txt_start_time);
      startTime.setText(_TFmedium.format(start));
    }
    // Wenn kein Eintrag vorhanden ist
    else {
      startButton.setEnabled(true);
      endButton.setEnabled(false);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = this.getMenuInflater();
    inflater.inflate(R.menu.main, menu);

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    // Aufrufen der Liste der Einträge
      case R.id.opt_list:
        Intent listIntent = new Intent(this, RecordListActivity.class);
        this.startActivity(listIntent);
        break;

      // Aufrufen der App Einstellungen
      case R.id.opt_preferences:
        Intent prefs = new Intent(this, AppPreferenceActivity.class);
        this.startActivity(prefs);
        break;

      default:
        break;
    }

    return super.onOptionsItemSelected(item);
  }

  private void runStartClick() {
    // Referenzierung auf das Layout
    EditText startTime = (EditText) findViewById(R.id.txt_start_time);
    Button startButton = (Button) findViewById(R.id.cmd_start);
    Button endButton = (Button) findViewById(R.id.cmd_end);
    // Aktuelles Datum bestimmen
    Date dateNow = new Date();

    // Aktuelle Datum ins Feld schreiben
    startTime.setText(_TFmedium.format(dateNow));
    startButton.setEnabled(false);
    endButton.setEnabled(true);

    // Speichern in der Datenbank
    WorktimeTable table = new WorktimeTable(this);
    table.saveWorktime(dateNow);
  }

  private void runEndClick() {
    // Referenzierung auf das Layout
    EditText endTime = (EditText) findViewById(R.id.txt_end_time);
    Button startButton = (Button) findViewById(R.id.cmd_start);
    Button endButton = (Button) findViewById(R.id.cmd_end);
    // Aktuelles Datum bestimmen
    Date dateNow = new Date();

    // Aktuelle Datum ins Feld schreiben
    endTime.setText(_TFmedium.format(dateNow));
    startButton.setEnabled(true);
    endButton.setEnabled(false);

    // Enddatum speichern
    WorktimeTable table = new WorktimeTable(this);
    long id = table.getOpenWorktime();
    if (id > 0) {
      table.updateWorktime(id, dateNow);
    }
  }
}