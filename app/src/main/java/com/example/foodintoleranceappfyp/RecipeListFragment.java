package com.example.foodintoleranceappfyp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class RecipeListFragment extends Fragment {

    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    String userId = fAuth.getCurrentUser().getEmail();
    CollectionReference recipeListReference = fStore.collection("recipes");
    ArrayList<Recipe> recipeList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);

        TextView tv_no_recipe = view.findViewById(R.id.tv_no_recipe);
        ListView lv_recipe_list = view.findViewById(R.id.lv_recipe_list);

        recipeListReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    for (DocumentSnapshot documentSnapshot : task.getResult())
                    {
                        String recipeName = documentSnapshot.getString("Recipe Name");
                        String recipeDescription = documentSnapshot.getString("Recipe Description");
                        String recipeImage = documentSnapshot.getString("Recipe Image");
                        String recipeURL = documentSnapshot.getString("Recipe URL");
                        String adminID = documentSnapshot.getString("Added By");
                        ArrayList<String> intolerance = (ArrayList)documentSnapshot.get("Intolerance");
                        recipeList.add(new Recipe(recipeName, recipeDescription, recipeImage, recipeURL, adminID, intolerance));
                    }

                    if(recipeList.isEmpty())
                    {
                        lv_recipe_list.setVisibility(View.GONE);
                        tv_no_recipe.setVisibility(View.VISIBLE);
                        tv_no_recipe.setText("No recipe...");
                    }
                    else
                    {
                        tv_no_recipe.setVisibility(View.GONE);
                        lv_recipe_list.setVisibility(View.VISIBLE);

                        DocumentReference userReference = fStore.collection("users").document(userId);
                        userReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful())
                                {
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    if(documentSnapshot.getString("UserType").equals("Admin"))
                                    {
                                        lv_recipe_list.setAdapter(new RecipeListAdapter(recipeList, getContext(), "Admin"));
                                    }
                                    if(documentSnapshot.getString("UserType").equals("Patient"))
                                    {
                                        lv_recipe_list.setAdapter(new RecipeListAdapter(recipeList, getContext(), "Patient"));
                                    }
                                }
                            }
                        });

                    }
                }
            }
        });


        return view;
    }
}