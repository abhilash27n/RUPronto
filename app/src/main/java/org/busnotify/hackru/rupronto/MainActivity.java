package org.busnotify.hackru.rupronto;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;

public class MainActivity extends Activity {

    //UI Items
    @BindView(R.id.selectBusStopText)
    Spinner selectBusStopText;

    @BindView(R.id.selectBusText)
    Spinner selectBusText;

    @BindView(R.id.selectLeavingTimeText)
    Button selectLeavingTimeText;

    @BindView(R.id.selectTimeToStopText)
    Button selectTimeToStopText;

    public static final String BUS_URL = "http://runextbus.herokuapp.com/active";
    public static final String CONFIG_URL = "http://runextbus.herokuapp.com/config";
    public static final int NOTIFICATION_ID = 1;
    public static final String TAG = "RUPronto";

    //Hashmap for sending request with stopId for selected stop
    HashMap<String,String> stopsIdMapping;

    //stores list of minutes for select bus and stop combination
    ArrayList<Integer> minutesList;

    //To get values on set reminder
    String selectedBus;
    String selectedStop;
    String busTiming;
    String timeToStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize minutesList
        minutesList = new ArrayList<>();
        //Initialize stops
        populateStopsIdMapping();

        //JSON Request to get buses and stops on app load
        Log.e(TAG,"Calling JSON");
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, BUS_URL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray busesJson = response.getJSONArray("routes");
                            JSONArray stopsJson = response.getJSONArray("stops");
                            Log.e(TAG, busesJson.toString());
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
                        Log.e(TAG, response.toString());
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG,"Error JSON");
                    }
                });
        Log.e(TAG, "JSON Complete");
        //Make request
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(jsObjRequest);
    }

    /*
     populate HashMap with stops and stopsId
     */
    private void populateStopsIdMapping() {
        stopsIdMapping = new HashMap<>();
        //Request for all stops to create hashmap with stops and stopid as key and value
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, CONFIG_URL, null, new Response.Listener<JSONObject>() {

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
                        Log.e(TAG,"Error JSON");
                    }
                });
        //Make request
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(jsObjRequest);
    }

    /*
   set Buses List to display on UI
    */
    public void setBusesList(ArrayList<String> busesList) {
        //String[] buses = new String[] { "A", "B", "LX" };
        ArrayAdapter<String> _busAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, busesList);

        selectBusText.setAdapter(_busAdapter);
    }

    /*
    set Stops list to display on UI
     */
    public void setStopsList(ArrayList<String> stopsList) {
        //String[] stops = new String[] { "RSC", "Scott Hall", "Train Station" };
        ArrayAdapter<String> _stopAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, stopsList);

        selectBusStopText.setAdapter(_stopAdapter);
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
    get arrival Timings for the selected route and stop id, called upon set reminder because of async calling. setReminder() will be called once time field is set
     */
    public void getTiming(View view) {

        String stopId = null;
        //to check if atleast one item is not selected
        boolean noNullFlag = true;
        Log.e(TAG,"Entering getTiming()");
        //Get the data from all the fields
        if(selectBusText!=null) {
            selectedBus = selectBusText.getSelectedItem().toString();
            Log.e(TAG, "The selected bus is: " + selectedBus);
        }
        else{
            noNullFlag = false;
        }

        if(selectBusStopText!=null) {
            selectedStop = selectBusStopText.getSelectedItem().toString();
            Log.e(TAG, "The selected stop is: " + selectedStop);
            stopId = stopsIdMapping.get(selectedStop);
        }
        else{
            noNullFlag = false;
        }

        if(!selectLeavingTimeText.getText().toString().equals("Select Time..")) {
            busTiming = selectLeavingTimeText.getText().toString();
            Log.e(TAG, "The time to catch bus is: " + busTiming);
        }
        else{
            noNullFlag = false;
        }

        if(!selectTimeToStopText.getText().toString().equals("Select Minutes..")) {
            timeToStop = selectTimeToStopText.getText().toString();
            Log.e(TAG, "The time to bus stop is: " + timeToStop);
        }
        else{
            noNullFlag = false;
        }

        if(stopId != null && noNullFlag) {
            //JSON Request to get buses and stops on app load
            String url = "http://runextbus.herokuapp.com/stop/" + stopId;
            Log.e(TAG, "Calling JSON getTiming for URL " + url);
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                    (url, new Response.Listener<JSONArray>() {

                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                Log.e(TAG, "Inside Response");
                                for (int i = 0; i < response.length(); i++) {
                                    String title = response.getJSONObject(i).get("title").toString();
                                    Log.e(TAG, title);
                                    if (title.equals(selectedBus)) {
                                        minutesList.clear();
                                        JSONArray predictionTimes = response.getJSONObject(i).getJSONArray("predictions");
                                        for (int j = 0; j < predictionTimes.length(); j++) {
                                            String minutes = predictionTimes.getJSONObject(j).get("minutes").toString();
                                            minutesList.add(Integer.parseInt(minutes));
                                            Log.e(TAG, "Adding minutes" + minutes);
                                        }
                                        setReminder();
                                        break;
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.e(TAG, response.toString());
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Error JSON");
                        }
                    });
            Log.e(TAG, "JSON Complete");
            RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
            requestQueue.add(jsonArrayRequest);
        }
        else{
            Toast.makeText(MainActivity.this, "Please select all options.", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    set reminder called upon set reminder button click to set reminder
     */
    public void setReminder() {
        int selectedTimeToBus;
        int timeToTimer;
        long minDiff = 0;
        Log.e("RUPronto","Entering setReminder with value: "+busTiming);
        DateFormat sdf = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);
        //get difference between current time and selected time
        try {
            Date date = sdf.parse(busTiming);
            String currentDate = sdf.format(new Date());
            Date currDate = sdf.parse(currentDate);
            minDiff = (date.getTime() - currDate.getTime()) / (60 * 1000);
            Log.e("RUPronto", "Time diff between selected time and current time in min is: " + date.toString() + " " + currDate.toString() + " " + minDiff);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Finding minimum difference using extra storage.. Urgh!   --SORRY I am commenting this(need to return which bus I want to select)
            /*ArrayList<Integer> minDiffList = new ArrayList<Integer>();
            for (int i = 0; i < minutesList.size(); i++) {
                int dist = minutesList.get(i) - (int)minDiff;
                if (dist > 0)
                    minDiffList.add(dist);
            }*/
        //int reminderMinutes = Collections.min(minDiffList);
        // Log.e("RUPronto", "The reminder time: "+reminderMinutes);

        //Finding minimum difference without extra storage.. Yayyiee!!
        selectedTimeToBus = 0;
        for(int i=0; i < minutesList.size(); i++){
            int diff = minutesList.get(i) -  (int)minDiff;
            if(diff > 0)
                break;
            selectedTimeToBus = minutesList.get(i);
        }
        Log.e(TAG, "The bus to catch is: "+selectedTimeToBus);
        timeToTimer = selectedTimeToBus - Integer.parseInt(timeToStop);
        Log.e(TAG, "Leave house in:" + timeToTimer);

        final int finalTimeToTimer = timeToTimer;
        //Ask confirmation from user if he/she wants to set reminder
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Set Reminder")
                .setMessage("\""+selectedBus+"\" bus at "+selectedStop+" in "+selectedTimeToBus+" minutes. Do you want to be reminded in "+timeToTimer+" minutes?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Schedule function to be called in milliseconds
                        Handler handler = new Handler();

                        //notification generator
                        Runnable runnable = new Runnable(){
                            public void run() {
                                //Set default sound from mobile device
                                Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this)
                                                .setSmallIcon(R.drawable.ic_launcher)
                                                .setContentTitle("RUPronto!!")
                                                .setSound(uri)
                                                .setContentText("It is time to leave to catch the \"" + selectedBus + "\" bus at " + selectedStop + ".");

                                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                if (notificationManager != null) {
                                    notificationManager.notify(NOTIFICATION_ID, builder.build());
                                }
                            }
                        };

                        //set notication to be called at time
                        handler.postDelayed(runnable, finalTimeToTimer *60000);
                        Toast.makeText(MainActivity.this, "Reminder Set!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
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

        @BindView(R.id.selectTimeToStopText)
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
            numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); //disable soft keyboard

            Log.e(TAG, timeToStop.getText().toString());

            if(!timeToStop.getText().toString().equals("Select Minutes..")) {
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

        @BindView(R.id.selectLeavingTimeText)
        Button leaveTime;

        @BindView(R.id.fragmentHolder)
        TextView fragmentHolder;

        int hour;
        int minute;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            if (fragmentHolder != null && fragmentHolder.getText() =="") {
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
            }
            else if (fragmentHolder != null){
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
            if (leaveTime == null || fragmentHolder == null) {
                    return;
            }
            // Do something with the time chosen by the user
            Toast.makeText(getActivity(), "Time Selected!", Toast.LENGTH_SHORT).show();
            String s = String.valueOf(hourOfDay)+ ":" + String.valueOf(minute);
            leaveTime.setText(s);
            fragmentHolder.setText((getString(R.string.hour_minute_string, hourOfDay, minute)));
        }
    }
}
