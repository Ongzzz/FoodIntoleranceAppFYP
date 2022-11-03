package com.example.foodintoleranceappfyp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TimePicker;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MakeAppointmentFragment extends Fragment {

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    String userId = fAuth.getCurrentUser().getEmail();
    DocumentReference patientReference = fStore.collection("patients").document(userId);
    CollectionReference doctorReference = fStore.collection("doctors");
    CollectionReference appointmentReference = fStore.collection("appointments");
    String doctorEmail;
    String patientName;
    boolean isFree = true;

    SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.CHINA);
    String currentDay = dayFormat.format(new Date());

    SimpleDateFormat hourFormat = new SimpleDateFormat("HH", Locale.CHINA);
    String currentHour = hourFormat.format(new Date());

    SimpleDateFormat minuteFormat = new SimpleDateFormat("mm", Locale.CHINA);
    String currentMinute = minuteFormat.format(new Date());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_make_appointment, container, false);

        Spinner spinner_doctor = view.findViewById(R.id.spinner_doctor);
        DatePicker dp_appointment = view.findViewById(R.id.dp_appointment);
        TimePicker tp_appointment = view.findViewById(R.id.tp_appointment);
        Button btn_makeAppointment = view.findViewById(R.id.btn_makeAppointment);

        List<String> doctorList =  new ArrayList<String>();
        doctorList.add("Please select a doctor");

        doctorReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if(task.isSuccessful())
                {
                    for (DocumentSnapshot documentSnapshot : task.getResult())
                    {
                        doctorList.add(documentSnapshot.getString("Name"));
                    }
                }
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_item, doctorList);

        spinner_doctor.setAdapter(adapter);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1 );

        dp_appointment.setMinDate(System.currentTimeMillis() - 1000);
        dp_appointment.setMaxDate(calendar.getTimeInMillis());

        tp_appointment.setIs24HourView(true);
        tp_appointment.setHour(Integer.valueOf(currentHour));
        tp_appointment.setMinute(Integer.valueOf(currentMinute));


        btn_makeAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int day = dp_appointment.getDayOfMonth();
                int month = dp_appointment.getMonth() + 1;
                int year = dp_appointment.getYear();
                int hour = tp_appointment.getHour();
                int minute = tp_appointment.getMinute();

                String doctorName = spinner_doctor.getSelectedItem().toString();
                boolean valid = true;

                currentDay = dayFormat.format(new Date());
                currentHour = hourFormat.format(new Date());
                currentMinute = minuteFormat.format(new Date());

                if(doctorName.equals(doctorList.get(0)))
                {
                    valid = false;
                    Toast.makeText(getContext(), "Please select a doctor", Toast.LENGTH_SHORT).show();
                }

                if(day == Integer.valueOf(currentDay))
                {
                    if((hour < Integer.valueOf(currentHour)) ||
                        (hour == Integer.valueOf(currentHour) && minute < Integer.valueOf(currentMinute))) //select a past time
                    {
                        valid = false;
                        Toast.makeText(getContext(), "Please select a future time", Toast.LENGTH_SHORT).show();
                    }
                   else if((hour - Integer.valueOf(currentHour) == 1 &&
                            Integer.valueOf(currentMinute) > minute ) ||
                            hour == Integer.valueOf(currentHour)) //select a time less than 1 hour from now
                   {
                       valid = false;
                       Toast.makeText(getContext(), "Please make an appointment that is at least 1 hour from now", Toast.LENGTH_SHORT).show();
                   }
                }

                if(hour == 0 && Integer.valueOf(currentHour) == 23)
                {
                    if( Integer.valueOf(currentMinute) > minute)
                    {
                        valid = false;
                        Toast.makeText(getContext(), "Please make an appointment that is at least 1 hour from now", Toast.LENGTH_SHORT).show();
                    }
                }

                if(valid)
                {
                    isFree = true;
                    doctorReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful())
                            {
                                for (DocumentSnapshot documentSnapshot : task.getResult())
                                {
                                    if (doctorName.equals(documentSnapshot.getString("Name")))
                                    {
                                        doctorEmail = documentSnapshot.getString("Email");
                                        break;
                                    }
                                }

                                appointmentReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                        if(task.isSuccessful())
                                        {
                                            for (DocumentSnapshot documentSnapshot : task.getResult())
                                            {
                                                if(documentSnapshot.getString("Doctor Email").equals(doctorEmail))
                                                {
                                                    if(documentSnapshot.getLong("Month").intValue() == month) //same month
                                                    {
                                                        if(documentSnapshot.getLong("Day").intValue() == day) //same day
                                                        {
                                                            if ((documentSnapshot.getLong("Hour").intValue() > hour &&             //1 hour difference
                                                                    documentSnapshot.getLong("Hour").intValue() - hour == 1)
                                                                    || (hour > documentSnapshot.getLong("Hour").intValue()  &&
                                                                    hour - documentSnapshot.getLong("Hour").intValue() == 1))
                                                            {

                                                                if((minute  > documentSnapshot.getLong("Minute").intValue()
                                                                        && documentSnapshot.getLong("Minute").intValue()+60 - minute <= 30)
                                                                        || (documentSnapshot.getLong("Minute").intValue() > minute
                                                                        && minute+60 - documentSnapshot.getLong("Minute").intValue() <= 30))
                                                                {
                                                                    Toast.makeText(getContext(),"The doctor is busy. Please select another time", Toast.LENGTH_SHORT).show();
                                                                    isFree = false;
                                                                    break;
                                                                }
                                                            }
                                                            if(documentSnapshot.getLong("Hour").intValue() == hour) //same hour
                                                            {
                                                                if((minute > documentSnapshot.getLong("Minute").intValue()
                                                                        && minute - documentSnapshot.getLong("Minute").intValue() <= 30)
                                                                        || (documentSnapshot.getLong("Minute").intValue() >= minute
                                                                        && documentSnapshot.getLong("Minute").intValue() - minute <= 30))
                                                                {
                                                                    Toast.makeText(getContext(),"The doctor is busy. Please select another time", Toast.LENGTH_SHORT).show();
                                                                    isFree = false;
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                        else //different day
                                                        {
                                                            if(hour == 23 && documentSnapshot.getLong("Hour").intValue() == 0 ||
                                                                    hour == 0 && documentSnapshot.getLong("Hour").intValue() == 23)
                                                            {
                                                                if((minute  > documentSnapshot.getLong("Minute").intValue()
                                                                        && documentSnapshot.getLong("Minute").intValue()+60 - minute <= 30)
                                                                        || (documentSnapshot.getLong("Minute").intValue() > minute
                                                                        && minute+60 - documentSnapshot.getLong("Minute").intValue() <= 30))
                                                                {
                                                                    Toast.makeText(getContext(),"The doctor is busy. Please select another time", Toast.LENGTH_SHORT).show();
                                                                    isFree = false;
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                            }

                                            if (isFree)
                                            {
                                                patientReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful())
                                                        {
                                                            DocumentSnapshot documentSnapshot = task.getResult();
                                                            patientName = documentSnapshot.getString("Name");

                                                            Map<String, Object> appointment = new HashMap<>();
                                                            appointment.put("Datetime", year+"."+month+"."+day+" "+hour+":"+minute);
                                                            appointment.put("Year", year);
                                                            appointment.put("Month", month);
                                                            appointment.put("Day", day);
                                                            appointment.put("Hour", hour);
                                                            appointment.put("Minute", minute);
                                                            appointment.put("Doctor Email", doctorEmail);
                                                            appointment.put("Doctor Name", doctorName);
                                                            appointment.put("Patient Email", userId);
                                                            appointment.put("Patient Name", patientName);
                                                            appointment.put("Status", "Pending");

                                                            DocumentReference addAppointmentReference = fStore.collection("appointments")
                                                                    .document(year+"."+month+"."+day+" "+hour+":"+minute+""+userId+doctorEmail);

                                                            addAppointmentReference.set(appointment).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    Toast.makeText(getContext(), "The appointment is made successfully", Toast.LENGTH_SHORT).show();

                                                                    String senderEmail="foodintoleranceapp53@gmail.com";
                                                                    String senderPassword="lkqgijyawiwshwjc";
                                                                    String messageToSend="You receive a video consultation request from a patient.";
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
                                                                        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(doctorEmail));
                                                                        message.setSubject("Video Consultation Request");
                                                                        message.setText(messageToSend);
                                                                        Transport.send(message);
                                                                    }catch (MessagingException e){
                                                                        throw new RuntimeException(e);
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });

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

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        return view;
    }
}