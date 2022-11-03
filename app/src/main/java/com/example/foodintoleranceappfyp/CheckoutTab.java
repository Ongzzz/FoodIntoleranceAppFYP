package com.example.foodintoleranceappfyp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.Map;

public class CheckoutTab extends Fragment {

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    String userId = fAuth.getCurrentUser().getEmail();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    DocumentReference cartReference = fStore.collection("carts").document(userId);
    ArrayList<Food> foodList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_checkout_tab, container, false);

        ListView lv_foodInCart_list = view.findViewById(R.id.lv_foodInCart_list);

        Bundle bundle = this.getArguments();

        if(bundle!=null)
        {
            Restaurant restaurant = (Restaurant)bundle.getSerializable("Restaurant");
            cartReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful())
                    {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        String patientName = documentSnapshot.getString("Patient Name");
                        ArrayList<Map<String, Object>> foodsInCart = (ArrayList<Map<String, Object>>) documentSnapshot.get("Cart");
                        for(int i =0; i<foodsInCart.size(); i++)
                        {
                            String name = foodsInCart.get(i).get("name").toString();
                            String description = foodsInCart.get(i).get("description").toString();
                            double price = Double.parseDouble(foodsInCart.get(i).get("price").toString());
                            int quantity = Integer.valueOf(foodsInCart.get(i).get("quantity").toString());
                            ArrayList<String> intolerance = (ArrayList<String>)foodsInCart.get(i).get("intolerance");
                            String imagePath = foodsInCart.get(i).get("imagePath").toString();
                            String restaurantName = foodsInCart.get(i).get("restaurantName").toString();
                            String status = foodsInCart.get(i).get("status").toString();
                            Food foodInCart = new Food(name,description,price,quantity,intolerance,imagePath,restaurantName,status);
                            foodList.add(foodInCart);
                        }
                        Cart cart = new Cart(foodList, patientName, foodList.get(0).getRestaurantName());
                        lv_foodInCart_list.setAdapter(new CartListAdapter(cart, getContext(), restaurant));

                    }
                }
            });
        }



        return view;

    }
}