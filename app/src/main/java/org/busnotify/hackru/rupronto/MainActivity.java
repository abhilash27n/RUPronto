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

import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;


public class MainActivity extends Activity {

    Spinner selectBusStopText;
    Spinner selectBusText;
    Button selectLeavingTimeText;
    Button selectTimeToStopText;


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

        String[] buses = new String[] { "A", "B", "LX" };
        ArrayAdapter<String> _busAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,buses);

        selectBusText.setAdapter(_busAdapter);


        String[] stops = new String[] { "RSC", "Scott Hall", "Train Station" };
        ArrayAdapter<String> _stopAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,stops);

        selectBusStopText.setAdapter(_stopAdapter);


        String url = "http://runextbus.herokuapp.com/active";
        Log.e("RUPronto","Calling JSON");
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("RUPronto", response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.e("RUPronto","Error JSON");

                    }
                });
        Log.e("RUPronto","JSON Complete");

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
        Log.e("RUPRONTO", "Selecting bus...");
        CharSequence[] bus = {"LX","Wknd1"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.bus)
                .setItems(bus, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                    }
                });
        builder.show();
    }

    public void selectLeavingTime(View view) {
    }

    public void selectTimeToStop(View view) {
    }

    public void setReminder(View view) {
    }

}
