package com.example.healthmonito;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class AccelSensorHandler extends Service implements SensorEventListener {

    private SensorManager accelManage;
    private Sensor senseAccel;
    float accelValuesX[] = new float[1000];
    float accelValuesY[] = new float[1000];
    float accelValuesZ[] = new float[1000];
    long limit = 30;
    int index = 0;
    boolean started = false;
    long time;
    Calendar init;
    Calendar close;
    ArrayList<Float> sensorDataY = new ArrayList<>();
    static final float ALPHA = 0.15f;

    public AccelSensorHandler() {
    }

    @Override
    public  int onStartCommand(Intent intent, int flags, int startId){
        limit = intent.getIntExtra("timeLimit",30);
        Toast.makeText(this, "Service Started. Recording for "+limit+" seconds", Toast.LENGTH_LONG).show();
        accelManage = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senseAccel = accelManage.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelManage.registerListener(this, senseAccel, SensorManager.SENSOR_DELAY_NORMAL);
        return START_STICKY;
    }
    @Override
    public void onCreate(){


    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // TODO Auto-generated method stub
        Sensor mySensor = sensorEvent.sensor;
        if(!started)
        {
            init = Calendar.getInstance();
            time = init.getTimeInMillis();
            started = true;
        }

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            index++;
//            accelValuesX[index] = sensorEvent.values[0];
//            accelValuesY[index] = sensorEvent.values[1];
//            accelValuesZ[index] = sensorEvent.values[2];
            sensorDataY.add(sensorEvent.values[1]);
            close = Calendar.getInstance();
            boolean isTime = (close.getTimeInMillis() - time)/1000 >limit;
            if(index >= 10000 || isTime){
                index = 0;
                accelManage.unregisterListener(this);
                try {
                    measureResp(sensorDataY);
//                    callMeasureRespRate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //accelManage.registerListener(this, senseAccel, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    private void measureResp(ArrayList<Float> sensorDataY) throws IOException {
        float[] yValues = new float[sensorDataY.size()];
        for(int i=0;  i<sensorDataY.size(); i++) {
            yValues[i] = sensorDataY.get(i);
        }
        float[] smoothData = Smoothen.smoothen(yValues);
        recordRespRate(smoothData);
        int i2 = 0;
        int windows;
        switch ((int) limit){
            case 15: windows=2;break;
            case 30: windows =2;break;
            case 45: windows=4;break;
            case 60: windows=4;break;
            default:windows = 2;
        }
        int windowSize = smoothData.length/windows;
        int[] rates = new int[windows];
        for(int i=0;i<smoothData.length;i=i+(windowSize))
        {

            float[] yVals = new float[(windowSize)];
            int indx = 0;
            for(int j=i;j<i+(windowSize);j++){
                if(j>=smoothData.length)
                {
                    break;
                }
                yVals[indx] = smoothData[j];
                indx++;
            }
//            float[] smoothData = Smoothen.smoothen(yVals);
            if(i2>=windows){
                break;
            }
            rates[i2] = measureRate2(yVals);
            i2++;
        }
        int rate = median(rates);
        int realRate = (int)((double)60/limit*(rate*windows));
        sendDataToActivity(realRate);
    }
    static int median(int[] values) {
        Arrays.sort(values);
        int median;
        int totalElements = values.length;
        if (totalElements % 2 == 0) {
            int sumOfMiddleElements = values[totalElements / 2] +
                    values[(totalElements / 2) - 1];
            median = ((int) sumOfMiddleElements) / 2;
        } else {
            median = (int) values[values.length / 2];
        }
        return median;
    }

    private void recordRespRate(float[] smoothData) throws IOException {
        Toast.makeText(AccelSensorHandler.this, "Started Writing File", Toast.LENGTH_LONG).show();

        File output = new File(getApplicationContext().getExternalFilesDir(null).getAbsolutePath()+"/CSVBreathe.csv");
        FileWriter dataOutput = null;
        try {
            dataOutput = new FileWriter(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
        dataOutput.append(init.getTime().toString()+","+close.getTime().toString());
        dataOutput.append("x,y,z\n");

        for(int i=0;  i<sensorDataY.size(); i++) {
            try {
                dataOutput.append(sensorDataY.get(i)+",");
                dataOutput.append(smoothData[i]+"\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            dataOutput.flush();
            dataOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(AccelSensorHandler.this, "Done Writing", Toast.LENGTH_LONG).show();

    }

    private void sendDataToActivity(int rate)
    {
        Intent sendLevel = new Intent();
        sendLevel.setAction("GET_RESP_DATA");
        sendLevel.putExtra( "resprate",rate);
        sendBroadcast(sendLevel);

    }
    private int measureRate2(float[] z){
        int rate = 0;
        float ca=0;
        float cb = 0;
        float minWidth = (float) 0.052;
        for(int i = 0; i<z.length-1;i++){
            if(z[i+1]<=z[i])
            {
                ca = ca==0?z[i]:ca;
            }
            else if(ca!=0){
                cb = z[i];
                if((ca-cb)>minWidth){
                    rate++;
                    ca = 0;
                }
                else
                {
                    ca=0;
                }
            }

        }
        return rate;
    }
    private int measureRate(float[] z){
//        int rate = 0;
        float maxZ =z[0];
        float minZ=10000;
        float offset = 0;
        int upperPeaks =0;
        int peakIndex=0;
        for (int i=0;i<z.length-1;i++){
            if(z[i]!=0&&z[i+1]!=0){

                if(z[i+1]>maxZ){
                    maxZ=z[i+1];
                }
                if(z[i+1]<minZ){
                    minZ=z[i+1];
                }

            }
        }
        offset = (maxZ - minZ)*25/100;
        float upperThreshold = maxZ - offset;
        float lowerThreshold = minZ + offset;
        for (int i=0;i<z.length-1;i++){
            if(z[i]>=upperThreshold)
            {
                if(peakIndex!=0 && i-peakIndex<=15)
                {
                    peakIndex = i;
                    continue;
                }
                else
                {
                    peakIndex = i;
                    upperPeaks++;
                }
            }
        }
        return  upperPeaks;
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
