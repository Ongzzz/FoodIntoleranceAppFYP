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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RestaurantOwnerListFragment extends Fragment {

    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    String userId = fAuth.getCurrentUser().getEmail();
    CollectionReference collectionReference = fStore.collection("restaurantOwners");

    ArrayList<RestaurantOwner> restaurantOwners = new ArrayList<>();
    String name, email;
    ArrayList<String> restaurantName = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_owner_list, container, false);

        ListView lv_restaurant_owner_list = view.findViewById(R.id.lv_restaurant_owner_list);
        TextView tv_no_restaurant_owner = view.findViewById(R.id.tv_no_restaurant_owner);

        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if(task.isSuccessful())
                {
                    for (DocumentSnapshot documentSnapshot : task.getResult())
                    {
                        name = documentSnapshot.getString("Name");
                        email = documentSnapshot.getString("Email");
                        restaurantName = (ArrayList<String>)documentSnapshot.get("Restaurant Name");
                        restaurantOwners.add(new RestaurantOwner(name, email, restaurantName));
                    }

                    if(!restaurantOwners.isEmpty())
                    {
                        tv_no_restaurant_owner.setVisibility(View.GONE);
                        lv_restaurant_owner_list.setVisibility(View.VISIBLE);
                        lv_restaurant_owner_list.setAdapter(new RestaurantOwnerListAdapter(restaurantOwners, getContext()));
                    }

                    else
                    {
                        tv_no_restaurant_owner.setText("No restaurant owner...");
                        tv_no_restaurant_owner.setVisibility(View.VISIBLE);
                        lv_restaurant_owner_list.setVisibility(View.GONE);
                    }

                }
            }
        });


        return view;
    }

}
