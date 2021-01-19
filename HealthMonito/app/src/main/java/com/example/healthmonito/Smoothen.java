package com.example.healthmonito;

import java.util.LinkedList;
import java.util.Queue;

public class Smoothen {
    private final Queue<Float> Dataset = new LinkedList<Float>();
    private final int period;
    private float sum;


    public Smoothen(int period)
    {
        this.period = period;
    }

    public void addData(float num)
    {
        sum += num;
        Dataset.add(num);


        if (Dataset.size() > period)
        {
            sum -= Dataset.remove();
        }
    }

    public float getMean()
    {
        return sum / period;
    }
    public static float[] smoothen(float[] input)
    {
        float[] output =new float[input.length];
        int per = 20;
        Smoothen obj = new Smoothen(per);
        for (int i = 0;i<input.length;i++) {
            obj.addData(input[i]);
            output[i]= (float) obj.getMean();
        }
        return output;
    }
}
