package com.example.foodintoleranceappfyp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;
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
import androidx.cardview.widget.CardView;

public class DoctorListAdapter extends BaseAdapter implements android.widget.ListAdapter {

    private ArrayList<Doctor> arrayList = new ArrayList<>();
    private Context context;
    private String fragmentName;

    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseStorage fStorage = FirebaseStorage.getInstance();

    public DoctorListAdapter(ArrayList<Doctor> arrayList, Context context, String fragmentName) {
        this.arrayList = arrayList;
        this.context = context;
        this.fragmentName = fragmentName;
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
            convertView = inflater.inflate(R.layout.list_doctor, null);
        }

        TextView tv_doctorInfo = convertView.findViewById(R.id.tv_doctorInfo);
        ImageView imgView_managePendingDoctor = convertView.findViewById(R.id.imgView_managePendingDoctor);
        ImageView imgView_viewDocument = convertView.findViewById(R.id.imgView_viewDocument);

        String doctorInfo = String.format("%-14s : %s", "Doctor Name", arrayList.get(position).getName())
                + System.getProperty("line.separator") + String.format("%-16s : %s", "Doctor Email", arrayList.get(position).getEmail())
                + System.getProperty("line.separator") + String.format("%-20s : %s", "Hospital", arrayList.get(position).getHospital());

        tv_doctorInfo.setText(doctorInfo);

        if(fragmentName.equals("Doctor List"))
        {
            imgView_managePendingDoctor.setVisibility(View.GONE);
            imgView_viewDocument.setVisibility(View.GONE);
        }

        if(fragmentName.equals("Admin Doctor List"))
        {
            imgView_managePendingDoctor.setVisibility(View.GONE);
        }


        imgView_viewDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //MainActivity activity = (MainActivity) context;

                View radioButtonView = v.inflate(context, R.layout.select_document, null);
                RadioGroup radioGroup_document = radioButtonView.findViewById(R.id.radioGroup_document);

                for (String documentName : arrayList.get(position).getDocumentNameList())
                {
                    RadioButton radioButton = new RadioButton(radioButtonView.getContext());
                    radioButton.setText(documentName.substring(documentName.lastIndexOf("/")+1));
                    radioButton.setId(arrayList.get(position).getDocumentNameList().indexOf(documentName));
                    radioButton.setTextSize(15);
                    radioGroup_document.addView(radioButton);
                    TextView tv = new TextView(radioButtonView.getContext());
                    tv.setText(System.getProperty("line.separator"));
                    radioGroup_document.addView(tv);
                }

                radioGroup_document.check(0);

                AlertDialog alertDialog = new AlertDialog.Builder(context)
                        .setView(radioButtonView)
                        .setTitle("View Document")
                        .setMessage("Please select a document to view")
                        .setPositiveButton("View", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Intent intent = new Intent(context, PdfViewActivity.class);
                                intent.putExtra("Document", arrayList.get(position).getDocumentNameList().get(radioGroup_document.getCheckedRadioButtonId()));
                                context.startActivity(intent);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();


            }
        });

        imgView_managePendingDoctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(context.getApplicationContext(), arrayList.get(position).getEmail(), Toast.LENGTH_SHORT).show();
                AlertDialog alertDialog = new AlertDialog.Builder(context)
                        .setTitle("Approve Doctor")
                        .setMessage("Approve this doctor? \n\n" +
                                tv_doctorInfo.getText().toString())
                        .setPositiveButton("Approve", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                                String dateTime = simpleDateFormat.format(new Date());
                                String userId = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                                DocumentReference addApprovedDoctor = fStore.collection("doctors").document(arrayList.get(position).getEmail());
                                Map<String, Object> doctor = new HashMap<>();
                                doctor.put("Name",arrayList.get(position).getName());
                                doctor.put("Email",arrayList.get(position).getEmail());
                                doctor.put("Hospital",arrayList.get(position).getHospital());
                                doctor.put("Approved Date", dateTime);
                                doctor.put("Approved By", userId);
                                addApprovedDoctor.set(doctor);

                                DocumentReference updateDoctorStatus = fStore.collection("users").document(arrayList.get(position).getEmail());
                                Map<String, Object> updateDoctor = new HashMap<>();
                                updateDoctor.put("UserType", "Doctor");
                                updateDoctorStatus.update(updateDoctor);

                                DocumentReference removePendingDoctor = fStore.collection("pendingDoctors").document(arrayList.get(position).getEmail());
                                removePendingDoctor.delete();

                                String senderEmail="foodintoleranceapp53@gmail.com";
                                String senderPassword="lkqgijyawiwshwjc";
                                String messageToSend="Your account is approved. You can consult patient now!";
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
                                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(arrayList.get(position).getEmail()));
                                    message.setSubject("Food Intolerance App Doctor Account Approval Email");
                                    message.setText(messageToSend);
                                    Transport.send(message);
                                }catch (MessagingException e){
                                    throw new RuntimeException(e);
                                }

                                Toast.makeText(context, arrayList.get(position).getName() + " is approved", Toast.LENGTH_SHORT).show();
                                arrayList.remove(position);
                                notifyDataSetChanged();

                            }
                        }).setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                DocumentReference removePendingDoctor = fStore.collection("pendingDoctors").document(arrayList.get(position).getEmail());
                                removePendingDoctor.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            StorageReference storageReference = fStorage.getReference("documents/doctors/"+arrayList.get(position).getEmail());
                                            storageReference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                                @Override
                                                public void onSuccess(ListResult listResult) {
                                                    for (StorageReference singleFileReference : listResult.getItems())
                                                    {
                                                        singleFileReference.delete();
                                                    }
                                                    Toast.makeText(context, arrayList.get(position).getName() + " is rejected", Toast.LENGTH_SHORT).show();

                                                    arrayList.remove(position);
                                                    notifyDataSetChanged();
                                                }
                                            });


                                        }
                                    }
                                });


                            }
                        }).show();

            }
        });

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        return convertView;
    }

}
