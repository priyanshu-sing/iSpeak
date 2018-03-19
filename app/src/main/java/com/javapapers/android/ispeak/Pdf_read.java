package com.javapapers.android.ispeak;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.javapapers.android.ispeak.ui.camera.GraphicOverlay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class Pdf_read extends AppCompatActivity {
    Button bt;
    TextView tv;
    ImageView img;
    Integer PICK_IMAGE_REQUEST=3;
    Button bt1;
    int READ_BLOCK_SIZE=100;
    TextToSpeech tts;
    private static final int PERMISSIONS_REQUIRED = 2;
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_read);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        }
        tv=(TextView)findViewById(R.id.speaktv);
        bt=(Button)findViewById(R.id.bt);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
// Show only images, no videos or anything else
                intent.setType("*/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
// Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });
        bt1=(Button)findViewById(R.id.bt1);
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tts.stop();
            }
        });
        TextToSpeech.OnInitListener listener =
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(final int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            Log.e("OnInitListener", "Text to speech engine started successfully.");
                            tts.setLanguage(Locale.US);
                        } else {
                            Log.e("OnInitListener", "Error starting the text to speech engine.");
                        }
                    }
                };
        tts = new TextToSpeech(this.getApplicationContext(), listener);
    }
    private void requestCameraPermission() {
       // Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUIRED);
            return;
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();
            Log.e("aaaaa",uri.toString());

            // Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            String myData="";
            try {
                Log.e("upperrrrr","akjfslg");
                if(!(uri.getPath().contains("pdf")))
                    myData=getStringFromFile(uri.getPath());
                else
                {
                    try {
                        // String parsedText="";
                        PdfReader reader = new PdfReader(uri.getPath());

                        Log.e("pdf","pdffffffff");
                        int n = reader.getNumberOfPages();
                        for (int i = 0; i <n ; i++) {
                            myData   = myData+ PdfTextExtractor.getTextFromPage(reader, i+1).trim()+"\n"; //Extracting the content from the different pages
                        }
                        Log.e("pdfffffff",myData);
                        reader.close();
                    } catch (Exception e) {
                        Log.e("pdfferrooooorrr",e.toString());
                    }
                }

                tv.setText(myData);
                Log.e("sdadsfdfsfsf",myData);
                tts.speak(myData,TextToSpeech.QUEUE_FLUSH,null,"DEFAULT");

            } catch (Exception e) {
                Log.e("adsfst","sgfgg");
                Log.e("erroeeee",e.toString());
            }

        }
    }
    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }
    @Override
    public void onPause() {
        tts.stop();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.stop();
    }

}

