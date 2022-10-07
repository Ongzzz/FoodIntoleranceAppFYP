package com.example.foodintoleranceappfyp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;

public class RestaurantLocationTab extends Fragment {

    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;


    GoogleMap map;

    LatLng restaurantLocation;
    Address l;


    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
            }

            LatLng myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(myLocation));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 5));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
            googleMap.addMarker(new MarkerOptions().position(myLocation).title("I am here"));
            googleMap.addMarker(new MarkerOptions().position(restaurantLocation).title("restaurant is here"));

        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_location_tab, container, false);
        Button btn_getDirection = view.findViewById(R.id.btn_getDirection);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        Bundle bundle = this.getArguments();
        if(bundle!=null)
        {
            Restaurant restaurant = (Restaurant)bundle.getSerializable("Restaurant");

            if (ActivityCompat.checkSelfPermission(
                    getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            }

            Task<Location> task = fusedLocationProviderClient.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null)
                    {

                        currentLocation = location;
                        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
                        if (mapFragment != null)
                        {
                            Geocoder coder = new Geocoder(getContext());
                            List<Address> address;

                            String restaurantAddress = restaurant.getRestaurantAddress();

                            try {
                                // May throw an IOException
                                address = coder.getFromLocationName(restaurantAddress, 1);
                                if (address == null) {
                                    return;
                                }

                                l = address.get(0);
                                restaurantLocation = new LatLng(l.getLatitude(), l.getLongitude());



                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mapFragment.getMapAsync(callback);
                        }
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new RestaurantLocationTab()).commit();
                }
            });

            btn_getDirection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(getContext(),"hello",Toast.LENGTH_SHORT).show();
                    //Uri gmmIntentUri = Uri.parse("geo:0,0?q="+restaurant.getRestaurantAddress());
                    Uri gmmIntentUri = Uri.parse("google.navigation:q="+l.getLatitude()+","+l.getLongitude());
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            });
        }


        return view;
    }


//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        if (ActivityCompat.checkSelfPermission(
//                getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//        {
//            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
//            super.onPause();
//        }
//        super.onResume();
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
//        Task<Location> task = fusedLocationProviderClient.getLastLocation();
//        task.addOnSuccessListener(new OnSuccessListener<Location>() {
//            @Override
//            public void onSuccess(Location location) {
//                if (location != null)
//                {
//                    currentLocation = location;
//                    SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
//                    if (mapFragment != null)
//                    {
//                        mapFragment.getMapAsync(callback);
//                    }
//                }
//
//            }
//        });
//
//
//
//
//
//    }

//    private void requestPermission() {
//
//        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
//    }
//
//
//    private ActivityResultLauncher<String> requestPermissionLauncher =
//            registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
//                @Override
//                public void onActivityResult(Boolean result) {
//                    if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION))
//                    {
//                        Toast.makeText(getActivity(), "We need the location permission to get your current location! Please turn on the location permission in the setting", Toast.LENGTH_SHORT).show();
//                    }
//
//                    else if(!result)
//                    {
//                        Toast.makeText(getActivity(), "We need the location permission to get your current location!", Toast.LENGTH_SHORT).show();
//                    }
//
//                }
//
//            });
//

}