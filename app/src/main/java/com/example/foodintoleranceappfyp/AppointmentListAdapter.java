package com.example.foodintoleranceappfyp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.StrictMode;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;
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

public class AppointmentListAdapter extends BaseAdapter implements android.widget.ListAdapter {

    private ArrayList<Appointment> arrayList = new ArrayList<>();
    private Context context;
    private String fragmentName;

    FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    public AppointmentListAdapter(ArrayList<Appointment> arrayList, Context context, String fragmentName) {
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
            convertView = inflater.inflate(R.layout.list_appointment, null);
        }

        TextView tv_appointmentInfo = convertView.findViewById(R.id.tv_appointmentInfo);
        ImageView imgView_manageAppointment = convertView.findViewById(R.id.imgView_manageAppointment);

        String patientName = arrayList.get(position).getPatientName();
        String patientEmail = arrayList.get(position).getPatientEmail();
        String doctorName = arrayList.get(position).getDoctorName();
        String doctorEmail = arrayList.get(position).getDoctorEmail();
        String dateTime = arrayList.get(position).getDateTime();

        String appointmentInfo = String.format("%-14s : %s", "Doctor Name", arrayList.get(position).getPatientName())
                + System.getProperty("line.separator") + String.format("%-16s : %s", "Doctor Email", arrayList.get(position).getPatientEmail())
                + System.getProperty("line.separator") + String.format("%-19s : %s", "Datetime", dateTime);

        tv_appointmentInfo.setText(appointmentInfo);

        if(fragmentName.equals("My Appointment"))
        {
            //imgView_manageAppointment.setVisibility(View.GONE);
            imgView_manageAppointment.setVisibility(View.VISIBLE);
        }

        if(fragmentName.equals("Manage Appointment"))
        {
            imgView_manageAppointment.setVisibility(View.VISIBLE);
        }

        DocumentReference appointmentReference = fStore.collection("appointments").document(dateTime+patientEmail+doctorEmail);
        //DocumentReference consultationReference = fStore.collection("consultations").document(dateTime+patientEmail+doctorEmail);

        imgView_manageAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog alertDialog = new AlertDialog.Builder(context)
                        .setTitle("Manage Appointment")
                        .setMessage("What to do with this appointment? \n\n" +
                                tv_appointmentInfo.getText().toString())
                        .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String senderEmail="foodintoleranceapp53@gmail.com";
                                String senderPassword="lkqgijyawiwshwjc";
                                String messageToSend="Your appointment with "+ doctorName +" on "+ dateTime +" is confirmed." +
                                        " An email with the video consultation link will be sent to you when the doctor is ready." +
                                        " The maximum duration of the consultation is 30 minutes.";
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
                                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(patientEmail));
                                    message.setSubject("Appointment to consult a doctor is confirmed");
                                    message.setText(messageToSend);
                                    Transport.send(message);
                                }catch (MessagingException e){
                                    throw new RuntimeException(e);
                                }

                                Map<String, Object> updateAppointment = new HashMap<>();
                                updateAppointment.put("Status", "Approved");
                                appointmentReference.update(updateAppointment);

                                arrayList.remove(position);
                                notifyDataSetChanged();

                            }
                        }).setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                appointmentReference.delete();

                                String senderEmail="foodintoleranceapp53@gmail.com";
                                String senderPassword="lkqgijyawiwshwjc";
                                String messageToSend="We are sorry to inform you that your appointment with "+ doctorName +" on "+ dateTime +" is declined." +
                                        " The doctor is unable to consult you on the appointment date." +
                                        " Please make another appointment in the app.";
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
                                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(patientEmail));
                                    message.setSubject("Appointment to consult a doctor is declined");
                                    message.setText(messageToSend);
                                    Transport.send(message);
                                }catch (MessagingException e){
                                    throw new RuntimeException(e);
                                }

                                arrayList.remove(position);
                                notifyDataSetChanged();
                            }
                        }).show();

            }
        });

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        return convertView;
    }
}
