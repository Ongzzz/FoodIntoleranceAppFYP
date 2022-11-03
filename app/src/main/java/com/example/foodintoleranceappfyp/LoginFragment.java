package com.example.foodintoleranceappfyp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class LoginFragment extends Fragment {

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    int count = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        TextView tv_forgotPassword = view.findViewById(R.id.tv_forgotPassword);
        TextView tv_signUp = view.findViewById(R.id.tv_signUp);
        EditText et_emailAddress = view.findViewById(R.id.et_emailAddress);
        EditText et_password = view.findViewById(R.id.et_password);
        Button btn_login = view.findViewById(R.id.btn_Login);
        TextView tv_swipeLeftToRegister = view.findViewById(R.id.tv_swipeLeftToRegister);
        ImageView img_showHide =  view.findViewById(R.id.img_showHide);

        tv_swipeLeftToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                if(count>=7)
                {
                    btn_login.setText("Register Admin");
                }
            }
        });
        
        img_showHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(et_password.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())){
                    //Show Password
                    et_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else{
                    //Hide Password
                    et_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        tv_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginRegister.viewpager.setCurrentItem(1);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = et_emailAddress.getText().toString().trim();
                String password = et_password.getText().toString().trim();
                boolean valid = true;
                boolean emailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches();

                if(email.isEmpty())
                {
                    et_emailAddress.setError("Please fill in your email!");
                    valid = false;
                }

                if(!email.isEmpty() && !emailValid)
                {
                    et_emailAddress.setError("This is not a valid email!");
                    valid = false;
                }

                if(password.isEmpty())
                {
                    et_password.setError("Please fill in your password!");
                    valid = false;
                }

                if(count>=7)
                {
                    if (!password.isEmpty()) {
                        if (password.length() < 8) {
                            et_password.setError("The password should be more than or equal to 8 characters!");
                            valid = false;
                        }

                        if (TextUtils.isDigitsOnly(password) || password.matches("[a-zA-Z]+")) {
                            et_password.setError("The password should contain digit and letter!");
                            valid = false;
                        }

                    }
                }

                if(valid)
                {
                    if(count>=7)
                    {
                        String userId = email;
                        fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task2) {

                                if(task2.isSuccessful())
                                {
                                    DocumentReference documentReference = fStore.collection("admins").document(userId);
                                    Map<String,Object> user = new HashMap<>();
                                    user.put("Email",email);
                                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getActivity(),"Admin Account is created!",Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    user.put("UserType","Admin");
                                    DocumentReference documentReference2 = fStore.collection("users").document(userId);
                                    documentReference2.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                        }
                                    });

                                    Intent i = new Intent(getActivity(), MainActivity.class);
                                    startActivity(i);
                                }
                                else
                                {
                                    if (task2.getException() instanceof FirebaseAuthUserCollisionException) {
                                        Toast.makeText(getActivity(), "The email is used by another user", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                    }
                    else
                    {
                        fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getActivity(), "Welcome!", Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(getActivity(), MainActivity.class);
                                    startActivity(i);
                                }
                                else
                                {
                                    if (task.getException() instanceof FirebaseAuthInvalidUserException)
                                    {
                                        Toast.makeText(getActivity(), "User account does not exist", Toast.LENGTH_SHORT).show();

                                    }

                                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                        Toast.makeText(getActivity(), "Incorrect password", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                    }



                }
            }
        });

        tv_forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), ForgotPassword.class);
                startActivity(i);
            }
        });


        return view;

    }
}