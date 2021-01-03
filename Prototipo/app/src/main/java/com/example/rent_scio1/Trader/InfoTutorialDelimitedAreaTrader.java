package com.example.rent_scio1.Trader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.example.rent_scio1.R;
import com.example.rent_scio1.Trader.TutorialDelimitedArea.BuildFragment;
import com.example.rent_scio1.Trader.TutorialDelimitedArea.ConfirmFragmentTutorial;
import com.example.rent_scio1.Trader.TutorialDelimitedArea.DeleteAllragmentTutorial;
import com.example.rent_scio1.Trader.TutorialDelimitedArea.DeleteLastFragmentTutorial;
import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InfoTutorialDelimitedAreaTrader extends AppCompatActivity {

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
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new BuildFragment(), "Crea area");
        adapter.addFragment(new DeleteLastFragmentTutorial(), "Elimina l'ultimo");
        adapter.addFragment(new DeleteAllragmentTutorial(), "Pulisci Area");
        adapter.addFragment(new ConfirmFragmentTutorial(), "Conferma");
        viewPager.setAdapter(adapter);
    }

    private void initViews(){
        Toolbar info_tutorial = findViewById(R.id.toolbar_info_tutorial_);
        setSupportActionBar(info_tutorial);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    static class Adapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NotNull
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