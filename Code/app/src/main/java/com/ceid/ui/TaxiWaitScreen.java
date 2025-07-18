package com.ceid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ceid.Network.ApiClient;
import com.ceid.Network.ApiService;
import com.ceid.Network.jsonStringParser;
import com.ceid.model.service.TaxiService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaxiWaitScreen extends AppCompatActivity {

    private Handler handler;
    private boolean status;
    private Timer reservationTimer;
    ApiService api= ApiClient.getApiService();
    private TaxiService taxiService;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_wait_screen);

        //Disable back button in this screen
        //=================================================================================
        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        dispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                //do nothing stay on the same screen
            }
        });

        //Get Taxi service data
        //=================================================================================
        Intent intent = getIntent();
        taxiService = (TaxiService) intent.getSerializableExtra("taxiService");

        handler = new Handler();

        //Check if ride has begun
        //=================================================================================
        rideStatus();

        reservationTimer = new Timer();

        //Alert user after 20 sec if taxi has not been found
        //=================================================================================
        reservationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                cancelTaxiReservation();
                handler.removeCallbacks(taxiFoundCheck);

                runOnUiThread(() -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(TaxiWaitScreen.this);
                    builder.setMessage("Select a option");
                    builder.setTitle("Taxi was not found");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Resend", (alertDialog, which) -> {
                        resumeTaxiReservation();
                        Intent intent = new Intent(TaxiWaitScreen.this, TaxiWaitScreen.class);
                        intent.putExtra("taxiService",taxiService);
                        startActivity(intent);
                        finish();
                    });
                    builder.setNegativeButton("Cancel", (alertDialog, which) -> finish());

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                });
            }
        },20000);
    }

    //Check ride status
    //=================================================================================
    Runnable taxiFoundCheck = new Runnable() {
        @Override
        public void run() {

            List<Map<String,Object>> values = new ArrayList<>();
            Map<String, Object> taxiReservationCheck = new LinkedHashMap<>();
            taxiReservationCheck.put("checkTaxiReservation",taxiService.getId());
            values.add(taxiReservationCheck);

            String jsonString = jsonStringParser.createJsonString("checkTaxiReservation",values);

            Call<ResponseBody> call = api.callProcedure(jsonString);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                    if(response.isSuccessful()){
                        try {
                            status = jsonStringParser.getbooleanFromJson(response);

                            if(status) {
                                Intent intent = new Intent(TaxiWaitScreen.this, TaxiRideScreen.class);
                                intent.putExtra("taxiService",taxiService);
                                startActivity(intent);
                                finish();
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }else{
                        System.out.println("Error message");
                    }

                }
                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                    System.out.println("Error message");
                }
            });
            handler.postDelayed(this, 2000);
        }
    };

    //Cancel Taxi reservation
    //=================================================================================
    public void cancelTaxiReservation(){

        List<Map<String,Object>> values = new ArrayList<>();
        Map<String, Object> taxiReservationCancel = new LinkedHashMap<>();
        taxiReservationCancel.put("cancelService",taxiService.getId());
        values.add(taxiReservationCancel);
        String jsonString = jsonStringParser.createJsonString("cancelService",values);

        Call<ResponseBody> call = api.callProcedure(jsonString);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
            }
            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                System.out.println("Error message");
            }
        });
    }

    //Resume taxi reservation
    //=================================================================================
    public void resumeTaxiReservation(){

        List<Map<String,Object>> values = new ArrayList<>();
        Map<String, Object> taxiReservationCancel = new LinkedHashMap<>();
        taxiReservationCancel.put("resumeService",taxiService.getId());
        values.add(taxiReservationCancel);

        String jsonString = jsonStringParser.createJsonString("resumeService",values);

        Call<ResponseBody> call = api.callProcedure(jsonString);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
            }
            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                System.out.println("Error message");
            }
        });
    }

    public void rideStatus() {
        handler.post(taxiFoundCheck);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(taxiFoundCheck);
        reservationTimer.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(taxiFoundCheck);
        reservationTimer.cancel();
    }
}
