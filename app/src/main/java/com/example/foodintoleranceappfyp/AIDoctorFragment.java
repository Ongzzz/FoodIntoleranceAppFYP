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

    private RecyclerView rv_ai_doctor;
    private ImageButton btn_send_message;
    private EditText et_message;
    private final String USER_KEY = "user";
    private final String BOT_KEY = "bot";

    private RequestQueue mRequestQueue;

    private ArrayList<Message> messageArrayList;
    private AIDoctorAdapter messageRVAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_a_i_doctor, container, false);

            rv_ai_doctor = view.findViewById(R.id.rv_ai_doctor);
            btn_send_message = view.findViewById(R.id.btn_send_message);
            et_message = view.findViewById(R.id.et_message);

            mRequestQueue = Volley.newRequestQueue(getActivity());
            mRequestQueue.getCache().clear();

            messageArrayList = new ArrayList<>();

            btn_send_message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (et_message.getText().toString().trim().isEmpty())
                    {
                        Toast.makeText(getContext(), "Please enter your message..", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    sendMessage(et_message.getText().toString().trim());

                    et_message.setText("");
                }
            });

            messageRVAdapter = new AIDoctorAdapter(messageArrayList, getContext());

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);

            rv_ai_doctor.setLayoutManager(linearLayoutManager);

            rv_ai_doctor.setAdapter(messageRVAdapter);

        return view;
    }

    private void sendMessage(String userMsg) {

        messageArrayList.add(new Message(userMsg, USER_KEY));
        messageRVAdapter.notifyDataSetChanged();

        String url = "http://api.brainshop.ai/get?bid=169710&key=jVklcm5bVnHQSbki&uid=[uid]&msg=" + userMsg;

        RequestQueue queue = Volley.newRequestQueue(getContext());

        // make a json object request for a get request and passing the url .
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // extract data from json response and adding this response to array list.
                    String botResponse = response.getString("cnt");
                    messageArrayList.add(new Message(botResponse, BOT_KEY));

                    messageRVAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();

                    messageArrayList.add(new Message("No response", BOT_KEY));
                    messageRVAdapter.notifyDataSetChanged();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                messageArrayList.add(new Message("Sorry no response found", BOT_KEY));
                Toast.makeText(getContext(), "No response from the bot..", Toast.LENGTH_SHORT).show();
            }
        });

        // add json object request to the queue.
        queue.add(jsonObjectRequest);
    }
}