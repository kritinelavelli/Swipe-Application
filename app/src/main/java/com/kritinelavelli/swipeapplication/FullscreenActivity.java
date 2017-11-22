package com.kritinelavelli.swipeapplication;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static android.graphics.Color.argb;
import static android.util.TypedValue.COMPLEX_UNIT_PX;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
//    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
//    private static final int UI_ANIMATION_DELAY = 300;

    private View mContentView;
    private View mControlsView;
    private View mfullscreen;
    private boolean mVisible;
    @IgnoreExtraProperties
    public static class point {
        public float x;
        public float y;
        public float pressure, orientation, size, touchMajor, touchMinor;
        public point () {

        }
        public point (float a, float b, float pres, float orien, float si, float major, float minor) {
            x = a;
            y = b;
            pressure = pres;
            orientation = orien;
            size = si;
            touchMajor = major;
            touchMinor = minor;
        }
    }
    private static class swipe{
        public Long startTime;
        public point start;
        public int width;
        public int height;
        public float xdpi;
        public List<point> coordinates;
        public String hand;
        public swipe() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }
    }
    swipe s;
    float textSize;
    FirebaseDatabase database;
    DatabaseReference myRef;
    int uniqueID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);
        // Write a message to the database

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        mfullscreen = findViewById(R.id.fullscreen);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);

        mfullscreen.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//        mControlsView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnTouchListener(myOnTouchListener());

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
        myRef.child("uniqueID").addListenerForSingleValueEvent(postListener);
        s = new swipe();
        s.coordinates = new ArrayList<point>();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        s.height = displayMetrics.heightPixels;
        s.width = displayMetrics.widthPixels;
        s.xdpi = displayMetrics.xdpi;
        TextView v = ((TextView)findViewById(R.id.text));
        textSize = v.getTextSize();


    }
    private View.OnTouchListener myOnTouchListener() {
        return new View.OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                float x = event.getRawX();
                float y = event.getRawY();
                float pressure = event.getPressure();
                float orientation = event.getOrientation();
                float siz = event.getSize();
                float touchMajor = event.getTouchMajor();
                float touchMinor = event.getTouchMinor();
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        s.startTime = System.currentTimeMillis()/1000;
                        s.start = new point(x,y, pressure, orientation, siz, touchMajor, touchMinor);
//                        s.c.add(new coordinates(x,y));
                        break;
                    case MotionEvent.ACTION_MOVE:
                        s.coordinates.add(new point(x,y, pressure, orientation, siz, touchMajor, touchMinor));
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        s.coordinates.add(new point(x,y, pressure, orientation, siz, touchMajor, touchMinor));
                        if (y <= s.start.y) {
                            view.animate().alpha(0.0f).setDuration(2000);
                            view.setVisibility(View.GONE);
                            mControlsView.setVisibility(View.VISIBLE);
                        }
                        return true;
                }
                TextView v = ((TextView)findViewById(R.id.text));
                float m = 3/(6*s.start.y);
                float newsize = textSize*m*(y+(1/3));
                if (y <= s.start.y) {
                    v.setTextSize(COMPLEX_UNIT_PX, newsize);
                    v.setAlpha(1f*m*(y+(1/3)));
                }

                //v.setText(""+textSize*y/s.start.y);
                return true;
            }
        };
    }
    ValueEventListener postListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // Get Post object and use the values to update the UI
            uniqueID = dataSnapshot.getValue(int.class);
            myRef.child("uniqueID").setValue(uniqueID+1);
            // ...
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            //Log.w("loadPost:onCancelled", databaseError.toException());
            // ...
        }
    };

    public void rightClicked(View view) {

        s.hand = "rightThumb";
        //String k = myRef.child("uniqueID").toString();
        myRef.child("_"+uniqueID).setValue(s);
        this.finish();
    }
    public void leftClicked(View view) {

        s.hand = "leftThumb";
        //String k = myRef.child("uniqueID").toString();
        myRef.child("_"+uniqueID).setValue(s);
        this.finish();
    }
    public void rightIndexClicked(View view) {

        s.hand = "rightIndex";
        //String k = myRef.child("uniqueID").toString();
        myRef.child("_"+uniqueID).setValue(s);
        this.finish();
    }
    public void leftIndexClicked(View view) {

        s.hand = "leftIndex";
        //String k = myRef.child("uniqueID").toString();
        myRef.child("_"+uniqueID).setValue(s);
        this.finish();
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
//        delayedHide(100);

    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
//    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(View view, MotionEvent motionEvent) {
//            if (AUTO_HIDE) {
//                delayedHide(AUTO_HIDE_DELAY_MILLIS);
//            }
//            return false;
//        }
//    };

//    private void toggle() {
//        if (mVisible) {
//            hide();
//        } else {
//            show();
//        }
//    }

//    private void hide() {
//        // Hide UI first
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.hide();
//        }
//        mControlsView.setVisibility(View.GONE);
//        mVisible = false;
//
//        // Schedule a runnable to remove the status and navigation bar after a delay
//        mHideHandler.removeCallbacks(mShowPart2Runnable);
//        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
//    }
//
//    private final Runnable mHidePart2Runnable = new Runnable() {
//        @SuppressLint("InlinedApi")
//        @Override
//        public void run() {
//            // Delayed removal of status and navigation bar
//
//            // Note that some of these constants are new as of API 16 (Jelly Bean)
//            // and API 19 (KitKat). It is safe to use them, as they are inlined
//            // at compile-time and do nothing on earlier devices.
//            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//        }
//    };

//    @SuppressLint("InlinedApi")
//    private void show() {
        // Show the system bar
//        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
//        mVisible = true;
//
//        // Schedule a runnable to display UI elements after a delay
//        mHideHandler.removeCallbacks(mHidePart2Runnable);
//        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
//    }

//    private final Runnable mShowPart2Runnable = new Runnable() {
//        @Override
//        public void run() {
//            // Delayed display of UI elements
//            ActionBar actionBar = getSupportActionBar();
//            if (actionBar != null) {
//                actionBar.show();
//            }
//            mControlsView.setVisibility(View.VISIBLE);
//        }
//    };

//    private final Handler mHideHandler = new Handler();
//    private final Runnable mHideRunnable = new Runnable() {
//        @Override
//        public void run() {
//            hide();
//        }
//    };
//
//    /**
//     * Schedules a call to hide() in [delay] milliseconds, canceling any
//     * previously scheduled calls.
//     */
//    private void delayedHide(int delayMillis) {
//        mHideHandler.removeCallbacks(mHideRunnable);
//        mHideHandler.postDelayed(mHideRunnable, delayMillis);
//    }
}
