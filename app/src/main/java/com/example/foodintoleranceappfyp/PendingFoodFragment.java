package com.example.foodintoleranceappfyp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class PendingFoodFragment extends Fragment {

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    String userId = fAuth.getCurrentUser().getEmail();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    CollectionReference pendingFoodReference = fStore.collection("pendingFoods");
    ArrayList<Food> menu = new ArrayList<>();
    String foodName, foodDescription, foodImagePath;
    int quantity;
    double foodPrice;
    ArrayList<String> intolerance = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pending_food, container, false);

        Bundle bundle = this.getArguments();

        if(bundle != null)
        {
            Restaurant restaurant = (Restaurant) bundle.getSerializable("restaurant");

            TextView tv_no_pending_food = view.findViewById(R.id.tv_no_pending_food);
            ListView lv_pending_food_list = view.findViewById(R.id.lv_pending_food_list);

            pendingFoodReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful())
                    {
                        for (DocumentSnapshot documentSnapshot : task.getResult())
                        {
                            if(documentSnapshot.getString("Restaurant Name").equals(restaurant.getRestaurantName()))
                            {
                                foodName = documentSnapshot.getString("Food Name");
                                foodDescription = documentSnapshot.getString("Food Description");
                                foodImagePath = documentSnapshot.getString("Food Image");
                                foodPrice = documentSnapshot.getDouble("Food Price");
                                quantity = documentSnapshot.getLong("Food Quantity").intValue();
                                intolerance = (ArrayList<String>) documentSnapshot.get("Intolerance");
                                menu.add(new Food(foodName,foodDescription,foodPrice,quantity,intolerance,foodImagePath, restaurant.getRestaurantName(),"Available"));
                            }
                        }

                        if(menu.isEmpty())
                        {
                            tv_no_pending_food.setVisibility(View.VISIBLE);
                            tv_no_pending_food.setText("No Food...");
                            lv_pending_food_list.setVisibility(View.GONE);
                        }
                        else
                        {
                            tv_no_pending_food.setVisibility(View.GONE);
                            lv_pending_food_list.setVisibility(View.VISIBLE);
                            lv_pending_food_list.setAdapter(new FoodListAdapter(menu, getContext(), "Pending Food", restaurant));
                        }

                    }

                }
            });

        }
        return view;
    }
}