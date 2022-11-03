package com.example.foodintoleranceappfyp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.util.StringUtil;
import com.gkash.gkashandroidsdk.GkashPayment;
import com.gkash.gkashandroidsdk.PaymentRequest;
import com.gkash.gkashandroidsdk.PaymentResponse;
import com.gkash.gkashandroidsdk.TransStatusCallback;
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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

public class CartListAdapter extends BaseAdapter implements android.widget.ListAdapter, TransStatusCallback/*, PaymentResultListener*/ {

    private Cart cart;
    private Context context;
    private Restaurant restaurant;

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    String userId = fAuth.getCurrentUser().getEmail();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseStorage fStorage = FirebaseStorage.getInstance();
    StorageReference storageReference;
    String restaurantOwnerEmail, availability, patientName;
    ArrayList<Food> foodList = new ArrayList<>();
    DocumentReference cartReference = fStore.collection("carts").document(userId);
    String summary, singleItem;
    double singleItemTotalPrice, totalPrice;
    int quantity;

    // Get instance of GkashPayment
    final GkashPayment gkashPayment = GkashPayment.getInstance();

    public CartListAdapter(Cart cart, Context context, Restaurant restaurant) {
        this.cart = cart;
        this.context = context;
        this.restaurant = restaurant;
    }

    @Override
    public int getCount() {
        return cart.getFood().size();
    }

    @Override
    public Object getItem(int position) {
        return cart.getFood().get(position);
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
            convertView = inflater.inflate(R.layout.list_cart, null);
        }

        TextView tv_restaurant_name = convertView.findViewById(R.id.tv_restaurant_name);
        ImageView imgView_food_image = convertView.findViewById(R.id.imgView_food_image);
        TextView tv_food_name = convertView.findViewById(R.id.tv_food_name);
        TextView tv_food_price = convertView.findViewById(R.id.tv_food_price);
        TextView tv_food_quantity = convertView.findViewById(R.id.tv_food_quantity);
        ImageView imgView_decrease_quantity = convertView.findViewById(R.id.imgView_decrease_quantity);
        ImageView imgView_increase_quantity = convertView.findViewById(R.id.imgView_increase_quantity);
        ImageView imgView_delete = convertView.findViewById(R.id.imgView_delete);
        Button btn_payment = convertView.findViewById(R.id.btn_payment);
        TextView tv_total_price = convertView.findViewById(R.id.tv_total_price);

        tv_food_name.setText(cart.getFood().get(position).getName());
        tv_food_price.setText("RM"+String.format("%.2f",cart.getFood().get(position).getPrice()));
        tv_food_quantity.setText(String.valueOf(cart.getFood().get(position).getQuantity()));

        tv_restaurant_name.setVisibility(View.GONE);
        btn_payment.setVisibility(View.GONE);
        tv_total_price.setVisibility(View.GONE);

        if(position == cart.getFood().size() - 1)
        {
            tv_restaurant_name.setVisibility(View.VISIBLE);
            tv_restaurant_name.setText("Purchased from: "+restaurant.getRestaurantName());
            btn_payment.setVisibility(View.VISIBLE);
            tv_total_price.setVisibility(View.VISIBLE);

            summary = String.format("%10s", "Name") + String.format("%25s", "Quantity")
                    + String.format("%25s", "Single Price")
                    + String.format("%25s", "Total Price") + System.getProperty("line.separator")
                    + System.getProperty("line.separator") + System.getProperty("line.separator");
            totalPrice = 0;

            for(int i =0; i<cart.getFood().size();i++)
            {
                singleItem = "";
                singleItemTotalPrice = cart.getFood().get(i).getPrice() * cart.getFood().get(i).getQuantity();
                totalPrice += singleItemTotalPrice;

                String name;
                name = cart.getFood().get(i).getName();
                ArrayList<String> splitText = new ArrayList<>();
                int length = name.length();
                int startIndex = 0;
                int endIndex = 0;
                int index;

                if(name.length()>15)
                {
                    String newLine;
                    String temp;
                    splitText.clear();
                    while(length!=endIndex+1)
                    {
                        endIndex=startIndex+15;
                        if(endIndex>length-1)
                        {
                            endIndex = length-1;
                            temp =  name.substring(startIndex);
                        }
                        else
                        {
                            temp =  name.substring(startIndex, endIndex);
                        }
                        index = temp.lastIndexOf(" ");

                        if(index!=-1)
                        {
                            if(endIndex-startIndex>=15)
                            {
                                newLine = temp.substring(0, index).trim()+ System.getProperty("line.separator");
                                startIndex = startIndex + index;
                            }
                            else
                            {
                                newLine = temp.trim();
                                startIndex = endIndex;
                            }
                            splitText.add(newLine);
                        }
                        else
                        {
                            splitText.add(temp);
                            startIndex=endIndex;
                        }

                    }
                    for (String text : splitText)
                    {
                        singleItem += String.format("%s", text);
                    }
                    singleItem += String.format("%12s",cart.getFood().get(i).getQuantity())
                            + String.format("%22s", "RM") + String.format("%-21.2f %s", cart.getFood().get(i).getPrice(),"RM")
                            + String.format("%.2f", singleItemTotalPrice)
                            + System.getProperty("line.separator") + System.getProperty("line.separator") ;

                }
                else
                {
                    singleItem = String.format("%s", cart.getFood().get(i).getName()) + String.format("%12s", cart.getFood().get(i).getQuantity())
                            + String.format("%22s", "RM") + String.format("%-21.2f %s", cart.getFood().get(i).getPrice(),"RM")
                            + String.format("%.2f", singleItemTotalPrice)
                            + System.getProperty("line.separator") + System.getProperty("line.separator");
                }

                summary += singleItem;
            }
            summary += System.getProperty("line.separator") + String.format("%100s", "RM")+String.format("%.2f", totalPrice);
            tv_total_price.setText(summary);
            tv_total_price.setTextSize(13);
        }

        if(!cart.getFood().get(position).getImagePath().isEmpty())
        {
            storageReference = fStorage.getReference(cart.getFood().get(position).getImagePath());
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

        imgView_increase_quantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity = Integer.valueOf(tv_food_quantity.getText().toString());
                quantity += 1;
                tv_food_quantity.setText(String.valueOf(quantity));
                cartReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            foodList.clear();
                            DocumentSnapshot documentSnapshot = task.getResult();
                            ArrayList<Map<String, Object>> foodsInCart = (ArrayList<Map<String, Object>>) documentSnapshot.get("Cart");
                            for (int i=0;i<foodsInCart.size();i++)
                            {
                                String name = foodsInCart.get(i).get("name").toString();

                                if(name.equals(cart.getFood().get(position).getName()))
                                {
                                    cart.getFood().get(position).setQuantity(quantity);
                                }

                                foodList.add(cart.getFood().get(i));
                            }
                            cartReference.update("Cart",foodList).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    notifyDataSetChanged();
                                }
                            });


                        }
                    }
                });



            }
        });

        imgView_decrease_quantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity = Integer.valueOf(tv_food_quantity.getText().toString());
                if(quantity > 1)
                {
                    quantity -= 1;
                    tv_food_quantity.setText(String.valueOf(quantity));
                    cartReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful())
                            {
                                foodList.clear();
                                DocumentSnapshot documentSnapshot = task.getResult();
                                ArrayList<Map<String, Object>> foodsInCart = (ArrayList<Map<String, Object>>) documentSnapshot.get("Cart");
                                for (int i=0;i<foodsInCart.size();i++)
                                {
                                    String name = foodsInCart.get(i).get("name").toString();

                                    if(name.equals(cart.getFood().get(position).getName()))
                                    {
                                        cart.getFood().get(position).setQuantity(quantity);
                                    }
                                    foodList.add(cart.getFood().get(i));
                                }
                                cartReference.update("Cart",foodList).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

        imgView_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cartReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            ArrayList<Map<String, Object>> foodsInCart = (ArrayList<Map<String, Object>>) documentSnapshot.get("Cart");
                            for (int i=0;i<foodsInCart.size();i++)
                            {
                                String name = foodsInCart.get(i).get("name").toString();

                                if(name.equals(cart.getFood().get(position).getName()))
                                {
                                    cart.getFood().remove(position);
                                    cartReference.update("Cart",cart.getFood());
                                    notifyDataSetChanged();
                                    if(cart.getFood().isEmpty())
                                    {
                                        cartReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Bundle bundle = new Bundle();
                                                bundle.putSerializable("Restaurant", restaurant);
                                                MainActivity activity = (MainActivity) context;
                                                FoodFragment foodFragment = new FoodFragment();
                                                foodFragment.setArguments(bundle);
                                                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                                        foodFragment).commit();
                                            }
                                        });
                                    }

                                    break;
                                }

                            }
                        }
                    }
                });
            }
        });

        btn_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String merchantID = "M161-U-40563";
                String signatureKey = "9qrsdIOONgrwYrD";

                MainActivity activity = (MainActivity) context;

                // Create instance of PaymentRequest
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                String cartID = sdf.format(new Date());

                PaymentRequest request = new PaymentRequest("1.5.0", merchantID, signatureKey, "MYR",
                        BigDecimal.valueOf(totalPrice), cartID, CartListAdapter.this);
                //set your callback url, email ,mobile number and return url
                //return url is your app url scheme
                request.setCallbackUrl("https://paymentdemo.gkash.my/callback.php");
                request.setEmail(userId);
                request.setMobileNo("60185700057");
                request.setReturnUrl("gkash://returntoapp");

                // Set environment and start payment
                gkashPayment.setProductionEnvironment(false);

                try
                {
                    gkashPayment.startPayment(activity, request);
                }
                catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

            }
        });

        return convertView;
    }

    @Override
    public void onStatusCallback(PaymentResponse response) {
        if (response != null)
        {
            if(response.status.equals("88 - Transferred") && response.description.equals("00 - Approved"))
            {
                MainActivity activity = (MainActivity) context;

                Toast.makeText(context,"Order successfully! Redirecting you to order page", Toast.LENGTH_SHORT).show();

                cartReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            String patientName = documentSnapshot.getString("Patient Name");
                            String restaurantName = documentSnapshot.getString("Restaurant Name");
                            ArrayList<Map<String, Object>> foodsInCart = (ArrayList<Map<String, Object>>) documentSnapshot.get("Cart");

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                            String orderDateTime = sdf.format(new Date());

                            DocumentReference orderReference = fStore.collection("pendingOrders").document(userId+orderDateTime);
                            Map<String, Object> order = new HashMap<>();
                            order.put("Ordered Food", foodsInCart);
                            order.put("Patient Name", patientName);
                            order.put("Patient Email", userId);
                            order.put("Restaurant Name", restaurantName);
                            order.put("Status", "Pending");
                            order.put("Order DateTime", orderDateTime);

                            CollectionReference restaurantsReference = fStore.collection("restaurants");
                            restaurantsReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful())
                                    {
                                        for(DocumentSnapshot snapshot : task.getResult())
                                        {
                                            if(snapshot.getString("Restaurant Name").equals(restaurantName))
                                            {
                                                String restaurantOwnerEmail = snapshot.getString("Restaurant Owner Email");
                                                String senderEmail="foodintoleranceapp53@gmail.com";
                                                String senderPassword="lkqgijyawiwshwjc";
                                                String messageToSend="Your restaurant receives a new order. Please proceed to the order page to view the order.";
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
                                                    javax.mail.Message message = new MimeMessage(session);
                                                    message.setFrom(new InternetAddress(senderEmail));
                                                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(restaurantOwnerEmail));
                                                    message.setSubject("New Order from Food Intolerance App Patient");
                                                    message.setText(messageToSend);
                                                    Transport.send(message);
                                                }catch (MessagingException e){
                                                    throw new RuntimeException(e);
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                            });

                            orderReference.set(order).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    cartReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            activity.navigationView.setCheckedItem(R.id.nav_orderHistory);
                                            activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                                    new PatientOrderHistoryFragment()).commit();
                                        }
                                    });
                                }
                            });

                        }
                    }
                });
            }
            else
            {
                Toast.makeText(context,"Transaction Failed...Please try again later.",Toast.LENGTH_SHORT).show();
            }
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }


}
