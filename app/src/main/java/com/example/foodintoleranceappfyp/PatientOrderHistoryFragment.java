package com.example.foodintoleranceappfyp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class PatientOrderHistoryFragment extends Fragment {

    ArrayList<Fragment> fragments = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_patient_order_history, container, false);

        TabLayout patient_order_history_tab_layout = view.findViewById(R.id.patient_order_history_tab_layout);

        ViewPager2 patient_order_history_viewpager2 = view.findViewById(R.id.patient_order_history_viewpager2);


            fragments.add(new PatientPendingOrderFragment());
            fragments.add(new PatientCompletedOrderFragment());


            ViewPagerFragmentAdapter viewPagerFragmentAdapter = new ViewPagerFragmentAdapter(getActivity().getSupportFragmentManager(),
                    getLifecycle(), fragments);

            patient_order_history_viewpager2.setAdapter(viewPagerFragmentAdapter);



            patient_order_history_tab_layout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {

                    patient_order_history_viewpager2.setCurrentItem(tab.getPosition());

                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

            patient_order_history_viewpager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    patient_order_history_tab_layout.selectTab(patient_order_history_tab_layout.getTabAt(position));
                }
            });


        return view;
    }
}