package com.example.foodintoleranceappfyp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

public class RegisterDoctor extends AppCompatActivity {

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseStorage fStorage = FirebaseStorage.getInstance();
    StorageReference storageReference;

    //Uri path;
    TextView tv_pdf_file;
    ArrayList<String> fileNameList = new ArrayList<>();
    ArrayList<String> storagePath = new ArrayList<>();
    ArrayList<Uri>pathList = new ArrayList<>();
    ArrayList<String> temp = new ArrayList<>();
    int j;

    String userId;
    boolean uploadSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_doctor);

        Spinner spinner_hospital = findViewById(R.id.spinner_hospital);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.spinner_item,
                getResources().getStringArray(R.array.hospital_array));

        spinner_hospital.setAdapter(adapter);

        EditText et_doctorName = findViewById(R.id.et_doctorName);
        EditText et_doctorEmail = findViewById(R.id.et_doctorEmail);
        EditText et_doctorPassword = findViewById(R.id.et_doctorPassword);
        tv_pdf_file = findViewById(R.id.tv_pdf_file);
        Button btn_registerDoctor = findViewById(R.id.btn_registerDoctor);
        ImageView img_showHide = findViewById(R.id.img_showHide);
        Button btn_upload_doctor_document = findViewById(R.id.btn_upload_doctor_document);
        Button btn_clear_document = findViewById(R.id.btn_clear_document);

        img_showHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(et_doctorPassword.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())){
                    //Show Password
                    et_doctorPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else{
                    //Hide Password
                    et_doctorPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        btn_registerDoctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = et_doctorName.getText().toString().trim();
                String email = et_doctorEmail.getText().toString().trim();
                String password = et_doctorPassword.getText().toString().trim();
                String hospital = spinner_hospital.getSelectedItem().toString();

                boolean valid = true;
                boolean emailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches();

                if(name.isEmpty())
                {
                    et_doctorName.setError("Please fill in your name!");
                    valid = false;
                }

                if(email.isEmpty())
                {
                    et_doctorEmail.setError("Please fill in your email!");
                    valid = false;
                }

                if(!email.isEmpty() && !emailValid)
                {
                    et_doctorEmail.setError("This is not a valid email!");
                    valid = false;
                }

                if(password.isEmpty())
                {
                    et_doctorPassword.setError("Please fill in your password!");
                    valid = false;
                }

                if (!password.isEmpty()) {
                    if (password.length() < 8) {
                        et_doctorPassword.setError("The password should be more than or equal to 8 characters!");
                        valid = false;
                    }

                    if (TextUtils.isDigitsOnly(password) || password.matches("[a-zA-Z]+")) {
                        et_doctorPassword.setError("The password should contain digit and letter!");
                        valid = false;
                    }

                }

                if(hospital.equals(getResources().getStringArray(R.array.hospital_array)[0]))
                {
                    Toast.makeText(getApplicationContext(), "Please select a hospital", Toast.LENGTH_SHORT).show();
                    valid = false;
                }

                if(pathList.isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Please attach supporting document to prove you are a doctor", Toast.LENGTH_SHORT).show();
                    valid = false;
                }

                if(valid)
                {

                    userId = email;
                    userId = userId.replaceAll("\\s","");

                    ProgressDialog progressDialog = new ProgressDialog(RegisterDoctor.this);
                    progressDialog.setMessage("Creating your account...");
                    progressDialog.show();

                    fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful())
                            {
                                for (String s : temp)
                                {
                                    storagePath.add(s.replace("doctorID", userId));
                                }

                                Map<String,Object> user = new HashMap<>();
                                user.put("Name",name);
                                user.put("Email",userId);
                                user.put("Hospital",hospital);
                                user.put("Document Path", storagePath);

                                DocumentReference documentReference = fStore.collection("pendingDoctors").document(userId);
                                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        uploadSuccess = true;
                                        for(j=0; j<pathList.size(); j++)
                                        {
                                            storageReference = fStorage.getReference("documents/doctors/"+userId+"/"+fileNameList.get(j));

                                            if(uploadSuccess)
                                            {
                                                storageReference.putFile(pathList.get(j)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(RegisterDoctor.this,"Failed to upload "+fileNameList.get(j), Toast.LENGTH_SHORT).show();
                                                        uploadSuccess = false;
                                                    }
                                                });
                                            }
                                            else
                                            {
                                                storageReference = fStorage.getReference("documents/doctors/"+userId);
                                                storageReference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                                    @Override
                                                    public void onSuccess(ListResult listResult) {
                                                        for (StorageReference singleFileReference : listResult.getItems())
                                                        {
                                                            singleFileReference.delete();
                                                        }
                                                    }
                                                });
                                                break;
                                            }
                                        }

                                        String senderEmail="foodintoleranceapp53@gmail.com";
                                        String senderPassword="lkqgijyawiwshwjc";
                                        String messageToSend="Your account is pending to be approved. An email will be sent to your email address when your account is approved!";
                                        Properties props = new Properties();
                                        props.put("mail.smtp.auth","true");
                                        props.put("mail.smtp.starttls.enable","true");
                                        props.put("mail.smtp.host","smtp.gmail.com");
                                        props.put("mail.smtp.port","587");
                                        Session session = Session.getInstance(props, new javax.mail.Authenticator(){
                                            @Override
                                            protected PasswordAuthentication getPasswordAuthentication() {
                                                return new PasswordAuthentication(senderEmail,senderPassword);
                                            }
                                        });

                                        try {
                                            Message message = new MimeMessage(session);
                                            message.setFrom(new InternetAddress(senderEmail));
                                            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                                            message.setSubject("Food Intolerance App Doctor Account Confirmation Email");
                                            message.setText(messageToSend);
                                            Transport.send(message);
                                            Toast.makeText(getApplicationContext(),"Please check your email address for the confirmation email!",Toast.LENGTH_LONG).show();
                                        }catch (MessagingException e){
                                            throw new RuntimeException(e);
                                        }
                                    }
                                });

                                DocumentReference userReference = fStore.collection("users").document(userId);
                                Map<String,Object> user2 = new HashMap<>();
                                user2.put("Name",name);
                                user2.put("UserType","Pending Doctor");
                                userReference.set(user2).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                });

                                if(progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();
                                }

                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(i);
                            }
                            else
                            {

                                if(progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();
                                }

                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    Toast.makeText(getApplicationContext(), "The email is used by another user", Toast.LENGTH_SHORT).show();
                                }
                            }


                        }

                    });



                }

            }
        });

        btn_clear_document.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pathList.isEmpty())
                {
                    Toast.makeText(RegisterDoctor.this, "No file is attached", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    tv_pdf_file.setText(getResources().getString(R.string.pdf));
                    pathList.clear();
                    fileNameList.clear();
                    temp.clear();
                }
            }
        });

        btn_upload_doctor_document.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setType("application/pdf");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                activityResultLauncher.launch(intent);
            }
        });

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        pathList.add(data.getData());
                        DocumentFile file = DocumentFile.fromSingleUri(getApplicationContext(), pathList.get(pathList.size()-1));

                        if(!fileNameList.isEmpty())
                        {
                            tv_pdf_file.setText(tv_pdf_file.getText().toString()
                                    + System.getProperty("line.separator")
                                    + file.getName());
                        }
                        else
                        {
                            tv_pdf_file.setText(file.getName());
                        }

                        fileNameList.add(file.getName());
                        temp.add("documents/doctors/doctorID/"+file.getName());

                    }
                }
            });
}