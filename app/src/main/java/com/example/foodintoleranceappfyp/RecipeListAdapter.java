package com.example.foodintoleranceappfyp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

public class RecipeListAdapter extends BaseAdapter implements android.widget.ListAdapter {

    private ArrayList<Recipe> arrayList = new ArrayList<>();
    private Context context;
    private String user;

    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseStorage fStorage = FirebaseStorage.getInstance();
    StorageReference storageReference;

    public RecipeListAdapter(ArrayList<Recipe> arrayList, Context context, String user) {
        this.arrayList = arrayList;
        this.context = context;
        this.user = user;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_recipe, null);
        }

        CardView cardView_recipe = convertView.findViewById(R.id.cardView_recipe);
        ImageView imgView_recipe = convertView.findViewById(R.id.imgView_recipe);
        TextView tv_recipe_name = convertView.findViewById(R.id.tv_recipe_name);
        ImageView imgView_delete_recipe = convertView.findViewById(R.id.imgView_delete_recipe);

        tv_recipe_name.setText(arrayList.get(position).getRecipeName());

        if(user.equals("Patient"))
        {
            imgView_delete_recipe.setVisibility(View.GONE);
        }

        if(!arrayList.get(position).getRecipeImage().isEmpty())
        {
            storageReference = fStorage.getReference(arrayList.get(position).getRecipeImage());
            try{
                File file = File.createTempFile("recipe","jpg");
                storageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        imgView_recipe.setImageBitmap(bitmap);
                    }
                });

                storageReference.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        imgView_recipe.setImageResource(android.R.color.transparent);
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        imgView_delete_recipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(context)
                        .setTitle("About this recipe...")
                        .setMessage("Delete "+ arrayList.get(position).getRecipeName()+"?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ProgressDialog progressDialog = new ProgressDialog(context);
                                progressDialog.setMessage("Deleting this recipe...");
                                progressDialog.show();

                                DocumentReference recipeReference = fStore.collection("recipes")
                                        .document(arrayList.get(position).getRecipeName());

                                recipeReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            storageReference = fStorage.getReference("images/recipes/"+arrayList.get(position).getRecipeName());
                                            storageReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        if(progressDialog.isShowing())
                                                        {
                                                            progressDialog.dismiss();
                                                        }
                                                        Toast.makeText(context,"The recipe is deleted successfully!", Toast.LENGTH_SHORT).show();
                                                        arrayList.remove(position);
                                                        notifyDataSetChanged();
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    if(progressDialog.isShowing())
                                                    {
                                                        progressDialog.dismiss();
                                                    }
                                                    Toast.makeText(context,"Failed to delete the recipe...", Toast.LENGTH_SHORT).show();
                                                }
                                            });
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

        cardView_recipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) context;
//                Bundle bundle = new Bundle();
//                bundle.putSerializable("Recipe",arrayList.get(position));
//                bundle.putString("UserType", user);
//                RecipeFragment recipeFragment = new RecipeFragment();
//                recipeFragment.setArguments(bundle);
//                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                        recipeFragment).commit();

                Intent i = new Intent(activity,RecipeActivity.class);
                i.putExtra("Recipe",arrayList.get(position));
                i.putExtra("UserType", user);
                activity.startActivity(i);
            }
        });


        return convertView;
    }
}
