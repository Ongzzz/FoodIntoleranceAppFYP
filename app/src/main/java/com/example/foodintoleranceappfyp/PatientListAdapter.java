package com.example.foodintoleranceappfyp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.StrictMode;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


import androidx.annotation.NonNull;

public class PatientListAdapter extends BaseAdapter implements android.widget.ListAdapter {

    private ArrayList<Patient> arrayList = new ArrayList<>();
    private Context context;

    FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    public PatientListAdapter(ArrayList<Patient> arrayList, Context context) {
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
            convertView = inflater.inflate(R.layout.list_patient, null);
        }

        TextView tv_patientInfo = convertView.findViewById(R.id.tv_patientInfo);

        String intolerance = TextUtils.join(", ", arrayList.get(position).getIntolerance());

        String patientInfo = String.format("%-13s : %s", "Patient Name", arrayList.get(position).getName())
                + System.getProperty("line.separator")
                + String.format("%-15s : %s","Patient Email", arrayList.get(position).getEmail())
                + System.getProperty("line.separator")
                + String.format("%-18s : %s","Gender", arrayList.get(position).getGender())
                + System.getProperty("line.separator")
                + String.format("%-21s : %s", "State", arrayList.get(position).getState())
                + System.getProperty("line.separator")
                + String.format("%-16s : %s","Intolerance", intolerance);

        tv_patientInfo.setText(patientInfo);


        return convertView;
    }
}
