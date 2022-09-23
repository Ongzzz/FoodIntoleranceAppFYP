package com.example.foodintoleranceappfyp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class RestaurantOwnerProfileFragment extends Fragment {

    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    String userId = fAuth.getCurrentUser().getEmail();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_owner_profile, container, false);

        TextView tv_restaurantOwnerUsernameField = view.findViewById(R.id.tv_restaurantOwnerUsernameField);
        ImageView imgView_changeRestaurantOwnerUsername = view.findViewById(R.id.imgView_changeRestaurantOwnerUsername);

        TextView tv_restaurantOwnerEmailField = view.findViewById(R.id.tv_restaurantOwnerEmailField);

        DocumentReference dr = fStore.collection("restaurantOwners").document(userId);
        dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot != null)
                    {
                        tv_restaurantOwnerUsernameField.setText(documentSnapshot.getString("Name"));
                        tv_restaurantOwnerEmailField.setText(documentSnapshot.getString("Email"));
                    }
                }

            }
        });


        imgView_changeRestaurantOwnerUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText et_changeUsername = new EditText(v.getContext());
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setTitle("Change username")
                        .setMessage("Do you want to change your username?")
                        .setView(et_changeUsername)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(et_changeUsername.getText().toString().trim().isEmpty())
                                {
                                    Toast.makeText(getActivity(),"Please fill in your new username!", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    DocumentReference documentReference = fStore.collection("restaurantOwners").document(userId);
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("Name", et_changeUsername.getText().toString().trim());
                                    documentReference.update(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getActivity(), "Username is updated successfully", Toast.LENGTH_SHORT).show();

                                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                                    new RestaurantOwnerProfileFragment()).commit();
                                        }
                                    });

                                    documentReference = fStore.collection("users").document(userId);
                                    documentReference.update(user);
                                }
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            }
        });

        return view;
    }

}
