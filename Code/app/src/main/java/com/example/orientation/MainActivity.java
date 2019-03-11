package com.example.orientation;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    private static final String TAG = "MainActivity";
    private SensorManager mSensorManager;
    private Sensor accSensor, gyroSensor, magSensor;
    private double apitch, aroll, ayaw, mpitch, myaw, mroll, gpitch = 0, groll = 0, gyaw = 0;
    private float yawFused=0, rollFused=0, pitchFused=0;
    TextView yaw, pitch, roll, y, p, r;
    private ImageView image;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private static final double rad_degree = 180/Math.PI;
    private float timestamp=0;
    private float accelerationValues[] = new float[3];
    private float magValues[] = new float[3];
    private float[] gyroValues = {0,0,0};
    private Button reset, start, stop;
    private float dT;
    StoreSensorValues s;
    private FrameLayout frame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*
        * All the initializations happen in the function
        * */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        yaw = (TextView)findViewById(R.id.yaw);
        pitch = (TextView)findViewById(R.id.pitch);
        roll = (TextView)findViewById(R.id.roll);
        y = (TextView)findViewById(R.id.y);
        p = (TextView)findViewById(R.id.p);
        r = (TextView)findViewById(R.id.r);

        image = (ImageView)findViewById(R.id.imageView1);
        reset = (Button)findViewById(R.id.reset);
        start = (Button)findViewById(R.id.start);
        stop = (Button)findViewById(R.id.stop);

        reset.setOnClickListener(this);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);

        s = new StoreSensorValues();
    }


    @Override
    public void onSensorChanged(SensorEvent event){
    /*
    * This functuon calculates the magnetometer, gyroscope and accelerometer values
    * Calculates the fused angles
    * Rotates image
    * */
        Sensor sensor = event.sensor;
        switch(sensor.getType()){

            case Sensor.TYPE_ACCELEROMETER:
                accelerationValues = event.values;
                Float[] acc = new Float[3];
                acc[0] = accelerationValues[0];
                acc[1] = accelerationValues[1];
                acc[2] = accelerationValues[2];
                accelerationValues = s.addToAccList(acc);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                myaw = event.values[2];
                mpitch = event.values[1];
                mroll = event.values[0];
                magValues = event.values;
                break;

            case Sensor.TYPE_GYROSCOPE:
                gyroValues = event.values.clone();
                    dT = (event.timestamp - timestamp) * NS2S;
                    if (timestamp != 0) {
                        gyaw = (gyaw - gyroValues[2] * dT * rad_degree) % 360;
                        gpitch = (gpitch - gyroValues[0] * dT * rad_degree) % 360;
                        groll = (groll + gyroValues[1] * dT * rad_degree) % 360;
                    }
                    timestamp = event.timestamp;

                break;
        }

        calculateAccelerometerAngles();
        complimentaryFilter();
        rotateImage();
        displayValues();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void complimentaryFilter(){
        /*
         * Complimetary filter to fuse the accelerometer and gyroscope values
         * */
        yawFused = (float) (0.98*(gyaw) + 0.02*ayaw);
        pitchFused = (float) (0.98*(gpitch) + 0.02*apitch);
        rollFused = (float) (0.98*groll + 0.02*aroll);
    }


    private void calculateAccelerometerAngles(){
        /*
         * Calculates the roll and pitch angles in degrees using the raw accelerometer values
         * Calculates the yaw angles in degrees using magnetometer and accelerometer values
         * */
        if(accelerationValues!=null) {
            aroll = Math.atan2(-accelerationValues[0], Math.sqrt(accelerationValues[1] * accelerationValues[1] + accelerationValues[2] * accelerationValues[2]));
            apitch = Math.atan2(accelerationValues[1], Math.sqrt(accelerationValues[0] * accelerationValues[0] + accelerationValues[2] * accelerationValues[2]));

            double y = magValues[2] * Math.sin(aroll) - magValues[0] * Math.cos(aroll);
            double x = magValues[0] * Math.cos(apitch) + magValues[1] * Math.sin(apitch)
                    * Math.sin(aroll) + magValues[2] * Math.sin(apitch) * Math.cos(aroll);
            ayaw = Math.atan2(-y, x);

            aroll = rad_degree * aroll;
            apitch = rad_degree * apitch;
            ayaw = rad_degree * ayaw;
        }
    }

    private void rotateImage(){
        /*
         * This function id used to rotate the image on X, Y and Z axis
         * */
        image.setRotationX((float) (pitchFused));
        image.setRotationY((float) (rollFused));
        image.setRotation((float) (yawFused));
    }


    private void displayValues(){
        /*
         * This function displays the fused yaw pitch and roll values on the screen
         * */
        yaw.setText(String.valueOf(yawFused));
        pitch.setText(String.valueOf(pitchFused));
        roll.setText(String.valueOf(rollFused));

    }

    @Override
    public void onClick(View v) {
        /*
        * This method describes the events that happen when a person clicks on the particular button*/
        switch (v.getId()){

            case R.id.reset:
                /*
                * Resets all the angles to zero when the person clicks on the reset button
                * */
                gpitch = 0;
                gyaw = 0;
                groll = 0;
                apitch=0;
                aroll=0;
                ayaw=0;
                mpitch=0;
                myaw=0;
                mroll=0;
                yawFused=0;
                pitchFused=0;
                rollFused=0;
                displayValues();
                break;

            case R.id.start:
                /*
                * Registers all the sensors and starts their readings when the person clicks on the start button
                * */
                mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                accSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mSensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
                gyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                mSensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
                magSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                mSensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_NORMAL);
                break;

            case R.id.stop:
                /*
                * Unregisters all the sensors
                * */
                mSensorManager.unregisterListener(this, accSensor);
                mSensorManager.unregisterListener(this, gyroSensor);
                mSensorManager.unregisterListener(this, magSensor);

        }
    }
}
