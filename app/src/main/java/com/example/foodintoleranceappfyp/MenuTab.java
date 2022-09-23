package com.example.foodintoleranceappfyp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firestore.v1.TargetOrBuilder;

import java.util.ArrayList;

public class MenuTab extends Fragment {

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    String userId = fAuth.getCurrentUser().getEmail();
    CollectionReference restaurantReference = fStore.collection("restaurants");
    DocumentReference userReference = fStore.collection("users").document(userId);
    ArrayList<Restaurant> restaurantList = new ArrayList<>();
    ArrayList<String> menu = new ArrayList<>();
    String restaurantName, restaurantState, restaurantAddress, restaurantLogoPath, userType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_menu_tab, container, false);

        TextView tv_no_menu = view.findViewById(R.id.tv_no_menu);
        ListView lv_menu = view.findViewById(R.id.lv_menu);

        userReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot.getString("UserType").equals("Restaurant Owner"))
                    {
                        userType = "Restaurant Owner";
                    }
                    if(documentSnapshot.getString("UserType").equals("Admin"))
                    {
                        userType = "Admin";
                    }
                    if(documentSnapshot.getString("UserType").equals("Patient"))
                    {
                        userType = "Patient";
                    }

                    restaurantReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful())
                            {
                                for (DocumentSnapshot documentSnapshot : task.getResult())
                                {
                                    if(userType.equals("Restaurant Owner"))
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

                                    else
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
                                    tv_no_menu.setText("No restaurant");
                                    lv_menu.setVisibility(View.GONE);
                                }
                                else
                                {
                                    tv_no_menu.setVisibility(View.GONE);
                                    lv_menu.setVisibility(View.VISIBLE);
                                    if(userType.equals("Patient"))
                                    {
                                        lv_menu.setAdapter(new RestaurantListAdapter(restaurantList, getContext(),"Patient"));
                                    }
                                    if(userType.equals("Admin"))
                                    {
                                        lv_menu.setAdapter(new RestaurantListAdapter(restaurantList, getContext(),"Admin"));
                                    }
                                    if(userType.equals("Restaurant Owner"))
                                    {
                                        lv_menu.setAdapter(new RestaurantListAdapter(restaurantList, getContext(),"Restaurant Owner"));
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });






        return view;
    }
}