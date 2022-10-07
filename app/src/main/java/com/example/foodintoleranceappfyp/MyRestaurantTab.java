package com.example.foodintoleranceappfyp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MyRestaurantTab extends Fragment {

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseStorage fStorage = FirebaseStorage.getInstance();
    StorageReference storageReference;

    String userId = fAuth.getCurrentUser().getEmail();
    DocumentReference userReference = fStore.collection("restaurantOwners").document(userId);
    CollectionReference restaurantReference = fStore.collection("restaurants");
    ArrayList<Restaurant> restaurantsDetail = new ArrayList<>();
    List<String> myRestaurants =  new ArrayList<>();

    String restaurantAddress, restaurantState, restaurantName, logoPath;
    ArrayList<String> restaurantMenu = new ArrayList<>();

    ArrayAdapter<String> adapter;

    Uri path;
    ImageView imgView_restaurant_logo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_restaurant_tab, container, false);

        //TextView tv_restaurantNameField = view.findViewById(R.id.tv_restaurantNameField);
        TextView tv_restaurantAddressField = view.findViewById(R.id.tv_restaurantAddressField);
//        TextView tv_restaurantStateField = view.findViewById(R.id.tv_restaurantStateField);
//        ImageView imgView_changeRestaurantAddress = view.findViewById(R.id.imgView_changeRestaurantAddress);
        Spinner spinner_myRestaurant = view.findViewById(R.id.spinner_myRestaurant);
        Button btn_addNewRestaurant = view.findViewById(R.id.btn_addNewRestaurant);
        Button btn_deleteRestaurant = view.findViewById(R.id.btn_deleteRestaurant);
        Button btn_select_restaurant_logo = view.findViewById(R.id.btn_select_restaurant_logo);
        Button btn_upload = view.findViewById(R.id.btn_upload);
        imgView_restaurant_logo = view.findViewById(R.id.imgView_restaurant_logo);
        ImageView imgView_arrow = view.findViewById(R.id.imgView_arrow);
        //myRestaurants.add("Please select your restaurant");

        userReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    ArrayList<String> restaurantList = new ArrayList<>();
                    restaurantList = (ArrayList<String>)documentSnapshot.get("Restaurant Name");
                    if(restaurantList.isEmpty())
                    {
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                new AddRestaurantFragment()).commit();
                    }
                    else
                    {
                        restaurantReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful())
                                {
                                    for(DocumentSnapshot documentSnapshot : task.getResult())
                                    {
                                        if(documentSnapshot.getString("Restaurant Owner Email").equals(userId))
                                        {
                                            restaurantName = documentSnapshot.getString("Restaurant Name");
                                            restaurantAddress = documentSnapshot.getString("Restaurant Address");
                                            restaurantState = documentSnapshot.getString("Restaurant State");
                                            restaurantMenu = (ArrayList<String>) documentSnapshot.get("Menu");
                                            logoPath = documentSnapshot.getString("Restaurant Logo");
                                            restaurantsDetail.add(new Restaurant(restaurantName, restaurantAddress, restaurantState
                                                    , userId, restaurantMenu, logoPath));

                                            myRestaurants.add(restaurantName);
                                            //Toast.makeText(getContext(),String.valueOf(myRestaurants.size()), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    adapter = new ArrayAdapter<String>(
                                            getContext(), android.R.layout.simple_spinner_item, myRestaurants);

                                    spinner_myRestaurant.setAdapter(adapter);

                                    int num =spinner_myRestaurant.getAdapter().getCount();

                                    storageReference = fStorage.getReference("images/"+restaurantsDetail.get(0).getRestaurantName()+"/Logo");

                                    try{
                                        final File file = File.createTempFile("logo","jpg");
                                        storageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                                imgView_restaurant_logo.setImageBitmap(bitmap);
                                            }
                                        });

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    if(num == 1) //only 1 restaurant
                                    {
                                        imgView_arrow.setVisibility(View.GONE);
                                        tv_restaurantAddressField.setText(restaurantsDetail.get(0).getRestaurantAddress());
                                        //tv_restaurantStateField.setText(restaurantsDetail.get(0).getRestaurantState());
                                    }
                                    else //more than 1 restaurant
                                    {
                                        imgView_arrow.setVisibility(View.VISIBLE);
                                        spinner_myRestaurant.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                            @Override
                                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                if(myRestaurants.get(position).equals(restaurantsDetail.get(position).getRestaurantName()))
                                                {
                                                    storageReference = fStorage.getReference("images/"+restaurantsDetail.get(position).getRestaurantName()+"/Logo");

                                                    try{
                                                        File file = File.createTempFile("logo","jpg");
                                                        storageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                            @Override
                                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                                                imgView_restaurant_logo.setImageBitmap(bitmap);
                                                            }
                                                        });

                                                        storageReference.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                imgView_restaurant_logo.setImageResource(android.R.color.transparent);
                                                            }
                                                        });

                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }

                                                    //tv_restaurantNameField.setText(restaurantsDetail.get(position).getRestaurantName());
                                                    tv_restaurantAddressField.setText(restaurantsDetail.get(position).getRestaurantAddress());
                                                    //tv_restaurantStateField.setText(restaurantsDetail.get(position).getRestaurantState());
                                                }
                                            }

                                            @Override
                                            public void onNothingSelected(AdapterView<?> parent) {

                                            }
                                        });
                                    }

                                }
                            }
                        });
                    }
                }
            }
        });


        btn_addNewRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new AddRestaurantFragment()).commit();
            }
        });

        btn_deleteRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                        .setTitle("Warning")
                        .setMessage("Do you want to delete this restaurant? Deleted restaurant can't be recovered.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                DocumentReference restaurantReference = fStore.collection("restaurants").document(userId+spinner_myRestaurant.getSelectedItem().toString());
                                restaurantReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            DocumentReference restaurantOwnerReference = fStore.collection("restaurantOwners").document(userId);
                                            restaurantOwnerReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        DocumentSnapshot documentSnapshot = task.getResult();
                                                        ArrayList<String> ownedRestaurantList = (ArrayList)documentSnapshot.get("Restaurant Name");
                                                        ownedRestaurantList.remove(spinner_myRestaurant.getSelectedItem().toString());
                                                        Map<String,Object> updatedRestaurantList = new HashMap<>();
                                                        updatedRestaurantList.put("Restaurant Name",ownedRestaurantList);
                                                        restaurantOwnerReference.update(updatedRestaurantList).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                {
                                                                    StorageReference storageReference = fStorage.getReference("images/"+spinner_myRestaurant.getSelectedItem().toString());
                                                                    storageReference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                                                        @Override
                                                                        public void onSuccess(ListResult listResult) {
                                                                            for (StorageReference singleFileReference : listResult.getItems())
                                                                            {
                                                                                singleFileReference.delete();
                                                                            }
                                                                        }
                                                                    });

                                                                    getActivity().getSupportFragmentManager().beginTransaction().
                                                                            replace(R.id.fragment_container, new MyRestaurantFragment()).commit();

//                                                                    storageReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                                        @Override
//                                                                        public void onComplete(@NonNull Task<Void> task) {
//                                                                            if(task.isSuccessful())
//                                                                            {
//                                                                                getActivity().getSupportFragmentManager().beginTransaction().
//                                                                                        replace(R.id.fragment_container, new MyRestaurantFragment()).commit();
//                                                                            }
//                                                                        }
//                                                                    });
                                                                }
                                                            }
                                                        });


                                                    }
                                                }
                                            });
                                        }
                                    }
                                });

                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            }
        });

        btn_select_restaurant_logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                //startActivityForResult(intent, SELECT_IMAGE_REQUEST);
                activityResultLauncher.launch(intent);
            }
        });

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(path != null)
                {
                    ProgressDialog progressDialog
                            = new ProgressDialog(getContext());
                    progressDialog.setTitle("Uploading the logo...");
                    progressDialog.show();

                    String filePath = "images/"+spinner_myRestaurant.getSelectedItem().toString()+"/Logo";
                    storageReference = fStorage.getReference(filePath);

                    storageReference.putFile(path).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            if(progressDialog.isShowing())
                            {
                                progressDialog.dismiss();
                            }
                            DocumentReference documentReference = fStore.collection("restaurants").document(userId+spinner_myRestaurant.getSelectedItem().toString());
                            Map<String, Object> updateLogoPath = new HashMap<>();
                            updateLogoPath.put("Restaurant Logo", filePath);
                            documentReference.update(updateLogoPath).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getContext(),"The restaurant logo is changed successfully!", Toast.LENGTH_SHORT).show();
                                }
                            });
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
                        // There are no request codes
                        Intent data = result.getData();
                        path = data.getData();
                        imgView_restaurant_logo.setImageURI(path);
                    }
                }
            });

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if(requestCode == SELECT_IMAGE_REQUEST && data != null && data.getData() != null)
//        {
//            path = data.getData();
//            imgView_restaurant_logo.setImageURI(path);
//        }
//    }
}