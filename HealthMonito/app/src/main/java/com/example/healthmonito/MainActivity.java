package com.example.healthmonito;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static int heartRate = 0;
    private static int respRate = 0;
    Intent respRateIntent;
    respRateReciever receiver;
    TextView respText;
    public static String PREFS_NAME = "userId";
    int hrTime = 30;
    int respTime = 30;
    final String[] time = {"Analysis duration","15","30","45","60"};
    String userId = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText userIdText = (EditText)findViewById(R.id.userId);
        final Button setUserIdButton = (Button)findViewById(R.id.setuid);
        final Button regSym = (Button) findViewById(R.id.SYMP);
        final Button MHR = (Button)findViewById(R.id.HeartRT);
        final Button respRateButton = (Button) findViewById(R.id.RespRT);
        respText = (TextView)findViewById(R.id.respRate);
        final Spinner hrTimeSpinner = (Spinner) findViewById(R.id.hrTime);
        final Spinner respTimeSpinner = (Spinner) findViewById(R.id.respTime);

        final TextView hrtv = (TextView)findViewById(R.id.HeartRate);
        final ArrayAdapter timeAdaptor = new ArrayAdapter(this,android.R.layout.simple_spinner_item,time);

        final SharedPreferences settings = getApplicationContext().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        userId = settings.getString("userId", "");
        if(!userId.isEmpty() && !userId.equalsIgnoreCase("UNKNOWN")){
            userIdText.setHint(userId);
            setUserIdButton.setVisibility(View.GONE);
            regSym.setVisibility(View.VISIBLE);
            MHR.setVisibility(View.VISIBLE);
            respRateButton.setVisibility(View.VISIBLE);
            hrtv.setVisibility(View.VISIBLE);
            respText.setVisibility(View.VISIBLE);
            hrTimeSpinner.setVisibility(View.VISIBLE);
            respTimeSpinner.setVisibility(View.VISIBLE);
        }

        userIdText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userIdText.setFocusable(View.FOCUSABLE);
                userIdText.getText().clear();
                setUserIdButton.setVisibility(View.VISIBLE);
            }
        });

        setUserIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userId = userIdText.getText().toString();
                userIdText.setFocusable(View.NOT_FOCUSABLE);
                setUserIdButton.setVisibility(View.GONE);
                if(userId.isEmpty())
                {
                    userId = "UNKNOWN";
                    userIdText.setText(userId, TextView.BufferType.NORMAL);
                }
                userIdText.getText().clear();
                userIdText.setHint(userId);
                regSym.setVisibility(View.VISIBLE);
                MHR.setVisibility(View.VISIBLE);
                respRateButton.setVisibility(View.VISIBLE);
                hrtv.setVisibility(View.VISIBLE);
                respText.setVisibility(View.VISIBLE);
                hrTimeSpinner.setVisibility(View.VISIBLE);
                respTimeSpinner.setVisibility(View.VISIBLE);

            }
        });




        Bundle extras = getIntent().getExtras();

        if(extras!=null){
            heartRate = extras.getInt("hrate",0);
        }



        timeAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hrTimeSpinner.setAdapter(timeAdaptor);
        respTimeSpinner.setAdapter(timeAdaptor);

        hrTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    hrTime = Integer.parseInt(parent.getItemAtPosition(position).toString());
                }
                catch (Exception e){}
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        respTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    respTime = Integer.parseInt(parent.getItemAtPosition(position).toString());
                }
                catch (Exception e){}
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        receiver = new respRateReciever();
        registerReceiver(receiver, new IntentFilter("GET_RESP_DATA"));

        if(respRate>0)
        {
            respText.setText("Respiratory Rate:" +Integer.toString(respRate));
        }
        if(heartRate>0)
        {
            hrtv.setText("Heart Rate:" +Integer.toString(heartRate));
        }



        regSym.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                settings = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("userId", userId);

                // Apply the edits!
                editor.apply();
                if(heartRate==0){
                    showToast("Heart rate is not recorded");
                }
                if(respRate==0){
                    showToast("Respiratory rate is not recorded");
                }
                Intent startSymptomLogging = new Intent(MainActivity.this, RegisterSymptomsScreen.class);
                startSymptomLogging.putExtra("userId", userId);
                startSymptomLogging.putExtra("resprate",respRate);
                startSymptomLogging.putExtra("heartRate",heartRate);
                startActivity(startSymptomLogging);
            }
        });
        MHR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent int2 = new Intent(MainActivity.this,CameraActivity.class);
                int2.putExtra("timeLimit",hrTime);
                startActivity(int2);
            }
        });

        respRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                respRateIntent = new Intent(MainActivity.this, AccelSensorHandler.class);
                respRateIntent.putExtra("timeLimit",respTime);
                startService(respRateIntent);
            }
        });
    }
    private void showToast(final String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
    class respRateReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals("GET_RESP_DATA"))
            {
                respRate = intent.getIntExtra("resprate",0);
                if(respRate!=0){
                    respText.setText("Respiration rate: "+Integer.toString(respRate));
                }
            }
        }
    }
}