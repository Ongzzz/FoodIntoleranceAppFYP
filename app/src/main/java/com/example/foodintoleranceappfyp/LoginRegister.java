package com.example.foodintoleranceappfyp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

public class LoginRegister extends AppCompatActivity {

    public static ViewPager2 viewpager;
    ViewPagerFragmentAdapter adapter;
    private ArrayList<Fragment> fragmentList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        viewpager = findViewById(R.id.viewpager);
        fragmentList.add(new LoginFragment());
        fragmentList.add(new RegisterFragment());

        adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), getLifecycle(), fragmentList);
        viewpager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewpager.setAdapter(adapter);

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        if (fAuth.getCurrentUser() != null)
        {
            Toast.makeText(getApplicationContext(),fAuth.getCurrentUser().getEmail(),Toast.LENGTH_SHORT).show();
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
