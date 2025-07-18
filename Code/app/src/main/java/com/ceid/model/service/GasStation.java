package com.ceid.model.service;

import androidx.annotation.NonNull;

import com.ceid.util.Coordinates;

import java.io.Serializable;

public class GasStation implements Serializable
{
	private int id;
	private Coordinates coords;
	private double gasPrice;

	public GasStation(int id, Coordinates coords, double gasPrice)
	{
		this.id = id;
		this.coords = coords;
		this.gasPrice = gasPrice;
	}

	public int getid()
	{
		return id;
	}

	public Coordinates getCoords()
	{
		return coords;
	}

	public double getGasPrice()
	{
		return gasPrice;
	}

	public double calculateGasPrice(int gas)
	{
		return gasPrice*gas;
	}

	@NonNull
	public String toString()
	{
		return String.format("===================================================\nID: %d\nCoords: (%f, %f)\nPrice: %f\n===================================================", id, coords.getLat(), coords.getLng(), gasPrice);
	}

}
