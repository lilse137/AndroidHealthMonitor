package com.example.healthmonito;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class RegisterSymptomsScreen extends AppCompatActivity{
    SQLiteDatabase db;
    String symptomSelected;
    RatingBar ratingB;
    String DBPath;
    String userId;
    int hrate;
    int resprate;
    static HashMap<String, Float> ratingsList;
    Button submit;
    static float selectedRating;
    private DBManager dbManager;
//    LatLng location;
//    LocationManager mLocationManager;
//    double lat,lon;
    private LocationManager locationManager;
    private Location onlyOneLocation;
    private final int REQUEST_FINE_LOCATION = 1234;
    String latitude;
    String longitude;
    String dateVal = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_symptoms_screen);
        //location things


        registerLocationUpdates();

        //end
        final Button uploadButton = (Button)findViewById(R.id.upload);
        final Button removeDB = (Button)findViewById(R.id.rmLoc);
        final TextView latitudeTv = (TextView)findViewById(R.id.lat);
        final TextView longitudeTv = (TextView)findViewById(R.id.lon);
        final TextView idtv = (TextView)findViewById(R.id.uId);
        final EditText dateText = (EditText)findViewById(R.id.date);
        final Button matrixButton = (Button)findViewById(R.id.matrix);
        Bundle extras = getIntent().getExtras();
        DBPath = "SymDB.sqlite";
        dbManager = new DBManager(this);
        dbManager.open();
        if (extras != null) {
            hrate = extras.getInt("heartRate", 0);
            resprate = extras.getInt("resprate", 0);
            userId = extras.getString("userId", "UNKNOWN");
        }
        idtv.setText(userId);
        final String[] symps = {"Nausea", "Headache", "Diarrhea", "Soar Throat", "Fever", "Muscle Ache", "Loss of Smell or Taste", "Cough", "Shortness of Breath", "Feeling Tired"};
        Spinner spinner = (Spinner) findViewById(R.id.SYMPList);
        // Create an ArrayAdapter using the string array and a default spinner layout
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, symps);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        ratingsList = new HashMap<String, Float>();
        ratingB = (RatingBar) findViewById(R.id.ratingBar1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View view, int i, long l) {

                String symptomSel = adapter.getItemAtPosition(i).toString();
                symptomSelected = symptomSel;
                if(ratingsList.containsKey(symptomSelected)){
                    ratingB.setRating((float)ratingsList.get(symptomSelected));
                }
                else
                {
                    ratingB.setRating((float) 0.0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ratingB.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                TextView view = (TextView) findViewById(R.id.rating);
                selectedRating = v;
                view.setText("Rating: " + selectedRating);
                ratingsList.put(symptomSelected, selectedRating);
            }
        });
        Button back = (Button) findViewById(R.id.BACK);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent int1 = new Intent(RegisterSymptomsScreen.this, MainActivity.class);
                startActivity(int1);
            }
        });



        removeDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbManager.delete();
            }
        });

        submit = (Button) findViewById(R.id.SUBMIT);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Double lat = Double.valueOf(latitude);
                Double lon = Double.valueOf(longitude);
                for (String symp : symps
                ) {
                    if (!ratingsList.containsKey(symp)) {
                        ratingsList.put(symp, (float) 0.0);
                    }
                }
                saveToDB2(userId, lat, lon, ratingsList);
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                UploadTask upload = new UploadTask();
                showToast("Stating Upload");
                UploadData();

//                upload.execute();
            }
        });

        matrixButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateVal = dateText.getText().toString();
                dateText.setFocusable(View.NOT_FOCUSABLE);
                calcAdjMatrix();
            }
        });

    }

    public void saveToDB2(String userId, double latitude,double longitude, HashMap<String, Float> ratingsList) {
//        String loc = location.toString();
        dbManager.insert(userId, latitude, longitude, hrate, resprate, ratingsList.get("Nausea"), ratingsList.get("Headache"), ratingsList.get("Diarrhea"), ratingsList.get("Soar Throat"), ratingsList.get("Fever"), ratingsList.get("Muscle Ache"), ratingsList.get("Loss of Smell or Taste"), ratingsList.get("Cough"), ratingsList.get("Shortness of Breath"), ratingsList.get("Feeling Tired"));
    }

    private void showToast(final String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    void registerLocationUpdates() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);

        locationManager = (LocationManager)this.getSystemService(LOCATION_SERVICE);

        String provider = locationManager.getBestProvider(criteria, true);

        // Cant get a hold of provider
//        if (provider == null) {
//            Log.v(TAG, "Provider is null");
//            showNoProvider();
//            return;
//        } else {
//            Log.v(TAG, "Provider: " + provider);
//        }

        MyLocationListener locationListener = new MyLocationListener();
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "First enable LOCATION ACCESS in settings.", Toast.LENGTH_LONG).show();
            return;
        }
        locationManager.requestLocationUpdates(provider, 1L, 1f, locationListener);

        // connect to the GPS location service
        Location oldLocation = locationManager.getLastKnownLocation(provider);

        if (oldLocation != null)  {
            showToast("Got Old location");
            latitude = Double.toString(oldLocation.getLatitude());
            longitude = Double.toString(oldLocation.getLongitude());
//            waitingForLocationUpdate = false;
//            getNearbyStores();
        } else {
            showToast("NO Last Location found");
        }
    }

    private class MyLocationListener implements LocationListener {


        public void onLocationChanged(Location location) {
            latitude = Double.toString(location.getLatitude());
            longitude = Double.toString(location.getLongitude());

            showToast("IN ON LOCATION CHANGE");

//            if (waitingForLocationUpdate) {
//                getNearbyStores();
//                waitingForLocationUpdate = false;
//            }

            locationManager.removeUpdates(this);
        }

        public void onStatusChanged(String s, int i, Bundle bundle) {
            showToast( "Status changed: " + s);
        }

        public void onProviderEnabled(String s) {
            showToast("PROVIDER DISABLED: " + s);
        }

        public void onProviderDisabled(String s) {
            showToast("PROVIDER DISABLED: " + s);
        }
    }

    public void UploadData(){
        try{
            File dbFile = new File(getApplicationContext().getExternalFilesDir(null).getAbsolutePath()+ File.separator + "databases"+ File.separator +"SYMPMONI.DB");
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            params.put("database",dbFile);
            String url =  "http://192.168.0.241:5000/";
            client.post(url, params, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    showToast("Upload Failed!");
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    showToast("Upload Sucessful");
                }
            });
        }
        catch (Exception e){

        }
    }


    public  void calcAdjMatrix()
    {
        try {
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            params.put("arg1", userId);
            params.put("arg2", dateVal);

            String url =  "http://192.168.0.241:5000/execute";
            client.post(url, params, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    showToast("Operation Failed!");
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    try {
                        File dir = new File(getApplicationContext().getExternalFilesDir(null).getAbsolutePath()+ File.separator +  "response");
                        dir.mkdirs();
                        File file = new File(dir, "resp.txt");
                        FileOutputStream output = new FileOutputStream(file);
                        PrintWriter writer = new PrintWriter(output);
                        writer.print(responseString);
                        writer.flush();
                        writer.close();
                        output.close();
                        showToast("Response stored at " + file.getAbsolutePath());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

//    public class UploadTask extends AsyncTask<String, String, String> {
//
//        @Override
//        protected String doInBackground(String... strings) {
//            try {
//                } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            return null;
//        }
//
//
//        @Override
//        protected void onProgressUpdate(String... text) {
//            Toast.makeText(getApplicationContext(), "In Background Task " + text[0], Toast.LENGTH_LONG).show();
//        }
//
//    }

}
