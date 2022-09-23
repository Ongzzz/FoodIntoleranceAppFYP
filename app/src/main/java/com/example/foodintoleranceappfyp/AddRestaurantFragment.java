package com.example.foodintoleranceappfyp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class AddRestaurantFragment extends Fragment {

    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    String userId = fAuth.getCurrentUser().getEmail();
    CollectionReference restaurantReference = fStore.collection("restaurants");
    FirebaseStorage fStorage = FirebaseStorage.getInstance();
    StorageReference storageReference;
    boolean found, valid;
    Uri path;
    ArrayList<String> restaurantList = new ArrayList<>();
    ArrayList<String> restaurantMenu = new ArrayList<>();
    ImageView imgView_addNewRestaurantLogo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_restaurant, container, false);

        Spinner spinner_newRestaurantState = view.findViewById(R.id.spinner_newRestaurantState);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_item,
                getResources().getStringArray(R.array.restaurantState_array));

        spinner_newRestaurantState.setAdapter(adapter);

        EditText et_newRestaurantName = view.findViewById(R.id.et_newRestaurantName);
        EditText et_newRestaurantAddress = view.findViewById(R.id.et_newRestaurantAddress);
        Button btn_registerNewRestaurant = view.findViewById(R.id.btn_registerNewRestaurant);
        imgView_addNewRestaurantLogo = view.findViewById(R.id.imgView_addNewRestaurantLogo);
        Button btn_select_new_restaurant_logo = view.findViewById(R.id.btn_select_new_restaurant_logo);

        btn_select_new_restaurant_logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                activityResultLauncher.launch(intent);
            }
        });

        btn_registerNewRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                found = false;
                valid = true;

                String restaurantName = et_newRestaurantName.getText().toString().trim();
                String restaurantAddress = et_newRestaurantAddress.getText().toString().trim();
                String restaurantState = spinner_newRestaurantState.getSelectedItem().toString();


                if(restaurantName.isEmpty())
                {
                    et_newRestaurantName.setError("Please fill in your restaurant name!");
                    valid = false;
                }

                if(restaurantAddress.isEmpty())
                {
                    et_newRestaurantAddress.setError("Please fill in your restaurant address!");
                    valid = false;
                }

                if(restaurantState.equals(getResources().getStringArray(R.array.restaurantState_array)[0]))
                {
                    Toast.makeText(getContext(), "Please select a state", Toast.LENGTH_SHORT).show();
                    valid = false;
                }

                if(path == null)
                {
                    Toast.makeText(getContext(), "Please select your restaurant logo", Toast.LENGTH_SHORT).show();
                    valid = false;
                }

                if(valid)
                {
                    restaurantReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                             if(task.isSuccessful())
                             {
                                 for (DocumentSnapshot documentSnapshot : task.getResult())
                                 {
                                     if(restaurantName.equalsIgnoreCase(documentSnapshot.getString("Restaurant Name")))
                                     {
                                         et_newRestaurantName.setError("The restaurant name is used!");
                                         found = true;
                                         break;
                                     }
                                 }
                                 if(!found)
                                 {
                                     ProgressDialog progressDialog
                                             = new ProgressDialog(getContext());
                                     progressDialog.setTitle("Setting up your new restaurant...");
                                     progressDialog.show();

                                     String filePath = "images/"+restaurantName+"/Logo";
                                     storageReference = fStorage.getReference(filePath);

                                     storageReference.putFile(path).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                         @Override
                                         public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                             if(progressDialog.isShowing())
                                             {
                                                 progressDialog.dismiss();
                                             }
                                         }
                                     }).addOnFailureListener(new OnFailureListener() {
                                         @Override
                                         public void onFailure(@NonNull Exception e) {

                                             if(progressDialog.isShowing())
                                             {
                                                 progressDialog.dismiss();
                                             }
                                             Toast.makeText(getContext(),"Some error occurs... Please try again.", Toast.LENGTH_SHORT).show();
                                         }
                                     }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                         @Override
                                         public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                                             double progress = (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                                             progressDialog.setMessage((int)progress + "%");
                                         }
                                     });

                                     DocumentReference restaurantOwnerReference = fStore.collection("restaurantOwners").document(userId);
                                     restaurantOwnerReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                         @Override
                                         public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if(task.isSuccessful())
                                            {
                                                DocumentSnapshot ds = task.getResult();
                                                restaurantList = (ArrayList<String>) ds.get("Restaurant Name");
                                                restaurantList.add(restaurantName);

                                                Map<String, Object> updatedRestaurantList = new HashMap<>();
                                                updatedRestaurantList.put("Restaurant Name", restaurantList);
                                                restaurantOwnerReference.update(updatedRestaurantList);

                                                DocumentReference restaurantReference = fStore.collection("restaurants").document(userId+restaurantName);
                                                Map<String, Object> newRestaurant = new HashMap<>();
                                                newRestaurant.put("Restaurant Owner Email", userId);
                                                newRestaurant.put("Restaurant Name",restaurantName);
                                                newRestaurant.put("Restaurant Address",restaurantAddress);
                                                newRestaurant.put("Restaurant State",restaurantState);
                                                newRestaurant.put("Menu", restaurantMenu);
                                                newRestaurant.put("Restaurant Logo", filePath);
                                                restaurantReference.set(newRestaurant);

                                                Toast.makeText(getContext(),"New restaurant is added successfully", Toast.LENGTH_SHORT).show();

                                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                                        new MyRestaurantFragment()).commit();

                                            }
                                         }
                                     });


                                 }

                             }
                        }
                    });


                }

            }
        });

        return view;
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        path = data.getData();
                        imgView_addNewRestaurantLogo.setImageURI(path);
                    }
                }
            });



}