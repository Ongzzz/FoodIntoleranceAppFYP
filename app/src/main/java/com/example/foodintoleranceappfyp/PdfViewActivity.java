package com.example.foodintoleranceappfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PdfViewActivity extends AppCompatActivity {

    FirebaseStorage fStorage = FirebaseStorage.getInstance();
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_view);

        PDFView pdf_viewer = findViewById(R.id.pdf_viewer);

        if(getIntent().getStringExtra("Document")!=null)
        {
            String filePath = getIntent().getStringExtra("Document");
            storageReference = fStorage.getReference(filePath);
            storageReference.getBytes(1024 * 1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    pdf_viewer.setVisibility(View.VISIBLE);
                    pdf_viewer.fromBytes(bytes).load();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PdfViewActivity.this,"Failed to retrieve the document...", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            });
        }

    }
}