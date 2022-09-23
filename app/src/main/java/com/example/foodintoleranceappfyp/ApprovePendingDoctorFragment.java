package com.example.foodintoleranceappfyp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class ApprovePendingDoctorFragment extends Fragment {

    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    CollectionReference pendingDoctorReference = fStore.collection("pendingDoctors");
    ArrayList<Doctor> pendingDoctorList = new ArrayList<>();
    String name;
    String email;
    String hospital;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_approve_pending_doctor, container, false);

        final SwipeRefreshLayout refresh = view.findViewById(R.id.refresh);
        refresh.setDistanceToTriggerSync(30);

        TextView tv_no_pending_doctor = view.findViewById(R.id.tv_no_pending_doctor);
        ListView lv_approve_pending_doctor = view.findViewById(R.id.lv_approve_pending_doctor);

        pendingDoctorReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful())
                {
                    pendingDoctorList.clear();
                    for (DocumentSnapshot documentSnapshot : task.getResult())
                    {
                        name = documentSnapshot.getString("Name");
                        email = documentSnapshot.getString("Email");
                        hospital = documentSnapshot.getString("Hospital");

                        Doctor doctor = new Doctor(name, email, hospital);
                        pendingDoctorList.add(doctor);
                    }

                    if(!pendingDoctorList.isEmpty())
                    {
                        tv_no_pending_doctor.setVisibility(View.GONE);
                        lv_approve_pending_doctor.setVisibility(View.VISIBLE);
                        lv_approve_pending_doctor.setAdapter(new DoctorListAdapter(pendingDoctorList, getContext(), "Approve Doctor"));
                    }

                    else
                    {
                        tv_no_pending_doctor.setText("No doctor is pending to be approved..");
                        tv_no_pending_doctor.setVisibility(View.VISIBLE);
                        lv_approve_pending_doctor.setVisibility(View.GONE);
                    }

                }
            }
        });

//        fetchData(new FireStoreCallback() {
//            @Override
//            public void onCallback(ArrayList<Doctor> doctorList) {
//
//                if(!doctorList.isEmpty())
//                {
//                    tv_no_pending_doctor.setVisibility(View.GONE);
//                    lv_approve_pending_doctor.setVisibility(View.VISIBLE);
//                    lv_approve_pending_doctor.setAdapter(new DoctorListAdapter(doctorList, getContext(), "Approve Doctor"));
//                }
//
//                else
//                {
//                    tv_no_pending_doctor.setText("No doctor is pending to be approved..");
//                    tv_no_pending_doctor.setVisibility(View.VISIBLE);
//                    lv_approve_pending_doctor.setVisibility(View.GONE);
//                }
//
//            }
//        });

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

//                pendingDoctorList.clear();
//                fetchData(new FireStoreCallback() {
//                    @Override
//                    public void onCallback(ArrayList<Doctor> doctorList) {
//
//                        if(!doctorList.isEmpty())
//                        {
//                            tv_no_pending_doctor.setVisibility(View.GONE);
//                            lv_approve_pending_doctor.setVisibility(View.VISIBLE);
//                            lv_approve_pending_doctor.setAdapter(new DoctorListAdapter(doctorList, getContext(), "Approve Doctor"));
//                        }
//
//                        else
//                        {
//                            tv_no_pending_doctor.setText("No doctor is pending to be approved..");
//                            tv_no_pending_doctor.setVisibility(View.VISIBLE);
//                            lv_approve_pending_doctor.setVisibility(View.GONE);
//                        }
//
//                    }
//                });

                pendingDoctorReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                        {
                            pendingDoctorList.clear();
                            for (DocumentSnapshot documentSnapshot : task.getResult())
                            {
                                name = documentSnapshot.getString("Name");
                                email = documentSnapshot.getString("Email");
                                hospital = documentSnapshot.getString("Hospital");

                                Doctor doctor = new Doctor(name, email, hospital);
                                pendingDoctorList.add(doctor);
                            }

                            if(!pendingDoctorList.isEmpty())
                            {
                                tv_no_pending_doctor.setVisibility(View.GONE);
                                lv_approve_pending_doctor.setVisibility(View.VISIBLE);
                                lv_approve_pending_doctor.setAdapter(new DoctorListAdapter(pendingDoctorList, getContext(), "Approve Doctor"));
                            }

                            else
                            {
                                tv_no_pending_doctor.setText("No doctor is pending to be approved..");
                                tv_no_pending_doctor.setVisibility(View.VISIBLE);
                                lv_approve_pending_doctor.setVisibility(View.GONE);
                            }

                        }
                    }
                });

                refresh.setRefreshing(false);
            }
        });

        return view;
    }

//    private interface FireStoreCallback
//    {
//        void onCallback(ArrayList<Doctor> doctorList);
//    }
//
//    private void fetchData(ApprovePendingDoctorFragment.FireStoreCallback fireStoreCallback)
//    {
//        pendingDoctorReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful())
//                {
//                    pendingDoctorList.clear();
//                    for (DocumentSnapshot documentSnapshot : task.getResult())
//                    {
//                        name = documentSnapshot.getString("Name");
//                        email = documentSnapshot.getString("Email");
//                        hospital = documentSnapshot.getString("Hospital");
//
//                        Doctor doctor = new Doctor(name, email, hospital);
//                        pendingDoctorList.add(doctor);
//
//                    }
//                    fireStoreCallback.onCallback(pendingDoctorList);
//                }
//            }
//        });
//    }
}
