package com.example.foodintoleranceappfyp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Console;
import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class ConsultPatientFragment extends Fragment{

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    String userId = fAuth.getCurrentUser().getEmail();
    CollectionReference collectionReference = fStore.collection("appointments");
    ArrayList<Consultation> consultations = new ArrayList<>();
    String patientName, patientEmail, dateTime, doctorName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_consult_patient, container, false);

        final SwipeRefreshLayout refreshPatientList = view.findViewById(R.id.refreshPatientList);
        refreshPatientList.setDistanceToTriggerSync(30);

        TextView tv_no_consult_patient = view.findViewById(R.id.tv_no_consult_patient);
        ListView lv_consult_patient = view.findViewById(R.id.lv_consult_patient);

        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    for(DocumentSnapshot documentSnapshot : task.getResult())
                    {
                        if(documentSnapshot.getString("Doctor Email").equals(userId) &&
                                documentSnapshot.getString("Status").equals("Approved"))
                        {
                            doctorName = documentSnapshot.getString("Doctor Name");
                            patientName = documentSnapshot.getString("Patient Name");
                            patientEmail = documentSnapshot.getString("Patient Email");
                            dateTime = documentSnapshot.getString("Datetime");

                            consultations.add(new Consultation(dateTime, patientName, patientEmail,doctorName, userId));
                        }

                    }

                    if(!consultations.isEmpty())
                    {
                        tv_no_consult_patient.setVisibility(View.GONE);
                        lv_consult_patient.setVisibility(View.VISIBLE);
                        lv_consult_patient.setAdapter(new ConsultationListAdapter(consultations, getContext(), "Consult Patient"));
                    }
                    else
                    {
                        tv_no_consult_patient.setVisibility(View.VISIBLE);
                        tv_no_consult_patient.setText("No appointment for consultation...");
                        lv_consult_patient.setVisibility(View.GONE);
                    }

                }
            }
        });


        refreshPatientList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                tv_no_consult_patient.setText("No patient...");
                tv_no_consult_patient.setVisibility(View.VISIBLE);
                lv_consult_patient.setVisibility(View.GONE);

                consultations.clear();
                collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful())
                        {
                            for(DocumentSnapshot documentSnapshot : task.getResult())
                            {
                                if(documentSnapshot.getString("Doctor Email").equals(userId) &&
                                        documentSnapshot.getString("Status").equals("Approved"))
                                {
                                    doctorName = documentSnapshot.getString("Doctor Name");
                                    patientName = documentSnapshot.getString("Patient Name");
                                    patientEmail = documentSnapshot.getString("Patient Email");
                                    dateTime = documentSnapshot.getString("Datetime");

                                    consultations.add(new Consultation(dateTime, patientName, patientEmail,doctorName, userId));
                                }

                            }

                            if(!consultations.isEmpty())
                            {
                                tv_no_consult_patient.setVisibility(View.GONE);
                                lv_consult_patient.setVisibility(View.VISIBLE);
                                lv_consult_patient.setAdapter(new ConsultationListAdapter(consultations, getContext(), "Consult Patient"));
                            }
                            else
                            {
                                tv_no_consult_patient.setVisibility(View.VISIBLE);
                                tv_no_consult_patient.setText("No appointment for consultation...");
                                lv_consult_patient.setVisibility(View.GONE);
                            }
                        }
                    }
                });

                refreshPatientList.setRefreshing(false);
            }
        });

        return view;
    }


}

