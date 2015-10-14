package org.busnotify.hackru.rupronto;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends Activity {

    Spinner selectBusStopText;
    Spinner selectBusText;
    Button selectLeavingTimeText;
    Button selectTimeToStopText;
    TextView fragmentHolder;
    HashMap<String,String> stopsIdMapping;

    //stores list of minutes for select bus and stop combination
    ArrayList<Integer> minutesList;

    //To get values on set reminder
    String selectedBus;
    String selectedStop;
    String busTiming;
    String timeToStop;

    public static final int NOTIFICATION_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize UI components
        selectBusStopText = (Spinner) findViewById(R.id.selectBusStopText);
        selectBusText = (Spinner) findViewById(R.id.selectBusText);
        selectLeavingTimeText = (Button) findViewById(R.id.selectLeavingTimeText);
        selectTimeToStopText = (Button) findViewById(R.id.selectTimeToStopText);
        fragmentHolder = (TextView) findViewById(R.id.fragmentHolder);
        //Spinner dynamicSpinner = (Spinner) findViewById(R.id.selectBusText);

        //Initialize minutesList
        minutesList = new ArrayList<Integer>();
        //Initialize stops
        populateStopsIdMapping();

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
                                String bus = busesJson.getJSONObject(i).get("title").toString();
                                buses.add(bus);
                            }
                            for(int i=0;i<stopsJson.length();i++){
                                String stop = stopsJson.getJSONObject(i).get("title").toString();
                                stops.add(stop);
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

    /*
     populate HashMap with stops and stopsId
     */
    private void populateStopsIdMapping() {
        stopsIdMapping = new HashMap<String,String>();
        String url = "http://runextbus.herokuapp.com/config";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject stops = response.getJSONObject("stops");
                            JSONArray stopsArray=stops.names();
                            for(int i=0;i<stopsArray.length();i++){

                                String id=stopsArray.getString(i);
                                String title = stops.getJSONObject(id).getString("title");

                                stopsIdMapping.put(title, id);

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.e("RUPronto","Error JSON");

                    }
                });

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


    /*
    set Buses List to display on UI
     */
    public void setBusesList(ArrayList<String> busesList) {
        //String[] buses = new String[] { "A", "B", "LX" };
        ArrayAdapter<String> _busAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,busesList);

        selectBusText.setAdapter(_busAdapter);
    }

    /*
    set Stops list to display on UI
     */
    public void setStopsList(ArrayList<String> stopsList) {
        //String[] stops = new String[] { "RSC", "Scott Hall", "Train Station" };
        ArrayAdapter<String> _stopAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,stopsList);

        selectBusStopText.setAdapter(_stopAdapter);
    }


    /*
    get arrival Timings for the selected route and stop id, called upon set reminder because of async calling. setReminder() will be called once time field is set
     */
    public void getTiming(View view) {

        String stopId = null;
        Log.e("RUPronto","Entering getTiming()");
        //Get the data from all the fields
        if(selectBusText!=null) {
            selectedBus = selectBusText.getSelectedItem().toString();
            Log.e("RUPronto", "The selected bus is: " + selectedBus);
        }

        if(selectBusStopText!=null) {
            selectedStop = selectBusStopText.getSelectedItem().toString();
            Log.e("RUPronto", "The selected stop is: " + selectedStop);
            stopId = stopsIdMapping.get(selectedStop);
        }

        if(selectLeavingTimeText!=null) {
            busTiming = selectLeavingTimeText.getText().toString();
            Log.e("RUPronto", "The time to catch bus is: " + busTiming);
        }

        if(selectTimeToStopText!=null) {
            timeToStop = selectTimeToStopText.getText().toString();
            Log.e("RUPronto", "The time to bus stop is: " + timeToStop);
        }

        if(stopId != null) {
            //JSON Request to get buses and stops on app load
            String url = "http://runextbus.herokuapp.com/stop/" + stopId;
            Log.e("RUPronto", "Calling JSON getTiming for URL " + url);
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                    (url, new Response.Listener<JSONArray>() {

                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                Log.e("RUPronto", "Inside Response");
                                //Log.e("RUPronto","I am testing this: "+titleJson.toString());
                                for (int i = 0; i < response.length(); i++) {
                                    String title = response.getJSONObject(i).get("title").toString();
                                    Log.e("RUPronto", title);
                                    if (title.equals(selectedBus)) {
                                        JSONArray predictionTimes = response.getJSONObject(i).getJSONArray("predictions");
                                        for (int j = 0; j < predictionTimes.length(); j++) {
                                            String minutes = predictionTimes.getJSONObject(j).get("minutes").toString();
                                            minutesList.add(Integer.parseInt(minutes));
                                            Log.e("RUPronto", "Adding minutes" + minutes);
                                        }
                                        setReminder();
                                        break;
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.e("RUPronto", response.toString());
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO Auto-generated method stub
                            Log.e("RUPronto", "Error JSON");

                        }
                    });
            Log.e("RUPronto", "JSON Complete");
            RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
            requestQueue.add(jsonArrayRequest);
        }
        else{
            Toast.makeText(MainActivity.this, "Bus Stop not selected!", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    set reminder called upon set reminder button click to set reminder
     */
    public void setReminder() {
        Log.e("RUPronto","Entering setReminder with value: "+busTiming.toString());
        DateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            Date date = sdf.parse(busTiming);
            String currentDate = sdf.format(new Date());
            Date currDate = sdf.parse(currentDate);
            long minDiff = (date.getTime() - currDate.getTime()) / (60 * 1000);

            // Finding minimum difference using extra storage.. Urgh!
            ArrayList<Integer> minDiffList = new ArrayList<Integer>();
            for (int i = 0; i < minutesList.size(); i++) {
                int dist = minutesList.get(i) - (int)minDiff;
                if (dist > 0)
                    minDiffList.add(dist);
            }

            int reminderMinutes = Collections.min(minDiffList);
            //TODO: Invoke timer here for reminder saying that there is a bus in (Time to Stop) minutes

            Log.e("RUPronto", "Time diff between selected time and current time in min is: " + date.toString() + " " + currentDate.toString() + " " + minDiff);
            Log.e("RUPronto", "The reminder time: "+reminderMinutes);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //Iterate through time and select closest time to departure to select which bus I want
        for(int i = 0; i < minutesList.size(); i++){
            Log.e("RUPronto","Looping through minutes"+minutesList.get(i));
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());



    }

    /*
    select time that you plan to leave from the bus stop
     */
    public void selectLeavingTime(View view) {

        DialogFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.show(getFragmentManager(), "SELECT TIME");
    }

    /*
    select how long it takes for you to walk to your stop
     */
    public void selectTimeToStop(View view) {

        NumberPickerFragment numberPickerFragment = new NumberPickerFragment();
        numberPickerFragment.show(getFragmentManager(), "TIME TO STOP");

    }


    /*
    Time to stop selection
    */
    public static class NumberPickerFragment extends DialogFragment{

        Context context;
        Button timeToStop;
        NumberPicker numberPicker;
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){

            // make dialog object
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            numberPicker = new NumberPicker(getActivity());
            numberPicker.setEnabled(true);
            numberPicker.setMaxValue(60);
            numberPicker.setMinValue(1);

            timeToStop = (Button) getActivity().findViewById(R.id.selectTimeToStopText);
            Log.e("RUPronto", timeToStop.getText().toString());

            if(!timeToStop.getText().toString().equals("choose one.."))
            {
                numberPicker.setValue(Integer.parseInt(timeToStop.getText().toString()));
            }

            final FrameLayout parent = new FrameLayout(getActivity());
            parent.addView(numberPicker, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER));

            builder.setView(parent);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    timeToStop.setText(String.valueOf(numberPicker.getValue()));
                    Toast.makeText(getActivity().getBaseContext(), "The time to stop is set!", Toast.LENGTH_SHORT).show();
                }
            });

            // create the dialog from the builder then show
            return builder.create();
        }
    }

    /*
    Leaving time selection
     */
    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

        int hour;
        int minute;
        Button leaveTime;
        TextView fragmentHolder;
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();

            leaveTime = (Button) getActivity().findViewById(R.id.selectLeavingTimeText);
            fragmentHolder = (TextView) getActivity().findViewById(R.id.fragmentHolder);

            if(fragmentHolder.getText()=="") {
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
            }
            else {
                String s = fragmentHolder.getText().toString();
                hour = Integer.parseInt(s.split(":")[0]);
                minute = Integer.parseInt(s.split(":")[1]);
            }

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    android.text.format.DateFormat.is24HourFormat(getActivity()));
        }

        /*
        what happens on time set
         */
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            Toast.makeText(getActivity(), "Time Selected!", Toast.LENGTH_SHORT).show();
            String s = String.valueOf(hourOfDay)+ ":" + String.valueOf(minute);
            leaveTime.setText(s);
            fragmentHolder.setText(""+hourOfDay+":"+minute);
        }


    }

}
