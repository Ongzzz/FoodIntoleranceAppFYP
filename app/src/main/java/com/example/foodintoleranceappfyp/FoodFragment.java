package com.example.foodintoleranceappfyp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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

import java.util.ArrayList;

public class FoodFragment extends Fragment {

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    String userId = fAuth.getCurrentUser().getEmail();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    DocumentReference userReference = fStore.collection("users").document(userId);
    CollectionReference foodReference = fStore.collection("foods");
    //ArrayList<String> menu = new ArrayList<>();
    ArrayList<Food> foodList = new ArrayList<>();
    String foodName, foodDescription, foodImagePath, userType, status;
    ArrayList<String> intolerance = new ArrayList<>();
    ArrayList<String> patientIntolerance = new ArrayList<>();
    boolean isIntolerant = false;
    int quantity;
    double price;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_food, container, false);

        Bundle bundle = this.getArguments();

        if(bundle != null)
        {
            Restaurant restaurant = (Restaurant) bundle.getSerializable("Restaurant");

            TextView tv_no_food = view.findViewById(R.id.tv_no_food);
            ListView lv_food_list = view.findViewById(R.id.lv_food_list);
            ImageView imgView_add_first_food = view.findViewById(R.id.imgView_add_first_food);
            imgView_add_first_food.setVisibility(View.GONE);


            DocumentReference patientReference = fStore.collection("patients").document(userId);
            patientReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task2) {
                    if(task2.isSuccessful())
                    {
                        DocumentSnapshot ds = task2.getResult();
                        patientIntolerance = (ArrayList<String>)ds.get("Intolerance");
                    }
                }
            });

            userReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful())
                    {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        userType = documentSnapshot.getString("UserType");

                        foodReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful())
                                {

                                    if(!userType.equals("Patient"))
                                    {
                                        for (DocumentSnapshot documentSnapshot : task.getResult())
                                        {
                                            if(documentSnapshot.getString("Restaurant Name").equals(restaurant.getRestaurantName()))
                                            {
                                                foodName = documentSnapshot.getString("Food Name");
                                                foodDescription = documentSnapshot.getString("Food Description");
                                                foodImagePath = documentSnapshot.getString("Food Image");
                                                price = documentSnapshot.getDouble("Food Price");
                                                quantity = documentSnapshot.getLong("Food Quantity").intValue();
                                                intolerance = (ArrayList<String>)documentSnapshot.get("Intolerance");
                                                status = documentSnapshot.getString("Status");
                                                foodList.add(new Food(foodName, foodDescription, price, quantity, intolerance, foodImagePath, restaurant.getRestaurantName(), status));
                                            }
                                        }
                                    }
                                    else
                                    {
                                        for (DocumentSnapshot documentSnapshot : task.getResult())
                                        {
                                            isIntolerant = false;
                                            if(documentSnapshot.getString("Restaurant Name").equals(restaurant.getRestaurantName()))
                                            {
                                                foodName = documentSnapshot.getString("Food Name");
                                                foodDescription = documentSnapshot.getString("Food Description");
                                                foodImagePath = documentSnapshot.getString("Food Image");
                                                price = documentSnapshot.getDouble("Food Price");
                                                quantity = documentSnapshot.getLong("Food Quantity").intValue();
                                                intolerance = (ArrayList<String>)documentSnapshot.get("Intolerance");
                                                status = documentSnapshot.getString("Status");

                                                if(status.equals("Available"))
                                                {
                                                    for(String nutrient : intolerance)
                                                    {
                                                        for(String i : patientIntolerance)
                                                        {
                                                            if(nutrient.equals(i))
                                                            {
                                                                isIntolerant = true;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }

                                                if(!isIntolerant && status.equals("Available"))
                                                {
                                                    foodList.add(new Food(foodName, foodDescription, price, quantity, intolerance, foodImagePath, restaurant.getRestaurantName(), status));
                                                }
                                            }
                                        }
                                    }



                                    if(foodList.isEmpty())
                                    {
                                        if(!userType.equals("Restaurant Owner"))
                                        {
                                            imgView_add_first_food.setVisibility(View.GONE);
                                        }
                                        else
                                        {
                                            imgView_add_first_food.setVisibility(View.VISIBLE);
                                        }
                                        tv_no_food.setVisibility(View.VISIBLE);
                                        tv_no_food.setText("No Food...");
                                        lv_food_list.setVisibility(View.GONE);
                                    }
                                    else
                                    {
                                        imgView_add_first_food.setVisibility(View.GONE);
                                        tv_no_food.setVisibility(View.GONE);
                                        lv_food_list.setVisibility(View.VISIBLE);

                                        if(documentSnapshot.getString("UserType").equals("Restaurant Owner"))
                                        {
                                            lv_food_list.setAdapter(new FoodListAdapter(foodList, getContext(), "Restaurant Owner", restaurant));
                                        }

                                        if(documentSnapshot.getString("UserType").equals("Patient"))
                                        {
                                            lv_food_list.setAdapter(new FoodListAdapter(foodList, getContext(), "Patient", restaurant));
                                        }

                                        if(documentSnapshot.getString("UserType").equals("Admin"))
                                        {
                                            lv_food_list.setAdapter(new FoodListAdapter(foodList, getContext(), "Admin", restaurant));
                                        }
                                    }

                                }
                            }
                        });

                    }
                }
            });

            imgView_add_first_food.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("Restaurant", restaurant);

                    //MainActivity activity = (MainActivity) context;
                    AddFoodFragment fragment = new AddFoodFragment();
                    fragment.setArguments(bundle);
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
                }
            });

        }
        return view;
    }
}