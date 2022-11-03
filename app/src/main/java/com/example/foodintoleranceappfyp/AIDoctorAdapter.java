package com.example.foodintoleranceappfyp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AIDoctorAdapter extends RecyclerView.Adapter {

    private ArrayList<Message> messageModalArrayList;
    private Context context;

    public AIDoctorAdapter(ArrayList<Message> messageModalArrayList, Context context) {
        this.messageModalArrayList = messageModalArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case 0:
                // inflate user message layout.
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_msg, parent, false);
                return new UserViewHolder(view);
            case 1:
                // inflate bot message layout.
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ai_doctor_msg, parent, false);
                return new BotViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // set data to layout file.
        Message message = messageModalArrayList.get(position);
        switch (message.getSender()) {
            case "user":
                // set the text to text view of user layout
                ((UserViewHolder) holder).userTV.setText(message.getMessage());
                break;
            case "bot":
                //set the text to text view of bot layout
                String msg = message.getMessage();
                msg = msg.replace("\\n",System.getProperty("line.separator"));
                ((BotViewHolder) holder).botTV.setText(msg);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messageModalArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {

        switch (messageModalArrayList.get(position).getSender()) {
            case "user":
                return 0;
            case "bot":
                return 1;
            default:
                return -1;
        }
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        TextView userTV;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userTV = itemView.findViewById(R.id.tv_patient_message);
        }
    }

    public static class BotViewHolder extends RecyclerView.ViewHolder {

        TextView botTV;

        public BotViewHolder(@NonNull View itemView) {
            super(itemView);
            botTV = itemView.findViewById(R.id.tv_ai_doctor_message);
        }
    }
}
