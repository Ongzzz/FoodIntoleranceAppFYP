package com.example.foodintoleranceappfyp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class RegisterFragment extends Fragment {

    RegisterPatient registerPatientFragment = new RegisterPatient();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        Spinner spinner_userType = view.findViewById(R.id.spinner_userType);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_item,
                getResources().getStringArray(R.array.userType_array));

        spinner_userType.setAdapter(adapter);

        Button btn_selectUserType = view.findViewById(R.id.btn_selectUserType);

        btn_selectUserType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userType = spinner_userType.getSelectedItem().toString();
                boolean valid = true;

                if (userType.equals(getResources().getStringArray(R.array.userType_array)[0]))
                {
                    Toast.makeText(getActivity(),"Please select a user type", Toast.LENGTH_SHORT).show();
                    valid = false;
                }

                if (valid)
                {
                    if(userType.equals(getResources().getStringArray(R.array.userType_array)[1]))
                    {
                        Intent i = new Intent(getActivity(), RegisterPatient.class);
                        startActivity(i);
                    }
                    if(userType.equals(getResources().getStringArray(R.array.userType_array)[2]))
                    {
                        Intent i = new Intent(getActivity(), RegisterDoctor.class);
                        startActivity(i);
                    }
                    if(userType.equals(getResources().getStringArray(R.array.userType_array)[3]))
                    {
                        Intent i = new Intent(getActivity(), RegisterRestaurantOwner.class);
                        startActivity(i);
                    }
                }
            }
        });



        return view;

    }



}