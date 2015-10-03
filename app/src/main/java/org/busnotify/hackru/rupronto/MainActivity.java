package org.busnotify.hackru.rupronto;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {

    EditText selectBusStopText;
    EditText selectBusText;
    EditText selectLeavingTimeText;
    EditText selectTimeToStopText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize UI components
        selectBusStopText = (EditText) findViewById(R.id.selectBusStopText);
        selectBusText = (EditText) findViewById(R.id.selectBusText);
        selectLeavingTimeText = (EditText) findViewById(R.id.selectLeavingTimeText);
        selectTimeToStopText = (EditText) findViewById(R.id.selectTimeToStopText);

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
