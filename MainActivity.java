package com.infenodesigns.feedsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.infenodesigns.feedsapp.fragments.AllPostsFragment;
import com.infenodesigns.feedsapp.fragments.MyPostsFragment;
import com.infenodesigns.feedsapp.fragments.NotificationsFragment;
import com.infenodesigns.feedsapp.fragments.PostsFragment;
import com.infenodesigns.feedsapp.fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFireStore;

    private String mCurrentUserId;

    private Toolbar mToolbar;

    private BottomNavigationView mBottomNavigationView;

    private AllPostsFragment allPostsFragment;
    private NotificationsFragment notificationsFragment;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Feeds App");

        mAuth = FirebaseAuth.getInstance();
        mFireStore = FirebaseFirestore.getInstance();

        mBottomNavigationView = findViewById(R.id.bottom_navigation_view);

        allPostsFragment = new AllPostsFragment();
        notificationsFragment = new NotificationsFragment();
        settingsFragment = new SettingsFragment();

        initializeFragment();

        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);

                switch (menuItem.getItemId()) {

                    case R.id.bottom_post_menu:
                        replaceFragment(allPostsFragment, currentFragment);
                        return true;
                    case R.id.bottom_notification_menu:
                        replaceFragment(notificationsFragment, currentFragment);
                        return true;
                    case R.id.bottom_settings_menu:
                        replaceFragment(settingsFragment, currentFragment);
                        return true;
                    default:
                        return false;

                }

            }
        });

    }

    private void initializeFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.add(R.id.frame_layout, allPostsFragment);
        fragmentTransaction.add(R.id.frame_layout, notificationsFragment);
        fragmentTransaction.add(R.id.frame_layout, settingsFragment);

        fragmentTransaction.hide(notificationsFragment);
        fragmentTransaction.hide(settingsFragment);

        fragmentTransaction.commit();
    }

    private void replaceFragment(Fragment fragment, Fragment currentFragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        if (fragment == allPostsFragment) {
            fragmentTransaction.hide(notificationsFragment);
            fragmentTransaction.hide(settingsFragment);
        }

        if (fragment == notificationsFragment) {
            fragmentTransaction.hide(allPostsFragment);
            fragmentTransaction.hide(settingsFragment);
        }

        if (fragment == settingsFragment) {
            fragmentTransaction.hide(allPostsFragment);
            fragmentTransaction.hide(notificationsFragment);
        }

        fragmentTransaction.show(fragment);

        //fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            sendToLogin();
        }
    }

    private void sendToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void hideBottomNavigation() {
        mBottomNavigationView.setVisibility(View.GONE);
    }

    public void showBottomNavigation() {
        mBottomNavigationView.setVisibility(View.VISIBLE);
    }
}
