package com.example.foodintoleranceappfyp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;


import java.util.ArrayList;

public class AIDoctorFragment extends Fragment {

    // creating variables for our
    // widgets in xml file.
    private RecyclerView rv_ai_doctor;
    private ImageButton btn_send_message;
    private EditText et_message;
    private final String USER_KEY = "user";
    private final String BOT_KEY = "bot";

    // creating a variable for
    // our volley request queue.
    private RequestQueue mRequestQueue;

    // creating a variable for array list and adapter class.
    private ArrayList<Message> messageArrayList;
    private AIDoctorAdapter messageRVAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_a_i_doctor, container, false);

            // on below line we are initializing all our views.
            rv_ai_doctor = view.findViewById(R.id.rv_ai_doctor);
            btn_send_message = view.findViewById(R.id.btn_send_message);
            et_message = view.findViewById(R.id.et_message);

            // below line is to initialize our request queue.
            mRequestQueue = Volley.newRequestQueue(getActivity());
            mRequestQueue.getCache().clear();

            // creating a new array list
            messageArrayList = new ArrayList<>();

            // adding on click listener for send message button.
            btn_send_message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // checking if the message entered
                    // by user is empty or not.
                    if (et_message.getText().toString().trim().isEmpty())
                    {
                        // if the edit text is empty display a toast message.
                        Toast.makeText(getContext(), "Please enter your message..", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // calling a method to send message
                    // to our bot to get response.
                    sendMessage(et_message.getText().toString().trim());

                    // below line we are setting text in our edit text as empty
                    et_message.setText("");
                }
            });

            // on below line we are initialing our adapter class and passing our array list to it.
            messageRVAdapter = new AIDoctorAdapter(messageArrayList, getContext());

            // below line we are creating a variable for our linear layout manager.
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);

            // below line is to set layout
            // manager to our recycler view.
            rv_ai_doctor.setLayoutManager(linearLayoutManager);

            // below line we are setting
            // adapter to our recycler view.
            rv_ai_doctor.setAdapter(messageRVAdapter);

        return view;
    }

    private void sendMessage(String userMsg) {
        // below line is to pass message to our
        // array list which is entered by the user.
        messageArrayList.add(new Message(userMsg, USER_KEY));
        messageRVAdapter.notifyDataSetChanged();

        // url for our brain
        // make sure to add mshape for uid.
        // make sure to add your url.
        String url = "http://api.brainshop.ai/get?bid=169710&key=jVklcm5bVnHQSbki&uid=[uid]&msg=" + userMsg;

        // creating a variable for our request queue.
        RequestQueue queue = Volley.newRequestQueue(getContext());

        // on below line we are making a json object request for a get request and passing our url .
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // in on response method we are extracting data
                    // from json response and adding this response to our array list.
                    String botResponse = response.getString("cnt");
                    messageArrayList.add(new Message(botResponse, BOT_KEY));

                    // notifying our adapter as data changed.
                    messageRVAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();

                    // handling error response from bot.
                    messageArrayList.add(new Message("No response", BOT_KEY));
                    messageRVAdapter.notifyDataSetChanged();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error handling.
                messageArrayList.add(new Message("Sorry no response found", BOT_KEY));
                Toast.makeText(getContext(), "No response from the bot..", Toast.LENGTH_SHORT).show();
            }
        });

        // at last adding json object
        // request to our queue.
        queue.add(jsonObjectRequest);
    }
}