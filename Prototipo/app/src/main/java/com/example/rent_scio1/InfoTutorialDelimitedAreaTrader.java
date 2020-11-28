package com.example.rent_scio1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class InfoTutorialDelimitedAreaTrader extends AppCompatActivity {

    private Toolbar info_tutorial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_tutorial_delimited_area_trader);


        TabLayout tabLayout = findViewById(R.id.tabella_tutorial);
        ViewPager viewPager = findViewById(R.id.viewPager_tutorial);

        tabLayout.setupWithViewPager(viewPager);

        setupViewPager(viewPager);

        initViews();
    }


    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager(), 1);
        adapter.addFragment(new CostruisciFragment(), "Costruisci");
        adapter.addFragment(new EliminaUltimoFragmentTutorial(), "Elimina l'ultimo");
        adapter.addFragment(new EliminaTuttoFragmentTutorial(), "Elimina tutto");
        adapter.addFragment(new ConfermaFragmentTutorial(), "Conferma");
        viewPager.setAdapter(adapter);
    }

    private void initViews(){
        info_tutorial = findViewById(R.id.toolbar_info_tutorial_);
        setSupportActionBar(info_tutorial);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    static class Adapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager, int behavior) {
            super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}