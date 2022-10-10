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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class PendingOrderFragment extends Fragment {

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    String userId = fAuth.getCurrentUser().getEmail();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    DocumentReference restaurantOwnerReference = fStore.collection("restaurantOwners").document(userId);
    CollectionReference orderListReference = fStore.collection("pendingOrders");
    ArrayList<String> restaurantList = new ArrayList<>();
    ArrayList<Order> orderList = new ArrayList<>();
//    ArrayList<Food> foodList = new ArrayList<>();

    String name, description, imagePath, restaurant, status;
    double price;
    int quantity;
    ArrayList<String> intolerance = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        TextView tv_no_order_history = view.findViewById(R.id.tv_no_order_history);
        ListView lv_order_history = view.findViewById(R.id.lv_order_history);

        final SwipeRefreshLayout refreshOrderHistoryList = view.findViewById(R.id.refreshOrderHistoryList);
        refreshOrderHistoryList.setDistanceToTriggerSync(30);

        restaurantOwnerReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    restaurantList = (ArrayList<String>) documentSnapshot.get("Restaurant Name");
                    orderListReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful())
                            {
                                for (DocumentSnapshot ds: task.getResult())
                                {
                                    for(String restaurantName : restaurantList)
                                    {
                                        if(ds.getString("Restaurant Name").equals(restaurantName) && ds.getString("Status").equals("Pending"))
                                        {
                                            ArrayList<Food> foodList = new ArrayList<>();

                                            ArrayList<Map<String, Object>> foodsInCart = (ArrayList<Map<String, Object>>) ds.get("Ordered Food");
                                            for(int i = 0; i< foodsInCart.size(); i++)
                                            {
                                                name = foodsInCart.get(i).get("name").toString();
                                                description = foodsInCart.get(i).get("description").toString();
                                                imagePath = foodsInCart.get(i).get("imagePath").toString();
                                                restaurant = foodsInCart.get(i).get("restaurantName").toString();
                                                status = foodsInCart.get(i).get("status").toString();
                                                intolerance = (ArrayList<String>)foodsInCart.get(i).get("intolerance");
                                                price = Double.valueOf(foodsInCart.get(i).get("price").toString());
                                                quantity = Integer.valueOf(foodsInCart.get(i).get("quantity").toString());
                                                Food food = new Food(name, description, price, quantity, intolerance, imagePath
                                                , restaurant, status);
                                                foodList.add(food);
                                            }
                                            String orderDateTime = ds.getString("Order DateTime");
                                            String orderStatus = ds.getString("Status");
                                            String patientName = ds.getString("Patient Name");
                                            String patientEmail = ds.getString("Patient Email");
                                            //Order order = new Order(foodList, orderDateTime, patientName, restaurant, orderStatus);
                                            orderList.add(new Order(foodList, orderDateTime, null, patientName, patientEmail, restaurant, orderStatus));

                                        }

                                    }
                                }

                                if(orderList.isEmpty())
                                {
                                    tv_no_order_history.setVisibility(View.VISIBLE);
                                    tv_no_order_history.setText("No order...");
                                    lv_order_history.setVisibility(View.GONE);
                                }
                                else
                                {
                                    tv_no_order_history.setVisibility(View.GONE);
                                    lv_order_history.setVisibility(View.VISIBLE);
                                    lv_order_history.setAdapter(new OrderListAdapter(orderList, getContext(),"Pending Order"));
                                }
                            }
                        }
                    });
                }
            }
        });

        refreshOrderHistoryList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                orderList.clear();
                restaurantOwnerReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            restaurantList = (ArrayList<String>) documentSnapshot.get("Restaurant Name");
                            orderListReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful())
                                    {
                                        for (DocumentSnapshot ds: task.getResult())
                                        {
                                            for(String restaurantName : restaurantList)
                                            {
                                                if(ds.getString("Restaurant Name").equals(restaurantName) && ds.getString("Status").equals("Pending"))
                                                {
                                                    ArrayList<Food> foodList = new ArrayList<>();

                                                    ArrayList<Map<String, Object>> foodsInCart = (ArrayList<Map<String, Object>>) ds.get("Ordered Food");
                                                    for(int i = 0; i< foodsInCart.size(); i++)
                                                    {
                                                        name = foodsInCart.get(i).get("name").toString();
                                                        description = foodsInCart.get(i).get("description").toString();
                                                        imagePath = foodsInCart.get(i).get("imagePath").toString();
                                                        restaurant = foodsInCart.get(i).get("restaurantName").toString();
                                                        status = foodsInCart.get(i).get("status").toString();
                                                        intolerance = (ArrayList<String>)foodsInCart.get(i).get("intolerance");
                                                        price = Double.valueOf(foodsInCart.get(i).get("price").toString());
                                                        quantity = Integer.valueOf(foodsInCart.get(i).get("quantity").toString());
                                                        Food food = new Food(name, description, price, quantity, intolerance, imagePath
                                                                , restaurant, status);
                                                        foodList.add(food);
                                                    }
                                                    String orderDateTime = ds.getString("Order DateTime");
                                                    String orderStatus = ds.getString("Status");
                                                    String patientName = ds.getString("Patient Name");
                                                    String patientEmail = ds.getString("Patient Email");
                                                    //Order order = new Order(foodList, orderDateTime, patientName, restaurant, orderStatus);
                                                    orderList.add(new Order(foodList, orderDateTime, null, patientName, patientEmail, restaurant, orderStatus));

                                                }

                                            }
                                        }

                                        if(orderList.isEmpty())
                                        {
                                            tv_no_order_history.setVisibility(View.VISIBLE);
                                            tv_no_order_history.setText("No order...");
                                            lv_order_history.setVisibility(View.GONE);
                                        }
                                        else
                                        {
                                            tv_no_order_history.setVisibility(View.GONE);
                                            lv_order_history.setVisibility(View.VISIBLE);
                                            lv_order_history.setAdapter(new OrderListAdapter(orderList, getContext(),"Pending Order"));
                                        }
                                    }
                                }
                            });
                        }
                    }
                });
                refreshOrderHistoryList.setRefreshing(false);
            }
        });




        return view;
    }
}