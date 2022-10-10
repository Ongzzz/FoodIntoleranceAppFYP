package com.example.foodintoleranceappfyp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;


public class CheckoutFragment extends Fragment {

    ArrayList<Fragment> fragments = new ArrayList<>();

    OnCallbackReceived mCallback;

    public interface OnCallbackReceived {
        public void getRestaurant(Restaurant restaurant);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mCallback = (OnCallbackReceived) context;
        Bundle bundle = this.getArguments();
        if(bundle!=null)
        {
            Restaurant restaurant = (Restaurant)bundle.getSerializable("Restaurant");
            mCallback.getRestaurant(restaurant);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_checkout, container, false);

        TabLayout tab_layout = view.findViewById(R.id.checkout_tab_layout);

        ViewPager2 checkout_viewpager2 = view.findViewById(R.id.checkout_viewpager2);

        Bundle bundle = this.getArguments();

        if(bundle!=null)
        {
            CheckoutTab checkoutTab = new CheckoutTab();
            checkoutTab.setArguments(bundle);
            RestaurantLocationTab restaurantLocationTab = new RestaurantLocationTab();
            restaurantLocationTab.setArguments(bundle);

            fragments.add(checkoutTab);
            fragments.add(restaurantLocationTab);

            checkout_viewpager2.setUserInputEnabled(false); //disable swiping to ensure the map is fully responsive

            ViewPagerFragmentAdapter viewPagerFragmentAdapter = new ViewPagerFragmentAdapter(getActivity().getSupportFragmentManager(),
                    getLifecycle(), fragments);

            checkout_viewpager2.setAdapter(viewPagerFragmentAdapter);

            int num = bundle.getInt("tab");
            checkout_viewpager2.setCurrentItem(num);
            tab_layout.selectTab(tab_layout.getTabAt(num));

            tab_layout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {

                    checkout_viewpager2.setCurrentItem(tab.getPosition());

                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

            checkout_viewpager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    tab_layout.selectTab(tab_layout.getTabAt(position));
                }
            });

        }

        return view;
    }
}