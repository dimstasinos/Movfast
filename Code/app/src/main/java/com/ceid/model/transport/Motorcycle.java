package com.ceid.model.transport;
import androidx.annotation.NonNull;

import com.ceid.util.Coordinates;
import com.ceid.util.PositiveInteger;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;


public class Motorcycle extends Rental {

	//?
	private String license_plate;

	public Motorcycle(String license_plate, boolean freeStatus, int id, String model, String manufacturer, String manuf_year, double rate, Coordinates coords, PositiveInteger gas) {
		super(freeStatus, id, model, manufacturer, manuf_year, rate, new SpecializedTracker(coords, gas));

		this.license_plate = license_plate;
	}

	public Motorcycle()
	{
		super();
		this.license_plate = null;
	}

	public Motorcycle(JsonNode vehicleData)
	{
		super(
				true,
				vehicleData.get("id").asInt(),
				vehicleData.get("model").asText(),
				vehicleData.get("manufacturer").asText(),
				vehicleData.get("manuf_year").asText(),
				vehicleData.get("rate").asDouble(),
				new SpecializedTracker(
						new Coordinates(vehicleData.get("coords")),
						new PositiveInteger(vehicleData.get("gas_level").asInt())
				)
		);

		this.license_plate = vehicleData.get("license_plate").asText();
	}

	@Override
	public boolean requiresLicense()
	{
		return true;
	}

	@Override
	public boolean validLicense(String license)
	{
		return Objects.equals(license, "MOTORCYCLE") || Objects.equals(license, "BOTH");
	}

	public String getLicensePlate()
	{
		return license_plate;
	}

	@NonNull
	public String toString()
	{
		return String.format("=======================================\nType: Motorcycle\nID: %d\nLicense Plate: %s\nModel: %s %s (%s)\nRate: %f\nCoords: (%f, %f)\nGas: %d\n=======================================", this.getId(), this.license_plate, this.getManufacturer(), this.getModel(), this.getManufYear(), this.getRate(), this.getTracker().getCoords().getLat(), this.getTracker().getCoords().getLng(), ((SpecializedTracker)this.getTracker()).getGas().getValue());
	}
}
