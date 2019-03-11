package com.example.orientation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StoreSensorValues {

    private List<Float[]> acc = new ArrayList();
    /*
    * Adds the accelerometer values to the list if the size of the list is less than 50.
    * If the list size is greater than 50, it calls the sendAccValues function
    * Input : Array of float consisting of accelerometer values
    * Returns: Returns the median of the accelerometer values
    * */
    public float[] addToAccList(Float[] a){
        float values[] = null;
        if(acc.size()<50){
            acc.add(a);
        }
        else{
            values = sendAccValues(acc);
            acc.clear();
        }

        return values;
    }
    /*
    * This function calculates the median of the raw accelerometer values
    * Input: List consisting raw accelerometer values
    * Return: Returns the median of the accelerometer values
    * */

    public float[] sendAccValues(List<Float[]> acc){
        float values[] = {0,0,0};
        Float x[] = new Float[50];
        Float y[] = new Float[50];
        Float z[] = new Float[50];
        for(int i=0; i<acc.size();i++){
            x[i] = acc.get(i)[0];
            y[i] = acc.get(i)[1];
            z[i] = acc.get(i)[2];
        }
        Arrays.sort(x);
        Arrays.sort(y);
        Arrays.sort(z);
        values[0] = x[25];
        values[1] = y[25];
        values[2] = z[25];

        return values;
    }

}
