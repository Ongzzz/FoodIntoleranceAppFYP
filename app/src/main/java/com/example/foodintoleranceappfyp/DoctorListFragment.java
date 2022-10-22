package com.example.foodintoleranceappfyp;

import android.net.Uri;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DoctorListFragment extends Fragment {

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    String userId = fAuth.getCurrentUser().getEmail();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    CollectionReference collectionReference = fStore.collection("doctors");
    DocumentReference userReference = fStore.collection("users").document(userId);

    ArrayList<Doctor> doctors = new ArrayList<>();
    String name;
    String email;
    String hospital;
    ArrayList<String> documentNameList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctor_list, container, false);

        ListView lv_doctor_list = view.findViewById(R.id.lv_doctor_list);
        TextView tv_no_doctor = view.findViewById(R.id.tv_no_doctor);

        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if(task.isSuccessful())
                {
                    for (DocumentSnapshot documentSnapshot : task.getResult())
                    {
                        name = documentSnapshot.getString("Name");
                        email = documentSnapshot.getString("Email");
                        hospital = documentSnapshot.getString("Hospital");
                        documentNameList = (ArrayList<String>)documentSnapshot.get("Document Path");
                        doctors.add(new Doctor(name, email, hospital, documentNameList));
                    }

                    if(!doctors.isEmpty())
                    {
                        tv_no_doctor.setVisibility(View.GONE);
                        lv_doctor_list.setVisibility(View.VISIBLE);
                        userReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful())
                                {
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    if(documentSnapshot.getString("UserType").equals("Admin"))
                                    {
                                        lv_doctor_list.setAdapter(new DoctorListAdapter(doctors, getContext(), "Admin Doctor List"));
                                    }
                                    else
                                    {
                                        lv_doctor_list.setAdapter(new DoctorListAdapter(doctors, getContext(), "Doctor List"));
                                    }
                                }
                            }
                        });
                    }

                    else
                    {
                        tv_no_doctor.setText("No doctor...");
                        tv_no_doctor.setVisibility(View.VISIBLE);
                        lv_doctor_list.setVisibility(View.GONE);
                    }
                }
            }
        });

        return view;
    }



}
