package ca.staugustinechs.staugustineapp.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Fragments.SettingsFragment;
import ca.staugustinechs.staugustineapp.R;

public class Launcher extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPreferences sharedPrefs = getSharedPreferences(SettingsFragment.sharedPrefFile, MODE_PRIVATE);
        boolean isDark = sharedPrefs.getBoolean(SettingsFragment.DARK_KEY, false);
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            getDelegate().applyDayNight();
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            getDelegate().applyDayNight();
        }
        //FIREBASE REMOTE CONFIG
        final FirebaseRemoteConfig frc = FirebaseRemoteConfig.getInstance();
        //IF IT HAS BEEN LESS THAN 360 SECONDS, WE DON'T GET THE LATEST RC VALUES
        //AND INSTEAD USE THE ONES WE HAVE SAVED IN CACHE
        frc.fetch(360).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    //USE RC VALUES WE HAVE FETCHED
                    frc.activateFetched();
                }
                //SET RC VARIABLES
                AppUtils.setDefaultVariables();

                //GO TO MAIN
                Intent intent = new Intent(Launcher.this, Main.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });

        this.setTheme(R.style.AppTheme_Launcher);
        super.onCreate(savedInstanceState);
    }
}
