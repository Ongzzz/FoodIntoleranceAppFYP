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

public class DoctorProfileFragment extends Fragment {

    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    String userId = fAuth.getCurrentUser().getEmail();
    DocumentReference documentReference = fStore.collection("users").document(userId);
    String name;
    String userType;
    String collectionName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctor_profile, container, false);

        TextView tv_doctorUsernameField = view.findViewById(R.id.tv_doctorUsernameField);
        ImageView imgView_changeDoctorUsername = view.findViewById(R.id.imgView_changeDoctorUsername);

        TextView tv_doctorEmailField = view.findViewById(R.id.tv_doctorEmailField);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful())
                {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null)
                    {
                        name = documentSnapshot.getString("Name");
                        userType = documentSnapshot.getString("UserType");
                        AppUser user = new AppUser(name, userType);

                        DocumentReference dr;
                        if(user.getUserType().equals("Doctor"))
                        {
                            dr = fStore.collection("doctors").document(userId);
                            dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful())
                                    {
                                        DocumentSnapshot documentSnapshot = task.getResult();
                                        if(documentSnapshot != null)
                                        {
                                            tv_doctorUsernameField.setText(documentSnapshot.getString("Name"));
                                            tv_doctorEmailField.setText(documentSnapshot.getString("Email"));
                                            collectionName = "doctors";
                                        }
                                    }

                                }
                            });
                        }

                        if(user.getUserType().equals("Pending Doctor"))
                        {
                            dr = fStore.collection("pendingDoctors").document(userId);
                            dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful())
                                    {
                                        DocumentSnapshot documentSnapshot = task.getResult();
                                        if(documentSnapshot != null)
                                        {
                                            tv_doctorUsernameField.setText(documentSnapshot.getString("Name"));
                                            tv_doctorEmailField.setText(documentSnapshot.getString("Email"));
                                            collectionName = "pendingDoctors";
                                        }
                                    }

                                }
                            });
                        }
                    }
                }
            }
        });

//        fetchData(new FireStoreCallback() {
//            @Override
//            public void onCallback(AppUser user) {
//
//                DocumentReference dr;
//                if(user.getUserType().equals("Doctor"))
//                {
//                    dr = fStore.collection("doctors").document(userId);
//                    dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                            if(task.isSuccessful())
//                            {
//                                DocumentSnapshot documentSnapshot = task.getResult();
//                                if(documentSnapshot != null)
//                                {
//                                    tv_doctorUsernameField.setText(documentSnapshot.getString("Name"));
//                                    tv_doctorEmailField.setText(documentSnapshot.getString("Email"));
//                                    collectionName = "doctors";
//                                }
//                            }
//
//                        }
//                    });
//                }
//
//                if(user.getUserType().equals("Pending Doctor"))
//                {
//                    dr = fStore.collection("pendingDoctors").document(userId);
//                    dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                            if(task.isSuccessful())
//                            {
//                                DocumentSnapshot documentSnapshot = task.getResult();
//                                if(documentSnapshot != null)
//                                {
//                                    tv_doctorUsernameField.setText(documentSnapshot.getString("Name"));
//                                    tv_doctorEmailField.setText(documentSnapshot.getString("Email"));
//                                    collectionName = "pendingDoctors";
//                                }
//                            }
//
//                        }
//                    });
//                }
//
//            }
//        });

        imgView_changeDoctorUsername.setOnClickListener(new View.OnClickListener() {
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
                                    DocumentReference documentReference = fStore.collection(collectionName).document(userId);
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("Name", et_changeUsername.getText().toString().trim());
                                    documentReference.update(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            DocumentReference userReference = fStore.collection("users").document(userId);
                                            userReference.update(user);

                                            if(collectionName.equals("doctors"))
                                            {
                                                CollectionReference appointmentReference = fStore.collection("appointments");
                                                appointmentReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if(task.isSuccessful())
                                                        {
                                                            Map<String, Object> updateAppointment = new HashMap<>();
                                                            updateAppointment.put("Doctor Name", et_changeUsername.getText().toString().trim());

                                                            for (DocumentSnapshot documentSnapshot : task.getResult())
                                                            {
                                                                if(documentSnapshot.getString("Doctor Name").equals(tv_doctorUsernameField.getText().toString()))
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
                                                                        updateConsultation.put("Doctor Name", et_changeUsername.getText().toString().trim());

                                                                        for (DocumentSnapshot ds : task.getResult())
                                                                        {
                                                                            if(ds.getString("Doctor Name").equals(tv_doctorUsernameField.getText().toString()))
                                                                            {
                                                                                DocumentReference updateConsultationReference = fStore.collection("consultations").document(ds.getId());
                                                                                updateConsultationReference.update(updateConsultation);
                                                                            }
                                                                        }

                                                                        Toast.makeText(getActivity(), "Username is updated successfully", Toast.LENGTH_SHORT).show();

                                                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                                                                new DoctorProfileFragment()).commit();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });


                                            }

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

//    private interface FireStoreCallback
//    {
//        void onCallback(AppUser user);
//    }
//
//    private void fetchData(FireStoreCallback fireStoreCallback)
//    {
//
//        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful())
//                {
//                    DocumentSnapshot documentSnapshot = task.getResult();
//                    if (documentSnapshot != null)
//                    {
//                        name = documentSnapshot.getString("Name");
//                        userType = documentSnapshot.getString("UserType");
//                        AppUser user = new AppUser(name, userType);
//
//                        fireStoreCallback.onCallback(user);
//
//                    }
//                }
//            }
//        });
//    }
}
