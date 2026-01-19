package com.gruposm.chile.appclavepaes26.utils;

import static org.opencv.core.CvType.CV_8UC1;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class OmrUtil {
    private static int widthGaussian = 5;
    private static int heightGaussian = 5;
    public  Mat applyCanny(Mat roi)
    {
        Mat blurred =  new Mat();
        Mat edged =  new Mat();
        Size s = new Size(5,5);
        blurred = new Mat(roi.size(), CV_8UC1);
        Imgproc.GaussianBlur(roi,blurred,s,0);
        edged = new Mat(blurred.size(), CV_8UC1);
        Imgproc.Canny(blurred,edged,100,300,3,false);
        Imgproc.dilate(edged, edged, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(20, 20)));
        return edged;
    }
    public Mat applyThreshold(Mat roi)
    {
        Mat threshold =  new Mat();
        Mat gray =  new Mat();
        Mat matGaussianBlur = new Mat();

        Imgproc.GaussianBlur(roi, matGaussianBlur, new Size(widthGaussian, heightGaussian), 3, 2.5);
        Imgproc.cvtColor(matGaussianBlur, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.adaptiveThreshold(gray, threshold, 255,
                Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY, 21, 20);
        Imgproc.adaptiveThreshold(threshold, threshold, 255,
                Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY, 21, 20);
        Imgproc.adaptiveThreshold(threshold, threshold, 255,
                Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY, 21, 20);
        Imgproc.adaptiveThreshold(threshold, threshold, 255,
                Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY, 21, 20);
        Imgproc.adaptiveThreshold(threshold, threshold, 255,
                Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY, 21, 20);

        /*Imgproc.adaptiveThreshold(gray, threshold, 255,
                Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY, 31, 10);*/
        return threshold;
    }
}
