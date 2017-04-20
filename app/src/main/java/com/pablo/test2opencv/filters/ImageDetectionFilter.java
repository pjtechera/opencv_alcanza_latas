package com.pablo.test2opencv.filters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.pablo.test2opencv.MainActivity;
import com.pablo.test2opencv.R;

import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageDetectionFilter extends AppCompatActivity implements Filter {

    // The reference image (this detector's target).
    private Mat mReferenceImage = new Mat();

    // Features of the reference image.
    private final MatOfKeyPoint mReferenceKeypoints = new MatOfKeyPoint();

    // Descriptors of the reference image's features.
    private final Mat mReferenceDescriptors = new Mat();

    // The corner coordinates of the reference image, in pixels.
    // CvType defines the color depth, number of channels, and
    // channel layout in the image. Here, each point is represented
    // by two 32-bit floats.
    private final Mat mReferenceCorners = new Mat(4, 1, CvType.CV_32FC2);

    // Features of the scene (the current frame).
    private final MatOfKeyPoint mSceneKeypoints = new MatOfKeyPoint();

    // Descriptors of the scene's features.
    private final Mat mSceneDescriptors = new Mat();

    // Tentative corner coordinates detected in the scene, in pixels.
    private final Mat mCandidateSceneCorners = new Mat(4, 1, CvType.CV_32FC2);

    // Good corner coordinates detected in the scene, in pixels.
    private final Mat mSceneCorners = new Mat(0, 0, CvType.CV_32FC2);

    // The good detected corner coordinates, in pixels, as integers.
    private final MatOfPoint mIntSceneCorners = new MatOfPoint();

    // A grayscale version of the scene.
    private final Mat mGraySrc = new Mat();

    // Tentative matches of scene features and reference features.
    private final MatOfDMatch mMatches = new MatOfDMatch();

    // A feature detector, which finds features in images.
    private final FeatureDetector mFeatureDetector = FeatureDetector.create(FeatureDetector.ORB);

    // A descriptor extractor, which creates descriptors of features.
    private final DescriptorExtractor mDescriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);

    // A descriptor matcher, which matches features based on their descriptors.
    private final DescriptorMatcher mDescriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);

    // The color of the outline drawn around the detected image.
    private final Scalar colorGreen = new Scalar(0, 255, 0);
    private final Scalar colorRed = new Scalar(255, 0, 0);
    private final Scalar colorBlue = new Scalar(0, 0, 255);

    public ImageDetectionFilter(){}

    private Context context;

    private int framesCount;

    public ImageDetectionFilter(final Context context, final int referenceImageResourceID) throws IOException {

        this.context = context;
        framesCount = 0; //inicializo en 0 al crear el filtro

        // Load the reference image from the app's resources.
        // It is loaded in BGR (blue, green, red) format.
        String filename = context.getResources().getResourceEntryName(referenceImageResourceID) + ".jpg";
        File file = new File(context.getCacheDir() + "/" + filename);
        if (!file.exists())
            try {

                InputStream is = context.getAssets().open(filename);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                FileOutputStream fos = new FileOutputStream(file);

                fos.write(buffer);
                fos.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        if (file.exists()) {
            InputStream stream = null;
            Uri uri = Uri.parse(file.getAbsolutePath()/* + "/" + filename*/);
            try {
                //stream = context.getContentResolver().openInputStream(uri);
                stream = context.getAssets().open(filename);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
            bmpFactoryOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

            int resourceId = referenceImageResourceID;

            Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), resourceId, bmpFactoryOptions);
            Utils.bitmapToMat(bmp, mReferenceImage);


        }

        // Create grayscale and RGBA versions of the reference image.
        final Mat referenceImageGray = new Mat();
        Imgproc.cvtColor(mReferenceImage, referenceImageGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(mReferenceImage, mReferenceImage, Imgproc.COLOR_BGR2RGBA);

        // Store the reference image's corner coordinates, in pixels.
        mReferenceCorners.put(0, 0, new double[] {0.0, 0.0});
        mReferenceCorners.put(1, 0, new double[] {referenceImageGray.cols(), 0.0});
        mReferenceCorners.put(2, 0, new double[] {referenceImageGray.cols(), referenceImageGray.rows()});
        mReferenceCorners.put(3, 0, new double[] {0.0, referenceImageGray.rows()});

        // Detect the reference features and compute their descriptors.
        mFeatureDetector.detect(referenceImageGray, mReferenceKeypoints);
        mDescriptorExtractor.compute(referenceImageGray, mReferenceKeypoints, mReferenceDescriptors);

    }

    @Override
    public void apply(final Mat src, final Mat dst) {

        framesCount++; //sumo 1 frame cada vez que se ejecuta

        //region de interes, donde buscar matchs, fuera de esta ROI no me importa buscar
        Rect roi = new Rect(0,0,960,420);
        Mat matROI = new Mat(src, roi);

        // Convert the scene to grayscale.
        //Imgproc.cvtColor(src, mGraySrc, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.cvtColor(matROI, mGraySrc, Imgproc.COLOR_RGBA2GRAY);

        // Detect the scene features, compute their descriptors,
        // and match the scene descriptors to reference descriptors.
        mFeatureDetector.detect(mGraySrc, mSceneKeypoints);
        mDescriptorExtractor.compute(mGraySrc, mSceneKeypoints, mSceneDescriptors);

        if (mSceneDescriptors.type() == mReferenceDescriptors.type() &&
                mSceneDescriptors.cols() == mReferenceDescriptors.cols()) {
            mDescriptorMatcher.match(mSceneDescriptors, mReferenceDescriptors, mMatches);

            // Attempt to find the target image's corners in the scene.
            findSceneCorners();

            // If the corners have been found, draw an outline around the target image.
            // Else, draw a thumbnail of the target image.
            draw(src, dst);
        }
    }

    private void findSceneCorners() {

        final List<DMatch> matchesList = mMatches.toList();
        if (matchesList.size() < 4) {
            // There are too few matches to find the homography.
            return;
        }

        final List<KeyPoint> referenceKeypointsList = mReferenceKeypoints.toList();
        final List<KeyPoint> sceneKeypointsList = mSceneKeypoints.toList();

        // Calculate the max and min distances between keypoints.
        double maxDist = 0.0;
        double minDist = Double.MAX_VALUE;
        for (final DMatch match : matchesList) {
            final double dist = match.distance;
            if (dist < minDist) {
                minDist = dist;
            }
            if (dist > maxDist) {
                maxDist = dist;
            }
        }

        // The thresholds for minDist are chosen subjectively
        // based on testing. The unit is not related to pixel
        // distances; it is related to the number of failed tests
        // for similarity between the matched descriptors.
        if (minDist > 50.0) {
            // The target is completely lost. Discard any previously found corners.
            mSceneCorners.create(0, 0, mSceneCorners.type());
            return;
        } else if (minDist > 25.0) {
            // The target is lost but maybe it is still close. Keep any previously found corners.
            return;
        }

        // Identify "good" keypoints based on match distance.
        final ArrayList<Point> goodReferencePointsList = new ArrayList<Point>();
        final ArrayList<Point> goodScenePointsList = new ArrayList<Point>();
        final double maxGoodMatchDist = 1.75 * minDist;
        for (final DMatch match : matchesList) {
            if (match.distance < maxGoodMatchDist) {
                goodReferencePointsList.add(referenceKeypointsList.get(match.trainIdx).pt);
                goodScenePointsList.add(sceneKeypointsList.get(match.queryIdx).pt);
            }
        }

        if (goodReferencePointsList.size() < 4 ||
                goodScenePointsList.size() < 4) {
            // There are too few good points to find the homography.
            return;
        }

        // There are enough good points to find the homography.
        // (Otherwise, the method would have already returned.)

        // Convert the matched points to MatOfPoint2f format, as
        // required by the Calib3d.findHomography function.
        final MatOfPoint2f goodReferencePoints = new MatOfPoint2f();
        goodReferencePoints.fromList(goodReferencePointsList);
        final MatOfPoint2f goodScenePoints = new MatOfPoint2f();
        goodScenePoints.fromList(goodScenePointsList);

        // Find the homography.
        final Mat homography = Calib3d.findHomography(goodReferencePoints, goodScenePoints);

        // Use the homography to project the reference corner coordinates into scene coordinates.
        Core.perspectiveTransform(mReferenceCorners, mCandidateSceneCorners, homography);

        // Convert the scene corners to integer format, as required
        // by the Imgproc.isContourConvex function.
        mCandidateSceneCorners.convertTo(mIntSceneCorners, CvType.CV_32S);

        // Check whether the corners form a convex polygon. If not,
        // (that is, if the corners form a concave polygon), the
        // detection result is invalid because no real perspective can
        // make the corners of a rectangular image look like a concave
        // polygon!
        if (Imgproc.isContourConvex(mIntSceneCorners)) {
            // The corners form a convex polygon, so record them as
            // valid scene corners.
            mCandidateSceneCorners.copyTo(mSceneCorners);
        }
    }

    protected void draw(final Mat src, final Mat dst) {

        if (dst != src) {
            src.copyTo(dst);
        }

        //la resolucion es 960x720
        //la zona entre (x,720) y (x,420) es la zona a despreciar, ahi es por arriba de la lata
        double clampY = 718;
        Imgproc.line(dst, new Point(0, clampY), new Point(960, clampY), colorRed, 4);
        clampY = 420;
        Imgproc.line(dst, new Point(0, clampY), new Point(960, clampY), colorRed, 4);
        Imgproc.line(dst, new Point(450, 450), new Point(650, 650), colorRed, 4);
        Imgproc.line(dst, new Point(450, 650), new Point(650, 450), colorRed, 4);


        double clampX = 720;
        Imgproc.line(dst, new Point(clampX, 0), new Point(clampX, 420), colorGreen, 8);
        //720 a 770 es la zona de aceptacion de centro de la lata!!!!!!!!!!!!!!!!
        clampX = 770;
        Imgproc.line(dst, new Point(clampX, 0), new Point(clampX, 420), colorBlue, 8);

        if (mSceneCorners.height() < 4) {
            // The target has not been found.

            //si no encuentro la imagen en 30 intentos reseteo contador y vuelvo a main
            if (framesCount >= 30){
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("templateX",0);
                intent.putExtra("templateY",0);
                intent.putExtra("distanceX",Double.MAX_VALUE);

                //encontrado entonces reseteo el contador de frames
                framesCount = 0;

                context.startActivity(intent);

            }

            // Draw a thumbnail of the target in the upper-left
            // corner so that the user knows what it is.

            // Compute the thumbnail's larger dimension as half the
            // video frame's smaller dimension.
            int height = mReferenceImage.height();
            int width = mReferenceImage.width();
            final int maxDimension = Math.min(dst.width(), dst.height()) / 2;
            final double aspectRatio = width / (double)height;
            if (height > width) {
                height = maxDimension;
                width = (int)(height * aspectRatio);
            } else {
                width = maxDimension;
                height = (int)(width / aspectRatio);
            }

            // Select the region of interest (ROI) where the thumbnail
            // will be drawn.
            //final Mat dstROI = dst.submat(0, height, 0, width);
            final Mat dstROI = dst.submat(src.height()-height, src.height(), 0, width);

            // Copy a resized reference image into the ROI.
            Imgproc.resize(mReferenceImage, dstROI, dstROI.size(), 0.0, 0.0, Imgproc.INTER_AREA);

            return;
        }

        //00 abajo izq
        //10 arriba izq
        //20 arriba der
        //30 abajo der
        Point p0 = new Point(mSceneCorners.get(0, 0));
        Point p1 = new Point(mSceneCorners.get(1, 0));
        Point p2 = new Point(mSceneCorners.get(2, 0));
        Point p3 = new Point(mSceneCorners.get(3, 0));

        // Outline the found target in green.
        Imgproc.line(dst, p0, p1, colorGreen, 4);
        Imgproc.line(dst, p1, p2, colorGreen, 4);
        Imgproc.line(dst, p2, p3, colorGreen, 4);
        Imgproc.line(dst, p3, p0, colorGreen, 4);

        double distanceTopX = Math.abs(p2.x - p1.x);
        double distanceBottomX = Math.abs(p3.x - p0.x);

        double distanceLeftY = Math.abs(p0.y - p1.y);
        double distanceRightY = Math.abs(p3.y - p2.y);

        double deltaX = Math.abs(distanceTopX - distanceBottomX);
        double deltaY = Math.abs(distanceLeftY - distanceRightY);

        //ToDo: revisar el multiplicador, puse 2 para permitir figuras mas alejadas del rectangulo
        if ((deltaX > 2*distanceBottomX || deltaX > 2*distanceTopX) || (deltaY > 2*distanceLeftY || deltaY > 2*distanceRightY)){
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("templateX",0);
            intent.putExtra("templateY",0);
            intent.putExtra("distanceX",Double.MAX_VALUE);

            //encontrado entonces reseteo el contador de frames
            framesCount = 0;

            context.startActivity(intent);
        }

        double templateCenterX = (p0.x + p1.x + p2.x + p3.x)/4;
        double templateCenterY = (p0.y + p1.y + p2.y + p3.y)/4;
        Point center = new Point(templateCenterX, templateCenterY);
        Imgproc.circle(dst, center, 16, colorGreen, 8);

        if (templateCenterX > 960 || templateCenterY > 420)
            templateCenterX = Double.MAX_VALUE;


        //clampY = 550;
        //Imgproc.line(dst, new Point(50, clampY), new Point(950, clampY), colorBlue, 4);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("templateX",templateCenterX);
        intent.putExtra("templateY",templateCenterY);
        intent.putExtra("distanceX",clampX-templateCenterX);

        //encontrado entonces reseteo el contador de frames
        framesCount = 0;

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


}
