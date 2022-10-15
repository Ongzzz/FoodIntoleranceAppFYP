package com.example.foodintoleranceappfyp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.security.AccessController.getContext;

public class RecipeActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener{

    FirebaseStorage fStorage = FirebaseStorage.getInstance();
    StorageReference storageReference;
    Uri path;
    ImageView imgView_recipe_image;
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    DocumentReference recipeReference;
    MediaController mediaController;
    VideoView vv_player;
    Recipe recipe;
    YouTubePlayerView yt_player;
    YouTubePlayer youtubePlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        imgView_recipe_image = findViewById(R.id.imgView_recipe_image);
        Button btn_select_recipe_new_image = findViewById(R.id.btn_select_recipe_new_image);
        Button btn_update_recipe_new_image = findViewById(R.id.btn_update_recipe_new_image);
        TextView tv_recipe_name = findViewById(R.id.tv_recipe_name);
        //ImageView imgView_changeRecipeName = findViewById(R.id.imgView_changeRecipeName);
        TextView tv_recipe_intolerance = findViewById(R.id.tv_recipe_intolerance);
        ImageView imgView_changeRecipeIntolerance = findViewById(R.id.imgView_changeRecipeIntolerance);
        TextView tv_recipe_URL = findViewById(R.id.tv_recipe_URL);
        ImageView imgView_changeRecipeURL = findViewById(R.id.imgView_changeRecipeURL);
        yt_player = findViewById(R.id.yt_player);
        vv_player = findViewById(R.id.vv_player);
        ImageView imgView_playVideo = findViewById(R.id.imgView_playVideo);

        TextView tv_recipe_description = findViewById(R.id.tv_recipe_description);
        ImageView imgView_changeRecipeDescription = findViewById(R.id.imgView_changeRecipeDescription);

        Intent intent = getIntent();

        if(intent!=null)
        {
            recipe = (Recipe)intent.getSerializableExtra("Recipe");
            String user = intent.getStringExtra("UserType");

            recipeReference = fStore.collection("recipes").document(recipe.getRecipeName());

            imgView_changeRecipeDescription.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText et_recipeNewDescription = new EditText(v.getContext());
                    AlertDialog alertDialog = new AlertDialog.Builder(RecipeActivity.this)
                            .setTitle("Change Recipe Description")
                            .setMessage("Please input the new recipe description")
                            .setView(et_recipeNewDescription)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    String newRecipeDescription = et_recipeNewDescription.getText().toString().trim();

                                    if(newRecipeDescription.isEmpty())
                                    {
                                        Toast.makeText(RecipeActivity.this, "Please enter a new recipe description", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        Map<String, Object> updatedRecipe = new HashMap<>();
                                        updatedRecipe.put("Recipe Description", newRecipeDescription);
                                        recipeReference.update(updatedRecipe).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    Toast.makeText(RecipeActivity.this, "The recipe description is changed successfully", Toast.LENGTH_SHORT).show();
                                                    youtubePlayer.pause();
                                                    youtubePlayer.release();
                                                    finish();
                                                    Intent i = new Intent(RecipeActivity.this, RecipeActivity.class);
                                                    recipe.setRecipeDescription(newRecipeDescription);
                                                    i.putExtra("Recipe", recipe);
                                                    i.putExtra("UserType", user);
                                                    startActivity(i);
                                                }
                                            }
                                        });
                                    }
                                }
                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                }
            });

            imgView_changeRecipeIntolerance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    View checkBoxView = v.inflate(RecipeActivity.this, R.layout.change_intolerance, null);
                    CheckBox change_gluten = checkBoxView.findViewById(R.id.cb_changeIntolerance_gluten);
                    CheckBox change_fructose = checkBoxView.findViewById(R.id.cb_changeIntolerance_fructose);
                    CheckBox change_lactose = checkBoxView.findViewById(R.id.cb_changeIntolerance_lactose);

                    AlertDialog alertDialog = new AlertDialog.Builder(RecipeActivity.this)
                            .setTitle("Change Recipe Nutrient")
                            .setMessage("Please select the new recipe nutrient. (Optional, if applicable)")
                            .setView(checkBoxView)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    ArrayList<String> intoleranceList = new ArrayList<>();
                                    if(change_gluten.isChecked())
                                    {
                                        intoleranceList.add("gluten");
                                    }
                                    if(change_lactose.isChecked())
                                    {
                                        intoleranceList.add("lactose");
                                    }
                                    if(change_fructose.isChecked())
                                    {
                                        intoleranceList.add("fructose");
                                    }

                                    Map<String, Object> updatedRecipe = new HashMap<>();
                                    updatedRecipe.put("Intolerance", intoleranceList);
                                    recipeReference.update(updatedRecipe).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                Toast.makeText(RecipeActivity.this, "The recipe nutrient is changed successfully", Toast.LENGTH_SHORT).show();
                                                youtubePlayer.pause();
                                                youtubePlayer.release();
                                                finish();
                                                Intent i = new Intent(RecipeActivity.this, RecipeActivity.class);
                                                recipe.setIntolerance(intoleranceList);
                                                i.putExtra("Recipe", recipe);
                                                i.putExtra("UserType", user);
                                                startActivity(i);
                                            }
                                        }
                                    });
                                }
                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();


                }
            });

            imgView_changeRecipeURL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText et_recipeNewURL = new EditText(v.getContext());
                    AlertDialog alertDialog = new AlertDialog.Builder(RecipeActivity.this)
                            .setTitle("Change Recipe URL")
                            .setMessage("Please input the new recipe URL")
                            .setView(et_recipeNewURL)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    String recipeNewURL = et_recipeNewURL.getText().toString().trim();
                                    if(!recipeNewURL.isEmpty())
                                    {
                                        boolean isValidURL = true;
                                        String pattern = "^(http(s)?:\\/\\/)?((w){3}.)?((m).)?youtu(be|.be)?(\\.com)?\\/.+";

                                        if(!Patterns.WEB_URL.matcher(recipeNewURL).matches())
                                        {
                                            et_recipeNewURL.setError("Please enter a valid URL for this recipe");
                                            isValidURL = false;
                                        }

                                        if(isValidURL)
                                        {
                                            if(recipeNewURL.matches(pattern)) //if is youtube url
                                            {
                                                if(recipeNewURL.contains("youtube.com/watch?v=") || recipeNewURL.contains("m.youtube.com/watch?v="))
                                                {

                                                    if(recipeNewURL.substring(recipeNewURL.lastIndexOf("=")).length()-1!=11 ) //youtube video ID invalid
                                                    {
                                                        isValidURL = false;
                                                    }

                                                    if(recipeNewURL.lastIndexOf("=")!=recipeNewURL.length()-12) //
                                                    {
                                                        isValidURL = false;
                                                    }

                                                }
                                                if(recipeNewURL.contains("youtu.be/"))
                                                {
                                                    if(recipeNewURL.substring(recipeNewURL.lastIndexOf("/")).length()-1!=11)
                                                    {
                                                        isValidURL = false;
                                                    }
                                                    if(recipeNewURL.lastIndexOf("/")!=recipeNewURL.length()-12)
                                                    {
                                                        isValidURL = false;
                                                    }
                                                }
                                                else if(!recipeNewURL.contains("youtube.com/watch?v=") && !recipeNewURL.contains("youtu.be/") && !recipeNewURL.contains("m.youtube.com/watch?v="))
                                                {
                                                    isValidURL = false;
                                                }
                                            }
                                            else if (!recipeNewURL.contains(".mp4") && !recipeNewURL.contains(".webm")) //if not youtube url
                                            {
                                                isValidURL = false;
                                            }

                                        }

                                        if(!isValidURL)
                                        {
                                            Toast.makeText(RecipeActivity.this, "Please enter a valid URL for this recipe", Toast.LENGTH_SHORT).show();
                                        }
                                        else
                                        {
                                            HashMap<String, Object> updatedRecipe = new HashMap<>();
                                            updatedRecipe.put("Recipe URL", recipeNewURL);
                                            recipeReference.update(updatedRecipe).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        Toast.makeText(RecipeActivity.this, "The recipe nutrient is changed successfully", Toast.LENGTH_SHORT).show();
                                                        youtubePlayer.pause();
                                                        youtubePlayer.release();
                                                        finish();
                                                        Intent i = new Intent(RecipeActivity.this, RecipeActivity.class);
                                                        recipe.setRecipeURL(recipeNewURL);
                                                        i.putExtra("Recipe", recipe);
                                                        i.putExtra("UserType", user);
                                                        startActivity(i);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();


                }
            });

            if(!recipe.getRecipeURL().isEmpty())
            {
                if(recipe.getRecipeURL().contains(".mp4") || recipe.getRecipeURL().contains(".webm"))
                {
                    imgView_playVideo.setVisibility(View.VISIBLE);
                    imgView_playVideo.bringToFront();
                    yt_player.setVisibility(View.INVISIBLE);
                    vv_player.setVisibility(View.VISIBLE);
                    Uri uri = Uri.parse(recipe.getRecipeURL());

                    vv_player.setVideoURI(uri);
                    mediaController =  new MediaController(this);
                    mediaController.setAnchorView(vv_player);
                    mediaController.setMediaPlayer(vv_player);
                    vv_player.setMediaController(mediaController);
                    vv_player.requestFocus();
                    vv_player.seekTo(1);

                    vv_player.setMediaController(null);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        mediaController.addOnUnhandledKeyEventListener((v, event) -> {
                            //Handle BACK button
                            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
                            {
                                mediaController.hide();
                            }
                            return true;
                        });

                    }

                    vv_player.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            vv_player.setMediaController(mediaController);

                            mediaController.show(2500);
                            imgView_playVideo.setVisibility(View.GONE);

                        }
                    });


                }

                else //is youtube video
                {
                    imgView_playVideo.setVisibility(View.GONE);
                    yt_player.setVisibility(View.VISIBLE);
                    vv_player.setVisibility(View.INVISIBLE);

//                    YouTubePlayer.OnInitializedListener listener = new YouTubePlayer.OnInitializedListener() {
//                        @Override
//                        public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
//                            if(!b)
//                            {
//                                String youtubeVideoID = recipe.getRecipeURL().substring(recipe.getRecipeURL().length()-11);
//                                youTubePlayer.cueVideo(youtubeVideoID);
//                            }
//
//                        }
//
//                        @Override
//                        public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
//                            Toast.makeText(RecipeActivity.this, "YouTube video failed to load", Toast.LENGTH_SHORT).show();
//                        }
//                    };
//
//                    yt_player.initialize("AIzaSyBjD51FgPv2XOAxWphOEEZYzx-sYOBK7Q0", listener);

                }

            }
            else
            {
                yt_player.setVisibility(View.GONE);
            }

            tv_recipe_name.setText(recipe.getRecipeName());
            String intolerance = TextUtils.join(", ", recipe.getIntolerance());
            if(!intolerance.isEmpty())
            {
                tv_recipe_intolerance.setText("Contain: "+intolerance);
            }
            else
            {
                tv_recipe_intolerance.setText("No gluten, fructose, and lactose");
            }
            tv_recipe_description.setText(recipe.getRecipeDescription());
            if(recipe.getRecipeURL().isEmpty())
            {
                tv_recipe_URL.setText("No Video...");
            }
            else
            {
                tv_recipe_URL.setText(recipe.getRecipeURL());
            }

            if(user.equals("Patient"))
            {
                tv_recipe_URL.setVisibility(View.GONE);
                btn_select_recipe_new_image.setVisibility(View.GONE);
                btn_update_recipe_new_image.setVisibility(View.GONE);
                //imgView_changeRecipeName.setVisibility(View.GONE);
                imgView_changeRecipeDescription.setVisibility(View.GONE);
                imgView_changeRecipeIntolerance.setVisibility(View.GONE);
                imgView_changeRecipeURL.setVisibility(View.GONE);
            }
            if(user.equals("Admin"))
            {
                tv_recipe_URL.setVisibility(View.VISIBLE);
                btn_select_recipe_new_image.setVisibility(View.VISIBLE);
                btn_update_recipe_new_image.setVisibility(View.VISIBLE);
                //imgView_changeRecipeName.setVisibility(View.VISIBLE);
                imgView_changeRecipeDescription.setVisibility(View.VISIBLE);
                imgView_changeRecipeIntolerance.setVisibility(View.VISIBLE);
                imgView_changeRecipeURL.setVisibility(View.VISIBLE);
            }

            if(!recipe.getRecipeImage().isEmpty())
            {
                storageReference = fStorage.getReference(recipe.getRecipeImage());
                try{
                    File file = File.createTempFile("recipe","jpg");
                    storageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                            imgView_recipe_image.setImageBitmap(bitmap);
                        }
                    });

                    storageReference.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            imgView_recipe_image.setImageResource(android.R.color.transparent);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            btn_select_recipe_new_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, 10);

                }
            });

            btn_update_recipe_new_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(path!=null)
                    {
                        String imagePath = "images/recipes/"+recipe.getRecipeName();
                        Map<String,Object> newRecipeImage = new HashMap<>();
                        newRecipeImage.put("Recipe Image",imagePath);

                        ProgressDialog progressDialog = new ProgressDialog(RecipeActivity.this);
                        progressDialog.setMessage("Uploading the new image of this recipe...");
                        progressDialog.show();

                        recipeReference.update(newRecipeImage).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                            Toast.makeText(RecipeActivity.this,"The image of this recipe is changed successfully!", Toast.LENGTH_SHORT).show();
                                            youtubePlayer.pause();
                                            youtubePlayer.release();
                                            finish();
                                            Intent i = new Intent(RecipeActivity.this, RecipeActivity.class);
                                            i.putExtra("Recipe", recipe);
                                            i.putExtra("UserType", user);
                                            startActivity(i);

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            if(progressDialog.isShowing())
                                            {
                                                progressDialog.dismiss();
                                            }
                                            Toast.makeText(RecipeActivity.this,"Failed to change the image of this recipe...", Toast.LENGTH_SHORT).show();
                                            youtubePlayer.pause();
                                            youtubePlayer.release();
                                            finish();
                                            Intent i = new Intent(RecipeActivity.this, RecipeActivity.class);
                                            i.putExtra("Recipe", recipe);
                                            i.putExtra("UserType", user);
                                            startActivity(i);
                                        }
                                    });



                                }
                            }
                        });
                    }
                    else
                    {
                        Toast.makeText(RecipeActivity.this,"No new image is selected", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK)
        {
            path = data.getData();
            imgView_recipe_image.setImageURI(path);
        }
    }

    @Override
    public void onBackPressed() {

        Intent i = new Intent(RecipeActivity.this, MainActivity.class);
        i.putExtra("RecipeListFragment","RecipeListFragment");
        startActivity(i);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer y, boolean b) {
        if(!b)
        {
            youtubePlayer = y;
            String youtubeVideoID = recipe.getRecipeURL().substring(recipe.getRecipeURL().length()-11);
            youtubePlayer.cueVideo(youtubeVideoID);
        }

    }


    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

        Toast.makeText(RecipeActivity.this, "YouTube video failed to load", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        yt_player.initialize("AIzaSyBjD51FgPv2XOAxWphOEEZYzx-sYOBK7Q0", this);

    }

}