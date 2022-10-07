package com.example.foodintoleranceappfyp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class MyRestaurantFragment extends Fragment {

    ArrayList<Fragment> fragments = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_restaurant, container, false );

        TabLayout tab_layout = view.findViewById(R.id.tab_layout);

        ViewPager2 my_restaurant_tab_viewpager2 = view.findViewById(R.id.my_restaurant_tab_viewpager2);

        fragments.add(new MyRestaurantTab());
        fragments.add(new MenuTab());
        fragments.add(new RestaurantCompletedOrderTab());

        ViewPagerFragmentAdapter viewPagerFragmentAdapter = new ViewPagerFragmentAdapter(getActivity().getSupportFragmentManager(),
                getLifecycle(), fragments);

        my_restaurant_tab_viewpager2.setAdapter(viewPagerFragmentAdapter);

        Bundle bundle = this.getArguments();
        if(bundle != null)
        {
            int num = bundle.getInt("tab");
            my_restaurant_tab_viewpager2.setCurrentItem(num);
            tab_layout.selectTab(tab_layout.getTabAt(num));
        }

        tab_layout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                my_restaurant_tab_viewpager2.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        my_restaurant_tab_viewpager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tab_layout.selectTab(tab_layout.getTabAt(position));
            }
        });


        return view;
    }
}