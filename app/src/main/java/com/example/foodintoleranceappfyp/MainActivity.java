package com.example.foodintoleranceappfyp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, CheckoutFragment.OnCallbackReceived {

    private DrawerLayout drawer;
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    String userId = fAuth.getCurrentUser().getEmail();
    DocumentReference documentReference = fStore.collection("users").document(userId);
    CollectionReference collectionReference = fStore.collection("appointments");
    Restaurant restaurant;
    NavigationView navigationView;

    String name;
    String userType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        View view = navigationView.getHeaderView(0);
        TextView tv_nameFirstChar = view.findViewById(R.id.tv_nameFirstChar);
        TextView tv_username = view.findViewById(R.id.tv_username);

        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.CHINA);
                    int currentYear = Integer.valueOf(yearFormat.format(new Date()));

                    SimpleDateFormat monthFormat = new SimpleDateFormat("MM", Locale.CHINA);
                    int currentMonth = Integer.valueOf(monthFormat.format(new Date()));

                    SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.CHINA);
                    int currentDay = Integer.valueOf(dayFormat.format(new Date()));

                    SimpleDateFormat hourFormat = new SimpleDateFormat("HH", Locale.CHINA);
                    int currentHour = Integer.valueOf(hourFormat.format(new Date()));

                    SimpleDateFormat minuteFormat = new SimpleDateFormat("mm", Locale.CHINA);
                    int currentMinute = Integer.valueOf(minuteFormat.format(new Date()));

                    for(DocumentSnapshot documentSnapshot : task.getResult())
                    {
                        boolean sameDay = false;
                        boolean past = false;
                        if(documentSnapshot.getLong("Year").intValue() == currentYear ||
                                documentSnapshot.getLong("Month").intValue() == currentMonth ||
                                documentSnapshot.getLong("Day").intValue() == currentDay)
                        {
                            sameDay = true;
                        }
                        else
                        {
                            sameDay = false;
                        }

                        if(!sameDay)
                        {
                            if(documentSnapshot.getLong("Year").intValue() < currentYear ||
                                    documentSnapshot.getLong("Month").intValue() < currentMonth ||
                                    documentSnapshot.getLong("Day").intValue() < currentDay)
                            {
                                past = true;
                            }
                            else
                            {
                                past = false;
                            }
                        }


                        if(!documentSnapshot.getString("Status").equals("Consulted"))
                        {
                            if((past) || (sameDay && (documentSnapshot.getLong("Hour").intValue() == currentHour
                                    && documentSnapshot.getLong("Minute").intValue() > currentMinute+15)))
                            {
                                Map<String, Object> expired = new HashMap<>();
                                expired.put("Status","Expired");
                                DocumentReference appointmentReference = fStore.collection("appointments")
                                        .document(documentSnapshot.getId());

                                appointmentReference.update(expired);
                            }
                        }
                    }
                }
            }
        });

        Intent i = getIntent();
        if(i.getStringExtra("RecipeListFragment")!=null)
        {
            if(i.getStringExtra("RecipeListFragment").equals("AdminRecipeListFragment"))
            {
                navigationView.getMenu().clear();
                navigationView.inflateMenu(R.menu.admin_menu);
            }
            i.removeExtra("RecipeListFragment");
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new RecipeListFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_viewRecipe);
        }
        else
        {
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

                            if(user.getUserType().equals("Admin"))
                            {
                                navigationView.getMenu().clear();
                                navigationView.inflateMenu(R.menu.admin_menu);
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                        new AdminProfileFragment()).commit();
                                navigationView.setCheckedItem(R.id.nav_admin_profile);
                            }

                            if(user.getUserType().equals("Doctor") || user.getUserType().equals("Pending Doctor"))
                            {
                                navigationView.getMenu().clear();
                                navigationView.inflateMenu(R.menu.doctor_menu);
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                        new DoctorProfileFragment()).commit();
                                navigationView.setCheckedItem(R.id.nav_doctor_profile);
                            }

                            if(user.getUserType().equals("Restaurant Owner"))
                            {
                                navigationView.getMenu().clear();
                                navigationView.inflateMenu(R.menu.restaurant_owner_menu);
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                        new RestaurantOwnerProfileFragment()).commit();
                                navigationView.setCheckedItem(R.id.nav_restaurant_owner_profile);
                            }

                            if(user.getUserType().equals("Patient"))
                            {
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                        new PatientProfileFragment()).commit();
                                navigationView.setCheckedItem(R.id.nav_patient_profile);
                            }
                        }
                    }
                }
            });
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

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
                                if(user.getUserType().equals("Admin"))
                                {
                                    tv_nameFirstChar.setText(userId.substring(0,1));
                                    tv_username.setText(userId);
                                }
                                else
                                {
                                    tv_nameFirstChar.setText(user.getName().substring(0,1));
                                    tv_username.setText(user.getName());
                                }
                            }
                        }
                    }
                });

            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.nav_admin_profile:

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new AdminProfileFragment()).commit();
                break;

            case R.id.nav_doctor_profile:

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new DoctorProfileFragment()).commit();
                break;

            case R.id.nav_patient_profile:

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new PatientProfileFragment()).commit();
                break;

            case R.id.nav_restaurant_owner_profile:

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new RestaurantOwnerProfileFragment()).commit();
                break;

            case R.id.nav_pendingOrder:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new PendingOrderFragment()).commit();
                break;

            case R.id.nav_myRestaurant:

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new MyRestaurantFragment()).commit();
                break;

            case R.id.nav_doctorList:

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new DoctorListFragment()).commit();
                break;

            case R.id.nav_all_user:

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new AllUserFragment()).commit();
                break;

            case R.id.nav_all_menu:

            case R.id.nav_pickup:

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new MenuTab()).commit();
                break;

            case R.id.nav_approveDoctor:

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ApprovePendingDoctorFragment()).commit();
                break;

            case R.id.nav_approveFood:

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ApprovePendingFoodFragment()).commit();
                break;

            case R.id.nav_addRecipe:

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new AddRecipeFragment()).commit();
                break;

            case R.id.nav_viewRecipe:

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new RecipeListFragment()).commit();
                break;

            case R.id.nav_appointment:

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new MakeAppointmentFragment()).commit();
                break;

            case R.id.nav_orderHistory:

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new PatientOrderHistoryFragment()).commit();
                break;

            case R.id.nav_consultPatient:

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ConsultPatientFragment()).commit();
                break;

            case R.id.nav_manageAppointment:

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ManageAppointmentFragment()).commit();
                break;

            case R.id.nav_consultationRecord:

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ConsultationRecordFragment()).commit();
                break;

            case R.id.nav_AIDoctor:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new AIDoctorFragment()).commit();
                break;

            case R.id.nav_logout:

                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent (getApplicationContext(), LoginRegister.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed()
    {
        if(drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if(fragment instanceof AddRestaurantFragment || fragment instanceof AddFoodFragment)
            {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new MyRestaurantFragment()).commit();
            }
            else if(fragment instanceof MenuFragment) //restaurant owner return from menu to restaurant list
            {
                Bundle bundle = new Bundle();
                bundle.putInt("tab",1);
                MyRestaurantFragment myRestaurantFragment = new MyRestaurantFragment();
                myRestaurantFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        myRestaurantFragment).commit();
            }
            else if(fragment instanceof FoodFragment) //patient return from menu to restaurant list
            {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new MenuTab()).commit();
            }
            else if(fragment instanceof CheckoutFragment)
            {
                Bundle bundle = new Bundle();
                bundle.putSerializable("Restaurant", restaurant);
                FoodFragment foodFragment = new FoodFragment();
                foodFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        foodFragment).commit();
            }
            else if(fragment instanceof CompletedOrderFragment)
            {
                Bundle bundle = new Bundle();
                bundle.putInt("tab",2);
                MyRestaurantFragment myRestaurantFragment = new MyRestaurantFragment();
                myRestaurantFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        myRestaurantFragment).commit();
            }

            else
            {
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("Close the app")
                        .setMessage("Do you want to close the app?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                finishAffinity(); //close the app
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            }
        }
    }


    @Override
    public void getRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }


}