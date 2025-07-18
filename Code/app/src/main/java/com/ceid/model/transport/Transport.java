package com.ceid.model.transport;

import com.ceid.util.*;

import java.io.Serializable;

public abstract class Transport implements Serializable
{
    private int id;
    private String model;
    private String manufacturer;
    private String manuf_year;

    public Transport(int id, String model, String manufacturer, String manuf_year)
    {
        this.id = id;
        this.model = model;
        this.manufacturer = manufacturer;
        this.manuf_year = manuf_year;
    }

    public Transport()
    {
        this.id = -1;
        this.model = null;
        this.manufacturer = null;
        this.manuf_year = null;
    }

    public String getModel() {
        return model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getManufYear() {
        return manuf_year;
    }

    public int getId() {
        return id;
    }

    public String getTitle()
    {
        return String.format("%s %s (%s)", manufacturer, model, manuf_year);
    }
}
