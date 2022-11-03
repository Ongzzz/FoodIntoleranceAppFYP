package com.example.foodintoleranceappfyp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;


public class MenuFragment extends Fragment {

    ArrayList<Fragment> fragments = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        TabLayout my_menu_tab_layout = view.findViewById(R.id.my_menu_tab_layout);

        ViewPager2 my_menu_tab_viewpager2 = view.findViewById(R.id.my_menu_tab_viewpager2);

        Bundle bundle = this.getArguments();

        if (bundle != null)
        {

            FoodFragment foodFragment = new FoodFragment();
            foodFragment.setArguments(bundle);

            PendingFoodFragment pendingFoodFragment = new PendingFoodFragment();
            pendingFoodFragment.setArguments(bundle);

            fragments.add(foodFragment);
            fragments.add(pendingFoodFragment);

            ViewPagerFragmentAdapter viewPagerFragmentAdapter = new ViewPagerFragmentAdapter(getActivity().getSupportFragmentManager(),
                    getLifecycle(), fragments);

            my_menu_tab_viewpager2.setAdapter(viewPagerFragmentAdapter);

            my_menu_tab_layout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    my_menu_tab_viewpager2.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

            my_menu_tab_viewpager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    my_menu_tab_layout.selectTab(my_menu_tab_layout.getTabAt(position));
                }
            });

        }

        return view;
    }
}