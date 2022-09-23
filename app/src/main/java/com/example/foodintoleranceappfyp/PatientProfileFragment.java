package com.example.foodintoleranceappfyp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class PatientProfileFragment extends Fragment {

    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    String userId = fAuth.getCurrentUser().getEmail();
    String collectionName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_profile, container, false);

        TextView tv_patientUsernameField = view.findViewById(R.id.tv_patientUsernameField);
        TextView tv_patientEmailField = view.findViewById(R.id.tv_patientEmailField);
        TextView tv_intoleranceField = view.findViewById(R.id.tv_intoleranceField);

        ImageView imgView_changePatientUsername = view.findViewById(R.id.imgView_changePatientUsername);
        ImageView imgView_changeIntolerance = view.findViewById(R.id.imgView_changeIntolerance);

        DocumentReference dr = fStore.collection("patients").document(userId);
        dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot != null)
                    {
                        tv_patientUsernameField.setText(documentSnapshot.getString("Name"));
                        tv_patientEmailField.setText(documentSnapshot.getString("Email"));
                        List<String> intoleranceList = (List<String>) documentSnapshot.get("Intolerance");
                        String intolerance = TextUtils.join(", ", intoleranceList);
                        tv_intoleranceField.setText(intolerance);
                    }
                }

            }
        });


        imgView_changePatientUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText et_changeUsername = new EditText(v.getContext());
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setTitle("Change username")
                        .setMessage("Do you want to change your username?")
                        .setView(et_changeUsername)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(et_changeUsername.getText().toString().trim().isEmpty())
                                {
                                    Toast.makeText(getActivity(),"Please fill in your new username!", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    DocumentReference documentReference = fStore.collection("patients").document(userId);
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("Name", et_changeUsername.getText().toString().trim());
                                    documentReference.update(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            DocumentReference userReference = fStore.collection("users").document(userId);
                                            userReference.update(user);

                                            CollectionReference appointmentReference = fStore.collection("appointments");
                                            appointmentReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        Map<String, Object> updateAppointment = new HashMap<>();
                                                        updateAppointment.put("Patient Name", et_changeUsername.getText().toString().trim());

                                                        for (DocumentSnapshot documentSnapshot : task.getResult())
                                                        {
                                                            if(documentSnapshot.getString("Patient Name").equals(tv_patientUsernameField.getText().toString()))
                                                            {
                                                                DocumentReference updateAppointmentReference = fStore.collection("appointments").document(documentSnapshot.getId());
                                                                updateAppointmentReference.update(updateAppointment);
                                                            }
                                                        }

                                                        CollectionReference consultationReference = fStore.collection("consultations");
                                                        consultationReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                if(task.isSuccessful())
                                                                {
                                                                    Map<String, Object> updateConsultation = new HashMap<>();
                                                                    updateConsultation.put("Patient Name", et_changeUsername.getText().toString().trim());

                                                                    for (DocumentSnapshot ds : task.getResult())
                                                                    {
                                                                        if(ds.getString("Doctor Name").equals(tv_patientUsernameField.getText().toString()))
                                                                        {
                                                                            DocumentReference updateConsultationReference = fStore.collection("consultations").document(ds.getId());
                                                                            updateConsultationReference.update(updateConsultation);
                                                                        }
                                                                    }

                                                                }
                                                            }
                                                        });

                                                        Toast.makeText(getActivity(), "Username is updated successfully", Toast.LENGTH_SHORT).show();

                                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                                                new PatientProfileFragment()).commit();
                                                    }
                                                }
                                            });

                                        }
                                    });



                                }
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            }
        });

        imgView_changeIntolerance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View checkBoxView = v.inflate(getActivity(), R.layout.change_intolerance, null);
                CheckBox change_gluten = checkBoxView.findViewById(R.id.cb_changeIntolerance_gluten);
                CheckBox change_fructose = checkBoxView.findViewById(R.id.cb_changeIntolerance_fructose);
                CheckBox change_lactose = checkBoxView.findViewById(R.id.cb_changeIntolerance_lactose);

                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setTitle("Change intolerance")
                        .setMessage("Do you want to change your intolerance?")
                        .setView(checkBoxView)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(!change_gluten.isChecked() && !change_fructose.isChecked() && !change_lactose.isChecked())
                                {
                                    Toast.makeText(getActivity(),"Please select at least one intolerance!", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    List<String> intoleranceList = new ArrayList<>();
                                    if(change_gluten.isChecked())
                                    {
                                        intoleranceList.add("gluten");
                                    }
                                    if(change_lactose.isChecked())
                                    {
                                        intoleranceList.add("lactose");
                                    }
                                    if(change_fructose.isChecked())
                                    {
                                        intoleranceList.add("fructose");
                                    }

                                    DocumentReference documentReference = fStore.collection("patients").document(userId);
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("Intolerance", intoleranceList);
                                    documentReference.update(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            Toast.makeText(getActivity(), "Intolerance is updated successfully", Toast.LENGTH_SHORT).show();

                                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                                    new PatientProfileFragment()).commit();
                                        }
                                    });
                                }
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            }
        });

        return view;
    }

}
