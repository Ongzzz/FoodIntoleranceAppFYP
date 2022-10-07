package com.example.foodintoleranceappfyp;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class RestaurantOwnerListAdapter extends BaseAdapter implements android.widget.ListAdapter {

    private ArrayList<RestaurantOwner> arrayList = new ArrayList<>();
    private Context context;

    FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    public RestaurantOwnerListAdapter(ArrayList<RestaurantOwner> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
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
            convertView = inflater.inflate(R.layout.list_restaurant_owner, null);
        }

        TextView tv_restaurantOwnerInfo = convertView.findViewById(R.id.tv_restaurantOwnerInfo);

        String restaurantList = TextUtils.join(", ", arrayList.get(position).getRestaurantName());

        String restaurantOwnerInfo =
                String.format("%-14s : %s","Restaurant Owner Name", arrayList.get(position).getName()) +
                System.getProperty("line.separator") +
                String.format("%-23s : %s","Restaurant Owner Email",arrayList.get(position).getEmail()) +
                System.getProperty("line.separator") +
                String.format("%-25s : %s","Owned Restaurant(s)", restaurantList);

        tv_restaurantOwnerInfo.setText(restaurantOwnerInfo);

        return convertView;
    }
}
