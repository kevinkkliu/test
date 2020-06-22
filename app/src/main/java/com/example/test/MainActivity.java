package com.example.test;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;



import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.*;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Net;

import org.opencv.dnn.Dnn;
import org.opencv.imgproc.Imgproc;

import org.opencv.utils.Converters;
import org.opencv.videoio.VideoCapture;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    TextView txt1;
    TextView txt2;
    int mFPS;
    long startTime = 0;
    long currentTime = 1000;
    Mat mat1;

    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;
    boolean startYolo = false;
    boolean firstTimeYolo = false;
    boolean layoutChg = false;

    Net tinyYolo;
//    int idGuy;
    int counter = 0 ;


    public void backtomain(View view){
        setContentView(R.layout.activity_main);
    }

    public void Click(View Button){

        if (startYolo == false){
            startYolo = true;

            if (firstTimeYolo == false){
                firstTimeYolo = true;
                String tinyYoloCfg = "/storage/emulated/0/dnns/yolov3-tiny.cfg"; //換成自己的神經
                String tinyYoloWeights =  "/storage/emulated/0/dnns/yolov3-tiny_20000.weights"; //換成自己的神經
                tinyYolo = Dnn.readNetFromDarknet(tinyYoloCfg, tinyYoloWeights);

            }

        }

        else{
            startYolo = false;

        }
    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        cameraBridgeViewBase = (JavaCameraView)findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
        Button btnPage2 = (Button)findViewById(R.id.btnPage2);
        btnPage2.setOnClickListener(btnPage2Listener);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txt1 = (TextView) findViewById(R.id.txt1);
            }
        });


//        Context context = getApplicationContext();
//        CharSequence text = Environment.getExternalStorageDirectory() + "/dnns/yolov3-tiny.cfg"; //驗證路徑
//        int duration = Toast.LENGTH_SHORT;
//
//        Toast toast = Toast.makeText(context, text, duration);
//        toast.show();

//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);
                switch(status){
                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        break;

                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };
    }
    private Button.OnClickListener btnPage2Listener = new Button.OnClickListener(){
        public void onClick(View v){
            detectCheck();

        }
    };


    private void detectCheck(){

        AlertDialog.Builder checkbox = new AlertDialog.Builder(this);
        checkbox.setTitle("確認");
        checkbox.setMessage("這裡是M3嗎?");

        checkbox.setPositiveButton("開始導航",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                destinationBox();
            }
        });
        checkbox.setNegativeButton("繼續辨識", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        checkbox.show();
    }

    public void destinationBox(){
        final EditText editText= new EditText(this);
        new AlertDialog.Builder(this).setTitle("你想要去哪裡")
            .setView(editText)
            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this,LocationTest.class);
                    startActivity(intent);
                }
            }).setNegativeButton("取消",null).show();
    }



    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) { // 畫框

        Mat frame = inputFrame.rgba();

        //Log.i(TAG, mat1.get(128, 128).toString());

        runOnUiThread(new Runnable() { //fps
            @Override
            public void run() {
                if (currentTime - startTime >= 1000) {
                    txt1.setText("FPS: " + String.valueOf(mFPS));
                    mFPS = 0;
                    startTime = System.currentTimeMillis();
                }
                currentTime = System.currentTimeMillis();
                mFPS += 1;

            }
        });

        if (startYolo == true) {
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);
            Mat imageBlob = Dnn.blobFromImage(frame, 0.00392, new Size(416,416),new Scalar(0, 0, 0),/*swapRB*/false, /*crop*/false);
            tinyYolo.setInput(imageBlob);
            java.util.List<Mat> result = new java.util.ArrayList<Mat>(2);
            List<String> outBlobNames = new java.util.ArrayList<>();

            outBlobNames.add(0, "yolo_16");
            outBlobNames.add(1, "yolo_23");
            tinyYolo.forward(result,outBlobNames);

            float confThreshold = 0.98f;

            List<Integer> clsIds = new ArrayList<>();
            List<Float> confs = new ArrayList<>();
            List<Rect> rects = new ArrayList<>();

            for (int i = 0; i < result.size(); ++i)
            {
                Mat level = result.get(i);
                for (int j = 0; j < level.rows(); ++j)
                {
                    Mat row = level.row(j);
                    Mat scores = row.colRange(5, level.cols());
                    Core.MinMaxLocResult mm = Core.minMaxLoc(scores);

                    float confidence = (float)mm.maxVal;
                    Point classIdPoint = mm.maxLoc;

                    if (confidence > confThreshold)//conf準確率大於 70%才進行畫框的動作 confidence 191
                    {
                        int centerX = (int)(row.get(0,0)[0] * frame.cols());
                        int centerY = (int)(row.get(0,1)[0] * frame.rows());
                        int width   = (int)(row.get(0,2)[0] * frame.cols());
                        int height  = (int)(row.get(0,3)[0] * frame.rows());

                        int left    = centerX - width  / 2;
                        int top     = centerY - height / 2;

                        clsIds.add((int)classIdPoint.x);
                        confs.add((float)confidence);

                        rects.add(new Rect(left, top, width, height));
                    }
                }
            }

            int ArrayLength = confs.size();

            if (ArrayLength>=1) {

                float nmsThresh = 0.2f;
                MatOfFloat confidences = new MatOfFloat(Converters.vector_float_to_Mat(confs));

                Rect[] boxesArray = rects.toArray(new Rect[0]);

                MatOfRect boxes = new MatOfRect(boxesArray);

                MatOfInt indices = new MatOfInt();

                Dnn.NMSBoxes(boxes, confidences, confThreshold, nmsThresh, indices);


                int[] ind = indices.toArray();
                for (int i = 0; i < ind.length; ++i) {

                    int idx = ind[i];
                    Rect box = boxesArray[idx];

                    int idGuy = clsIds.get(idx);

                    float conf = confs.get(idx);

                    List<String> cocoNames = Arrays.asList("COSMOS HOTEL M3 EXIT",
                            "Formosa Chang",
                            "Tofu pudding cold yumm",
                            "wanjinyushop",
                            "TKK International",
                            "PEGAVISION",
                            "MIS",
                            "Dudi 3C",
                            "COSMED ",
                            "Civic Avenue Exit ",
                            "Amo ",
                            "kuaiche jerky ",
                            "pintian bento ",
                            "THEFREEN BURGER ",
                            "Yoshinoya ",
                            "Mos Burger ",
                            "GIODANO ",
                            "Sushi Express ",
                            "50 lan ",
                            "Semeur ",
                            "Mister Donut ",
                            "Eslite Bookstore ",
                            "M3 Exit ",
                            "Station Front Metro Mall K ",
                            "Taiwan Rail ",
                            "Taiwan High Speed Rail B1 ",
                            "Taoyuan Airport MRT ",
                            "Tomod's ",
                            "Locker ",
                            "Taipei Transfer Station Qsquare ",
                            "Zhongxiao West Road ",
                            "Krispy kreme ",
                            "18 Single ",
                            "MRT ",
                            "Emergency Exit ",
                            "toilet");

                    int intConf = (int) (conf * 100);

                    Imgproc.putText(frame,cocoNames.get(idGuy) + " " + intConf + "%",box.tl(),Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0,255,0),1);

                    Imgproc.rectangle(frame, box.tl(), box.br(), new Scalar(255, 0, 0), 2);

                    TextView view = (TextView)findViewById(R.id.txt2);
                    view.setText("已辨識出:"+cocoNames.get(idGuy));

                    if (intConf>98){
                        counter++;
                        if (counter > 50) {
                            startYolo= false;
                            mat1.release();
                            view.setText("辨識完成！");
                            counter=0;
                            break;

                        }

                    }

                }

            }

        }

        return frame;
    }



    @Override
    public void onCameraViewStarted(int width, int height) {
        mat1 = new Mat(width, height, CvType.CV_8UC4);

        if (startYolo == true){

            String tinyYoloCfg = "/storage/emulated/0/dnns/yolov3-tiny.cfg"; //換成自己的神經
            String tinyYoloWeights = "/storage/emulated/0/dnns/yolov3-tiny_20000.weights"; //換成自己的神經

            tinyYolo = Dnn.readNetFromDarknet(tinyYoloCfg, tinyYoloWeights);

        }
    }

    @Override
    public void onCameraViewStopped() {

        mat1.release();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(),"有問題", Toast.LENGTH_SHORT).show();
        }
        else
        {
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraBridgeViewBase!=null){
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase!=null){
            cameraBridgeViewBase.disableView();
        }
    }
}