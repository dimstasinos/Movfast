package com.ceid.model.transport;

public abstract class OutCityTransport extends Transport {

    private String licensePlate;
    private int seats;
    private double rate;
    //private SpecializedTracker tracker;

    public OutCityTransport(String license_plate, double rate, int seats, int id, String model, String manufacturer, String manuf_year) {
        super(id, model, manufacturer, manuf_year);
        this.licensePlate = license_plate;
        this.seats = seats;
        this.rate = rate;
    }

    public String getLicensePlate()
    {
        return licensePlate;
    }

    public int getSeats()
    {
        return seats;
    }

    public double getRate()
    {
        return rate;
    }

    //Parcelable
    //=========================================================================

    /*
    //Step1
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.license_plate);
        dest.writeInt(this.getId());
        dest.writeString(this.getModel());
        dest.writeString(this.getManufacturer());
        dest.writeString(this.getManufYear());
    }

    //Step2
    private OutCityTransport(Parcel in)
    {
        this(in.readString(), in.readInt(), in.readString(), in.readString(), in.readString());
    }

    //Step3
    public static final Parcelable.Creator<OutCityTransport> CREATOR = new Parcelable.Creator<OutCityTransport>() {
        @Override
        public OutCityTransport createFromParcel(Parcel source) {

            switch(source.readInt())
            {
                case 0: return new OutCityCar(source);
                default: return null;
            }
        }

        @Override
        public OutCityTransport[] newArray(int size) {
            return new OutCityTransport[size];
        }
    };

    //Step4
    @Override
    public int describeContents() {
        return 0;
    }

     */
}
