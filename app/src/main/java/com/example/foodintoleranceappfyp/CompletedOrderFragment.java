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
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class CompletedOrderFragment extends Fragment {

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    String userId = fAuth.getCurrentUser().getEmail();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    CollectionReference orderListReference = fStore.collection("pickupOrders");
    ArrayList<Order> orderList = new ArrayList<>();

    String name, description, imagePath, restaurantName, status;
    double price;
    int quantity;
    ArrayList<String> intolerance = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        final SwipeRefreshLayout refreshOrderHistoryList = view.findViewById(R.id.refreshOrderHistoryList);
        refreshOrderHistoryList.setDistanceToTriggerSync(30);

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            Restaurant restaurant = (Restaurant) bundle.getSerializable("Restaurant");

            TextView tv_no_order_history = view.findViewById(R.id.tv_no_order_history);
            ListView lv_order_history = view.findViewById(R.id.lv_order_history);

            orderListReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot ds : task.getResult()) {
                            if (ds.getString("Restaurant Name").equals(restaurant.getRestaurantName())) {
                                ArrayList<Food> foodList = new ArrayList<>();

                                ArrayList<Map<String, Object>> foodsInCart = (ArrayList<Map<String, Object>>) ds.get("Ordered Food");
                                for (int i = 0; i < foodsInCart.size(); i++) {
                                    name = foodsInCart.get(i).get("name").toString();
                                    description = foodsInCart.get(i).get("description").toString();
                                    imagePath = foodsInCart.get(i).get("imagePath").toString();
                                    restaurantName = foodsInCart.get(i).get("restaurantName").toString();
                                    status = foodsInCart.get(i).get("status").toString();
                                    intolerance = (ArrayList<String>) foodsInCart.get(i).get("intolerance");
                                    price = Double.valueOf(foodsInCart.get(i).get("price").toString());
                                    quantity = Integer.valueOf(foodsInCart.get(i).get("quantity").toString());
                                    Food food = new Food(name, description, price, quantity, intolerance, imagePath
                                            , restaurantName, status);
                                    foodList.add(food);
                                }
                                String orderDateTime = ds.getString("Order DateTime");
                                String completedDateTime = ds.getString("Order Completed DateTime");
                                String orderStatus = ds.getString("Status");
                                String patientName = ds.getString("Patient Name");
                                String patientEmail = ds.getString("Patient Email");
                                orderList.add(new Order(foodList, orderDateTime, completedDateTime, patientName, patientEmail, restaurantName, orderStatus));

                            }

                        }

                        if (orderList.isEmpty()) {
                            tv_no_order_history.setVisibility(View.VISIBLE);
                            tv_no_order_history.setText("No completed order...");
                            lv_order_history.setVisibility(View.GONE);
                        } else {
                            tv_no_order_history.setVisibility(View.GONE);
                            lv_order_history.setVisibility(View.VISIBLE);
                            lv_order_history.setAdapter(new OrderListAdapter(orderList, getContext(),"Completed Order"));
                        }
                    }
                }
            });

            refreshOrderHistoryList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    orderList.clear();
                    orderListReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot ds : task.getResult()) {
                                    if (ds.getString("Restaurant Name").equals(restaurant.getRestaurantName())) {
                                        ArrayList<Food> foodList = new ArrayList<>();

                                        ArrayList<Map<String, Object>> foodsInCart = (ArrayList<Map<String, Object>>) ds.get("Ordered Food");
                                        for (int i = 0; i < foodsInCart.size(); i++) {
                                            name = foodsInCart.get(i).get("name").toString();
                                            description = foodsInCart.get(i).get("description").toString();
                                            imagePath = foodsInCart.get(i).get("imagePath").toString();
                                            restaurantName = foodsInCart.get(i).get("restaurantName").toString();
                                            status = foodsInCart.get(i).get("status").toString();
                                            intolerance = (ArrayList<String>) foodsInCart.get(i).get("intolerance");
                                            price = Double.valueOf(foodsInCart.get(i).get("price").toString());
                                            quantity = Integer.valueOf(foodsInCart.get(i).get("quantity").toString());
                                            Food food = new Food(name, description, price, quantity, intolerance, imagePath
                                                    , restaurantName, status);
                                            foodList.add(food);
                                        }
                                        String orderDateTime = ds.getString("Order DateTime");
                                        String completedDateTime = ds.getString("Order Completed DateTime");
                                        String orderStatus = ds.getString("Status");
                                        String patientName = ds.getString("Patient Name");
                                        String patientEmail = ds.getString("Patient Email");
                                        //Order order = new Order(foodList, orderDateTime, patientName, restaurant, orderStatus);
                                        orderList.add(new Order(foodList, orderDateTime, completedDateTime, patientName, patientEmail, restaurantName, orderStatus));

                                    }

                                }

                                if (orderList.isEmpty()) {
                                    tv_no_order_history.setVisibility(View.VISIBLE);
                                    tv_no_order_history.setText("No completed order...");
                                    lv_order_history.setVisibility(View.GONE);
                                } else {
                                    tv_no_order_history.setVisibility(View.GONE);
                                    lv_order_history.setVisibility(View.VISIBLE);
                                    lv_order_history.setAdapter(new OrderListAdapter(orderList, getContext(),"Completed Order"));
                                }
                            }
                        }
                    });
                    refreshOrderHistoryList.setRefreshing(false);
                }
            });

        }

        return view;
    }
}