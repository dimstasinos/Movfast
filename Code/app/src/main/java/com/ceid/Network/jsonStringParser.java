package com.ceid.Network;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ceid.model.payment_methods.Card;
import com.ceid.model.payment_methods.Payment;
import com.ceid.model.payment_methods.Wallet;
import com.ceid.model.service.GasStation;
import com.ceid.model.service.TaxiRequest;
import com.ceid.model.transport.SpecializedTracker;
import com.ceid.model.transport.Taxi;
import com.ceid.model.transport.VehicleTracker;
import com.ceid.model.users.Customer;
import com.ceid.model.users.TaxiDriver;
import com.ceid.model.users.User;
import com.ceid.util.Coordinates;
import com.ceid.util.PositiveInteger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Response;

public class jsonStringParser {

    public static JsonNode parseJson(String str)
    {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = null;

        try
        {
            jsonNode = mapper.readTree(str);
        }
        catch(JsonProcessingException ignored){}

        return jsonNode;
    }

    public static User parseJson(JsonNode data) throws IOException {
        User user=null;
        JsonNode userNode = data.get(0).get(0);
        String checker=userNode.get("type").asText();

        if(checker.equals("customer")) {

            Wallet wallet = new Wallet(userNode.get("wallet_balance").asDouble());

            // Extract card information
            JsonNode cardsNode = data.get(1);
            for (JsonNode card : cardsNode) {
                Card newCard = new Card(
                        card.get("card_number").asText(),
                        card.get("card_holder").asText(),
                        card.get("expiration_date").asText(),
                        card.get("cvv").asText()
                );
                wallet.addPaymentMethod(newCard); // Assuming you have a method to add a card to the wallet
            }

            // Extract customer information
            byte[] img = userNode.get("img").binaryValue(); // Get binary data directly

            return new Customer(
                    userNode.get("cus_username").asText(),
                    userNode.get("cus_password").asText(),
                    userNode.get("cus_name").asText(),
                    userNode.get("cus_lname").asText(),
                    userNode.get("email").asText(),
                    img,
                    wallet,
                    userNode.get("cus_licence").asText(),

                    userNode.get("cus_points").asInt()
            );
        }
        else{
            JsonNode taxiNode = data.get(0).get(0);
            Coordinates coords;

            Wallet wallet = new Wallet(taxiNode.get("wallet_balance").asDouble());

            // Extract card information
            JsonNode cardsNode = data.get(1);
            for (JsonNode card : cardsNode) {
                Card newCard = new Card(
                        card.get("card_number").asText(),
                        card.get("card_holder").asText(),
                        card.get("expiration_date").asText(),
                        card.get("cvv").asText()
                );
                wallet.addPaymentMethod(newCard); // Assuming you have a method to add a card to the wallet
            }

            Taxi taxi = new Taxi(
                    taxiNode.get("taxi_id").asInt(),
                    taxiNode.get("taxi_model").asText(),
                    taxiNode.get("taxi_year").asText(),
                    taxiNode.get("manuf").asText(),
                    taxiNode.get("licensePlate").asText(),
                    coords= new Coordinates(taxiNode.get("lat").asDouble(),taxiNode.get("lng").asDouble())
            );


            return new TaxiDriver(
                    taxiNode.get("username").asText(),
                    taxiNode.get("password").asText(),
                    taxiNode.get("name").asText(),
                    taxiNode.get("lname").asText(),
                    taxiNode.get("email").asText(),
                    wallet,
                    Boolean.parseBoolean(taxiNode.get("taxi_status").asText()),
                    taxi
            );

        }

    }

    public static boolean getbooleanFromJson(@NonNull Response<ResponseBody> response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.body().string());
        String booleanString=jsonNode.get(0).get(0).get("result").asText();

        return Boolean.parseBoolean(booleanString);
    }

    public static ArrayList<String> getResults(@NonNull Response<ResponseBody> response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.body().string());

        ArrayList<String> list=new ArrayList<>();
        JsonNode root=jsonNode.get(0).get(0);

        for(JsonNode node: root){
            list.add(node.asText());
        }

        return list;
    }


    public static void printJsonArray(JsonArray jsonArray) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(jsonArray);
        Log.d("JsonArray", jsonString);
    }


    public static String createJsonString(String tableName, List<Map<String, Object>> values) {
        ObjectMapper mapper = new ObjectMapper();

        // Data to insert
        ArrayNode dataArray = mapper.createArrayNode();
        for (Map<String, Object> value : values) {
            ObjectNode jsonObject = mapper.createObjectNode();
            for (Map.Entry<String, Object> entry : value.entrySet()) {
                String key = entry.getKey();
                Object val = entry.getValue();

                // Explicitly cast values to their corresponding types
                if (val instanceof String) {
                    jsonObject.put(key, (String) val);
                } else if (val instanceof Integer) {
                    jsonObject.put(key, (Integer) val);
                } else if (val instanceof Long) {
                    jsonObject.put(key, (Long) val);
                } else if (val instanceof Double) {
                    jsonObject.put(key, (Double) val);
                } else if (val instanceof Boolean) {
                    jsonObject.put(key, (Boolean) val);

                }
                else if (val == null) {
                    // Handle null values
                    jsonObject.putNull(key);}
                else {
                    // Handle other types or convert to string
                    jsonObject.put(key, val.toString());
                }
            }
            dataArray.add(jsonObject);
        }

        // Create JSON object
        ObjectNode jsonObject = mapper.createObjectNode();
        jsonObject.put("table", tableName);
        jsonObject.set("values", dataArray);

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String createJsonString(List<Map<String, Object>> values) {
        ObjectMapper mapper = new ObjectMapper();

        // Data to insert
        ArrayNode dataArray = mapper.createArrayNode();
        for (Map<String, Object> value : values) {
            ObjectNode jsonObject = mapper.createObjectNode();
            for (Map.Entry<String, Object> entry : value.entrySet()) {
                String key = entry.getKey();
                Object val = entry.getValue();

                // Explicitly cast values to their corresponding types
                if (val instanceof String) {
                    jsonObject.put(key, (String) val);
                } else if (val instanceof Integer) {
                    jsonObject.put(key, (Integer) val);
                } else if (val instanceof Long) {
                    jsonObject.put(key, (Long) val);
                } else if (val instanceof Double) {
                    jsonObject.put(key, (Double) val);
                } else if (val instanceof Boolean) {
                    jsonObject.put(key, (Boolean) val);

                }
                else if (val == null) {
                    // Handle null values
                    jsonObject.putNull(key);}
                else {
                    // Handle other types or convert to string
                    jsonObject.put(key, val.toString());
                }
            }
            dataArray.add(jsonObject);
        }

        // Create JSON object
        ObjectNode jsonObject = mapper.createObjectNode();
        jsonObject.set("values", dataArray);

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int[] extractInsertIds(Response<ResponseBody> response) throws IOException{
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.body().string());
            JsonNode insertIdsNode = rootNode.get("insertIds");

            if (insertIdsNode != null && insertIdsNode.isArray() && insertIdsNode.size() > 0) {
                int[] insertIds = new int[insertIdsNode.size()];
                for (int i = 0; i < insertIdsNode.size(); i++) {
                    insertIds[i] = insertIdsNode.get(i).asInt();
                }
                return insertIds;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Return an empty array if insertIds are not found or if there's an error
        return new int[0];
    }

    public static <T> List<T> parseDataList(String response, Class<T> targetClass) throws JsonSyntaxException {
        if (response == null || response.isEmpty()) {
            return Collections.emptyList();
        }
        Gson gson = new Gson();
        Type type = TypeToken.getParameterized(List.class, targetClass).getType();
        return gson.fromJson(response, type);
    }

    public static ArrayList<TaxiRequest> parseTaxiRequest(Response<ResponseBody> response) throws IOException {
        ObjectMapper mapper=new ObjectMapper();
        JsonNode rootNode = mapper.readTree(response.body().string());
        ArrayList<TaxiRequest> requestList= new ArrayList<>();

        JsonNode arrayNode=rootNode;

        for(JsonNode node : arrayNode){

            JsonNode pickUpLocationNode=node.get("pickup_location");
            Coordinates pickUp= new Coordinates(pickUpLocationNode.get("x").asDouble(),pickUpLocationNode.get("y").asDouble());

            JsonNode destLocationNode=node.get("destination");
            Coordinates dest= new Coordinates(destLocationNode.get("x").asDouble(),destLocationNode.get("y").asDouble());
            Payment.Method method = Payment.setPaymentType(node.get("payment_method").asText());


            requestList.add( new TaxiRequest(
                    Integer.parseInt(node.get("id").asText()),
                    pickUp,
                    dest,
                    method
                    )
                    );

        }


        return requestList;


    }

    public static ArrayList<GasStation> parseGarage(Response<ResponseBody> response) throws IOException {
        ObjectMapper mapper=new ObjectMapper();
        JsonNode rootNode = mapper.readTree(response.body().string());
        ArrayList<GasStation> gasStationList= new ArrayList<>();

        for(JsonNode node:rootNode){

            JsonNode coordsJson=node.get("coords");

            Coordinates coords=new Coordinates(coordsJson.get("x").asDouble(),coordsJson.get("y").asDouble());

            gasStationList.add( new GasStation(
                            Integer.parseInt(node.get("id").asText()),
                            coords,
                            Double.parseDouble(node.get("gas_price").asText())

                    )
                    );
        }

        return gasStationList;
    }

    public static VehicleTracker parseTracker(Response<ResponseBody> response)throws IOException{
        ObjectMapper mapper=new ObjectMapper();
        JsonNode rootNode = mapper.readTree(response.body().string());
        VehicleTracker tracker=null;

        if(rootNode.get("type").asText().equals("simple")){
            JsonNode coordsJson=rootNode.get("coords");
            Coordinates coords=new Coordinates(coordsJson.get("x").asDouble(),coordsJson.get("y").asDouble());
            tracker= new VehicleTracker(
                    coords,
                    rootNode.get("distance").asDouble(),
                    rootNode.get("isStopped").asBoolean()


            );
        }
        else{
            JsonNode coordsJson=rootNode.get("coords");
            Coordinates coords=new Coordinates(coordsJson.get("x").asDouble(),coordsJson.get("y").asDouble());
            tracker= new SpecializedTracker(
                    coords,
                    rootNode.get("distance").asDouble(),
                    rootNode.get("isStopped").asBoolean(),
                    new PositiveInteger(rootNode.get("gas").asInt())

            );
        }

        return tracker;
    }

}
