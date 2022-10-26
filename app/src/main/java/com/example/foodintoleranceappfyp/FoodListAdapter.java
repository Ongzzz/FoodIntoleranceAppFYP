package com.example.foodintoleranceappfyp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
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
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import androidx.annotation.NonNull;

public class FoodListAdapter extends BaseAdapter implements android.widget.ListAdapter {

    private ArrayList<Food> arrayList = new ArrayList<>();
    private Context context;
    private String fragment;
    private Restaurant restaurant;

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    String userId = fAuth.getCurrentUser().getEmail();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseStorage fStorage = FirebaseStorage.getInstance();
    StorageReference storageReference;
    String restaurantOwnerEmail, availability, patientName;
    ArrayList<Food> addedFoodList = new ArrayList<>();
    DocumentReference cartReference = fStore.collection("carts").document(userId);
    boolean found, sameRestaurant;

    public FoodListAdapter(ArrayList<Food> arrayList, Context context, String fragment, Restaurant restaurant) {
        this.arrayList = arrayList;
        this.context = context;
        this.fragment = fragment;
        this.restaurant = restaurant;
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
            convertView = inflater.inflate(R.layout.list_food, null);
        }

        TextView tv_pending_restaurant_name = convertView.findViewById(R.id.tv_pending_restaurant_name);
        TextView tv_food_name = convertView.findViewById(R.id.tv_food_name);
        TextView tv_food_intolerance = convertView.findViewById(R.id.tv_food_intolerance);
        TextView tv_food_description = convertView.findViewById(R.id.tv_food_description);
        TextView tv_food_price = convertView.findViewById(R.id.tv_food_price);
        TextView tv_food_quantity = convertView.findViewById(R.id.tv_food_quantity);
        TextView tv_food_availability = convertView.findViewById(R.id.tv_food_availability);
        ImageView imgView_food_image = convertView.findViewById(R.id.imgView_food_image);
        ImageView imgView_decrease = convertView.findViewById(R.id.imgView_decrease);
        ImageView imgView_increase = convertView.findViewById(R.id.imgView_increase);
        ImageView imgView_add_to_cart = convertView.findViewById(R.id.imgView_add_to_cart);
        Button btn_checkout = convertView.findViewById(R.id.btn_checkout);
        ImageView imgView_add_food = convertView.findViewById(R.id.imgView_add_food);

        tv_food_name.setText(arrayList.get(position).getName());

        if(arrayList.get(position).getIntolerance().isEmpty())
        {
            tv_food_intolerance.setText("No gluten, lactose and fructose");
        }
        else
        {
            String intolerance = TextUtils.join(", ", arrayList.get(position).getIntolerance());
            tv_food_intolerance.setText("Contain: "+intolerance);
        }
        tv_food_description.setText(arrayList.get(position).getDescription());
        //tv_food_price.setText("RM"+String.valueOf(arrayList.get(position).getPrice()));
        tv_food_price.setText("RM"+String.format("%.2f",arrayList.get(position).getPrice()));

        tv_food_quantity.setText("1");
        tv_pending_restaurant_name.setText(arrayList.get(position).getRestaurantName());

        if(!arrayList.get(position).getImagePath().isEmpty())
        {
            storageReference = fStorage.getReference(arrayList.get(position).getImagePath());
            try{
                File file = File.createTempFile("image","jpg");
                storageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        imgView_food_image.setImageBitmap(bitmap);
                    }
                });

                storageReference.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        imgView_food_image.setImageResource(android.R.color.transparent);
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(fragment.equals("Restaurant Owner"))
        {
            tv_pending_restaurant_name.setVisibility(View.GONE);
            imgView_decrease.setVisibility(View.GONE);
            imgView_increase.setVisibility(View.GONE);
            btn_checkout.setVisibility(View.GONE);
            tv_food_quantity.setVisibility(View.GONE);

            tv_food_availability.setVisibility(View.VISIBLE);
            imgView_add_to_cart.setVisibility(View.VISIBLE);
            imgView_add_to_cart.setImageResource(R.drawable.ic_setting);

            if(arrayList.get(position).getStatus().equals("Available"))
            {
                tv_food_availability.setText("Hide this food");
                tv_food_availability.setTextColor(Color.parseColor("#FF0000"));
            }
            else
            {
                tv_food_availability.setText("Show this food");
                tv_food_availability.setTextColor(Color.parseColor("#00FF00"));
            }
            if(position < arrayList.size() - 1)
            {
                imgView_add_food.setVisibility(View.GONE);
            }

        }


        if(fragment.equals("Patient"))
        {
            tv_food_availability.setVisibility(View.GONE);
            tv_pending_restaurant_name.setVisibility(View.GONE);
            imgView_add_food.setVisibility(View.GONE);

            imgView_decrease.setVisibility(View.VISIBLE);
            imgView_increase.setVisibility(View.VISIBLE);
            imgView_add_to_cart.setVisibility(View.VISIBLE);
            btn_checkout.setVisibility(View.VISIBLE);
            tv_food_quantity.setVisibility(View.VISIBLE);

            if(position < arrayList.size() - 1)
            {
                btn_checkout.setVisibility(View.GONE);
            }

            DocumentReference userReference = fStore.collection("patients").document(userId);
            userReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful())
                    {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        patientName = documentSnapshot.getString("Name");
                    }
                }
            });
        }


        if(fragment.equals("Admin"))
        {
            tv_food_availability.setVisibility(View.GONE);
            imgView_decrease.setVisibility(View.GONE);
            imgView_increase.setVisibility(View.GONE);
            btn_checkout.setVisibility(View.GONE);
            tv_food_quantity.setVisibility(View.GONE);
            imgView_add_food.setVisibility(View.GONE);
            tv_pending_restaurant_name.setVisibility(View.GONE);

            imgView_add_to_cart.setVisibility(View.VISIBLE);
            imgView_add_to_cart.setImageResource(R.drawable.ic_setting);

        }

        if(fragment.equals("Pending Food"))
        {
            tv_food_availability.setVisibility(View.GONE);
            tv_pending_restaurant_name.setVisibility(View.GONE);
            imgView_decrease.setVisibility(View.GONE);
            imgView_increase.setVisibility(View.GONE);
            imgView_add_to_cart.setVisibility(View.GONE);
            btn_checkout.setVisibility(View.GONE);
            tv_food_quantity.setVisibility(View.GONE);
            imgView_add_food.setVisibility(View.GONE);
        }

        if(fragment.equals("Approve Pending Food"))
        {
            tv_food_availability.setVisibility(View.GONE);
            imgView_decrease.setVisibility(View.GONE);
            imgView_increase.setVisibility(View.GONE);
            btn_checkout.setVisibility(View.GONE);
            tv_food_quantity.setVisibility(View.GONE);
            imgView_add_food.setVisibility(View.GONE);

            tv_pending_restaurant_name.setVisibility(View.VISIBLE);
            imgView_add_to_cart.setVisibility(View.VISIBLE);
            imgView_add_to_cart.setImageResource(R.drawable.ic_setting);
        }




        tv_food_availability.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DocumentReference restaurantReference = fStore.collection("foods")
                .document(restaurant.getRestaurantName()+arrayList.get(position).getName());

                restaurantReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            availability = documentSnapshot.getString("Status");

                            if(availability.equals("Available"))
                            {
                                Map<String, Object> updateAvailability = new HashMap<>();
                                updateAvailability.put("Status", "Not Available");
                                restaurantReference.update(updateAvailability);
                                tv_food_availability.setText("Show this food");
                                tv_food_availability.setTextColor(Color.parseColor("#00FF00"));

                                CollectionReference cartListReference = fStore.collection("carts");
                                cartListReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if(task.isSuccessful())
                                        {

                                            for (DocumentSnapshot documentSnapshot : task.getResult())
                                            {
                                                ArrayList<Food> foodList = new ArrayList<>();
                                                ArrayList<Map<String, Object>> foodsInCart = (ArrayList<Map<String, Object>>) documentSnapshot.get("Cart");
                                                for (int i=0;i<foodsInCart.size();i++)
                                                {
                                                    String name = foodsInCart.get(i).get("name").toString();
                                                    String description = foodsInCart.get(i).get("description").toString();
                                                    String imagePath = foodsInCart.get(i).get("imagePath").toString();
                                                    ArrayList<String> intolerance = (ArrayList)foodsInCart.get(i).get("intolerance");
                                                    String restaurantName = foodsInCart.get(i).get("restaurantName").toString();
                                                    String status = foodsInCart.get(i).get("status").toString();
                                                    double price = Double.valueOf(foodsInCart.get(i).get("price").toString());
                                                    int quantity = Integer.valueOf(foodsInCart.get(i).get("quantity").toString());
                                                    Food food = new Food(name,description,price,quantity,intolerance,imagePath,restaurantName,status);

                                                    if(!name.equals(arrayList.get(position).getName()))
                                                    {
                                                        foodList.add(food);
                                                    }
                                                }

                                                DocumentReference documentReference = fStore.collection("carts").document(documentSnapshot.getId());
                                                documentReference.update("Cart",foodList);
                                                if(foodList.isEmpty())
                                                {
                                                    documentReference.delete();
                                                }

                                            }

                                        }
                                    }
                                });
                            }
                            if(availability.equals("Not Available"))
                            {
                                Map<String, Object> updateAvailability = new HashMap<>();
                                updateAvailability.put("Status", "Available");
                                restaurantReference.update(updateAvailability);
                                tv_food_availability.setText("Hide this food");
                                tv_food_availability.setTextColor(Color.parseColor("#FF0000"));

                            }
                        }
                    }
                });

            }
        });

        imgView_increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.valueOf(tv_food_quantity.getText().toString());
                quantity += 1;
                tv_food_quantity.setText(String.valueOf(quantity));
            }
        });

        imgView_decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.valueOf(tv_food_quantity.getText().toString());
                if(quantity > 1)
                {
                    quantity -= 1;
                    tv_food_quantity.setText(String.valueOf(quantity));
                }
            }
        });

        imgView_add_to_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(fragment.equals("Approve Pending Food"))
                {
                    AlertDialog alertDialog = new AlertDialog.Builder(context)
                            .setTitle("Approve Food")
                            .setMessage("Approve "+ arrayList.get(position).getName()+" from "+arrayList.get(position).getRestaurantName()+" ?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    MainActivity activity = (MainActivity) context;
                                    ProgressDialog progressDialog
                                            = new ProgressDialog((activity));
                                    progressDialog.setTitle("Approving the food...");
                                    progressDialog.setMessage("Please wait...");
                                    progressDialog.show();

                                    DocumentReference foodReference = fStore.collection("foods")
                                            .document(arrayList.get(position).getRestaurantName()+arrayList.get(position).getName());

                                    Map<String, Object> food = new HashMap<>();
                                    food.put("Food Name",arrayList.get(position).getName());
                                    food.put("Food Description", arrayList.get(position).getDescription());
                                    food.put("Food Price", arrayList.get(position).getPrice());
                                    food.put("Food Quantity", arrayList.get(position).getQuantity());
                                    food.put("Intolerance", arrayList.get(position).getIntolerance());
                                    food.put("Food Image", arrayList.get(position).getImagePath());
                                    food.put("Restaurant Name", arrayList.get(position).getRestaurantName());
                                    food.put("Status", arrayList.get(position).getStatus());
                                    food.put("Approved By", userId);
                                    foodReference.set(food);


                                    CollectionReference restaurantReference = fStore.collection("restaurants");
                                    restaurantReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                            if(task.isSuccessful())
                                            {
                                                for (DocumentSnapshot documentSnapshot : task.getResult())
                                                {
                                                    if (documentSnapshot.getString("Restaurant Name").equals(arrayList.get(position).getRestaurantName()))
                                                    {
                                                        restaurantOwnerEmail = documentSnapshot.getString("Restaurant Owner Email");
                                                        break;
                                                    }
                                                }

                                                DocumentReference documentReference = fStore.collection("restaurants").document(restaurantOwnerEmail+arrayList.get(position).getRestaurantName());
                                                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if(task.isSuccessful())
                                                        {
                                                            DocumentSnapshot documentSnapshot = task.getResult();
                                                            Map<String, Object> updateMenu = new HashMap<>();
                                                            ArrayList<String> menu = new ArrayList<>();
                                                            menu = (ArrayList<String>)documentSnapshot.get("Menu");
                                                            menu.add(arrayList.get(position).getName());
                                                            updateMenu.put("Menu", menu);
                                                            documentReference.update(updateMenu);

                                                            String senderEmail="foodintoleranceapp53@gmail.com";
                                                            String senderPassword="lkqgijyawiwshwjc";
                                                            String messageToSend="Your food: "+ arrayList.get(position).getName()+" from "+
                                                                    arrayList.get(position).getRestaurantName()+" is approved!";
                                                            Properties props = new Properties();
                                                            props.put("mail.smtp.auth","true");
                                                            props.put("mail.smtp.starttls.enable","true");
                                                            props.put("mail.smtp.host","smtp.gmail.com");
                                                            props.put("mail.smtp.port","587");
                                                            Session session = Session.getInstance(props, new javax.mail.Authenticator(){
                                                                @Override
                                                                protected PasswordAuthentication getPasswordAuthentication() {
                                                                    return new PasswordAuthentication(senderEmail,senderPassword);
                                                                }
                                                            });

                                                            try {
                                                                Message message = new MimeMessage(session);
                                                                message.setFrom(new InternetAddress(senderEmail));
                                                                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(restaurantOwnerEmail));
                                                                message.setSubject("Pending Food Approval");
                                                                message.setText(messageToSend);
                                                                Transport.send(message);
                                                            }catch (MessagingException e){
                                                                throw new RuntimeException(e);
                                                            }

                                                            DocumentReference pendingFoodReference = fStore.collection("pendingFoods")
                                                                    .document(arrayList.get(position).getRestaurantName()+arrayList.get(position).getName());

                                                            pendingFoodReference.delete();
                                                            arrayList.remove(position);
                                                            notifyDataSetChanged();

                                                            if(progressDialog.isShowing())
                                                            {
                                                                progressDialog.dismiss();
                                                            }
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

                                    MainActivity activity = (MainActivity) context;
                                    ProgressDialog progressDialog
                                            = new ProgressDialog((activity));
                                    progressDialog.setTitle("Rejecting the food...");
                                    progressDialog.setMessage("Please wait...");
                                    progressDialog.show();

                                    CollectionReference restaurantReference = fStore.collection("restaurants");
                                    restaurantReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                            if(task.isSuccessful())
                                            {
                                                for (DocumentSnapshot documentSnapshot : task.getResult())
                                                {
                                                    if (documentSnapshot.getString("Restaurant Name").equals(arrayList.get(position).getRestaurantName())) {
                                                        restaurantOwnerEmail = documentSnapshot.getString("Restaurant Owner Email");
                                                        break;
                                                    }
                                                }

                                                //remove image from storage
                                                storageReference = fStorage.getReference(arrayList.get(position).getImagePath());
                                                storageReference.delete();

                                                String senderEmail="foodintoleranceapp53@gmail.com";
                                                String senderPassword="lkqgijyawiwshwjc";
                                                String messageToSend="Your food: "+ arrayList.get(position).getName()+" from "+
                                                        arrayList.get(position).getRestaurantName()+" is rejected as it is not friendly to the food intolerance patient.";
                                                Properties props = new Properties();
                                                props.put("mail.smtp.auth","true");
                                                props.put("mail.smtp.starttls.enable","true");
                                                props.put("mail.smtp.host","smtp.gmail.com");
                                                props.put("mail.smtp.port","587");
                                                Session session = Session.getInstance(props, new javax.mail.Authenticator(){
                                                    @Override
                                                    protected PasswordAuthentication getPasswordAuthentication() {
                                                        return new PasswordAuthentication(senderEmail,senderPassword);
                                                    }
                                                });

                                                try {
                                                    Message message = new MimeMessage(session);
                                                    message.setFrom(new InternetAddress(senderEmail));
                                                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(restaurantOwnerEmail));
                                                    message.setSubject("Pending Food Rejected");
                                                    message.setText(messageToSend);
                                                    Transport.send(message);
                                                }catch (MessagingException e){
                                                    throw new RuntimeException(e);
                                                }

                                                DocumentReference pendingFoodReference = fStore.collection("pendingFoods")
                                                        .document(arrayList.get(position).getRestaurantName()+arrayList.get(position).getName());

                                                pendingFoodReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful())
                                                        {
                                                            arrayList.remove(position);
                                                            notifyDataSetChanged();

                                                            if(progressDialog.isShowing())
                                                            {
                                                                progressDialog.dismiss();
                                                            }
                                                        }
                                                    }
                                                });

                                            }
                                        }
                                    });




                                }
                            }).show();
                }

                if(fragment.equals("Admin") || fragment.equals("Restaurant Owner"))
                {
                    AlertDialog alertDialog = new AlertDialog.Builder(context)
                            .setTitle("About this food...")
                            .setMessage("Delete "+ arrayList.get(position).getName()+"?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    DocumentReference foodReference = fStore.collection("foods")
                                            .document(arrayList.get(position).getRestaurantName()+arrayList.get(position).getName());

                                    foodReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {

                                                CollectionReference cartListReference = fStore.collection("carts");
                                                cartListReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if(task.isSuccessful())
                                                        {

                                                            for (DocumentSnapshot documentSnapshot : task.getResult())
                                                            {
                                                                ArrayList<Food> foodList = new ArrayList<>();
                                                                ArrayList<Map<String, Object>> foodsInCart = (ArrayList<Map<String, Object>>) documentSnapshot.get("Cart");
                                                                for (int i=0;i<foodsInCart.size();i++)
                                                                {
                                                                    String name = foodsInCart.get(i).get("name").toString();
                                                                    String description = foodsInCart.get(i).get("description").toString();
                                                                    String imagePath = foodsInCart.get(i).get("imagePath").toString();
                                                                    ArrayList<String> intolerance = (ArrayList)foodsInCart.get(i).get("intolerance");
                                                                    String restaurantName = foodsInCart.get(i).get("restaurantName").toString();
                                                                    String status = foodsInCart.get(i).get("status").toString();
                                                                    double price = Double.valueOf(foodsInCart.get(i).get("price").toString());
                                                                    int quantity = Integer.valueOf(foodsInCart.get(i).get("quantity").toString());
                                                                    Food food = new Food(name,description,price,quantity,intolerance,imagePath,restaurantName,status);

                                                                    if(!name.equals(arrayList.get(position).getName()))
                                                                    {
                                                                        foodList.add(food);
                                                                    }
                                                                }

                                                                DocumentReference documentReference = fStore.collection("carts").document(documentSnapshot.getId());
                                                                documentReference.update("Cart",foodList);
                                                                if(foodList.isEmpty())
                                                                {
                                                                    documentReference.delete();
                                                                }

                                                            }

                                                            CollectionReference restaurantListReference = fStore.collection("restaurants");
                                                            restaurantListReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                    if(task.isSuccessful())
                                                                    {
                                                                        for(DocumentSnapshot ds : task.getResult())
                                                                        {
                                                                            if(ds.getString("Restaurant Name").equals(arrayList.get(position).getRestaurantName()))
                                                                            {
                                                                                ArrayList<String> menu = (ArrayList)ds.get("Menu");
                                                                                menu.remove(arrayList.get(position).getName());
                                                                                Map<String, Object> updatedMenu = new HashMap<>();
                                                                                updatedMenu.put("Menu",menu);
                                                                                DocumentReference restaurantReference = fStore.collection("restaurants").document(ds.getId());
                                                                                restaurantReference.update(updatedMenu).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if(task.isSuccessful())
                                                                                        {
                                                                                            StorageReference storageReference = FirebaseStorage.getInstance().getReference(arrayList.get(position).getImagePath());
                                                                                            storageReference.delete();
                                                                                            arrayList.remove(position);
                                                                                            notifyDataSetChanged();
                                                                                        }
                                                                                    }
                                                                                });
                                                                                break;

                                                                            }
                                                                        }
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

                if(fragment.equals("Patient"))
                {
                    addedFoodList.clear();
                    found = false;
                    Food food = new Food(arrayList.get(position).getName(), arrayList.get(position).getDescription(),
                            arrayList.get(position).getPrice(), Integer.valueOf(tv_food_quantity.getText().toString()),
                            arrayList.get(position).getIntolerance(), arrayList.get(position).getImagePath(),
                            arrayList.get(position).getRestaurantName(), arrayList.get(position).getStatus());


                    //Cart cart = new Cart(addedFoodList, patientName, food.getRestaurantName());

                    cartReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful())
                            {
                                sameRestaurant = true;
                                DocumentSnapshot documentSnapshot = task.getResult();
                                if (!documentSnapshot.exists())
                                {
//                                    Map<String, Object> newCart = new HashMap<>();
//                                    newCart.put("Cart", "");
//                                    newCart.put("Patient Name", patientName);
//                                    newCart.put("Restaurant Name", food.getRestaurantName());
//                                    cartReference.set(newCart);
                                }
                                else
                                {
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

                                        if(!restaurantName.equals(arrayList.get(position).getRestaurantName()))
                                        {
                                            sameRestaurant = false;
                                            AlertDialog alertDialog = new AlertDialog.Builder(context)
                                                    .setTitle("Please wait...")
                                                    .setMessage("The cart contains food from other restaurant. Remove it?")
                                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            cartReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    sameRestaurant = true;
                                                                    addedFoodList.clear();
                                                                    addedFoodList.add(food);
                                                                    Map<String, Object> newCart = new HashMap<>();
                                                                    newCart.put("Cart", addedFoodList);
                                                                    newCart.put("Patient Name", patientName);
                                                                    newCart.put("Restaurant Name", food.getRestaurantName());
                                                                    cartReference.set(newCart).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful())
                                                                            {
                                                                                Toast.makeText(context,food.getName()+" is added to your cart", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });

                                                                }
                                                            });
                                                        }
                                                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                        }
                                                    }).show();

                                            break;
                                        }

                                        if(name.equals(arrayList.get(position).getName()))
                                        {
                                            food.setQuantity(foodInCart.getQuantity()+Integer.valueOf(tv_food_quantity.getText().toString()));
                                        }
                                        else
                                        {
                                            addedFoodList.add(foodInCart);
                                        }

                                    }
                                }
                                if(sameRestaurant)
                                {
                                    addedFoodList.add(food);

                                    Map<String, Object> existingCart = new HashMap<>();
                                    existingCart.put("Cart", addedFoodList);
                                    existingCart.put("Patient Name", patientName);
                                    existingCart.put("Restaurant Name", food.getRestaurantName());
                                    cartReference.set(existingCart).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                Toast.makeText(context,food.getName()+" is added to your cart", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

//                                    cartReference.update("Cart",addedFoodList).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if(task.isSuccessful())
//                                            {
//                                                Toast.makeText(context,food.getName()+" is added to your cart", Toast.LENGTH_SHORT).show();
//                                            }
//                                        }
//                                    });
                                }



                            }
                        }
                    });

                }
            }
        });

        btn_checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cartReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if(documentSnapshot.exists())
                            {
                                Bundle bundle = new Bundle();
                                if(restaurant.getRestaurantName().equals(documentSnapshot.getString("Restaurant Name"))) //same menu
                                {
                                    bundle.putSerializable("Restaurant",restaurant);
                                    MainActivity activity = (MainActivity) context;
                                    CheckoutFragment checkoutFragment = new CheckoutFragment();
                                    checkoutFragment.setArguments(bundle);

                                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                            checkoutFragment).commit();
                                }
                                else //different menu
                                {
                                    CollectionReference restaurantListReference = fStore.collection("restaurants");
                                    restaurantListReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if(task.isSuccessful())
                                            {
                                                for (DocumentSnapshot ds : task.getResult())
                                                {
                                                    if(ds.getString("Restaurant Name").equals(documentSnapshot.getString("Restaurant Name")))
                                                    {
                                                        restaurant = new Restaurant(ds.getString("Restaurant Name"),
                                                                ds.getString("Restaurant Address"),
                                                                ds.getString("Restaurant State"),
                                                                ds.getString("Restaurant Owner Email"),
                                                                (ArrayList)ds.get("Menu"),
                                                                ds.getString("Restaurant Logo"));
                                                        break;
                                                    }

                                                }
                                                bundle.putSerializable("Restaurant",restaurant);
                                                MainActivity activity = (MainActivity) context;
                                                CheckoutFragment checkoutFragment = new CheckoutFragment();
                                                checkoutFragment.setArguments(bundle);

                                                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                                        checkoutFragment).commit();
                                            }
                                        }
                                    });

                                }

                            }
                            else
                            {
                                Toast.makeText(context,"The cart is empty!",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

            }
        });

        imgView_add_food.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();
                bundle.putSerializable("Restaurant", restaurant);

                MainActivity activity = (MainActivity) context;
                AddFoodFragment fragment = new AddFoodFragment();
                fragment.setArguments(bundle);
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
            }
        });

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        return convertView;
    }
}
