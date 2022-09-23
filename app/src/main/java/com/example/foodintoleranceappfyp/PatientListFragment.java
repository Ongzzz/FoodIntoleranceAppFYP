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
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PatientListFragment extends Fragment {

    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    String userId = fAuth.getCurrentUser().getEmail();
    CollectionReference collectionReference = fStore.collection("patients");

    ArrayList<Patient> patients = new ArrayList<>();
    String name;
    String email;
    String state;
    String gender;
    ArrayList<String> intolerance = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_list, container, false);

        ListView lv_patient_list = view.findViewById(R.id.lv_patient_list);
        TextView tv_no_patient = view.findViewById(R.id.tv_no_patient);

        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if(task.isSuccessful())
                {
                    for (DocumentSnapshot documentSnapshot : task.getResult())
                    {
                        name = documentSnapshot.getString("Name");
                        email = documentSnapshot.getString("Email");
                        state = documentSnapshot.getString("State");
                        gender = documentSnapshot.getString("Gender");
                        intolerance = (ArrayList<String>) documentSnapshot.get("Intolerance");
                        patients.add(new Patient(name, email, state, gender, intolerance));
                    }

                    if(!patients.isEmpty())
                    {
                        tv_no_patient.setVisibility(View.GONE);
                        lv_patient_list.setVisibility(View.VISIBLE);
                        lv_patient_list.setAdapter(new PatientListAdapter(patients, getContext()));
                    }

                    else
                    {
                        tv_no_patient.setText("No patient...");
                        tv_no_patient.setVisibility(View.VISIBLE);
                        lv_patient_list.setVisibility(View.GONE);
                    }

                }
            }
        });


        return view;
    }

}
