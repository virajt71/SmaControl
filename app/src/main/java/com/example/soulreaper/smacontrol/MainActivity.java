package com.example.soulreaper.smacontrol;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private static final String TAG = "MainActivity";

    DatabaseHelper mDatabaseHelper;
    DatabaseHelper1 mDatabaseHelper1;
    DatabaseHelper2 mDatabaseHelper2;

    TextView mTextView, mTextView_1, mTextView_2,
            message_r_0, message_r_1,
            message_y_0, message_y_1;

    private EditText editText;
    Button mButtonAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabaseHelper = new DatabaseHelper(this);
        mDatabaseHelper1 = new DatabaseHelper1(this);
        mDatabaseHelper2 = new DatabaseHelper2(this);

        message_r_0 = findViewById(R.id.textView_r_0);
        message_r_1 = findViewById(R.id.textView_r_1);
        message_y_0 = findViewById(R.id.textView_y_0);
        message_y_1 = findViewById(R.id.textView_y_1);

        editText = findViewById(R.id.editText);
        mButtonAdd = findViewById(R.id.buttonAdd);

        mTextView = findViewById(R.id.textView_No);
        mTextView_1 = findViewById(R.id.textView_No_1);
        mTextView_2 = findViewById(R.id.textView_No_2);

        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                populateListView();
                                populateListView1();
                                populateListView2();
                            }
                        });
                    }
                }
                catch (InterruptedException e) {
                }
            }
        };

        t.start();

        //Check if the application has draw over other apps permission or not?
        //This permission is by default available for API<23. But for API > 23
        //you have to ask for the permission in runtime.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        } else {
            initializeView();
        }

        populateListView();
        populateListView1();
        populateListView2();

        mButtonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newEntry = editText.getText().toString();
                if (editText.length() != 0) {
                    AddData(newEntry);
                    editText.setText("");
                } else {
                    toastMessage("You must put something in the text field!");
                }

            }
        });

    }

    /**
     * Set and initialize the view elements.
     */

    private void initializeView() {
        findViewById(R.id.notify_me).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(new Intent(MainActivity.this, FloatingViewService.class));
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {

            //Check if the permission is granted or not.
            if (resultCode == RESULT_OK) {
                initializeView();
            } else { //Permission is not available
                Toast.makeText(this,
                        "Draw over other app permission not available. Closing the application",
                        Toast.LENGTH_SHORT).show();

                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void AddData(String newEntry) {
        boolean insertData = mDatabaseHelper.addData(newEntry);
        if (insertData) {
          //  toastMessage("Data Successfully Inserted!");
        } else {
            toastMessage("Something went wrong");
        }
    }

    /**
     * customizable toast
     * @param message
     */

    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }

    private void populateListView() {
        Log.d(TAG, "populateListView: Displaying data in the ListView.");

        //get the data and append to a list
        Cursor data = mDatabaseHelper.getData();
        while (data.moveToNext()) {
            mTextView.setText("Send msg to this number : " + data.getString(1));
        }
    }

    private void populateListView1() {
        Log.d(TAG, "populateListView1: Displaying data in the ListView.");

        //get the data and append to a list
        Cursor data1 = mDatabaseHelper1.getData1();
        while (data1.moveToNext()) {
            mTextView_1.setText(data1.getString(1));
        }
    }

    private void populateListView2() {
        Log.d(TAG, "populateListView2: Displaying data in the ListView.");

        //get the data and append to a list
        Cursor data2 = mDatabaseHelper2.getData2();
        while (data2.moveToNext()) {
            mTextView_2.setText(data2.getString(1));
        }
    }

    public static boolean sendSMS(String toPhoneNumber, String smsMessage) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(toPhoneNumber, null, smsMessage, null, null);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}