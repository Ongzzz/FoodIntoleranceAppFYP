package com.example.foodintoleranceappfyp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

public class RestaurantListAdapter extends BaseAdapter implements android.widget.ListAdapter {

    private ArrayList<Restaurant> arrayList = new ArrayList<>();
    private Context context;
    private String operation;

    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseStorage fStorage = FirebaseStorage.getInstance();
    StorageReference storageReference;

    public RestaurantListAdapter(ArrayList<Restaurant> arrayList, Context context, String operation) {
        this.arrayList = arrayList;
        this.context = context;
        this.operation = operation;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_restaurant, null);
        }

        CardView cardView_restaurant = convertView.findViewById(R.id.cardView_restaurant);
        TextView tv_restaurant_name = convertView.findViewById(R.id.tv_restaurant_name);
        ImageView imgView_restaurant_logo = convertView.findViewById(R.id.imgView_restaurant_logo);

        tv_restaurant_name.setText(arrayList.get(position).getRestaurantName());


        if(!arrayList.get(position).getLogoPath().isEmpty())
        {
            storageReference = fStorage.getReference(arrayList.get(position).getLogoPath());
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
        }

        cardView_restaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                Restaurant restaurant = new Restaurant(arrayList.get(position).getRestaurantName(), arrayList.get(position).getRestaurantAddress(),
                        arrayList.get(position).getRestaurantState(), arrayList.get(position).getRestaurantOwnerEmail(),
                        arrayList.get(position).getMenu(), arrayList.get(position).getLogoPath());

                bundle.putSerializable("Restaurant", restaurant);

                MainActivity activity = (MainActivity) context;
                if(operation.equals("Admin"))
                {
                    FoodFragment fragment = new FoodFragment();
                    fragment.setArguments(bundle);
                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
                }
                if(operation.equals("Patient"))
                {
                    FirebaseAuth fAuth = FirebaseAuth.getInstance();
                    String userId = fAuth.getCurrentUser().getEmail();
                    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
                    DocumentReference patientReference = fStore.collection("patients").document(userId);
                    patientReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful())
                            {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                if(!documentSnapshot.getString("State").equals(arrayList.get(position).getRestaurantState()))
                                {
                                    AlertDialog alertDialog = new AlertDialog.Builder(context)
                                            .setTitle("Please wait...")
                                            .setMessage("This restaurant is not located in your state. Do you want to view the menu?")
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    FoodFragment fragment = new FoodFragment();
                                                    fragment.setArguments(bundle);
                                                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
                                                }
                                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            }).show();
                                }
                                else
                                {
                                    FoodFragment fragment = new FoodFragment();
                                    fragment.setArguments(bundle);
                                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
                                }
                            }
                        }
                    });

                }
                if(operation.equals("Restaurant Owner"))
                {
                    MenuFragment fragment = new MenuFragment();
                    fragment.setArguments(bundle);
                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
                }
                if(operation.equals("Completed Order"))
                {
                    CompletedOrderFragment fragment = new CompletedOrderFragment();
                    fragment.setArguments(bundle);
                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
                }
            }
        });

        return convertView;
    }
}
