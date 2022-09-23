package com.example.foodintoleranceappfyp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class ManageAppointmentFragment extends Fragment{

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    String userId = fAuth.getCurrentUser().getEmail();
    CollectionReference collectionReference = fStore.collection("appointments");
    String dateTime, patientEmail, patientName, doctorName, doctorEmail;
    int year, month, day, hour, minute;

    ArrayList <Appointment> appointmentList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_appointment, container, false);

        final SwipeRefreshLayout refresh = view.findViewById(R.id.refreshAppointmentList);
        refresh.setDistanceToTriggerSync(30);

        TextView tv_no_appointment = view.findViewById(R.id.tv_no_appointment);
        ListView lv_appointmentList = view.findViewById(R.id.lv_appointmentList);

        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    for(DocumentSnapshot documentSnapshot : task.getResult())
                    {
                        if(documentSnapshot.getString("Doctor Email").equals(userId) &&
                                documentSnapshot.getString("Status").equals("Pending"))
                        {
                            dateTime = documentSnapshot.getString("Datetime");
                            year = documentSnapshot.getLong("Year").intValue();
                            month = documentSnapshot.getLong("Month").intValue();
                            day = documentSnapshot.getLong("Day").intValue();
                            hour = documentSnapshot.getLong("Hour").intValue();
                            minute = documentSnapshot.getLong("Minute").intValue();
                            patientName = documentSnapshot.getString("Patient Name");
                            patientEmail = documentSnapshot.getString("Patient Email");
                            doctorName = documentSnapshot.getString("Doctor Name");
                            doctorEmail = documentSnapshot.getString("Doctor Email");

                            appointmentList.add(new Appointment(dateTime, year, month, day, hour, minute, patientName
                                    ,patientEmail, doctorName, doctorEmail));
                        }
                    }
                    if(!appointmentList.isEmpty())
                    {
                        tv_no_appointment.setVisibility(View.GONE);
                        lv_appointmentList.setVisibility(View.VISIBLE);
                        lv_appointmentList.setAdapter(new AppointmentListAdapter(appointmentList, getContext(), "Manage Appointment"));
                    }

                    else
                    {
                        tv_no_appointment.setText("No appointment...");
                        tv_no_appointment.setVisibility(View.VISIBLE);
                        lv_appointmentList.setVisibility(View.GONE);
                    }
                }
            }
        });

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                appointmentList.clear();

                collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful())
                        {
                            for(DocumentSnapshot documentSnapshot : task.getResult())
                            {
                                if(documentSnapshot.getString("Doctor Email").equals(userId) &&
                                        documentSnapshot.getString("Status").equals("Pending"))
                                {
                                    dateTime = documentSnapshot.getString("Datetime");
                                    year = documentSnapshot.getLong("Year").intValue();
                                    month = documentSnapshot.getLong("Month").intValue();
                                    day = documentSnapshot.getLong("Day").intValue();
                                    hour = documentSnapshot.getLong("Hour").intValue();
                                    minute = documentSnapshot.getLong("Minute").intValue();
                                    patientName = documentSnapshot.getString("Patient Name");
                                    patientEmail = documentSnapshot.getString("Patient Email");
                                    doctorName = documentSnapshot.getString("Doctor Name");
                                    doctorEmail = documentSnapshot.getString("Doctor Email");

                                    appointmentList.add(new Appointment(dateTime, year, month, day, hour, minute, patientName
                                            ,patientEmail, doctorName, doctorEmail));
                                }
                            }
                            if(!appointmentList.isEmpty())
                            {
                                tv_no_appointment.setVisibility(View.GONE);
                                lv_appointmentList.setVisibility(View.VISIBLE);
                                lv_appointmentList.setAdapter(new AppointmentListAdapter(appointmentList, getContext(), "Manage Appointment"));
                            }

                            else
                            {
                                tv_no_appointment.setText("No appointment...");
                                tv_no_appointment.setVisibility(View.VISIBLE);
                                lv_appointmentList.setVisibility(View.GONE);
                            }
                        }
                    }
                });

                refresh.setRefreshing(false);
            }
        });

        return view;
    }

}

