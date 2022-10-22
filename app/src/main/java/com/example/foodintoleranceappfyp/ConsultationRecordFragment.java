package com.example.foodintoleranceappfyp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.type.DateTime;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class ConsultationRecordFragment extends Fragment{

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    String userId = fAuth.getCurrentUser().getEmail();
    DocumentReference documentReference = fStore.collection("users").document(userId);
    CollectionReference collectionReference = fStore.collection("consultations");
    ArrayList<Consultation> consultations = new ArrayList<>();
    String patientName, patientEmail, dateTime, doctorName, doctorEmail, userType;
    ArrayList<String> intolerance = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_consultation_record, container, false);

        final SwipeRefreshLayout refresh = view.findViewById(R.id.refreshPatientList);
        refresh.setDistanceToTriggerSync(30);

        TextView tv_no_consultation_record = view.findViewById(R.id.tv_no_consultation_record);
        ListView lv_consultation_record = view.findViewById(R.id.lv_consultation_record);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    userType = documentSnapshot.getString("UserType");

                    collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful())
                            {
                                for (DocumentSnapshot ds : task.getResult())
                                {
                                    if(userType.equals("Admin"))
                                    {
                                        patientEmail = ds.getString("Patient Email");
                                        patientName = ds.getString("Patient Name");
                                        doctorEmail = ds.getString("Doctor Email");
                                        doctorName = ds.getString("Doctor Name");
                                        dateTime = ds.getString("DateTime");
                                        consultations.add(new Consultation(dateTime,patientName,patientEmail,
                                                null,null, null, doctorName, doctorEmail));
                                    }
                                    if(userType.equals("Doctor"))
                                    {
                                        if(ds.getString("Doctor Email").equals(userId))
                                        {
                                            patientEmail = ds.getString("Patient Email");
                                            patientName = ds.getString("Patient Name");
                                            doctorEmail = ds.getString("Doctor Email");
                                            doctorName = ds.getString("Doctor Name");
                                            dateTime = ds.getString("DateTime");
                                            consultations.add(new Consultation(dateTime,patientName,patientEmail,
                                                    null,null, null, doctorName, doctorEmail));
                                        }
                                    }
                                    if(userType.equals("Patient"))
                                    {
                                        if(ds.getString("Patient Email").equals(userId))
                                        {
                                            patientEmail = ds.getString("Patient Email");
                                            patientName = ds.getString("Patient Name");
                                            doctorEmail = ds.getString("Doctor Email");
                                            doctorName = ds.getString("Doctor Name");
                                            dateTime = ds.getString("DateTime");
                                            consultations.add(new Consultation(dateTime,patientName,patientEmail,
                                                    null,null, null, doctorName, doctorEmail));
                                        }
                                    }
                                }

                                if(!consultations.isEmpty())
                                {
                                    tv_no_consultation_record.setVisibility(View.GONE);
                                    lv_consultation_record.setVisibility(View.VISIBLE);
                                    lv_consultation_record.setAdapter(new ConsultationListAdapter(consultations, getContext(), "Consultation Record"));
                                }

                                else
                                {
                                    tv_no_consultation_record.setText("No record...");
                                    tv_no_consultation_record.setVisibility(View.VISIBLE);
                                    lv_consultation_record.setVisibility(View.GONE);
                                }
                            }
                        }
                    });
                }
            }
        });

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                consultations.clear();
                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            userType = documentSnapshot.getString("UserType");

                            collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful())
                                    {
                                        for (DocumentSnapshot ds : task.getResult())
                                        {
                                            if(userType.equals("Admin"))
                                            {
                                                patientEmail = ds.getString("Patient Email");
                                                patientName = ds.getString("Patient Name");
                                                doctorEmail = ds.getString("Doctor Email");
                                                doctorName = ds.getString("Doctor Name");
                                                dateTime = ds.getString("DateTime");
                                                consultations.add(new Consultation(dateTime,patientName,patientEmail,
                                                        null,null, null, doctorName, doctorEmail));
                                            }
                                            if(userType.equals("Doctor"))
                                            {
                                                if(ds.getString("Doctor Email").equals(userId))
                                                {
                                                    patientEmail = ds.getString("Patient Email");
                                                    patientName = ds.getString("Patient Name");
                                                    doctorEmail = ds.getString("Doctor Email");
                                                    doctorName = ds.getString("Doctor Name");
                                                    dateTime = ds.getString("DateTime");
                                                    consultations.add(new Consultation(dateTime,patientName,patientEmail,
                                                            null,null, null, doctorName, doctorEmail));
                                                }
                                            }
                                            if(userType.equals("Patient"))
                                            {
                                                if(ds.getString("Patient Email").equals(userId))
                                                {
                                                    patientEmail = ds.getString("Patient Email");
                                                    patientName = ds.getString("Patient Name");
                                                    doctorEmail = ds.getString("Doctor Email");
                                                    doctorName = ds.getString("Doctor Name");
                                                    dateTime = ds.getString("DateTime");
                                                    consultations.add(new Consultation(dateTime,patientName,patientEmail,
                                                            null,null, null, doctorName, doctorEmail));
                                                }
                                            }
                                        }

                                        if(!consultations.isEmpty())
                                        {
                                            tv_no_consultation_record.setVisibility(View.GONE);
                                            lv_consultation_record.setVisibility(View.VISIBLE);
                                            lv_consultation_record.setAdapter(new ConsultationListAdapter(consultations, getContext(), "Consultation Record"));
                                        }

                                        else
                                        {
                                            tv_no_consultation_record.setText("No record...");
                                            tv_no_consultation_record.setVisibility(View.VISIBLE);
                                            lv_consultation_record.setVisibility(View.GONE);
                                        }
                                    }
                                }
                            });
                        }
                    }
                });
                refresh.setRefreshing(false);
            }
        });

        return view;
    }

}

