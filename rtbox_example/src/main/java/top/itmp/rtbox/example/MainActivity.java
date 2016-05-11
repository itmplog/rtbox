package top.itmp.rtbox.example;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.Pair;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by hz on 16/5/11.
 */
public class MainActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private PagerTabStrip tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager)findViewById(R.id.viewpager);
        MainPagerAdapter pagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        pagerAdapter.add(new FragmentNormal(), "normal");
        pagerAdapter.add(new FramentNew(), "exec");

        viewPager.setAdapter(pagerAdapter);
        tabs = (PagerTabStrip) findViewById(R.id.tabs);
        tabs.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tabs.setTextColor(Color.WHITE);

    }

    private static class MainPagerAdapter extends FragmentPagerAdapter{
        private List<Pair<Fragment, String>> fragments = new ArrayList<>();

        public void add(Fragment fragment, String title){
            fragments.add(new Pair(fragment, title));
        }

        public MainPagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position).first;
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragments.get(position).second;
        }
    }
}
