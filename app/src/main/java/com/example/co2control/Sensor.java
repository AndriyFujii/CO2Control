package com.example.co2control;

public class Sensor
{
    private int CO2Threshold;
    private boolean boolExhauster;
    private boolean boolWindow;
    private boolean boolAutomatic;

    public Sensor()
    {

    }

    public int getCO2Threshold()
    {
        return CO2Threshold;
    }

    public void setCO2Threshold(int CO2Threshold)
    {
        this.CO2Threshold = CO2Threshold;
    }

    public boolean isBoolExhauster()
    {
        return boolExhauster;
    }

    public void setBoolExhauster(boolean boolExhauster)
    {
        this.boolExhauster = boolExhauster;
    }

    public boolean isBoolWindow() {
        return boolWindow;
    }

    public void setBoolWindow(boolean boolWindow)
    {
        this.boolWindow = boolWindow;
    }

    public boolean isBoolAutomatic()
    {
        return boolAutomatic;
    }

    public void setBoolAutomatic(boolean boolAutomatic)
    {
        this.boolAutomatic = boolAutomatic;
    }
}

