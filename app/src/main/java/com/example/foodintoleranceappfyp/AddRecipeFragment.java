package com.example.foodintoleranceappfyp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dropbox.core.util.StringUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.oney.WebRTCModule.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class AddRecipeFragment extends Fragment {

    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseStorage fStorage = FirebaseStorage.getInstance();
    StorageReference storageReference;
    String userId = fAuth.getCurrentUser().getEmail();
    Uri path;
    CollectionReference recipeListReference = fStore.collection("recipes");
    ImageView imgView_recipe_image;
    boolean found;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_recipe, container, false);

        imgView_recipe_image = view.findViewById(R.id.imgView_recipe_image);
        Button btn_select_recipe_image = view.findViewById(R.id.btn_select_recipe_image);
        EditText et_recipe_name = view.findViewById(R.id.et_recipe_name);
        EditText et_recipe_description = view.findViewById(R.id.et_recipe_description);
        EditText et_recipe_video_url = view.findViewById(R.id.et_recipe_video_url);
        CheckBox cb_gluten = view.findViewById(R.id.cb_gluten);
        CheckBox cb_lactose = view.findViewById(R.id.cb_lactose);
        CheckBox cb_fructose = view.findViewById(R.id.cb_fructose);
        Button btn_addRecipe = view.findViewById(R.id.btn_addRecipe);

        btn_select_recipe_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                activityResultLauncher.launch(intent);
            }
        });

        btn_addRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valid = true;
                found = false;
                String recipeName = et_recipe_name.getText().toString().trim();
                String recipeDescription = et_recipe_description.getText().toString().trim();
                String recipeURL = et_recipe_video_url.getText().toString().trim();
                ArrayList<String> intolerance = new ArrayList<>();

                if(path==null)
                {
                    Toast.makeText(getContext(),"Please select an image for this recipe", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if(recipeName.isEmpty())
                    {
                        et_recipe_name.setError("Please enter a name for this recipe!");
                        valid = false;
                    }

                    if(recipeDescription.isEmpty())
                    {
                        et_recipe_description.setError("Please enter the description/step of this recipe!");
                        valid = false;
                    }

//                    if(recipeURL.isEmpty())
//                    {
//                        et_recipe_video_url.setError("Please enter the URL of this recipe");
//                        valid = false;
//                    }

                    if(!recipeURL.isEmpty())
                    {
                        boolean isValidURL = true;
                        String pattern = "^(http(s)?:\\/\\/)?((w){3}.)?((m).)?youtu(be|.be)?(\\.com)?\\/.+";

                        if(!Patterns.WEB_URL.matcher(recipeURL).matches())
                        {
                            et_recipe_video_url.setError("Please enter a valid URL for this recipe");
                            valid = false;
                            isValidURL = false;
                        }

                        if(isValidURL)
                        {
                            if(recipeURL.matches(pattern)) //if is youtube url
                            {
                                Toast.makeText(getContext(),"here",Toast.LENGTH_SHORT).show();
                                if(recipeURL.contains("youtube.com/watch?v=") || recipeURL.contains("m.youtube.com/watch?v="))
                                {

                                    if(recipeURL.substring(recipeURL.lastIndexOf("=")).length()-1!=11 ) //youtube video ID invalid
                                    {
                                        valid = false;
                                        isValidURL = false;
                                    }

                                    if(recipeURL.lastIndexOf("=")!=recipeURL.length()-12) //
                                    {
                                        valid = false;
                                        isValidURL = false;
                                    }

                                }
                                if(recipeURL.contains("youtu.be/"))
                                {
                                    if(recipeURL.substring(recipeURL.lastIndexOf("/")).length()-1!=11)
                                    {
                                        valid = false;
                                        isValidURL = false;
                                    }
                                    if(recipeURL.lastIndexOf("/")!=recipeURL.length()-12)
                                    {
                                        valid = false;
                                        isValidURL = false;
                                    }
                                }
                                else if(!recipeURL.contains("youtube.com/watch?v=") && !recipeURL.contains("youtu.be/") && !recipeURL.contains("m.youtube.com/watch?v="))
                                {
                                    valid = false;
                                    isValidURL = false;
                                }
                            }
                            else if (!recipeURL.contains(".mp4") && !recipeURL.contains(".webm")) //if not youtube url
                            {
                                valid = false;
                                isValidURL = false;
                            }

                        }



                        if(!isValidURL)
                        {
                            et_recipe_video_url.setError("Please enter a valid URL for this recipe");
                        }


                    }

                    if(valid)
                    {
                        if(cb_gluten.isChecked())
                        {
                            intolerance.add("gluten");
                        }
                        if(cb_fructose.isChecked())
                        {
                            intolerance.add("fructose");
                        }
                        if(cb_lactose.isChecked())
                        {
                            intolerance.add("lactose");
                        }


                        recipeListReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful())
                                {
                                    for (DocumentSnapshot documentSnapshot : task.getResult())
                                    {
                                        if(documentSnapshot.getString("Recipe Name").equals(recipeName))
                                        {
                                            Toast.makeText(getContext(),"This recipe exists!", Toast.LENGTH_SHORT).show();
                                            found = true;
                                            break;
                                        }
                                    }
                                    if(!found)
                                    {

                                        ProgressDialog progressDialog = new ProgressDialog(getContext());
                                        progressDialog.setMessage("Adding your new recipe...");
                                        progressDialog.show();

                                        String imagePath = "images/recipes/"+recipeName;
                                        DocumentReference recipeReference = fStore.collection("recipes").document(recipeName);
                                        Map<String, Object> recipe = new HashMap<>();
                                        recipe.put("Recipe Name", recipeName);
                                        recipe.put("Recipe Description", recipeDescription);
                                        recipe.put("Recipe URL", recipeURL);
                                        recipe.put("Recipe Image", imagePath);
                                        recipe.put("Intolerance", intolerance);
                                        recipe.put("Added By", userId);
                                        recipeReference.set(recipe).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    storageReference = fStorage.getReference(imagePath);
                                                    storageReference.putFile(path).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                        @Override
                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                            if(progressDialog.isShowing())
                                                            {
                                                                progressDialog.dismiss();
                                                            }
                                                            Toast.makeText(getContext(),"The recipe is added successfully!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            if(progressDialog.isShowing())
                                                            {
                                                                progressDialog.dismiss();
                                                            }
                                                            Toast.makeText(getContext(),"Unexpected error occurs. Please try again later...", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            }
                                        });

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

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        path = data.getData();
                        imgView_recipe_image.setImageURI(path);
                    }
                }
            });
}