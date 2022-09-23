package com.example.foodintoleranceappfyp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class ApprovePendingFoodFragment extends Fragment {

    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    CollectionReference pendingFoodReference = fStore.collection("pendingFoods");
    ArrayList<Food> pendingFoodList = new ArrayList<>();
    String foodName, foodDescription, foodImagePath, restaurantName, status;
    int quantity;
    double foodPrice;
    ArrayList<String> intolerance = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_approve_pending_food, container, false);

        final SwipeRefreshLayout refresh_pending_food = view.findViewById(R.id.refresh_pending_food);
        refresh_pending_food.setDistanceToTriggerSync(30);

        TextView tv_no_pending_food = view.findViewById(R.id.tv_no_pending_food);
        ListView lv_approve_pending_food = view.findViewById(R.id.lv_approve_pending_food);

        pendingFoodReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    for (DocumentSnapshot documentSnapshot : task.getResult())
                    {
                        foodName = documentSnapshot.getString("Food Name");
                        foodDescription = documentSnapshot.getString("Food Description");
                        foodImagePath = documentSnapshot.getString("Food Image");
                        foodPrice = documentSnapshot.getDouble("Food Price");
                        quantity = documentSnapshot.getLong("Food Quantity").intValue();
                        intolerance = (ArrayList<String>)documentSnapshot.get("Intolerance");
                        restaurantName = documentSnapshot.getString("Restaurant Name");
                        status = documentSnapshot.getString("Status");
                        Food food = new Food(foodName, foodDescription, foodPrice, quantity, intolerance, foodImagePath, restaurantName, status);
                        pendingFoodList.add(food);
                    }

                    if(pendingFoodList.isEmpty())
                    {
                        tv_no_pending_food.setVisibility(View.VISIBLE);
                        tv_no_pending_food.setText("No Pending Food...");
                        lv_approve_pending_food.setVisibility(View.GONE);
                    }
                    else
                    {
                        tv_no_pending_food.setVisibility(View.GONE);
                        lv_approve_pending_food.setVisibility(View.VISIBLE);
                        lv_approve_pending_food.setAdapter(new FoodListAdapter(pendingFoodList, getContext(), "Approve Pending Food", null));
                    }
                }
            }
        });

        refresh_pending_food.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                pendingFoodList.clear();
                pendingFoodReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful())
                        {
                            for (DocumentSnapshot documentSnapshot : task.getResult())
                            {
                                foodName = documentSnapshot.getString("Food Name");
                                foodDescription = documentSnapshot.getString("Food Description");
                                foodImagePath = documentSnapshot.getString("Food Image");
                                foodPrice = documentSnapshot.getDouble("Food Price");
                                quantity = documentSnapshot.getLong("Food Quantity").intValue();
                                intolerance = (ArrayList<String>)documentSnapshot.get("Intolerance");
                                restaurantName = documentSnapshot.getString("Restaurant Name");
                                Food food = new Food(foodName, foodDescription, foodPrice, quantity, intolerance, foodImagePath, restaurantName, status);
                                pendingFoodList.add(food);
                            }

                            if(pendingFoodList.isEmpty())
                            {
                                tv_no_pending_food.setVisibility(View.VISIBLE);
                                tv_no_pending_food.setText("No Pending Food...");
                                lv_approve_pending_food.setVisibility(View.GONE);
                            }
                            else
                            {
                                tv_no_pending_food.setVisibility(View.GONE);
                                lv_approve_pending_food.setVisibility(View.VISIBLE);
                                lv_approve_pending_food.setAdapter(new FoodListAdapter(pendingFoodList, getContext(), "Approve Pending Food", null));
                            }
                        }
                    }
                });

                refresh_pending_food.setRefreshing(false);

            }
        });

        return view;
    }
}