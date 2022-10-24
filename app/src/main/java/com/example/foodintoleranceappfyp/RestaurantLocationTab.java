package com.example.foodintoleranceappfyp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

    LatLng restaurantLocation;
    Address l;
    Bundle bundle = new Bundle();

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
            LatLng center = new LatLng((myLocation.latitude+restaurantLocation.latitude)/2, (myLocation.longitude+restaurantLocation.longitude)/2);
            //googleMap.animateCamera(CameraUpdateFactory.newLatLng(center));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(center));
            //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 14f));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(14f));
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

        bundle = this.getArguments();

        if (ActivityCompat.checkSelfPermission(
                getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        else
        {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

            Restaurant restaurant = (Restaurant)bundle.getSerializable("Restaurant");

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

                    Uri gmmIntentUri = Uri.parse("google.navigation:q="+l.getLatitude()+","+l.getLongitude());
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            });
        }




        return view;


    }

    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if(result)
                    {
                        RestaurantLocationTab restaurantLocationTab = new RestaurantLocationTab();
                        restaurantLocationTab.setArguments(bundle);
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                restaurantLocationTab).commit();
                    }

                }
            }
    );

}