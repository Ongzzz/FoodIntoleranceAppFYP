package com.example.foodintoleranceappfyp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class AppointmentConsultationTab extends Fragment {

    ArrayList<Fragment> fragments = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_consultation_tab, container, false );

        TabLayout tab_layout = view.findViewById(R.id.tab_layout);

        ViewPager2 consultation_tab_viewpager2 = view.findViewById(R.id.consultation_tab_viewpager2);

        Bundle approvedBundle = new Bundle();
        approvedBundle.putString("Appointment Status", "Approved");
        AppointmentRecordFragment approvedAppointmentFragment = new AppointmentRecordFragment();
        approvedAppointmentFragment.setArguments(approvedBundle);

        Bundle pendingBundle = new Bundle();
        pendingBundle.putString("Appointment Status", "Pending");
        AppointmentRecordFragment pendingAppointmentFragment = new AppointmentRecordFragment();
        pendingAppointmentFragment.setArguments(pendingBundle);


        fragments.add(approvedAppointmentFragment);
        fragments.add(pendingAppointmentFragment);
        fragments.add(new ConsultationRecordFragment());

        ViewPagerFragmentAdapter viewPagerFragmentAdapter = new ViewPagerFragmentAdapter(getActivity().getSupportFragmentManager(),
                getLifecycle(), fragments);

        consultation_tab_viewpager2.setAdapter(viewPagerFragmentAdapter);

        tab_layout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                consultation_tab_viewpager2.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        consultation_tab_viewpager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tab_layout.selectTab(tab_layout.getTabAt(position));
            }
        });

        return view;
    }
}