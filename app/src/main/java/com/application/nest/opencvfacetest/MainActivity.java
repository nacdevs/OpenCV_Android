package com.application.nest.opencvfacetest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
    private CameraBridgeViewBase   mOpenCvCameraView;
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    private Mat mRgba;
    private Mat mGray;
    private int absoluteFaceSize;
    //private int                    mDetectorType       = JAVA_DETECTOR;

    private CascadeClassifier      mJavaDetector;
    private CascadeClassifier mJavaEyeDetector;
    //private DetectionBasedTracker  mNativeDetector;
    Mat redlight;
    Mat redlight2;


    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;

    private float                  mRelativeEyeSize   = 0.2f;
    private int                    mAbsoluteEyeSize   = 0;

    private static final int MY_CAMERA_REQUEST_CODE = 101;


    //pixel size
    private Mat mIntermediateMat;
    private  Size mSize0;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    initializeOpenCVDependencies();
                    Log.e("OpenCVActivity", "Dependencia iniciadas");
                    break;
                default:
                    super.onManagerConnected(status);
                    Log.e("OpenCVActivity", "Dependencia no iniciadas");
                    break;
            }
        }
    };

    private void initializeOpenCVDependencies() {
        //Carga Face Cascade
        try {
            // Copy the resource into a temp file so OpenCV can load it
            InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface2.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);


            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);


            }
            is.close();
            os.close();

            // Load the cascade classifier
            mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            mJavaDetector.load(mCascadeFile.getAbsolutePath());
            if (mJavaDetector.empty()) {
                Log.e("msg", "Failed to load cascade classifier");
                mJavaDetector = null;
            }else{
                Log.i("msg", "Classifier loaded properly");
            }
        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error loading cascade: "+e);
        }



        try {
            // Copy the resource into a temp file so OpenCV can load it
            InputStream is = getResources().openRawResource(R.raw.haarcascade_eye);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "lbpcascade_eyes.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);


            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);


            }
            is.close();
            os.close();

            // Load the cascade classifier
            mJavaEyeDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            mJavaEyeDetector.load(mCascadeFile.getAbsolutePath());
            if (mJavaEyeDetector.empty()) {
                Log.e("msg", "Failed to load eye cascade classifier");
                mJavaEyeDetector = null;
            }else{
                Log.i("msg", "Eye Classifier loaded properly");
            }
        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error loading cascade (Eye): "+e);
        }




        //Carga Imagen

       /* Drawable drawable = getResources().getDrawable(R.drawable.redlight);
        BitmapDrawable bitmapDrawable = ((BitmapDrawable) drawable);
        Bitmap bitmap = bitmapDrawable.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream); //use the compression format of your need
        InputStream is = new ByteArrayInputStream(stream.toByteArray());*/


               try {
            // Copy the resource into a temp file so OpenCV can load it
            InputStream is = this.getResources().openRawResource(0 + R.raw.eyeanime1);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File ImgFile = new File(cascadeDir, "redlight.png");
            FileOutputStream os = new FileOutputStream(ImgFile);



            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);


            }
            is.close();
            os.close();

            // Load the Imagen
            redlight = Imgcodecs.imread(ImgFile.getAbsolutePath(),Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
            Imgproc.cvtColor(redlight,redlight, Imgproc.COLOR_BGR2BGRA);
            Log.i("imgpath", ImgFile.getAbsolutePath().toString());
            if (redlight==null) {
                Log.e("msg", "Failed to image eyes1");
            }else{
                Log.i("msg", "Image eyes1 loaded properly");
                Log.i("msg", redlight.cols()+"x"+redlight.rows());
            }
        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error loading cascade: "+e);
        }


        try {
            // Copy the resource into a temp file so OpenCV can load it
            InputStream is = this.getResources().openRawResource(0 + R.raw.eyeanime2);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File ImgFile = new File(cascadeDir, "redlight2.png");
            FileOutputStream os = new FileOutputStream(ImgFile);



            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);


            }
            is.close();
            os.close();

            // Load the Imagen
            redlight2 = Imgcodecs.imread(ImgFile.getAbsolutePath(),Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
            Imgproc.cvtColor(redlight2,redlight2, Imgproc.COLOR_BGR2BGRA);
            Log.i("imgpath", ImgFile.getAbsolutePath().toString());
            if (redlight2==null) {
                Log.e("msg", "Failed to image eyes2");
            }else{
                Log.i("msg", "Image eyes2 loaded properly");
                Log.i("msg", redlight2.cols()+"x"+redlight2.rows());
            }
        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error loading cascade: "+e);
        }



        // And we are ready to go
        mOpenCvCameraView.enableView();



    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }
        Log.i("msg","called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.face_detect_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }
    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("TAG", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("TAG", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }




    @Override
    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
        mIntermediateMat = new Mat();
        mSize0= new Size();

    }

    @Override
    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat dst=null;
        Mat dstGray=null;
        mRgba = inputFrame.rgba();
        Size mRgbaSize = mRgba.size();
        int originalCols= (int) mRgbaSize.height;
        int originalRows= (int) mRgbaSize.width;
        mGray = inputFrame.gray();

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }

        }

        if (mAbsoluteEyeSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeEyeSize) > 0) {
                mAbsoluteEyeSize = Math.round(height * mRelativeEyeSize);
            }

        }


        MatOfRect faces = new MatOfRect();
        MatOfRect eyes = new MatOfRect();


            if (mJavaDetector != null){
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 3, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());}

           //Eye detect buscar lbpclassifier
           if (mJavaEyeDetector != null){
                mJavaEyeDetector.detectMultiScale(mGray, eyes, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                    new Size(mAbsoluteEyeSize, mAbsoluteEyeSize), new Size());}




        Rect[] facesArray = faces.toArray();
        Rect[] eyesArray = eyes.toArray();
        System.out.println("Array caras: "+String.valueOf(facesArray.length));
        //Mat img = getResources().getR//Imgcodecs.imread(getResources().getDrawable(R.drawable.androidbot).toString());
        Mat img2= new Mat();
        Imgproc.resize(redlight, img2, new Size(220, 220));
        Mat img3= new Mat();
        Imgproc.resize(redlight2, img3, new Size(220, 220));


        Log.i("Array caras", String.valueOf(facesArray.length));
            for (int i = 0; i < facesArray.length; i++){
                try{
                    //Mat BGRMat = Imgcodecs.imread(getResources().getDrawable(R.drawable.redlight).toString());
                    //Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
                    for (int j=0; j<eyesArray.length; j++){
                        try{
                            //Imgproc.rectangle(mRgba, eyesArray[j].tl(), eyesArray[j].br(), FACE_RECT_COLOR, 3);
                            if(j==0){
                                Rect eye = eyesArray[j];
                                int x = eye.x;
                                int y = eye.y;

                                Mat logo=new Mat();
                                Mat logo_alpha = new Mat();
                                Vector<Mat> rgba = new Vector<Mat>();
                                // split RBGA image for separate channels
                                Core.split(img2,rgba);
                                // get alpha channel
                                System.out.println("Tamaño1 rgba: "+rgba.size());
                                logo_alpha = rgba.get(3);
                                // remove alpha channel to get RGB image
                                rgba.remove(rgba.size()-1);
                                // get image with only RGB components
                                System.out.println("Tamaño2 rgba: "+rgba.size());
                                Core.merge(rgba,logo);
                                Rect roi = new Rect(x, y, logo.cols(), logo.rows());
                                Log.i("roi","Roix: "+logo.cols()+ " y:" + logo.rows());
                                Mat imageROI = mRgba.submat(roi);
                                // place logo with alpha on the frame
                                img2.copyTo(imageROI,logo_alpha);
                            }

                            if(j>0){
                                Rect eye = eyesArray[j];
                                int x = eye.x;
                                int y = eye.y;

                                Mat logo=new Mat();
                                Mat logo_alpha = new Mat();
                                Vector<Mat> rgba = new Vector<Mat>();
                                // split RBGA image for separate channels
                                Core.split(img3,rgba);
                                // get alpha channel
                                System.out.println("Tamaño1 rgba: "+rgba.size());
                                logo_alpha = rgba.get(3);
                                // remove alpha channel to get RGB image
                                rgba.remove(rgba.size()-1);
                                // get image with only RGB components
                                System.out.println("Tamaño2 rgba: "+rgba.size());
                                Core.merge(rgba,logo);
                                Rect roi = new Rect(x, y, logo.cols(), logo.rows());
                                Log.i("roi","Roix: "+logo.cols()+ " y:" + logo.rows());
                                Mat imageROI = mRgba.submat(roi);
                                // place logo with alpha on the frame
                                img3.copyTo(imageROI,logo_alpha);
                                break;
                            }


                           //img2.copyTo(mRgba.submat(new Rect(x,y,img2.cols(),img2.rows())));


                        }catch (Exception e){
                            Log.e("Error eyes ", e.toString());
                        }
                    }

                    Size size = new Size (400,400);
                    Rect crop= facesArray[i];
                    int x2= crop.x;
                    int y2= crop.y;
                    Mat cropped = mRgba.submat(new Rect(x2, y2, 250, 250));
                    //Mat cropped = new Mat(mRgba,new Rect(x2, y2, 200, 200));
                    dst = new Mat();
                    //dstGray= new Mat();
                    //Mat croppedGray = mGray.submat(new Rect(x2, y2, 250, 250) );
                    Imgproc.resize(cropped,dst,size);
                    //Imgproc.resize(croppedGray,dstGray,size);
                    //Imgproc.cvtColor(dst, dstGray, Imgproc.COLOR_BGR2GRAY);

                    dst.copyTo(mRgba.submat(new Rect(0,0,dst.cols(),dst.rows())));
                    //MatFinal= new Mat(originalRows,originalCols, CvType.CV_8UC1);
                    //Core.addWeighted(mRgba,1,dst,1,0,MatFinal);*/
                }catch(Exception e){
                    Log.e("error face",e.toString());
                }

            }




        return mRgba;
       // return MatFinal;
       // return dst;
    }


    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }



    public void overlayImage(Mat background, Mat foreground,Mat output)//, Point location)
    {
        background.copyTo(output);
        Mat dst = new Mat();
        Imgproc.resize(foreground, dst, background.size());
        double alpha;
        // start at row 0/col 0
        for (int y = 0; y < background.rows(); ++y) {
            for (int x = 0; x < background.cols(); ++x) {
                double info[] = dst.get(y, x);
                alpha = info[3];
                // and now combine the background and foreground pixel, using the opacity,but only if opacity > 0.
                if (alpha > 0) //rude but this is what I need
                {
                    double infof[] = dst.get(y, x);
                    output.put(y, x, infof);
                }
            }
        }
    }

        @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_CAMERA_REQUEST_CODE) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();

            } else {

                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();

            }
        }
    }


}
