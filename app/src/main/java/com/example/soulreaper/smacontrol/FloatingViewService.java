package com.example.soulreaper.smacontrol;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import static com.example.soulreaper.smacontrol.MainActivity.sendSMS;


public class FloatingViewService extends Service {

    private WindowManager mWindowManager;
    private View mFloatingView;

    private static final String TAG = "FloatingViewService";
    DatabaseHelper fDatabaseHelper;
    DatabaseHelper1 fDatabaseHelper1;
    DatabaseHelper2 fDatabaseHelper2;

    TextView fTextView, fTextView_1, fTextView_2,
            fmessage_r_0, fmessage_r_1,
            fmessage_y_0, fmessage_y_1;
    ToggleButton fToggleButton1, fToggleButton2, fToggleButton3, fToggleButton4;
    Thread mUiThread;
    final Handler mHandler = new Handler();

    public FloatingViewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Inflate the floating view layout we created
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);

        fDatabaseHelper = new DatabaseHelper(this);
        fDatabaseHelper1 = new DatabaseHelper1(this);
        fDatabaseHelper2 = new DatabaseHelper2(this);

        fmessage_r_0 = mFloatingView.findViewById(R.id.ftextView_r_0);
        fmessage_r_1= mFloatingView.findViewById(R.id.ftextView_r_1);
        fmessage_y_0 = mFloatingView.findViewById(R.id.ftextView_y_0);
        fmessage_y_1= mFloatingView.findViewById(R.id.ftextView_y_1);

        fTextView = mFloatingView.findViewById(R.id.ftextView_No_);
        fTextView_1 = mFloatingView.findViewById(R.id.ftextView_No1);
        fTextView_2 = mFloatingView.findViewById(R.id.ftextView_No2);

        fToggleButton1 = mFloatingView.findViewById(R.id.toggleButton1);
        fToggleButton2 = mFloatingView.findViewById(R.id.toggleButton2);
        fToggleButton3 = mFloatingView.findViewById(R.id.toggleButton3);
        fToggleButton4 = mFloatingView.findViewById(R.id.toggleButton4);

        //Add the view to the window.
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        //Specify the view position
        params.gravity = Gravity.TOP | Gravity.START;        //Initially view will be added to top-left corner
        params.x = 0;
        params.y = 100;

        //Add the view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);

        //The root element of the collapsed view layout
        final View collapsedView = mFloatingView.findViewById(R.id.collapse_view);
        //The root element of the expanded view layout
        final View expandedView = mFloatingView.findViewById(R.id.expanded_container);


        //Set the close button
        ImageView closeButtonCollapsed =  mFloatingView.findViewById(R.id.close_btn);
        closeButtonCollapsed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //close the service and remove the from from the window
                stopSelf();
            }
        });


        //Set the close button
        ImageView closeButton =  mFloatingView.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapsedView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);
            }
        });

        //Open the application on thi button click
        ImageView openButton = mFloatingView.findViewById(R.id.open_button);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Open the application  click.
                Intent intent = new Intent(FloatingViewService.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                //close the service and remove view from the view hierarchy
                stopSelf();
            }
        });

        //Drag and move floating view using user's touch action.
        mFloatingView.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        //remember the initial position.
                        initialX = params.x;
                        initialY = params.y;

                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);

                        //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                        //So that is click event.
                        if (Xdiff < 10 && Ydiff < 10) {
                            if (isViewCollapsed()) {
                                //When user clicks on the image view of the collapsed layout,
                                //visibility of the collapsed layout will be changed to "View.GONE"
                                //and expanded view will become visible.
                                collapsedView.setVisibility(View.GONE);
                                expandedView.setVisibility(View.VISIBLE);
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);

                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mFloatingView, params);
                        return true;
                }
                return false;
            }
        });

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
                                onDataChange();
                                onDataChange2();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();

        fToggleButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fToggleButton1.isChecked())
                {
                    String str_addtes = fTextView.getText().toString();
                    String str_message = fmessage_r_1.getText().toString();
                    fToggleButton1.setBackgroundResource(R.drawable.circle_with_double_outline);

                    if (str_addtes.length() > 0 && str_message.length() > 0) {

                        if(sendSMS(str_addtes, str_message))
                        {
                            Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT).show();
                        }
                    }
                    String newEntry1 = fmessage_r_1.getText().toString();
                    if (fmessage_r_1.length() != 0) {
                        AddData1(newEntry1);

                    } else {
                        toastMessage("You must put something in the text field!");
                    }
                }
                else {
                    String str_addtes = fTextView.getText().toString();
                    String str_message = fmessage_r_0.getText().toString();
                    fToggleButton1.setBackgroundResource(R.drawable.circle_with_outline);

                    if (str_addtes.length() > 0 && str_message.length() > 0) {

                        if(sendSMS(str_addtes, str_message))
                        {
                            Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT).show();
                        }
                    }
                    String newEntry1 = fmessage_r_0.getText().toString();
                    if (fmessage_r_0.length() != 0) {
                        AddData1(newEntry1);

                    } else {
                        toastMessage("You must put something in the text field!");
                    }
                }
            }
        });

        fToggleButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fToggleButton2.isChecked())
                {
                    String str_addtes = fTextView.getText().toString();
                    String str_message = fmessage_y_1.getText().toString();
                    fToggleButton2.setBackgroundResource(R.drawable.circle_with_double_outline);

                    if (str_addtes.length() > 0 && str_message.length() > 0) {

                        if(sendSMS(str_addtes, str_message))
                        {
                            Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT).show();
                        }
                    }
                    String newEntry2 = fmessage_y_1.getText().toString();
                    if (fmessage_y_1.length() != 0) {
                        AddData2(newEntry2);

                    } else {
                        toastMessage("You must put something in the text field!");
                    }
                }
                else {
                    String str_addtes = fTextView.getText().toString();
                    String str_message = fmessage_y_0.getText().toString();
                    fToggleButton2.setBackgroundResource(R.drawable.circle_with_outline);

                    if (str_addtes.length() > 0 && str_message.length() > 0) {

                        if(sendSMS(str_addtes, str_message))
                        {
                            Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT).show();
                        }
                    }
                    String newEntry2 = fmessage_y_0.getText().toString();
                    if (fmessage_y_0.length() != 0) {
                        AddData2(newEntry2);

                    } else {
                        toastMessage("You must put something in the text field!");
                    }
                }
            }
        });

        fToggleButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fToggleButton3.isChecked())
                {
                    fToggleButton3.setBackgroundResource(R.drawable.circle_with_double_outline);
                }
                else {
                    fToggleButton3.setBackgroundResource(R.drawable.circle_with_outline);
                }
            }
        });

        fToggleButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fToggleButton4.isChecked())
                {
                    fToggleButton4.setBackgroundResource(R.drawable.circle_with_double_outline);
                }
                else {
                    fToggleButton4.setBackgroundResource(R.drawable.circle_with_outline);
                }
            }
        });
    }

    private void runOnUiThread(Runnable runnable) {
        if (Thread.currentThread() != mUiThread) {
            mHandler.post(runnable);
        } else {
            runnable.run();
        }
    }

    /**
     * Detect if the floating view is collapsed or expanded.
     *
     * @return true if the floating view is collapsed.
     */
    private boolean isViewCollapsed() {
        return mFloatingView == null || mFloatingView.findViewById(R.id.collapse_view).getVisibility() == View.VISIBLE;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
    }


    private void AddData1(String newEntry1) {
        boolean insertData1 = fDatabaseHelper1.addData1(newEntry1);
        if (insertData1) {
           // toastMessage("Data Successfully Inserted!");
        } else {
            toastMessage("Something went wrong");
        }
    }

    private void AddData2(String newEntry2) {
        boolean insertData2 = fDatabaseHelper2.addData2(newEntry2);
        if (insertData2) {
            // toastMessage("Data Successfully Inserted!");
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
        Cursor data = fDatabaseHelper.getData();
        while (data.moveToNext()) {
            fTextView.setText(data.getString(1));
        }
    }

    private void populateListView1() {
        Log.d(TAG, "populateListView1: Displaying data in the ListView.");

        //get the data and append to a list
        Cursor data1 = fDatabaseHelper1.getData1();
        while (data1.moveToNext()) {
            fTextView_1.setText(data1.getString(1));
        }
    }

    private void populateListView2() {
        Log.d(TAG, "populateListView2: Displaying data in the ListView.");

        //get the data and append to a list
        Cursor data2 = fDatabaseHelper2.getData2();
        while (data2.moveToNext()) {
            fTextView_2.setText(data2.getString(1));
        }
    }

    public void onDataChange() {
        String value = fTextView_1.getText().toString();
        fToggleButton1.setTag(value);
        String value1 = String.valueOf("r1");
        if (value.equals(value1)){
            fToggleButton1.setBackgroundResource(R.drawable.circle_with_double_outline);
            fToggleButton1.setText("ON");
        }else{
            fToggleButton1.setBackgroundResource(R.drawable.circle_with_outline);
            fToggleButton1.setText("OFF");
        }
    }

    public void onDataChange2() {
        String value = fTextView_2.getText().toString();
        fToggleButton2.setTag(value);
        String value1 = String.valueOf("y1");
        if (value.equals(value1)){
            fToggleButton2.setBackgroundResource(R.drawable.circle_with_double_outline);
            fToggleButton2.setText("ON");
        }else{
            fToggleButton2.setBackgroundResource(R.drawable.circle_with_outline);
            fToggleButton2.setText("OFF");
        }
    }
}
