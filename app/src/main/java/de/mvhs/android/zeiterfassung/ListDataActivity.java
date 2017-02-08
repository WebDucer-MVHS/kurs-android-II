package de.mvhs.android.zeiterfassung;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Kurs on 21.12.2016.
 */

public class ListDataActivity extends AppCompatActivity
    implements IItemSelected {
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_list_data);
  }

  @Override
  public void onItemSelected(long id) {
    FragmentManager manager = getSupportFragmentManager();
    Fragment detailFragment = manager.findFragmentById(R.id.DetailFragment);

    if (detailFragment instanceof IChangeContent && detailFragment.isAdded()) {
      // Fragment vorhanden
      ((IChangeContent) detailFragment).onChangeContent(id);
    } else {
      // Intent für die Bearbeitung
      Intent detailIntent = new Intent(this, BindableEditActivity.class);
      detailIntent.putExtra(BindableEditActivity.ID_KEY, id);
      startActivity(detailIntent);
    }
  }

  @Override
  public boolean canEditOverSelection() {
    FragmentManager manager = getSupportFragmentManager();
    Fragment detailFragment = manager.findFragmentById(R.id.DetailFragment);

    if (detailFragment instanceof IChangeContent && detailFragment.isAdded()) {
      return false;
    }

    return true;
  }
}
