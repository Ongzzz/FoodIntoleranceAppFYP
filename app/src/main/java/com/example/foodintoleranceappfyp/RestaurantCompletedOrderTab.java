package com.example.foodintoleranceappfyp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class RestaurantCompletedOrderTab extends Fragment {

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    String userId = fAuth.getCurrentUser().getEmail();
    CollectionReference restaurantReference = fStore.collection("restaurants");
    ArrayList<Restaurant> restaurantList = new ArrayList<>();
    ArrayList<String> menu = new ArrayList<>();
    String restaurantName, restaurantState, restaurantAddress, restaurantLogoPath;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_menu_tab, container, false);

        TextView tv_no_menu = view.findViewById(R.id.tv_no_menu);
        ListView lv_menu = view.findViewById(R.id.lv_menu);

        restaurantReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    for (DocumentSnapshot documentSnapshot : task.getResult())
                    {
                        if(documentSnapshot.getString("Restaurant Owner Email").equals(userId))
                        {
                            restaurantName = documentSnapshot.getString("Restaurant Name");
                            restaurantAddress = documentSnapshot.getString("Restaurant Address");
                            restaurantState = documentSnapshot.getString("Restaurant State");
                            menu = (ArrayList<String>)documentSnapshot.get("Menu");
                            restaurantLogoPath = documentSnapshot.getString("Restaurant Logo");

                            restaurantList.add(new Restaurant(restaurantName, restaurantAddress,
                                    restaurantState, userId, menu, restaurantLogoPath));
                        }
                    }
                    if(restaurantList.isEmpty())
                    {
                        tv_no_menu.setVisibility(View.VISIBLE);
                        tv_no_menu.setText("No Completed Order,,,");
                        lv_menu.setVisibility(View.GONE);
                    }
                    else
                    {
                        tv_no_menu.setVisibility(View.GONE);
                        lv_menu.setVisibility(View.VISIBLE);
                        lv_menu.setAdapter(new RestaurantListAdapter(restaurantList, getContext(),"Completed Order"));

                    }
                }
            }
        });







        return view;
    }
}