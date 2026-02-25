package cl.gruposm.conectaevaluaciones.detection;

import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.approxPolyDP;
import static org.opencv.imgproc.Imgproc.arcLength;

import android.graphics.Bitmap;
import android.util.Log;

import cl.gruposm.conectaevaluaciones.drawing.DrawOverScreen;
import cl.gruposm.conectaevaluaciones.object.AnswerSheet;
import cl.gruposm.conectaevaluaciones.object.Mark;
import cl.gruposm.conectaevaluaciones.object.Quiz;
import cl.gruposm.conectaevaluaciones.utils.OmrUtil;
import cl.gruposm.conectaevaluaciones.utils.Util;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetectionUtil {

    private static String TAG="DetectionUtil";
    private OmrUtil omrUtil;
    private Quiz quiz;
    private AnswerSheet answerSheet;
    private Point pointLeftTop =  null;
    private Point pointRightTop =  null;
    private Point pointRightBottom =  null;
    private Point pointLeftBottom =  null;
    private Map<Integer, Mark> marks = new HashMap<Integer, Mark>();
    public static int PIXEL_PERCENTAGE = 90;
    public boolean isErrorDetection = false;
    public DetectionUtil(Quiz quiz)
    {
        omrUtil =  new OmrUtil();
        this.quiz =  quiz;
        Log.d("TAG", "correctas: " + quiz.getCorrectas());
        answerSheet =  new AnswerSheet(743,895,0.60,0.95);
        // Configuración por tipo

        answerSheet.setNumAnswers(this.quiz.getTotalPreguntas());
        answerSheet.setOptionsPerAnswers(this.quiz.getTotalOpciones());
        answerSheet.setTipo(this.quiz.getTipo());
        // if(this.quiz.getTipo().equals("A"))
        // {
        //     answerSheet.setNumBlocks(4);
        //     answerSheet.setAnswersPerBlock(20);
        // }
        // // 55 preguntas
        // else if(this.quiz.getTipo().equals("B") || this.quiz.getTipo().equals("C"))
        // {
        //     answerSheet.setNumBlocks(3);
        //     answerSheet.setAnswersPerBlock(20);
        // }
        // else
        // {
        //     answerSheet.setNumBlocks(4);
        //     answerSheet.setAnswersPerBlock(20);
        // }
        answerSheet.setNumBlocks(4);
        answerSheet.setAnswersPerBlock(20);
        answerSheet.setCorrects(quiz.getCorrectas());
        answerSheet.setOptionsMarkCorrects();
        // if(!this.isErrorDetection)
        // {
        //     answerSheet.setOptionsMarkCorrects();
        // }
    }
    public Map<String, Integer> calculateSquare(Bitmap bitmap)
    {
        int cols  = bitmap.getWidth();
        int rows =  bitmap.getHeight();
        int w = (cols*20)/100;
        int maxHeight = rows-w;
        int posY = (maxHeight * 60)/100;
        int initY = (100*rows)/1461;
        Map<String, Integer> calculates = new HashMap<String, Integer>();
        calculates.put("w",w);
        calculates.put("posY",posY);
        calculates.put("initY",initY);
        calculates.put("cols",cols);
        calculates.put("rows",rows);
        return calculates;
    }
    public Map<Integer, Rect> markScreen(Bitmap bitmap)
    {
        Map<String, Integer> calculates = this.calculateSquare(bitmap);
        int cols  = calculates.get("cols");
        int rows =  calculates.get("rows");
        int w = calculates.get("w");
        int posY = calculates.get("posY");
        int initY = calculates.get("initY");
        Point point1Rect1 = new Point(0, initY);
        Point point2Rect1 = new Point(w, w+initY);
        Point point1Rect2 = new Point(cols-w, initY);
        Point point2Rect2 = new Point(cols, w+initY);
        Point point1Rect3 = new Point(0, posY);
        Point point2Rect3 = new Point(w, posY+w);
        Point point1Rect4 = new Point(cols-w, posY);
        Point point2Rect4 = new Point(cols, posY+w);
        Rect rect1 = new Rect(point1Rect1,point2Rect1);
        Rect rect2 = new Rect(point1Rect2,point2Rect2);
        Rect rect3 = new Rect(point1Rect3,point2Rect3);
        Rect rect4 = new Rect(point1Rect4,point2Rect4);
        Map<Integer, Rect> mapRect = new HashMap<Integer, Rect>();
        mapRect.put(1,rect1);
        mapRect.put(2,rect2);
        mapRect.put(3,rect3);
        mapRect.put(4,rect4);
        return mapRect;
    }
    public boolean findFourPoint(Mat rgb,Map<Integer, Rect> mapRect, DrawOverScreen drawOverScreen)
    {

        Mat edgeRoi1 = new Mat(rgb.clone(),mapRect.get(1));
        Mat edgeRoi2 = new Mat(rgb.clone(),mapRect.get(2));
        Mat edgeRoi3 = new Mat(rgb.clone(),mapRect.get(3));
        Mat edgeRoi4 = new Mat(rgb.clone(),mapRect.get(4));
        Mat canny1 = omrUtil.applyCanny(rgb.clone().submat(mapRect.get(1)));
        Mat canny2 = omrUtil.applyCanny(rgb.clone().submat(mapRect.get(2)));
        Mat canny3 = omrUtil.applyCanny(rgb.clone().submat(mapRect.get(3)));
        Mat canny4 = omrUtil.applyCanny(rgb.clone().submat(mapRect.get(4)));
        pointLeftTop = this.findPoint(canny1,1,mapRect.get(1));
        pointRightTop = this.findPoint(canny2,2,mapRect.get(2));
        pointLeftBottom = this.findPoint(canny3,3,mapRect.get(3));
        pointRightBottom = this.findPoint(canny4, 4,mapRect.get(4));
        Log.d("TAG", "point_1:" + pointLeftTop);
        Log.d("TAG", "point_2:" + pointRightTop);
        Log.d("TAG", "point_3:" + pointLeftBottom);
        Log.d("TAG", "point_4:" + pointRightBottom);
        drawOverScreen.rectangle1 = new android.graphics.Rect( 0,0,0,0);
        drawOverScreen.rectangle2 = new android.graphics.Rect( 0,0,0,0);
        drawOverScreen.rectangle3 = new android.graphics.Rect( 0,0,0,0);
        drawOverScreen.rectangle4 = new android.graphics.Rect( 0,0,0,0);
        int adjust = 6;
        int width = 12;
        if(pointLeftTop != null) {
            android.graphics.Rect rectangle1 = new android.graphics.Rect(0 + (int) pointLeftTop.x-adjust, (int) pointLeftTop.y-adjust, width + (int) pointLeftTop.x-adjust, width + (int) pointLeftTop.y-adjust);
            drawOverScreen.rectangle1 = rectangle1;
        }
        if(pointRightTop != null) {
            android.graphics.Rect rectangle2 = new android.graphics.Rect(0 + (int) pointRightTop.x-adjust, (int) pointRightTop.y-adjust, width + (int) pointRightTop.x-adjust, width + (int) pointRightTop.y-adjust);
            drawOverScreen.rectangle2 = rectangle2;

        }
        if(pointLeftBottom != null) {
            android.graphics.Rect rectangle3 = new android.graphics.Rect(0 + (int) pointLeftBottom.x-adjust, (int) pointLeftBottom.y-adjust, width + (int) pointLeftBottom.x-adjust, width + (int) pointLeftBottom.y-adjust);
            drawOverScreen.rectangle3 = rectangle3;
        }
        if(pointRightBottom != null) {
            android.graphics.Rect rectangle4 = new android.graphics.Rect(0 + (int) pointRightBottom.x-adjust, (int) pointRightBottom.y-adjust, width + (int) pointRightBottom.x-adjust, width + (int) pointRightBottom.y-adjust);
            drawOverScreen.rectangle4 = rectangle4;
        }
        drawOverScreen.invalidate();
        if(pointLeftTop != null  && pointRightTop != null && pointLeftBottom != null && pointRightBottom != null)
        {
            int a = (int) Math.abs(pointLeftBottom.x-pointRightBottom.x);
            int b = (int) Math.abs(pointLeftBottom.y-pointLeftTop.y);
            int result = (int) Math.sqrt((a*a)+(b*b));
            int diagonal = Util.diagonalRectangle(pointRightBottom, pointLeftTop);
            int abs = Math.abs(result-diagonal);
            Rect rectPaper = new Rect(pointLeftTop,pointRightBottom);
            double ratio = (double)rectPaper.width/(double)rectPaper.height;
            return true;
        }
        return false;
    }
    public Mat adjustPerpective(Mat rgb)
    {
        List<Point> sourcePoints = new ArrayList<Point>();
        sourcePoints.add(pointLeftTop);
        sourcePoints.add(pointRightTop);
        sourcePoints.add(pointRightBottom);
        sourcePoints.add(pointLeftBottom);
        Mat startM = Converters.vector_Point2f_to_Mat(sourcePoints);
        List<Point> destPoints = new ArrayList<>();
        Point ptCornerPoints[];
        ptCornerPoints = this.getNewCornerPoints(answerSheet.getWidth(), answerSheet.getHeight());
        for(int i=0; i< ptCornerPoints.length; i++){
            destPoints.add(ptCornerPoints[i]);
        }
        Mat endM = Converters.vector_Point2f_to_Mat(destPoints);
        Mat outputMat = new Mat(answerSheet.getWidth(), answerSheet.getHeight(), CvType.CV_8UC4);
        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);
        Imgproc.warpPerspective(rgb, outputMat, perspectiveTransform, new Size(answerSheet.getWidth(), answerSheet.getHeight()));
        return outputMat;
    }
    public String findRut(Mat warped)
    {
        Mat threshold = omrUtil.applyThreshold(warped.clone());
        Point point1Rect1 = new Point(482, 157);
        Point point2Rect1 = new Point(704, 359);
        Rect rect1 = new Rect(point1Rect1,point2Rect1);
        Mat crop = threshold.submat(rect1);
        double spaceMarkY = 4;
        int spaceMarkX = 3;
        int totalVertical = 11;
        int totalHorizontal = 12;
        double heightMark = 15;
        double widthMark = 16;
        double poinInitX = 0;
        double poinInitY = 0;
        String rut = "";
        boolean error = false;
        for(int k=1; k<=totalHorizontal; k++)
        {
            int countfindMark = 0;
            for (int i = 1; i <= totalVertical; i++) {
                Point p1 = new Point(poinInitX, poinInitY);
                Point p2 = new Point(poinInitX+widthMark, poinInitY+heightMark);
                Rect r = new Rect(p1, p2);
                int noOfWhitePixels = Core.countNonZero(crop.submat(r));
                double pixel = ((double)noOfWhitePixels / ((double)r.width*(double)r.height)) * 100;

                if(pixel < PIXEL_PERCENTAGE)
                {
                    countfindMark++;
                    String numFinderStr = String.valueOf(i-1);
                    if(numFinderStr.equals("10"))
                    {
                        numFinderStr = "K";
                    }

                    rut += numFinderStr;
                }
                Imgproc.rectangle(crop, r, new Scalar(0, 0, 255), 0);
                poinInitY += (heightMark+spaceMarkY)-0.3;
            }
            if(countfindMark > 1)
            {
                error = true;
            }
            if(k == 1 && countfindMark == 0)
            {
                rut += "0";
            }
            poinInitY = 0;
            poinInitX += (widthMark+spaceMarkX)-0.3;
        }
        if(error)
        {
            return null;
        }

        if(rut.length() == 9)
        {
            boolean isRut = Util.isRut(rut);
            if(isRut)
            {
                return rut;
            }
            //return rut;
        }
        return null;
    }
    public Mat findAnswers(Mat warped)
    {
        marks.clear();
        Mat threshold = omrUtil.applyThreshold(warped.clone());
        double heightMark = 15;
        double widthMark = 15;
        double poinInitX = 0;
        double poinInitY = 0;
        double spaceMarkY = 4;
        int spaceMarkX = 8;
        Map<Integer, String> letters = new HashMap<Integer, String>();
        letters.put(1,"A");
        letters.put(2,"B");
        letters.put(3,"C");
        letters.put(4,"D");
        letters.put(5,"E");
        int sumAnswers = 0;
        int countOptions = 1;
        int sumMarks = 0;
        for(int i = 1; i <= answerSheet.getNumBlocks(); i++)
        {
            Point point1Rect1 = new Point(92, 478);
            Point point2Rect1 = new Point(201, 858);
            if(i == 2)
            {
                point1Rect1.x = 248;
                point2Rect1.x = 358;
            }
            if(i == 3)
            {
                point1Rect1.x = 405;
                point2Rect1.x = 515;
            }
            if(i == 4)
            {
                point1Rect1.x = 562;
                point2Rect1.x = 671;
            }
            Rect rect1 = new Rect(point1Rect1,point2Rect1);
            Mat crop = threshold.submat(rect1);
            for (int k = 1; k <= answerSheet.getAnswersPerBlock(i); k++)
            {

                int countfindMark = 0;
                int indexKey = k + sumAnswers;
                boolean isOpenQuestion = answerSheet.isOpenQuestion(indexKey);
                String letterMarked = "";
                for (int j = 1; j <= answerSheet.getOptionsPerAnswers(); j++)
                {
                    Point p1 = new Point(poinInitX, poinInitY);
                    Point p2 = new Point(poinInitX + widthMark, poinInitY + heightMark);
                    Rect r = new Rect(p1, p2);
                    int noOfWhitePixels = Core.countNonZero(crop.submat(r));
                    double pixel = ((double) noOfWhitePixels / ((double) r.width * (double) r.height)) * 100;
                    if(k==3 && i ==1 && j== 4 ) {
                        Log.d("pixel", "pixel:" + pixel);


                    }

                    if (pixel < PIXEL_PERCENTAGE) {
                        int p1Context = (int) (r.x + point1Rect1.x);
                        int p2Context = (int) (r.y + point1Rect1.y);
                        Rect rectContext = new Rect(p1Context, p2Context, r.width, r.height);
                        String letter = letters.get(j);
                        if (!isOpenQuestion) {
                            letterMarked = letter;
                            answerSheet.setOptionMarkUser(countOptions,letter);
                            boolean isCorrect = answerSheet.checkOptionMark(countOptions);
                            Mark  mark =  new Mark(rectContext,isCorrect);
                            marks.put(countOptions, mark);
                            countfindMark++;
                            sumMarks++;
                        }
                    }
                    Imgproc.rectangle(crop, r, new Scalar(0, 0, 255), 1);
                    poinInitX += (widthMark + spaceMarkX) + 0.3;
                    countOptions ++;
                }
                poinInitX = 0;
                poinInitY += (heightMark + spaceMarkY) + 0.2;
                if (countfindMark > 1) {
                    letterMarked = "";
                }
                if (isOpenQuestion) {
                    letterMarked = "";
                }
                answerSheet.setAnswerLetter(indexKey,letterMarked);
            }
            poinInitX = 0;
            poinInitY = 0;
            sumAnswers += answerSheet.getAnswersPerBlock(i);
        }
        Log.d("sumMarks", "sumMarks:" + sumMarks);
        return threshold;

    }
    public Mat drawAnswer(Mat warped)
    {
        for (Map.Entry<Integer, Mark> entry : marks.entrySet()) {
            boolean isCorrect = entry.getValue().isCorrect();
            Scalar scalar;
            if(isCorrect)
            {
                scalar =  new Scalar(0, 255, 0);
            }
            else
            {
                scalar =  new Scalar(255, 0, 0);
            }
            Imgproc.rectangle(warped, entry.getValue().getRect(), scalar, 2);
        }
        return warped;
    }
    public Map<String, String> printResult()
    {
        answerSheet.setResults();
        Map<String, String> results = new HashMap<String, String>();
        results.put("corrects",String.valueOf(answerSheet.getCorrectAnswers()));
        results.put("incorrects",String.valueOf(answerSheet.getIncorrectAnswers()));
        results.put("omitteds",String.valueOf(answerSheet.getOmittedAnswers()));
        results.put("percentages",String.valueOf(answerSheet.getPercentageAnswers()));
        results.put("total",String.valueOf(answerSheet.getTotalEvaluated()));
        results.put("json",String.valueOf(answerSheet.getJsonAnswer()));
        return results;
    }
    private Point[] getNewCornerPoints(int pictureWidth, int pictureHeight){
        Point pt[] = new Point[4];
        pt[0] = new Point(0, 0);
        pt[1] = new Point(pictureWidth, 0);
        pt[2] = new Point(pictureWidth, pictureHeight);
        pt[3] = new Point(0, pictureHeight);
        for(int i=0; i<4; i++){
            if(pt[i]==null)
                return null;
        }
        return pt;
    }
    private Point findPoint(Mat roi, int point, Rect _rect)
    {
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy =  new Mat();
        Imgproc.findContours(roi.clone(),contours,hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        if(point == 1) {
            Log.d(TAG, "rect.widthsize:" + contours.size());
        }
        for(int i = 0; i < contours.size(); i++)
        {
            MatOfPoint2f approxCurve = new MatOfPoint2f( contours.get(i).toArray() );
            approxPolyDP(approxCurve, approxCurve, 0.02 * arcLength(approxCurve, true), true);

            if(approxCurve.toArray().length == 4){
                Rect rect = Imgproc.boundingRect(contours.get(i));
                double ratio = (double)rect.height/(double)rect.width;
                if(point == 1) {
                    Log.d(TAG, "rect.width:" + rect.width+_rect.width);
                    Log.d(TAG, "rect.width:" + ratio + "_" + point);
                }

                if(ratio < 0.9 || ratio > 1.1)
                {
                    return null;
                }
                int posX = (rect.x+(rect.width/2)) + _rect.x;
                int posY = (rect.y+(rect.height/2)) + _rect.y;
                return new Point(posX, posY);
            }
        }
        hierarchy.release();
        contours.clear();
        return null;
    }

}
