package com.example.foodintoleranceappfyp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


public class RegisterRestaurantOwner extends AppCompatActivity {

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    CollectionReference collectionReference_pendingDoctors = fStore.collection("pendingDoctors");
    CollectionReference collectionReference_restaurants = fStore.collection("restaurants");
    boolean valid = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_restaurant_owner);

//        Spinner spinner_state = findViewById(R.id.spinner_restaurantState);
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
//                R.layout.spinner_item,
//                getResources().getStringArray(R.array.restaurantState_array));
//
//        spinner_state.setAdapter(adapter);

        EditText et_restaurantOwnerName = findViewById(R.id.et_restaurantOwnerName);
        EditText et_restaurantOwnerEmail = findViewById(R.id.et_restaurantOwnerEmail);
        EditText et_restaurantOwnerPassword = findViewById(R.id.et_restaurantOwnerPassword);
//        EditText et_restaurantName = findViewById(R.id.et_restaurantName);
//        EditText et_restaurantAddress = findViewById(R.id.et_restaurantAddress);
        Button btn_registerRestaurantOwner = findViewById(R.id.btn_registerRestaurantOwner);
        ImageView img_showHide = findViewById(R.id.img_showHide);

        img_showHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(et_restaurantOwnerPassword.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())){
                    //Show Password
                    et_restaurantOwnerPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else{
                    //Hide Password
                    et_restaurantOwnerPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        btn_registerRestaurantOwner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = et_restaurantOwnerName.getText().toString().trim();
                String email = et_restaurantOwnerEmail.getText().toString().trim();
                String password = et_restaurantOwnerPassword.getText().toString().trim();
//                String restaurantName = et_restaurantName.getText().toString().trim();
//                String restaurantAddress = et_restaurantAddress.getText().toString().trim();
//                String restaurantState = spinner_state.getSelectedItem().toString();

                boolean emailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches();

                if(name.isEmpty())
                {
                    et_restaurantOwnerName.setError("Please fill in your name!");
                    valid = false;
                }

                if(email.isEmpty())
                {
                    et_restaurantOwnerEmail.setError("Please fill in your email!");
                    valid = false;
                }

                if(!email.isEmpty() && !emailValid)
                {
                    et_restaurantOwnerEmail.setError("This is not a valid email!");
                    valid = false;
                }

                if(password.isEmpty())
                {
                    et_restaurantOwnerPassword.setError("Please fill in your password!");
                    valid = false;
                }

                if (!password.isEmpty()) {
                    if (password.length() < 8) {
                        et_restaurantOwnerPassword.setError("The password should be more than or equal to 8 characters!");
                        valid = false;
                    }

                    if (TextUtils.isDigitsOnly(password) || password.matches("[a-zA-Z]+")) {
                        et_restaurantOwnerPassword.setError("The password should contain digit and letter!");
                        valid = false;
                    }

                }

//                if(restaurantName.isEmpty())
//                {
//                    et_restaurantName.setError("Please fill in your restaurant name!");
//                    valid = false;
//                }
//
//                if(restaurantAddress.isEmpty())
//                {
//                    et_restaurantAddress.setError("Please fill in your restaurant address!");
//                    valid = false;
//                }
//
//                if(restaurantState.equals(getResources().getStringArray(R.array.restaurantState_array)[0]))
//                {
//                    Toast.makeText(getApplicationContext(), "Please select a state", Toast.LENGTH_SHORT).show();
//                    valid = false;
//                }


                if(valid)
                {

                    fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful())
                            {
                                ArrayList<String> restaurantList = new ArrayList<>();
                                String userId = email;
                                //String restaurantId = userId + restaurantName;
                                DocumentReference documentReference = fStore.collection("restaurantOwners").document(userId);
                                Map<String,Object> user = new HashMap<>();
                                user.put("Name",name);
                                user.put("Email",email);
                                //user.put("Restaurant ID", restaurantId);
                                //restaurantList.add(restaurantName);
                                user.put("Restaurant Name",restaurantList);
                                //user.put("UserType","Restaurant Owner");
                                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getApplicationContext(),"User Account is created!",Toast.LENGTH_SHORT).show();
                                    }
                                });

                                DocumentReference documentReference2 = fStore.collection("users").document(userId);
                                Map<String,Object> user2 = new HashMap<>();
                                user2.put("Name",name);
                                user2.put("UserType","Restaurant Owner");
                                documentReference2.set(user2).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                });
//                                ArrayList<Food> restaurantMenu = new ArrayList<>();
//                                DocumentReference restaurantReference = fStore.collection("restaurants").document(restaurantId);
//                                Map<String,Object> restaurant = new HashMap<>();
//                                //restaurant.put("Restaurant Owner:", name);
//                                restaurant.put("Restaurant Owner Email", email);
//                                restaurant.put("Restaurant Name",restaurantName);
//                                restaurant.put("Restaurant Address",restaurantAddress);
//                                restaurant.put("Restaurant State",restaurantState);
//                                restaurant.put("Restaurant Logo", "");
//                                restaurant.put("Menu", restaurantMenu);
//                                restaurantReference.set(restaurant).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(Void aVoid) {
//                                    }
//                                });

                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
//                                Bundle bundle = new Bundle();
//                                bundle.putString("userType","Restaurant Owner");
//                                i.putExtras(bundle);
                                startActivity(i);
                            }
                            else
                            {
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    Toast.makeText(getApplicationContext(), "The email is used by another user", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });



                }

            }
        });

    }
}