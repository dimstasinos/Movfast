package com.ceid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Chronometer;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ceid.Network.ApiClient;
import com.ceid.Network.ApiService;
import com.ceid.Network.jsonStringParser;
import com.ceid.model.service.TaxiService;
import com.ceid.model.users.Customer;
import com.ceid.model.users.Points;
import com.ceid.model.users.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaxiRideScreen extends AppCompatActivity {

    private Chronometer timer;
    private Handler handlerPickUp = new Handler();
    private Handler handlerStatus;

    private Runnable taxiRideCheck;
    private Runnable taxiPickUp;
    private TaxiService taxiService;
    private ApiService api= ApiClient.getApiService();
    private Customer customer;
    private double cost;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_ride_screen);

        //Disable back button in this screen
        //=================================================================================
        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        dispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                //do nothing stay on the same screen
            }
        });

        //Get customer and taxi service data
        //=================================================================================
        Intent intent = getIntent();
        customer = (Customer) User.getCurrentUser();
        taxiService = (TaxiService) intent.getSerializableExtra("taxiService");


        //Check is the customer is picked every 2 sec
        //=================================================================================
        taxiPickUp = new Runnable() {
            @Override
            public void run() {

                isPickUp();

                handlerPickUp.postDelayed(this, 2000);
            }
        };

        handlerPickUp.postDelayed(taxiPickUp, 2000);



    }


    //Check is the customer is picked
    //=================================================================================
    public void isPickUp() {
        List<Map<String,Object>> values = new ArrayList<>();
        java.util.Map<String, Object> taxiPickUncheck = new LinkedHashMap<>();
        taxiPickUncheck.put("request_id_in",taxiService.getId());
        values.add(taxiPickUncheck);

        String jsonString = jsonStringParser.createJsonString("checkTaxiPickUp",values);
        Call<ResponseBody> call = api.callProcedure(jsonString);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                if(response.isSuccessful()){

                    try {
                        boolean status = jsonStringParser.getbooleanFromJson(response);

                        if(status){

                            //Ride start
                            //=================================================================================
                            handlerPickUp.removeCallbacks(taxiPickUp);
                            handlerStatus = new Handler();
                            timer = findViewById(R.id.timer);
                            timer.setBase(SystemClock.elapsedRealtime());
                            timer.start();

                            //Check if ride have finished every 2 sec
                            //=================================================================================
                            taxiRideCheck = new Runnable() {
                                @Override
                                public void run() {
                                    rideStatus();
                                    handlerStatus.postDelayed(this, 2000);
                                }
                            };

                            handlerStatus.postDelayed(taxiRideCheck, 2000);

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
    }

    //Check if ride have finished
    //=================================================================================
    public void rideStatus(){
        List<Map<String,Object>> values = new ArrayList<>();
        java.util.Map<String, Object> taxiComplete = new LinkedHashMap<>();
        taxiComplete.put("service_id",taxiService.getId());
        values.add(taxiComplete);

        String jsonString = jsonStringParser.createJsonString("checkTaxiComplete",values);
        Call<ResponseBody> call = api.callProcedure(jsonString);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                if(response.isSuccessful()){

                    try {
                        boolean status = jsonStringParser.getbooleanFromJson(response);
                        if(status) {
                            timer.stop();
                            handlerStatus.removeCallbacks(taxiRideCheck);

                            //Retrieve Ride cost
                            //=================================================================================
                            rideCost();
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
    }

    //Retrieve ride cost from database
    //=================================================================================
    public void rideCost(){
        List<Map<String,Object>> values = new ArrayList<>();
        Map<String, Object> paymentCheck = new LinkedHashMap<>();
        paymentCheck.put("service_id",taxiService.getId());
        values.add(paymentCheck);
        String jsonString = jsonStringParser.createJsonString("getPayment",values);

        Call<ResponseBody> call = api.callProcedure(jsonString);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    try {

                        //Get payment method
                        //=================================================================================
                        ArrayList<String> responseArray = jsonStringParser.getResults(response);
                        cost = Double.parseDouble(responseArray.get(0));
                        String payment = String.valueOf(taxiService.getPayment().getMethod());

                        if (payment.equals("CASH")) {
                            Intent intent = new Intent(TaxiRideScreen.this, MainScreen.class);
                            startActivity(intent);
                            finish();
                        } else {

                            //Get customer wallet balance, calculate points, update points and balance
                            //=================================================================================
                            double balance = customer.getWallet().getBalance();
                            if (balance > cost) {
                                customer.getWallet().withdraw(cost);

                                int points = Points.calculatePoints(cost);
                                customer.addPoints(points);
                                taxiService.addPoints(points);

                                int new_points = customer.getPoints().getPoints();

                                List<Map<String, Object>> values = new ArrayList<>();
                                java.util.Map<String, Object> updatePoints = new LinkedHashMap<>();
                                updatePoints.put("service_id", taxiService.getId());
                                updatePoints.put("points", points);
                                updatePoints.put("username", customer.getUsername());
                                updatePoints.put("newPoints", new_points);
                                values.add(updatePoints);

                                String jsonString = jsonStringParser.createJsonString("updatePoints", values);
                                Call<ResponseBody> call_points = api.callProcedure(jsonString);

                                call_points.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                        List<Map<String, Object>> values = new ArrayList<>();
                                        java.util.Map<String, Object> updateWallet = new LinkedHashMap<>();
                                        updateWallet.put("username", customer.getUsername());
                                        updateWallet.put("balance", customer.getWallet().getBalance());
                                        values.add(updateWallet);

                                        String jsonString = jsonStringParser.createJsonString("updateWallet", values);
                                        Call<ResponseBody> call_wallet = api.callProcedure(jsonString);

                                        call_wallet.enqueue(new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                                Intent intent = new Intent(TaxiRideScreen.this, MainScreen.class);
                                                startActivity(intent);
                                                finish();
                                            }

                                            @Override
                                            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {

                                            }
                                        });


                                    }

                                    @Override
                                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {

                                    }
                                });

                            } else {
                                customer.getWallet().withdraw(cost);

                                List<Map<String, Object>> values = new ArrayList<>();
                                java.util.Map<String, Object> updateWallet = new LinkedHashMap<>();
                                updateWallet.put("username", customer.getUsername());
                                updateWallet.put("balance", customer.getWallet().getBalance());
                                values.add(updateWallet);

                                String jsonString = jsonStringParser.createJsonString("updateWallet", values);
                                Call<ResponseBody> call_wallet = api.callProcedure(jsonString);

                                call_wallet.enqueue(new Callback<ResponseBody>() {
                                 @Override
                                 public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                     Toast.makeText(getApplicationContext(), "Your wallet balance is negative", Toast.LENGTH_SHORT).show();
                                     Intent intent = new Intent(TaxiRideScreen.this, MainScreen.class);
                                     startActivity(intent);
                                     finish();
                                 }
                                 @Override
                                 public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {}
                                });


                            }
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

            }
        });



    }


    @Override
    protected void onStop() {
        super.onStop();
       handlerPickUp.removeCallbacks(taxiPickUp);
       handlerStatus.removeCallbacks(taxiRideCheck);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handlerPickUp.removeCallbacks(taxiPickUp);
        handlerStatus.removeCallbacks(taxiRideCheck);
    }

}
