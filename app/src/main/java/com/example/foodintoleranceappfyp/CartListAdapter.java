package com.example.foodintoleranceappfyp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.util.Log;
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

import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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
import androidx.core.text.HtmlCompat;

public class CartListAdapter extends BaseAdapter implements android.widget.ListAdapter, PaymentResultListener {

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
        //PayPalButton payPalButton = convertView.findViewById(R.id.btn_payment);
        TextView tv_total_price = convertView.findViewById(R.id.tv_total_price);

        tv_food_name.setText(cart.getFood().get(position).getName());
        tv_food_price.setText("RM"+String.format("%.2f",cart.getFood().get(position).getPrice()));
        tv_food_quantity.setText(String.valueOf(cart.getFood().get(position).getQuantity()));

        tv_restaurant_name.setVisibility(View.GONE);
        btn_payment.setVisibility(View.GONE);
        //payPalButton.setVisibility(View.GONE);
        tv_total_price.setVisibility(View.GONE);

//        if(position == 0)
//        {
//            tv_restaurant_name.setVisibility(View.VISIBLE);
//            tv_restaurant_name.setText("Purchased from: "+cart.getFood().get(0).getRestaurantName());
//        }



        if(position == cart.getFood().size() - 1)
        {
            tv_restaurant_name.setVisibility(View.VISIBLE);
            tv_restaurant_name.setText("Purchased from: "+restaurant.getRestaurantName());
            btn_payment.setVisibility(View.VISIBLE);
            //payPalButton.setVisibility(View.VISIBLE);
            tv_total_price.setVisibility(View.VISIBLE);

            summary = "Name" + String.format("%20s", "Quantity")
                    + String.format("%20s", "Single Price")
                    + String.format("%20s", "Total Price") + System.getProperty("line.separator")
                    + System.getProperty("line.separator") + System.getProperty("line.separator");
            totalPrice = 0;

            for(int i =0; i<cart.getFood().size();i++)
            {
                singleItemTotalPrice = cart.getFood().get(i).getPrice() * cart.getFood().get(i).getQuantity();
                totalPrice += singleItemTotalPrice;
                singleItem = String.format("%5s",cart.getFood().get(i).getName()) + String.format("%20s", cart.getFood().get(i).getQuantity())
                        + String.format("%20s", "RM") + String.format("%-18.2f %s", cart.getFood().get(i).getPrice(),"RM")
                        + String.format("%.2f", singleItemTotalPrice)
                        + System.getProperty("line.separator") + System.getProperty("line.separator");
                summary += singleItem;
            }
            summary += System.getProperty("line.separator") + String.format("%81s", "RM")+String.format("%.2f", totalPrice);
            tv_total_price.setText(summary);
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

//                Bundle bundle = new Bundle();
//                bundle.putSerializable("Restaurant", restaurant);
//                Intent i = new Intent(context,PaymentActivity.class);
//                i.putExtras(bundle);
//                i.putExtra("Total Price", totalPrice);
//                context.startActivity(i);

                MainActivity activity = (MainActivity) context;
                Checkout checkout = new Checkout();
                //set key id
                checkout.setKeyID("rzp_test_WXyqLSujm7ZVOq");
                //set image
                checkout.setImage(R.drawable.ic_payment);
                //initialize json object
                JSONObject object = new JSONObject();

                try {
                    //put name
                    object.put("name","Food Intolerance App");
                    //put description
                    object.put("description","Purchase from:"+restaurant.getRestaurantName());
                    //put theme color
                    object.put("theme.color","#0093DD");
                    //put currency unit
                    object.put("currency","MYR");
                    //put amount
                    object.put("amount",totalPrice*100);
                    //put mobile number
                    //object.put("prefill.contact", "60185700057");
                    //put email
                    object.put("prefill.email", userId);
                    //open razorpay checkout activity
                    checkout.open(activity,object);
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }

            }
        });

//        payPalButton.setup(
//                new CreateOrder() {
//                    @Override
//                    public void create(@NotNull CreateOrderActions createOrderActions) {
//                        ArrayList<PurchaseUnit> purchaseUnits = new ArrayList<>();
//                        purchaseUnits.add(
//                                new PurchaseUnit.Builder()
//                                        .amount(
//                                                new Amount.Builder()
//                                                        .currencyCode(CurrencyCode.USD)
//                                                        .value("10.00")
//                                                        .build()
//                                        )
//                                        .build()
//                        );
//                        Order order = new Order(
//                                OrderIntent.CAPTURE,
//                                new AppContext.Builder()
//                                        .userAction(UserAction.PAY_NOW)
//                                        .build(),
//                                purchaseUnits,
//                                null
//                        );
//                        createOrderActions.create(order, (CreateOrderActions.OnOrderCreated) null);
//                    }
//                },
//                new OnApprove() {
//                    @Override
//                    public void onApprove(@NotNull Approval approval) {
//                        approval.getOrderActions().capture(new OnCaptureComplete() {
//                            @Override
//                            public void onCaptureComplete(@NotNull CaptureOrderResult result) {
//                                Log.i("CaptureOrder", String.format("CaptureOrderResult: %s", result));
//                            }
//                        });
//                    }
//                }
//
//
//        );



        return convertView;
    }

    @Override
    public void onPaymentSuccess(String s) {
        //initialize alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Payment")
                .setMessage(s);
        builder.show();
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(context,s, Toast.LENGTH_SHORT).show();
    }
}
