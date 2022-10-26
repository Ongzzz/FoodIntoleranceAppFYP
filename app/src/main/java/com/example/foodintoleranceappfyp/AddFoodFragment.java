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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddFoodFragment extends Fragment {

    Uri path;
    ImageView imgView_addFoodImage;
    ArrayList<String> intolerance = new ArrayList<String>();
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    String userId = fAuth.getCurrentUser().getEmail();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseStorage fStorage = FirebaseStorage.getInstance();
    StorageReference storageReference;
    boolean valid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_food, container, false);

        Bundle bundle = this.getArguments();

        if(bundle != null)
        {
            Restaurant restaurant = (Restaurant) bundle.getSerializable("Restaurant");
            Toast.makeText(getContext(),restaurant.getRestaurantName(),Toast.LENGTH_SHORT).show();
            imgView_addFoodImage = view.findViewById(R.id.imgView_addFoodImage);
            Button btn_select_food_image = view.findViewById(R.id.btn_select_food_image);
            EditText et_newFoodName = view.findViewById(R.id.et_newFoodName);
            EditText et_newFoodDescription = view.findViewById(R.id.et_newFoodDescription);
            EditText et_newFoodPrice = view.findViewById(R.id.et_newFoodPrice);
            CheckBox cb_gluten = view.findViewById(R.id.cb_gluten);
            CheckBox cb_lactose = view.findViewById(R.id.cb_lactose);
            CheckBox cb_fructose = view.findViewById(R.id.cb_fructose);
            Button btn_addNewFood = view.findViewById(R.id.btn_addNewFood);

            btn_addNewFood.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String foodName = et_newFoodName.getText().toString().trim();
                    String foodDescription = et_newFoodDescription.getText().toString().trim();
                    String foodPrice = et_newFoodPrice.getText().toString().trim();
                    valid = true;

                    CollectionReference pendingFoodReference = fStore.collection("pendingFoods");
                    pendingFoodReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful())
                            {
                                for (DocumentSnapshot documentSnapshot : task.getResult())
                                {
                                    if(documentSnapshot.getString("Food Name").equalsIgnoreCase(foodName) &&
                                        documentSnapshot.getString("Restaurant Name").equals(restaurant.getRestaurantName()))
                                    {
                                        et_newFoodName.setError("The food is pending! Please wait for approval!");
                                        valid = false;
                                        break;
                                    }
                                }
                                if(valid)
                                {
                                    CollectionReference foodReference = fStore.collection("foods");
                                    foodReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if(task.isSuccessful())
                                            {
                                                for (DocumentSnapshot documentSnapshot : task.getResult())
                                                {
                                                    if(documentSnapshot.getString("Food Name").equalsIgnoreCase(foodName) &&
                                                            documentSnapshot.getString("Restaurant Name").equals(restaurant.getRestaurantName()))
                                                    {
                                                        et_newFoodName.setError("The food exists in this restaurant!");
                                                        valid = false;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }

                                if(foodName.isEmpty())
                                {
                                    et_newFoodName.setError("Please fill in the food name");
                                    valid = false;
                                }
                                if(foodDescription.isEmpty())
                                {
                                    et_newFoodDescription.setError("Please fill in the food description");
                                    valid = false;
                                }
                                if(foodPrice.isEmpty())
                                {
                                    et_newFoodPrice.setError("Please fill in the food price");
                                    valid = false;
                                }
                                if(!foodPrice.isEmpty())
                                {
                                    char firstChar = foodPrice.charAt(0);
                                    char lastChar = foodPrice.charAt(foodPrice.length()-1);
                                    if(Character.compare(firstChar,'.') == 0) //no number before .
                                    {
                                        et_newFoodPrice.setError("There must be a number in front of .");
                                        valid = false;
                                    }
                                    else if(Character.compare(firstChar,'0') == 0 && foodPrice.contains(".") == false) //first number is 0
                                    {
                                        et_newFoodPrice.setError("The first number should not be 0");
                                        valid = false;
                                    }
                                    else if( Character.compare(lastChar,'.') == 0) //no number after .
                                    {
                                        et_newFoodPrice.setError("There should be a number after .");
                                        valid = false;
                                    }
                                    else
                                    {
                                        //Double d = Double.parseDouble(foodPrice);
                                        if(foodPrice.contains("."))
                                        {
                                            String[] splitter = foodPrice.split("\\.");
                                            //splitter[0].length();   // Before Decimal Count
                                            //splitter[1].length();   // After  Decimal Count
                                            //Toast.makeText(getContext(), String.valueOf(splitter[1].length()), Toast.LENGTH_SHORT).show();

                                            if(splitter[1].length()<2)
                                            {
                                                et_newFoodPrice.setError("There should be 2 number after .");
                                                valid = false;
                                            }
                                            if(splitter[1].length()>2)
                                            {
                                                et_newFoodPrice.setError("At most 2 decimal places are acceptable!");
                                                valid = false;
                                            }
                                        }



                                    }


                                    //et_newFoodPrice.setText(String.format("%.2f", Long.parseLong(foodPrice)));
                                }

                                if(path == null)
                                {
                                    valid = false;
                                    Toast.makeText(getContext(),"Please upload an image of the food", Toast.LENGTH_SHORT).show();
                                }

                                if(valid)
                                {
                                    Double price = Double.valueOf(et_newFoodPrice.getText().toString());

                                    ProgressDialog progressDialog
                                            = new ProgressDialog(getContext());
                                    progressDialog.setMessage("Adding your new food...");
                                    progressDialog.show();

                                    if(cb_fructose.isChecked())
                                    {
                                        intolerance.add("fructose");
                                    }
                                    if(cb_gluten.isChecked())
                                    {
                                        intolerance.add("gluten");
                                    }
                                    if(cb_lactose.isChecked())
                                    {
                                        intolerance.add("lactose");
                                    }

                                    String filePath = "images/"+restaurant.getRestaurantName()+"/"+foodName;
                                    DocumentReference foodReference = fStore.collection("pendingFoods").document(restaurant.getRestaurantName()+foodName);
                                    Map<String, Object> food = new HashMap<>();
                                    food.put("Food Name",foodName);
                                    food.put("Food Description", foodDescription);
                                    food.put("Food Price", price);
                                    food.put("Food Quantity", 1);
                                    food.put("Intolerance", intolerance);
                                    food.put("Food Image", filePath);
                                    food.put("Restaurant Name", restaurant.getRestaurantName());
                                    food.put("Status", "Available");
                                    foodReference.set(food);

                                    storageReference = fStorage.getReference(filePath);

                                    storageReference.putFile(path).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                            if(progressDialog.isShowing())
                                            {
                                                progressDialog.dismiss();
                                            }
                                            Toast.makeText(getContext(),"The food is added successfully! ", Toast.LENGTH_SHORT).show();
                                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyRestaurantFragment()).commit();

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
                                    });
                                }

                            }
                        }
                    });



                }
            });

            btn_select_food_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    activityResultLauncher.launch(intent);
                }
            });
        }

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
                        imgView_addFoodImage.setImageURI(path);
                    }
                }
            });


}