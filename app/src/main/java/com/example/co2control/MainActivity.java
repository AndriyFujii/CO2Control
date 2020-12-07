package com.example.co2control;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity
{
    EditText etCO2Threshold;

    Switch switchExhauster;
    Switch switchWindow;
    Switch switchAutomatic;
    boolean boolExhauster;
    boolean boolWindow;
    boolean boolAutomatic;

    Button bSave;

    Sensor sensor;
    int sensorID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bSave = findViewById(R.id.bSave);
        etCO2Threshold = findViewById(R.id.etCO2Threshold);
        switchExhauster = findViewById(R.id.switchExhauster);
        switchWindow = findViewById(R.id.switchWindow);
        switchAutomatic = findViewById(R.id.switchAutomatic);

        sensor = new Sensor();
        sensorID = 1;

        switchExhauster.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                    boolExhauster = true;
                else
                    boolExhauster = false;
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
    }

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

    public void onClickSave(View v)
    {
        String value = etCO2Threshold.getText().toString();

        boolean error = false;
        if(isEmpty(etCO2Threshold))
            error = true;

        if(!error)
        {
            int CO2Threshold = Integer.parseInt(value);

            sensor.setCO2Threshold(CO2Threshold);
            sensor.setBoolExhauster(boolExhauster);
            sensor.setBoolWindow(boolWindow);
            sensor.setBoolAutomatic(boolAutomatic);
            // Write a message to the database
            /*FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference(String.valueOf(sensorID));*/
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("Sensor");
            myRef.child(String.valueOf(sensorID)).setValue(sensor);

            Toast.makeText(MainActivity.this, "Saved successfully!", Toast.LENGTH_SHORT).show();
        }
    }
}