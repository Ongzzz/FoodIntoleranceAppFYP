package com.example.foodintoleranceappfyp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
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
import androidx.appcompat.app.AppCompatActivity;


public class RegisterPatient extends AppCompatActivity {

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_patient);

        Spinner spinner_state = findViewById(R.id.spinner_patientState);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.spinner_item,
                getResources().getStringArray(R.array.state_array));

        spinner_state.setAdapter(adapter);

        EditText et_name = findViewById(R.id.et_name);
        EditText et_signUpEmail = findViewById(R.id.et_signUpEmail);
        EditText et_signUpPassword = findViewById(R.id.et_signUpPassword);
        RadioGroup rg_gender = findViewById(R.id.radioGroup_gender);
        CheckBox gluten = findViewById(R.id.cb_gluten);
        CheckBox lactose = findViewById(R.id.cb_lactose);
        CheckBox fructose = findViewById(R.id.cb_fructose);
        Button btn_register = findViewById(R.id.btn_registerPatient);
        ImageView img_showHide = findViewById(R.id.img_showHide);

        img_showHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(et_signUpPassword.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())){
                    //Show Password
                    et_signUpPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else{
                    //Hide Password
                    et_signUpPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = et_name.getText().toString().trim();
                String email = et_signUpEmail.getText().toString().trim();
                String password = et_signUpPassword.getText().toString().trim();
                int selectedButtonId = rg_gender.getCheckedRadioButtonId();

                String state = spinner_state.getSelectedItem().toString();

                boolean valid = true;
                boolean emailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches();

                if(name.isEmpty())
                {
                    et_name.setError("Please fill in your name!");
                    valid = false;
                }

                if(email.isEmpty())
                {
                    et_signUpEmail.setError("Please fill in your email!");
                    valid = false;
                }

                if(!email.isEmpty() && !emailValid)
                {
                    et_signUpEmail.setError("This is not a valid email!");
                    valid = false;
                }

                if(password.isEmpty())
                {
                    et_signUpPassword.setError("Please fill in your password!");
                    valid = false;
                }
                if (!password.isEmpty()) {
                    if (password.length() < 8) {
                        et_signUpPassword.setError("The password should be more than or equal to 8 characters!");
                        valid = false;
                    }

                    if (TextUtils.isDigitsOnly(password) || password.matches("[a-zA-Z]+")) {
                        et_signUpPassword.setError("The password should contain digit and letter!");
                        valid = false;
                    }

                }

                if (selectedButtonId == -1)
                {
                    Toast.makeText(getApplicationContext(), "Please select a gender", Toast.LENGTH_SHORT).show();
                    valid = false;
                }

                if(!gluten.isChecked() && !lactose.isChecked() && !fructose.isChecked())
                {
                    Toast.makeText(getApplicationContext(), "Please select at least one intolerance", Toast.LENGTH_SHORT).show();
                    valid = false;
                }

                if(state.equals(getResources().getStringArray(R.array.state_array)[0]))
                {
                    Toast.makeText(getApplicationContext(), "Please select a state", Toast.LENGTH_SHORT).show();
                    valid = false;
                }

                if(valid)
                {
                    RadioButton rb_gender = findViewById(selectedButtonId);
                    String gender = rb_gender.getText().toString();
                    List<String> intolerance = new ArrayList<String>();
                    if(gluten.isChecked())
                    {
                        intolerance.add("gluten");
                    }
                    if(lactose.isChecked())
                    {
                        intolerance.add("lactose");
                    }
                    if(fructose.isChecked())
                    {
                        intolerance.add("fructose");
                    }

                    fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful())
                            {
                                String userId = email;
                                DocumentReference documentReference = fStore.collection("patients").document(userId);
                                Map<String,Object> patient = new HashMap<>();
                                patient.put("Name",name);
                                patient.put("Email",email);
                                patient.put("Gender",gender);
                                patient.put("Intolerance",intolerance);
                                patient.put("State",state);
                                documentReference.set(patient).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getApplicationContext(),"User Account is created!",Toast.LENGTH_SHORT).show();
                                    }
                                });

                                DocumentReference userReference = fStore.collection("users").document(userId);
                                Map<String,Object> user = new HashMap<>();
                                user.put("Name",name);
                                user.put("UserType","Patient");
                                userReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                });

                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(i);
                            }
                            else
                            {
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    Toast.makeText(getApplicationContext(), "The email is used by another user", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });

                }

            }
        });


    }

}