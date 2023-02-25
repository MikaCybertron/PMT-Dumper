package platinmods.com.dumper.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.topjohnwu.superuser.Shell;

import platinmods.com.dumper.BuildConfig;
import platinmods.com.dumper.R;

public class MainActivity extends AppCompatActivity {

    static {
        // Set settings before the main shell can be created
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
        Shell.setDefaultBuilder(Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        );
    }


    BottomNavigationView bottomNavigationView;
    MemoryFragment memoryFragment;
    ConsoleFragment consoleFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        memoryFragment = new MemoryFragment();
        consoleFragment = new ConsoleFragment();

        bottomNavigationView = findViewById(R.id.nav_view);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_memory:
                        loadFragment(memoryFragment);
                        break;
                    case R.id.nav_console:
                        loadFragment(consoleFragment);
                        break;
                }
                return true;
            }
        });

        requestPermission();
        Shell.getShell(shell -> {
            loadFragment(memoryFragment);
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (fragment.isAdded()) {
            fragmentTransaction.show(fragment);
        } else {
            fragmentTransaction.add(R.id.main_layout, fragment);
        }
        for (Fragment frag : getSupportFragmentManager().getFragments()) {
            if (frag != fragment) {
                fragmentTransaction.hide(frag);
            }
        }
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.appbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.Github:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/MikaCybertron/PMT-Dumper")));
                break;
            case R.id.Platinmods:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://platinmods.com/threads/dump-libil2cpp-so-and-global-metadata-dat-with-pmt-dumper.178986/")));
                break;
        }
        return true;
    }

    private void requestPermission() {
        String[] PERMISSIONS =  { android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, PERMISSIONS, 101);
    }

}