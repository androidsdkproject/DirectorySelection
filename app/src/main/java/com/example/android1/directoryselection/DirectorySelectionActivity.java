package com.example.android1.directoryselection;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class DirectorySelectionActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory_selection);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, DirectorySelectionFragment.newInstance())
                    .commit();
        }
    }
}
