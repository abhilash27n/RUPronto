package org.busnotify.hackru.rupronto;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

    EditText selectBusStopText;
    TextView selectBusText;
    EditText selectLeavingTimeText;
    EditText selectTimeToStopText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize UI components
        selectBusStopText = (EditText) findViewById(R.id.selectBusStopText);
        selectBusText = (TextView) findViewById(R.id.selectBusText);
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
        CharSequence[] bus = {"LX","Wknd1"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.bus)
                .setItems(bus, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                    }
                });
        builder.create();
    }

    public void selectLeavingTime(View view) {
    }

    public void selectTimeToStop(View view) {
    }

    public void setReminder(View view) {
    }


}
