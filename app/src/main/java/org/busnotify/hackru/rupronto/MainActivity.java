package org.busnotify.hackru.rupronto;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;


public class MainActivity extends Activity {

    Spinner selectBusStopText;
    Spinner selectBusText;
    Button selectLeavingTimeText;
    Button selectTimeToStopText;
    ArrayList<String> stopsList = new ArrayList<>();


    public void setBusesList(ArrayList<String> busesList) {
        //String[] buses = new String[] { "A", "B", "LX" };
        ArrayAdapter<String> _busAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,busesList);

        selectBusText.setAdapter(_busAdapter);
    }

    public void setStopsList(ArrayList<String> stopsList) {
        //String[] stops = new String[] { "RSC", "Scott Hall", "Train Station" };
        ArrayAdapter<String> _stopAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,stopsList);

        selectBusStopText.setAdapter(_stopAdapter);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize UI components
        selectBusStopText = (Spinner) findViewById(R.id.selectBusStopText);
        selectBusText = (Spinner) findViewById(R.id.selectBusText);
        selectLeavingTimeText = (Button) findViewById(R.id.selectLeavingTimeText);
        selectTimeToStopText = (Button) findViewById(R.id.selectTimeToStopText);
        //Spinner dynamicSpinner = (Spinner) findViewById(R.id.selectBusText);



        //JSON Request to get buses and stops on app load
        String url = "http://runextbus.herokuapp.com/active";
        Log.e("RUPronto","Calling JSON");
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray busesJson = response.getJSONArray("routes");
                            JSONArray stopsJson = response.getJSONArray("stops");
                            Log.e("RUPronto-JSON",busesJson.toString());
                            ArrayList<String> buses = new ArrayList<>();
                            ArrayList<String> stops = new ArrayList<>();
                            for(int i=0;i<busesJson.length();i++){
                                buses.add(busesJson.get(i).toString());
                            }
                            for(int i=0;i<stopsJson.length();i++){
                                stops.add(stopsJson.get(i).toString());
                            }

                            setBusesList(buses);
                            setStopsList(stops);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.e("RUPronto", response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.e("RUPronto","Error JSON");

                    }
                });
        Log.e("RUPronto", "JSON Complete");

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(jsObjRequest);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void selectSource(View view) {
    }

    public void selectBus(View view) {
    }

    public void selectLeavingTime(View view) {
    }

    public void selectTimeToStop(View view) {
    }

    public void setReminder(View view) {
    }

}
