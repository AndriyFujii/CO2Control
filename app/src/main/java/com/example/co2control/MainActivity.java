package com.example.co2control;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
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

public class MainActivity extends AppCompatActivity
{
    TextView tvCO2Value;

    EditText etCO2Threshold;
    EditText etSensorID;

    Switch switchFan;
    Switch switchWindow;
    Switch switchAutomatic;
    boolean boolFan;
    boolean boolWindow;
    boolean boolAutomatic;

    Button bSave;

    Sensor sensor;
    int sensorID;

    DatabaseReference sensorConfigRef;
    DatabaseReference sensorValuesRef;

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
    public void updateValues()
    {
        sensorConfigRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    String dbCO2Threshold = snapshot.child("co2Threshold").getValue().toString();
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etSensorID = findViewById(R.id.etSensorID);
        tvCO2Value = findViewById(R.id.tvCO2Value);
        etCO2Threshold = findViewById(R.id.etCO2Threshold);
        switchFan = findViewById(R.id.switchFan);
        switchWindow = findViewById(R.id.switchWindow);
        switchAutomatic = findViewById(R.id.switchAutomatic);
        bSave = findViewById(R.id.bSave);

        sensor = new Sensor();

        sensorID = Integer.parseInt(etSensorID.getText().toString());

        sensorConfigRef = FirebaseDatabase.getInstance().getReference().child("SensorConfigs").child(String.valueOf(sensorID));
        sensorValuesRef = FirebaseDatabase.getInstance().getReference().child("SensorValues");

        updateValues();

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
                        sensorConfigRef = FirebaseDatabase.getInstance().getReference().child("SensorConfigs").child(String.valueOf(sensorID));
                        updateValues();
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
                    String dbCO2Value = snapshot.child(String.valueOf(sensorID)).child("co2Value").getValue().toString();

                    tvCO2Value.setText(dbCO2Value);
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
            int CO2Threshold = Integer.parseInt(value);

            // Write data to sensor class
            sensor.setCO2Threshold(CO2Threshold);
            sensor.setBoolFan(boolFan);
            sensor.setBoolWindow(boolWindow);
            sensor.setBoolAutomatic(boolAutomatic);

            // Write a message to the database
            sensorConfigRef.setValue(sensor);

            Toast.makeText(MainActivity.this, "Saved successfully!", Toast.LENGTH_SHORT).show();
        }
    }
}