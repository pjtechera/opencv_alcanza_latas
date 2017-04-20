package com.pablo.test2opencv.filters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.pablo.test2opencv.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import java.io.IOException;

public class DetectionActivity extends AppCompatActivity implements CvCameraViewListener2 {

    public static Context mainContext;
    public static String parentContext;

    // A TAG for log output.
    private static final String TAG = DetectionActivity.class.getSimpleName();

    // The indices of the active filters.
    private int mImageDetectionFilterIndex;

    // The filters.
    private Filter[] mImageDetectionFilters;

    // Keys for storing the indices of the active filters.
    private static final String STATE_IMAGE_DETECTION_FILTER_INDEX = "imageDetectionFilterIndex";

    // The camera view.
    private CameraBridgeViewBase mCameraView;

    //frame size width
    private static final int FRAME_SIZE_WIDTH = 1280;//800;//640;

    //frame size height
    private static final int FRAME_SIZE_HEIGHT = 720;//600;//480;

    //whether or not to use a fixed frame size -> results usually in higher FPS 640 x 480
    private static final boolean FIXED_FRAME_SIZE = true;


    // The OpenCV loader callback.
    private BaseLoaderCallback mLoaderCallback =
            new BaseLoaderCallback(this) {
                @Override
                public void onManagerConnected(final int status) {
                    switch (status) {
                        case LoaderCallbackInterface.SUCCESS:
                            Log.d(TAG, "OpenCV loaded successfully");
                            mCameraView.enableView();

                            final Filter cocaColaComun;
                            try {
                                cocaColaComun = new ImageDetectionFilter(DetectionActivity.this, R.drawable.cocacomun1);
                                //ToDo: agregar las otras
                            } catch (IOException e) {
                                Log.e(TAG, "Failed to load drawable: " + "cocaColaComun");
                                e.printStackTrace();
                                break;
                            }

                            final Filter cocaColaZero;
                            try {
                                cocaColaZero = new ImageDetectionFilter(DetectionActivity.this, R.drawable.cocalight1);
                                //ToDo: agregar las otras
                            } catch (IOException e) {
                                Log.e(TAG, "Failed to load drawable: " + "cocaColaLight");
                                e.printStackTrace();
                                break;
                            }

                            final Filter fantaComun;
                            try {
                                fantaComun = new ImageDetectionFilter(DetectionActivity.this, R.drawable.fantacomun1);
                                //ToDo: agregar las otras
                            } catch (IOException e) {
                                Log.e(TAG, "Failed to load drawable: " + "fantaComun");
                                e.printStackTrace();
                                break;
                            }

                            final Filter spriteComun;
                            try {
                                spriteComun = new ImageDetectionFilter(DetectionActivity.this, R.drawable.spritecomun1);
                                //ToDo: agregar las otras
                            } catch (IOException e) {
                                Log.e(TAG, "Failed to load drawable: " + "spriteComun");
                                e.printStackTrace();
                                break;
                            }

                            mImageDetectionFilters = new Filter[] {
                                    new NoneFilter(),
                                    cocaColaComun, //index = 1
                                    cocaColaZero, //index = 2
                                    fantaComun, //index = 3
                                    spriteComun //index = 4
                            };

                            break;
                        default:
                            super.onManagerConnected(status);
                            break;
                    }
                }
            };

    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);



        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /*if (savedInstanceState != null) {
            mImageDetectionFilterIndex = savedInstanceState.getInt(STATE_IMAGE_DETECTION_FILTER_INDEX, 0);
        }else{
            mImageDetectionFilterIndex = 0;
        }*/

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {

            if (extras.containsKey(STATE_IMAGE_DETECTION_FILTER_INDEX)) {
                mImageDetectionFilterIndex = extras.getInt(STATE_IMAGE_DETECTION_FILTER_INDEX);
            }
        }

        setContentView(R.layout.activity_detection);

        mCameraView = (CameraBridgeViewBase) findViewById(R.id.OpenCVCamera);

        if (FIXED_FRAME_SIZE) {
            mCameraView.setMaxFrameSize(FRAME_SIZE_WIDTH, FRAME_SIZE_HEIGHT);
        }

        mCameraView.setVisibility(SurfaceView.VISIBLE);

        mCameraView.setCvCameraViewListener(this);

        mainContext = getApplicationContext();

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the current filter index.
        /*savedInstanceState.putInt(STATE_IMAGE_DETECTION_FILTER_INDEX, mImageDetectionFilterIndex);*/

        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void recreate(){
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            super.recreate();
        }else{
            finish();
            startActivity(getIntent());
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mCameraView != null)
            mCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mCameraView != null)
            mCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {}

    @Override
    public void onCameraViewStopped() {}

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        final Mat rgba = inputFrame.rgba();

        // Apply the active filters.
        if (mImageDetectionFilters != null) {
            mImageDetectionFilters[mImageDetectionFilterIndex].apply(rgba, rgba);
        }

        return rgba;
    }


}
