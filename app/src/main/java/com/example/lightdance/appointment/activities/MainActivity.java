package com.example.lightdance.appointment.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.lightdance.appointment.R;
import com.example.lightdance.appointment.fragments.BrowseFragment;
import com.example.lightdance.appointment.fragments.DatePickerFragment;
import com.example.lightdance.appointment.fragments.NewAppointmentFragment;
import com.example.lightdance.appointment.fragments.NewsFragment;
import com.example.lightdance.appointment.fragments.PersonalCenterFragment;

public class MainActivity extends AppCompatActivity implements DatePickerFragment.dateListener{

    private int yearSelect;
    private int monthSelect;
    private int daySelect;

    private BottomNavigationView bottomNavigationView;

    private NewAppointmentFragment newAppointmentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        newAppointmentFragment = new NewAppointmentFragment();

        changeFragment(newAppointmentFragment);

        bottomNavigationView = (BottomNavigationView)findViewById(R.id.main_bottomnavigationview);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_appointment:
                        Toast.makeText(MainActivity.this, "first item" , Toast.LENGTH_SHORT).show();
                        changeFragment(new NewAppointmentFragment());
                        break;
                    case R.id.menu_browse:
                        Toast.makeText(MainActivity.this , "second item" , Toast.LENGTH_SHORT).show();
                        changeFragment(new BrowseFragment());
                        break;
                    case R.id.menu_news:
                        Toast.makeText(MainActivity.this , "third item" , Toast.LENGTH_SHORT).show();
                        changeFragment(new NewsFragment());
                        break;
                    case R.id.menu_me:
                        Toast.makeText(MainActivity.this , "fourth item" , Toast.LENGTH_SHORT).show();
                        changeFragment(new PersonalCenterFragment());
                        break;
                }
                return true;
            }

        });
    }

    //动态加载碎片的方法 TEST
    private void changeFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container,fragment);
        transaction.commit();
    }

    @Override
    public void dateSave(int year, int month, int day) {
        yearSelect  = year;
        monthSelect = month;
        daySelect   = day;
        changeData();
    }

    public void changeData(){
            newAppointmentFragment.setDate(yearSelect,monthSelect,daySelect);
    }
}