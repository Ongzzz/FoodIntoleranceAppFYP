package com.example.foodintoleranceappfyp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import androidx.annotation.NonNull;

public class OrderListAdapter extends BaseAdapter implements android.widget.ListAdapter {

    private ArrayList<Order> arrayList = new ArrayList<>();
    private Context context;
    private String operation;
    private Restaurant restaurant;

    FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    public OrderListAdapter(ArrayList<Order> arrayList, Context context, String operation) {
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
            convertView = inflater.inflate(R.layout.list_order, null);
        }

        TextView tv_orderInfo = convertView.findViewById(R.id.tv_orderInfo);
        ImageView imgView_updateOrderStatus = convertView.findViewById(R.id.imgView_updateOrderStatus);
        String orderInfo = "";

        //String intolerance = TextUtils.join(", ", arrayList.get(position).getIntolerance());

        if(arrayList.get(position).getFoodList().size()>1)
        {
            orderInfo = "";

            for(int i = 0; i< arrayList.get(position).getFoodList().size();i++)
            {
                if(i==arrayList.get(position).getFoodList().size()-1)
                {
                    orderInfo += String.format("%-27s : %s", "Food Name", arrayList.get(position).getFoodList().get(i).getName())
                            + System.getProperty("line.separator")
                            + String.format("%-32s : %s", "Quantity", arrayList.get(position).getFoodList().get(i).getQuantity())
                            + System.getProperty("line.separator")
                            + String.format("%-22s : %s", "Restaurant Name", arrayList.get(position).getRestaurantName())
                            + System.getProperty("line.separator")
                            + String.format("%-22s : %s", "Ordered DateTime", arrayList.get(position).getOrderDateTime())
                            + System.getProperty("line.separator");
                    if(operation.equals("Completed Order"))
                    {
                        orderInfo += String.format("%-18s : %s", "Completed DateTime", arrayList.get(position).getCompletedDateTime())
                                + System.getProperty("line.separator");
                    }
                    orderInfo += String.format("%-33s : %s", "Status", arrayList.get(position).getStatus());
                }
                else
                {
                    orderInfo += String.format("%-27s : %s", "Food Name", arrayList.get(position).getFoodList().get(i).getName())
                            + System.getProperty("line.separator")
                            + String.format("%-32s : %s", "Quantity", arrayList.get(position).getFoodList().get(i).getQuantity())
                            + System.getProperty("line.separator") + System.getProperty("line.separator");
                }
            }

        }
        else
        {
            orderInfo = String.format("%-27s : %s", "Food Name", arrayList.get(position).getFoodList().get(0).getName())
                    + System.getProperty("line.separator")
                    + String.format("%-32s : %s", "Quantity", arrayList.get(position).getFoodList().get(0).getQuantity())
                    + System.getProperty("line.separator")
                    + String.format("%-22s : %s", "Restaurant Name", arrayList.get(position).getRestaurantName())
                    + System.getProperty("line.separator")
                    + String.format("%-22s : %s", "Ordered DateTime", arrayList.get(position).getOrderDateTime())
                    + System.getProperty("line.separator");
                    if(operation.equals("Completed Order"))
                    {
                        orderInfo += String.format("%-18s : %s", "Completed DateTime", arrayList.get(position).getCompletedDateTime())
                                + System.getProperty("line.separator");
                    }
                    orderInfo += String.format("%-33s : %s", "Status", arrayList.get(position).getStatus());

        }
        tv_orderInfo.setText(orderInfo);

        if(operation.equals("Completed Order"))
        {
            imgView_updateOrderStatus.setVisibility(View.GONE);
        }
        if(operation.equals("Pending Order"))
        {
            imgView_updateOrderStatus.setVisibility(View.VISIBLE);
        }
        if(operation.equals("Patient Pending Order"))
        {
            imgView_updateOrderStatus.setImageResource(R.drawable.ic_location);
        }

        imgView_updateOrderStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(operation.equals("Pending Order"))
                {
                    AlertDialog alertDialog = new AlertDialog.Builder(context)
                            .setTitle("About this order...")
                            .setMessage("This order is ready?"+System.getProperty("line.separator")+System.getProperty("line.separator")+
                                    tv_orderInfo.getText().toString())
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ProgressDialog progressDialog = new ProgressDialog(context);
                                    progressDialog.setTitle("Notifying the patient...");
                                    progressDialog.show();

                                    DocumentReference pendingOrderReference = fStore.collection("pendingOrders").
                                            document(arrayList.get(position).getPatientEmail() + arrayList.get(position).getOrderDateTime());

                                    Map<String,Object> status = new HashMap<>();
                                    status.put("Status","Food is Ready");

                                    pendingOrderReference.update(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                String senderEmail="foodintoleranceapp53@gmail.com";
                                                String senderPassword="lkqgijyawiwshwjc";
                                                String messageToSend="Your Food is Ready. Please go to "
                                                        + arrayList.get(position).getRestaurantName() + " as soon as possible to pick up your food. Thank you.";
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
                                                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(arrayList.get(position).getPatientEmail()));
                                                    message.setSubject("Your Order "+ pendingOrderReference.getId()+" is Ready");
                                                    message.setText(messageToSend);
                                                    Transport.send(message);
                                                }catch (MessagingException e){
                                                    throw new RuntimeException(e);
                                                }
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
                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                }
                if(operation.equals("Patient Pending Order"))
                {
                    if(arrayList.get(position).getStatus().equals("Food is Ready"))
                    {
                        AlertDialog alertDialog = new AlertDialog.Builder(context)
                                .setTitle("About this order...")
                                .setMessage("What to do with this order?")
                                .setPositiveButton("Pickup Successfully", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ProgressDialog progressDialog = new ProgressDialog(context);
                                        progressDialog.setTitle("Confirming the pickup...");
                                        progressDialog.show();
                                        DocumentReference pickupOrderReference = fStore.collection("pickupOrders").
                                                document(arrayList.get(position).getPatientEmail()+arrayList.get(position).getOrderDateTime());

                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                                        String completedDateTime = sdf.format(new Date());

                                        Map<String, Object> order = new HashMap<>();
                                        order.put("Ordered Food", arrayList.get(position).getFoodList());
                                        order.put("Patient Name", arrayList.get(position).getPatientName());
                                        order.put("Patient Email", arrayList.get(position).getPatientEmail());
                                        order.put("Restaurant Name", arrayList.get(position).getRestaurantName());
                                        order.put("Status", "Completed");
                                        order.put("Order DateTime", arrayList.get(position).getOrderDateTime());
                                        order.put("Order Completed DateTime", completedDateTime);

                                        pickupOrderReference.set(order).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    DocumentReference documentReference = fStore.collection("pendingOrders")
                                                            .document(arrayList.get(position).getPatientEmail()+arrayList.get(position).getOrderDateTime());
                                                    documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                }).setNegativeButton("Get Direction", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        CollectionReference restaurantListReference = fStore.collection("restaurants");
                                        restaurantListReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if(task.isSuccessful())
                                                {
                                                    for (DocumentSnapshot documentSnapshot : task.getResult())
                                                    {
                                                        if(documentSnapshot.getString("Restaurant Name").equals(arrayList.get(position).getRestaurantName()))
                                                        {
                                                            restaurant = new Restaurant(documentSnapshot.getString("Restaurant Name"),
                                                                    documentSnapshot.getString("Restaurant Address"),
                                                                    documentSnapshot.getString("Restaurant State"),
                                                                    documentSnapshot.getString("Restaurant Owner Email"),
                                                                    (ArrayList)documentSnapshot.get("Menu"),
                                                                    documentSnapshot.getString("Restaurant Logo"));
                                                            break;
                                                        }
                                                    }

                                                    Bundle bundle = new Bundle();
                                                    bundle.putSerializable("Restaurant",restaurant);
                                                    RestaurantLocationTab restaurantLocationTab = new RestaurantLocationTab();
                                                    restaurantLocationTab.setArguments(bundle);

                                                    MainActivity activity = (MainActivity) context;
                                                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                                            restaurantLocationTab).commit();
                                                }
                                            }
                                        });
                                    }
                                }).show();
                    }
                    else //food haven't ready
                    {
                        AlertDialog alertDialog = new AlertDialog.Builder(context)
                                .setTitle("About this order...")
                                .setMessage("What to do with this order?")
                                .setPositiveButton("Get Direction", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        CollectionReference restaurantListReference = fStore.collection("restaurants");
                                        restaurantListReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if(task.isSuccessful())
                                                {
                                                    for (DocumentSnapshot documentSnapshot : task.getResult())
                                                    {
                                                        if(documentSnapshot.getString("Restaurant Name").equals(arrayList.get(position).getRestaurantName()))
                                                        {
                                                            restaurant = new Restaurant(documentSnapshot.getString("Restaurant Name"),
                                                                    documentSnapshot.getString("Restaurant Address"),
                                                                    documentSnapshot.getString("Restaurant State"),
                                                                    documentSnapshot.getString("Restaurant Owner Email"),
                                                                    (ArrayList)documentSnapshot.get("Menu"),
                                                                    documentSnapshot.getString("Restaurant Logo"));
                                                            break;
                                                        }
                                                    }

                                                    Bundle bundle = new Bundle();
                                                    bundle.putSerializable("Restaurant",restaurant);
                                                    RestaurantLocationTab restaurantLocationTab = new RestaurantLocationTab();
                                                    restaurantLocationTab.setArguments(bundle);

                                                    MainActivity activity = (MainActivity) context;
                                                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                                            restaurantLocationTab).commit();
                                                }
                                            }
                                        });
                                    }
                                }).show();
                    }


                }


            }
        });

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        return convertView;
    }
}
