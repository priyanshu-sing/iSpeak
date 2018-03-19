package com.javapapers.android.ispeak;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import com.javapapers.android.ispeak.*;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.majeur.cling.Cling;
import com.majeur.cling.ClingManager;

import java.util.Locale;

import static android.R.attr.permission;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    int GLOBAL_TOUCH_POSITION_X = 0,c=0;
    int GLOBAL_TOUCH_CURRENT_POSITION_X = 0;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private static final String FOR_FIRST_TIME = "for first time" ;
    int size,speechrate;
    private AlertDialog enableNotificationListenerAlertDialog;
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    public static int flag=1;
    // Use a compound button so either checkbox or switch widgets work.
    private TextToSpeech tts;
    private CompoundButton autoFocus;
    private CompoundButton useFlash;
    private CompoundButton notify;
    private Button stop;
    private Button repeat;
    private Button srate;
    private Button tsize;
    private Button speakfile;
    private TextView statusMessage;
    private TextView textValue;
    String text;
    private static final int RC_OCR_CAPTURE = 9003;
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST = 1;
    private static String [] PERMISSIONS_REQUIRED = new String[]{
      Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        LinearLayout TextLoggerLayout = (LinearLayout)findViewById(R.id.ActivityView);
         int camera_permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
         int storage_permission=ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE);
        int storage_permission1=ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(camera_permission != PackageManager.PERMISSION_GRANTED||storage_permission != PackageManager.PERMISSION_GRANTED ||storage_permission1 != PackageManager.PERMISSION_GRANTED ){
            ActivityCompat.requestPermissions(this,PERMISSIONS_REQUIRED,PERMISSION_REQUEST);
        }

       /* TextLoggerLayout.setOnTouchListener(
                new LinearLayout.OnTouchListener(){

                    @Override
                    public boolean onTouch(View v, MotionEvent m) {
                        handleTouch(m);
                        return true;
                    }

                });*/
        TextToSpeech.OnInitListener listener =
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(final int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            Log.d("OnInitListener", "Text to speech engine started successfully.");
                            tts.setLanguage(Locale.US);
                        } else {
                            Log.d("OnInitListener", "Error starting the text to speech engine.");
                        }
                    }
                };
        tts = new TextToSpeech(this.getApplicationContext(), listener);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                //Toast.makeText(MainActivity.this,"lkjhvvbc",Toast.LENGTH_LONG).show();
                tts.speak("Text detection started",TextToSpeech.QUEUE_FLUSH,null,"DEFAULT");
                Intent intent = new Intent(MainActivity.this, OcrCaptureActivity.class);
                intent.putExtra(OcrCaptureActivity.AutoFocus, autoFocus.isChecked());
                intent.putExtra(OcrCaptureActivity.UseFlash, useFlash.isChecked());

                startActivityForResult(intent, RC_OCR_CAPTURE);
            }
        });
        statusMessage = (TextView) findViewById(R.id.status_message);
        textValue = (TextView) findViewById(R.id.text_value);
        stop = (Button) findViewById(R.id.button2);
        repeat = (Button) findViewById(R.id.button4);
        srate = (Button) findViewById(R.id.srate);
        tsize = (Button) findViewById(R.id.tsize);
        speakfile=(Button)findViewById(R.id.speakfile);
        //stop.setVisibility();
        if(!isNotificationServiceEnabled()){
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }
        autoFocus = (CompoundButton) findViewById(R.id.auto_focus);
        useFlash = (CompoundButton) findViewById(R.id.use_flash);
        findViewById(R.id.read_text).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button4).setOnClickListener(this);
        findViewById(R.id.srate).setOnClickListener(this);
        findViewById(R.id.tsize).setOnClickListener(this);
        findViewById(R.id.speakfile).setOnClickListener(this);

       /* ((CompoundButton) findViewById(R.id.notify)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    ed
                }
            }
        });*/
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
       /* if (prefs.getInt(NOTIFY_FLAG, 1)==1)
        {
            notify.setChecked(true);
        }
        else
            notify.setChecked(false);*/
        if(prefs.getBoolean(FOR_FIRST_TIME,true)) {
            showTut();
            // addNotification(MainActivity.this,8);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(FOR_FIRST_TIME, false);
            editor.apply();
        } /* if(notify.isChecked())
            {
                flag=1;

            }
            else
                flag=0;
    */}

    public void SetValue(View view){
        ShowDialog();
    }


    public void ShowDialog() {

        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        final SeekBar seek = new SeekBar(this);
        int value = 0;
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("mySharedPrefsFilename", Context.MODE_PRIVATE);
        value = prefs.getInt("seekBarValue", 1);
        seek.setProgress(value);
        seek.setMax(3);
        popDialog.setView(seek);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                //Update textview value
  //              tv.setText("Value : " + progress);
            }
            public void onStartTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Update your preferences only when the user has finished moving the seek bar
                SharedPreferences prefs =getApplicationContext().getSharedPreferences("mySharedPrefsFilename", Context.MODE_PRIVATE);
                // Don't forget to call commit() when changing preferences.
                prefs.edit().putInt("seekBarValue", seekBar.getProgress()).commit();
                speechrate = prefs.getInt("seekBarValue", 1);

                tts.setSpeechRate(speechrate);

            }
        });
        // Button
        popDialog.setMessage("Set speech rate").setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        popDialog.create();
        popDialog.show();
    }

    public void ShowDialog2() {

        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        final SeekBar seek = new SeekBar(this);
        int value = 0;
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("mySharedPrefsFilename", Context.MODE_PRIVATE);
        value = prefs.getInt("seekBarValue2", 20);
        seek.setProgress(value);
        seek.setMax(40);
        popDialog.setView(seek);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                
            }
            public void onStartTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Update your preferences only when the user has finished moving the seek bar
                SharedPreferences prefs =getApplicationContext().getSharedPreferences("mySharedPrefsFilename", Context.MODE_PRIVATE);
                // Don't forget to call commit() when changing preferences.
                prefs.edit().putInt("seekBarValue2", seekBar.getProgress()).commit();
                size = prefs.getInt("seekBarValue2", 20);
               if(repeat.getVisibility()== View.VISIBLE)
                textValue.setTextSize(size);
            }
        });
        // Button
        popDialog.setMessage("Set text size").setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        popDialog.create();
        popDialog.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.textsize)
        {
            showTut();
            return true;
        }
        else if(id==R.id.speechrate)
        {
            PackageManager pm = getPackageManager();
            ApplicationInfo appInfo;
            try {
                appInfo = pm.getApplicationInfo(getPackageName(),
                        PackageManager.GET_META_DATA);
                Intent sendBt = new Intent(Intent.ACTION_SEND);
                sendBt.setType("*/*");
                sendBt.putExtra(Intent.EXTRA_STREAM,
                        Uri.parse("file://" + appInfo.publicSourceDir));
                startActivity(Intent.createChooser(sendBt,
                        "Share it using"));
            } catch (PackageManager.NameNotFoundException e1) {
                e1.printStackTrace();
            }
            return true;
        }
        else if(id==R.id.aboutus)
        {
            Intent in=new Intent(MainActivity.this,AboutActivity.class);
            startActivity(in);
            return true;
        }
        else if(id==R.id.notify)
        {
            startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
        }
        return true;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.read_text) {
            tts.speak("Text detection started",TextToSpeech.QUEUE_FLUSH,null,"DEFAULT");
            Intent intent = new Intent(MainActivity.this, OcrCaptureActivity.class);
            intent.putExtra(OcrCaptureActivity.AutoFocus, autoFocus.isChecked());
            intent.putExtra(OcrCaptureActivity.UseFlash, useFlash.isChecked());

            startActivityForResult(intent, RC_OCR_CAPTURE);
        }

        else if(v.getId()==R.id.srate)
        {
            ShowDialog();
        }
        else if(v.getId()==R.id.speakfile)
        {
            Intent i=new Intent(MainActivity.this,Pdf_read.class);
            startActivity(i);
        }
        else if(v.getId()==R.id.tsize)
        {
            ShowDialog2();
            SharedPreferences prefs =getApplicationContext().getSharedPreferences("mySharedPrefsFilename", Context.MODE_PRIVATE);
            size = prefs.getInt("seekBarValue2", 20);
            textValue.setTextSize(size);

        }
        else if(v.getId()==R.id.button2)
        {
            if(tts!=null)
            {
                tts.stop();
            }
        }
        else if(v.getId()==R.id.button4)
        {
                tts.speak(text, TextToSpeech.QUEUE_ADD, null, "DEFAULT");
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RC_OCR_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    stop.setVisibility(View.VISIBLE);
                    repeat.setVisibility(View.VISIBLE);
                    SharedPreferences prefs =getApplicationContext().getSharedPreferences("mySharedPrefsFilename", Context.MODE_PRIVATE);
                    size = prefs.getInt("seekBarValue2", 20);
                    textValue.setTextSize(size);
                    speechrate = prefs.getInt("seekBarValue", 1);
                    tts.setSpeechRate(speechrate);
                    text = data.getStringExtra(OcrCaptureActivity.TextBlockObject);
                    statusMessage.setText(R.string.ocr_success);
                    Log.e("StringMain",text);
                    textValue.setText(text);

                    tts.speak(text, TextToSpeech.QUEUE_ADD, null, "DEFAULT");
                    Log.d(TAG, "Text read: " + text);
                } else {
                    statusMessage.setText(R.string.ocr_failure);
                    Log.d(TAG, "No Text captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.ocr_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showTut() {
        ClingManager mClingManager = new ClingManager(this);

        mClingManager.addCling(new Cling.Builder(this)
                .setTitle("Welcome to iSPEAK!")
                .setContent("An application that will read everything for you")
                .build());
        String content = "Help read out any text to you\n\nRead notifications for you";

        mClingManager.addCling(new Cling.Builder(this)
                .setTitle("This app can...")
                .setContent(content)
                .build());
        mClingManager.addCling(new Cling.Builder(this)
                .setTitle("SPEECH RATE")
                .setContent("Set the text speech rate")
                .setTarget(new com.majeur.cling.ViewTarget(this, R.id.srate))
                .build());

        mClingManager.addCling(new Cling.Builder(this)
                .setTitle("TEXT SIZE")
                .setContent("Edit text size here")
                .setTarget(new com.majeur.cling.ViewTarget(this, R.id.tsize))
                .build());


        mClingManager.addCling(new Cling.Builder(this)
                .setTitle("Speak File")
                .setContent("Tap here to speak the file contents")
                .setTarget(new com.majeur.cling.ViewTarget(this, R.id.speakfile))
                .build());

        mClingManager.addCling(new Cling.Builder(this)
                .setTitle("AutoFocus")
                .setContent("Slide to enable/disable auto focus")
                .setTarget(new com.majeur.cling.ViewTarget(this, R.id.auto_focus))
                .build());

        mClingManager.addCling(new Cling.Builder(this)
                .setTitle("Flash")
                .setContent("Slide this to enable/disable flash on image")
                .setTarget(new com.majeur.cling.ViewTarget(this, R.id.use_flash))
                .build());

        mClingManager.addCling(new Cling.Builder(this)
                .setTitle("Detect Text")
                .setContent("Press this to detect any text on image")
                .setTarget(new com.majeur.cling.ViewTarget(this, R.id.read_text))
                .build());

        content="You can also SHAKE your phone to start text detection :)";

        mClingManager.addCling(new Cling.Builder(this)
                .setTitle("Alternatively")
                .setContent(content)
                .build());
        content="To Enable or Disable Notification\nGo to Menu";
        mClingManager.addCling(new Cling.Builder(this)
                .setTitle("Note")
                .setContent(content)
                .build());

        if(stop.getVisibility()== View.VISIBLE)
        {
            mClingManager.addCling(new Cling.Builder(this)
                    .setTitle("STOP")
                    .setContent("Stop the speech in mid")
                    .setTarget(new com.majeur.cling.ViewTarget(this, R.id.button2))
                    .build());
        }

        if(repeat.getVisibility()== View.VISIBLE)
        {
            mClingManager.addCling(new Cling.Builder(this)
                    .setTitle("REPEAT")
                    .setContent("Repeat the text detected")
                    .setTarget(new com.majeur.cling.ViewTarget(this, R.id.button4))
                    .build());
        }

        content="You are ready to use app now";
        mClingManager.addCling(new Cling.Builder(this)
                .setTitle("Go ahead..")
                .setContent(content)
                .build());

        mClingManager.start();
    }
    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private AlertDialog buildNotificationServiceAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.notification_listener_service);
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation);
        alertDialogBuilder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        alertDialogBuilder.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        return(alertDialogBuilder.create());
    }


    @Override
    public void onPause() {
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        tts.stop();
        super.onPause();
    }
    protected void onResume() {
        c=0;
        super.onResume();
        //Two-Finger Drag Gesture detection
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,    SensorManager.SENSOR_DELAY_UI);


    }
    @Override
    protected void onDestroy() {

        Log.e("Destroooooooooooooo","sfnslnjsfnj");
        tts.stop();
        super.onDestroy();
    }

}
