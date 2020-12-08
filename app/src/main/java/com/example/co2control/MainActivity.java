package com.example.co2control;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    private TextView tvCO2Value;
    private TextView tvPPM;

    private EditText etCO2Threshold;
    private EditText etSensorID;

    private Switch switchFan;
    private Switch switchWindow;
    private Switch switchAutomatic;
    private boolean boolFan;
    private boolean boolWindow;
    private boolean boolAutomatic;

    private Sensor sensor;
    private int sensorID;
    private int CO2Threshold;

    private DatabaseReference sensorConfigRef;
    private DatabaseReference sensorValuesRef;

    LineGraphSeries<DataPoint> series;
    GraphView valuesLinePlot;
    private ArrayList<Integer> valuesArray;
    private int x;

    //Verifies if the editText is empty and pops an error if it is
    //Receives the editText as parameter
    //Returns true if it's empty
    public boolean isEmpty(EditText et)
    {
        String validation = et.getText().toString();
        if(TextUtils.isEmpty(validation))
        {
            et.setError("Can't be empty");
            return true;
        }

        return false;
    }

    // Updates the app config values from the database
    public void updateConfigValues()
    {
        sensorConfigRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    String dbCO2Threshold = snapshot.child("co2Threshold").getValue().toString();
                    CO2Threshold = Integer.parseInt(dbCO2Threshold);
                    boolean dbBoolFan = Boolean.parseBoolean(snapshot.child("boolFan").getValue().toString());
                    boolean dbBoolWindow = Boolean.parseBoolean(snapshot.child("boolWindow").getValue().toString());
                    boolean dbBoolAutomatic = Boolean.parseBoolean(snapshot.child("boolAutomatic").getValue().toString());


                    etCO2Threshold.setText(dbCO2Threshold);
                    switchFan.setChecked(dbBoolFan);
                    switchWindow.setChecked(dbBoolWindow);
                    switchAutomatic.setChecked(dbBoolAutomatic);
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Sensor not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }

    // Updates the sensor CO2 values from the database
    public void updateSensorValues()
    {
        sensorValuesRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    String dbCO2Value = snapshot.child("co2Value").getValue().toString();

                   // tvCO2Value.setTextColor(Color.parseColor("#FF0000"));
                    tvCO2Value.setText(dbCO2Value);
                }
                else
                {
                    Toast.makeText(MainActivity.this,
                            "Sensor data not found!",
                            Toast.LENGTH_SHORT).show();

                    tvCO2Value.setText("----");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }

    // Hides the keyboard
    private void hideKeyboard(View view) {
        InputMethodManager manager = (InputMethodManager) view.getContext()
                .getSystemService(INPUT_METHOD_SERVICE);
        if (manager != null)
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }



    private void createLinePlot()
    {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etSensorID = findViewById(R.id.etSensorID);
        tvCO2Value = findViewById(R.id.tvCO2Value);
        tvPPM = findViewById(R.id.tvPPM);
        etCO2Threshold = findViewById(R.id.etCO2Threshold);
        switchFan = findViewById(R.id.switchFan);
        switchWindow = findViewById(R.id.switchWindow);
        switchAutomatic = findViewById(R.id.switchAutomatic);
        valuesLinePlot = findViewById(R.id.valuesPlot);
        valuesArray = new ArrayList<>();
        series = new LineGraphSeries<>();

        sensor = new Sensor();

        sensorID = Integer.parseInt(etSensorID.getText().toString());
        CO2Threshold = 9999;
        x = 0;
        valuesLinePlot.getViewport().setXAxisBoundsManual(true);


        sensorConfigRef = FirebaseDatabase.getInstance().getReference()
                .child("SensorConfigs")
                .child(String.valueOf(sensorID));
        sensorValuesRef = FirebaseDatabase.getInstance().getReference()
                .child("SensorValues")
                .child(String.valueOf(sensorID));

        updateConfigValues();

        // Updates the sensor ID value when ENTER is pressed, and closes the keyboard
        etSensorID.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if(actionId == EditorInfo.IME_ACTION_DONE)
                {
                    etSensorID.clearFocus();
                    hideKeyboard(v);
                }
                return false;
            }
        });

        // When the Sensor ID loses focus, update its value
        etSensorID.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if(!hasFocus)
                {
                    if(!isEmpty(etSensorID))
                    {
                        sensorID = Integer.parseInt(etSensorID.getText().toString());
                        sensorConfigRef = FirebaseDatabase.getInstance().getReference()
                                .child("SensorConfigs")
                                .child(String.valueOf(sensorID));
                        sensorValuesRef = FirebaseDatabase.getInstance().getReference()
                                .child("SensorValues")
                                .child(String.valueOf(sensorID));
                        updateConfigValues();
                        updateSensorValues();
                    }
                }
            }
        });

        // Updates the switch value when they're changed
        switchFan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                    boolFan = true;
                else
                    boolFan = false;
            }
        });
        switchWindow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                    boolWindow = true;
                else
                    boolWindow = false;
            }
        });
        switchAutomatic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                    boolAutomatic = true;
                else
                    boolAutomatic = false;
            }
        });

        // Updates the ppm value whenever it changes with the one saved in the firebase
        sensorValuesRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    String dbCO2Value = snapshot.child("co2Value").getValue().toString();

                    valuesLinePlot.getViewport().setMinX(x);
                    valuesLinePlot.getViewport().setMaxX(x+10);
                    series.appendData(new DataPoint(x, Integer.parseInt(dbCO2Value)), true, 100);
                    valuesLinePlot.addSeries(series);
                    x++;

                    tvCO2Value.setText(dbCO2Value);

                    valuesArray.add(Integer.parseInt(dbCO2Value));
                    if(Integer.parseInt(dbCO2Value) > CO2Threshold)
                    {
                        tvCO2Value.setTextColor(Color.parseColor("#FF0000"));
                        tvPPM.setTextColor(Color.parseColor("#FF0000"));
                    }
                    else
                    {
                        tvCO2Value.setTextColor(Color.parseColor("#000000"));
                        tvPPM.setTextColor(Color.parseColor("#000000"));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }

    public void onClickSave(View v)
    {
        String value = etCO2Threshold.getText().toString();

        boolean error = false;
        if(isEmpty(etCO2Threshold))
            error = true;
        if(isEmpty(etSensorID))
            error = true;

        if(!error)
        {
            CO2Threshold = Integer.parseInt(value);

            // Write data to sensor class
            sensor.setCO2Threshold(CO2Threshold);
            sensor.setBoolFan(boolFan);
            sensor.setBoolWindow(boolWindow);
            sensor.setBoolAutomatic(boolAutomatic);

            // Write a message to the database
            sensorConfigRef.setValue(sensor);

            Toast.makeText(MainActivity.this,
                    "Saved successfully!",
                    Toast.LENGTH_SHORT).show();
        }
    }
}