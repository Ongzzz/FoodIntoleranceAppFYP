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
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.TimeZone;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import androidx.annotation.NonNull;

public class ConsultationListAdapter extends BaseAdapter implements android.widget.ListAdapter {

    private ArrayList<Consultation> arrayList = new ArrayList<>();
    private Context context;
    private String fragmentName;

    FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    String userId = fAuth.getCurrentUser().getEmail();

    public ConsultationListAdapter(ArrayList<Consultation> arrayList, Context context, String fragmentName) {
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
            convertView = inflater.inflate(R.layout.list_consultation, null);
        }

        TextView tv_consultationInfo = convertView.findViewById(R.id.tv_consultationInfo);
        ImageView imgView_consultPatient = convertView.findViewById(R.id.imgView_consultPatient);

        if(fragmentName.equals("Consult Patient"))
        {
            imgView_consultPatient.setVisibility(View.VISIBLE);

            String consultationInfo = String.format("%-14s : %s", "Appointment Time", arrayList.get(position).getDateTime())
                    + System.getProperty("line.separator") + String.format("%-21s : %s", "Patient Name", arrayList.get(position).getPatientName())
                    + System.getProperty("line.separator") + String.format("%-23s : %s", "Patient Email", arrayList.get(position).getPatientEmail());


            tv_consultationInfo.setText(consultationInfo);

            imgView_consultPatient.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog alertDialog = new AlertDialog.Builder(context)
                            .setTitle("Consult Patient")
                            .setMessage("Consult this patient? \n\n" +
                                    tv_consultationInfo.getText().toString())
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    FirebaseAuth fAuth = FirebaseAuth.getInstance();
                                    String doctorId = fAuth.getCurrentUser().getEmail();
                                    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
                                    DocumentReference documentReference = fStore.collection("doctors").document(doctorId);

                                    String patientName = arrayList.get(position).getPatientName();
                                    String patientEmail = arrayList.get(position).getPatientEmail();
                                    URL serverURL;

                                    try {
                                        serverURL = new URL("https://meet.jit.si");
                                        JitsiMeetConferenceOptions defaultOptions =
                                                new JitsiMeetConferenceOptions.Builder()
                                                        .setServerURL(serverURL)
                                                        .build();
                                        JitsiMeet.setDefaultConferenceOptions(defaultOptions);

                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    }

                                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if(task.isSuccessful())
                                            {
                                                String doctorName;
                                                DocumentSnapshot documentSnapshot = task.getResult();
                                                if (documentSnapshot != null)
                                                {
                                                    doctorName = documentSnapshot.getString("Name");
                                                    String dateTime = arrayList.get(position).getDateTime();
                                                    String urlDateTime = dateTime.replaceAll("\\s","")
                                                            .replace(".","").replace(":","")
                                                            +patientEmail+doctorId;

                                                    JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                                                            .setRoom(urlDateTime+patientEmail+doctorId)
                                                            .build();

                                                    String senderEmail="foodintoleranceapp53@gmail.com";
                                                    String senderPassword="lkqgijyawiwshwjc";
                                                    String messageToSend="Click the following link to join the meeting with "+
                                                            doctorName + ": https://meet.jit.si/"+urlDateTime+patientEmail+doctorId;
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
                                                        message.setSubject("Food Intolerance App Conference");
                                                        message.setText(messageToSend);
                                                        Transport.send(message);
                                                    }catch (MessagingException e){
                                                        throw new RuntimeException(e);
                                                    }

                                                    JitsiMeetActivity.launch(context.getApplicationContext(), options);

                                                    Map <String, Object> consultation = new HashMap<>();
                                                    consultation.put("Patient Name", patientName);
                                                    consultation.put("Patient Email", patientEmail);
                                                    consultation.put("Doctor Name", doctorName);
                                                    consultation.put("Doctor Email", doctorId);
                                                    consultation.put("DateTime", dateTime);
                                                    DocumentReference recordReference = fStore.collection("consultations").document(dateTime+patientEmail+doctorId);
                                                    recordReference.set(consultation).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                        }
                                                    });

                                                    DocumentReference appointmentReference = fStore.collection("appointments").document(dateTime+patientEmail+doctorId);
                                                    Map <String, Object> appointment = new HashMap<>();
                                                    appointment.put("Status", "Consulted");
                                                    appointmentReference.update(appointment);

                                                    arrayList.remove(position);
                                                    notifyDataSetChanged();
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
            });
        }
        if(fragmentName.equals("Consultation Record"))
        {
            imgView_consultPatient.setVisibility(View.GONE);

            String consultationRecord;

            if(userId.equals(arrayList.get(position).getPatientEmail()))
            {
                consultationRecord = String.format("%-14s : %s", "Doctor Name", arrayList.get(position).getDoctorName())
                        + System.getProperty("line.separator") + String.format("%-16s : %s", "Doctor Email", arrayList.get(position).getDoctorEmail())
                        + System.getProperty("line.separator") + String.format("%-19s : %s", "Consultation Time", arrayList.get(position).getDateTime());
            }
            else
            {
                if(userId.equals(arrayList.get(position).getDoctorEmail()))
                {
                    consultationRecord = String.format("%-14s : %s", "Patient Name", arrayList.get(position).getPatientName())
                            + System.getProperty("line.separator") + String.format("%-16s : %s", "Patient Email", arrayList.get(position).getPatientEmail())
                            + System.getProperty("line.separator") + String.format("%-19s : %s", "Consulation Time", arrayList.get(position).getDateTime());
                }
                else
                {
                    consultationRecord = String.format("%-14s : %s", "Patient Name", arrayList.get(position).getPatientName())
                            + System.getProperty("line.separator") + String.format("%-16s : %s", "Patient Email", arrayList.get(position).getPatientEmail())
                            + System.getProperty("line.separator") + String.format("%-14s : %s", "Doctor Name", arrayList.get(position).getDoctorName())
                            + System.getProperty("line.separator") + String.format("%-16s : %s", "Doctor Email", arrayList.get(position).getDoctorEmail())
                            + System.getProperty("line.separator") + String.format("%-19s : %s", "Consultation Time", arrayList.get(position).getDateTime());
                }

            }

            tv_consultationInfo.setText(consultationRecord);
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        return convertView;
    }
}
