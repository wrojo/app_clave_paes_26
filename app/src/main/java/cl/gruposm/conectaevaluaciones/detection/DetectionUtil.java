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
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DetectionUtil {

    private static String TAG="DetectionUtil";
    private enum SheetTemplate {
        LEGACY,
        ANSWER_SHEET_2
    }

    private static final int LEGACY_SHEET_WIDTH = 743;
    private static final int LEGACY_SHEET_HEIGHT = 895;
    private static final int LEGACY_BLOCKS = 4;
    private static final int LEGACY_ANSWERS_PER_BLOCK = 20;

    private static final int SHEET_2_WIDTH = 1700;
    private static final int SHEET_2_HEIGHT = 2200;
    private static final int SHEET_2_BLOCKS = 2;
    private static final int SHEET_2_ANSWERS_PER_BLOCK = 25;
    // Coordinates below are normalized to warped space (markers mapped to image corners).
    private static final int[] SHEET_2_RUT_COL_X = {223, 272, 346, 393, 439, 515, 561, 608, 683};
    private static final int[] SHEET_2_RUT_ROW_Y = {78, 127, 177, 226, 275, 325, 374, 423, 474, 523, 585};
    private static final double SHEET_2_RUT_MARK_WIDTH = 40;
    private static final double SHEET_2_RUT_MARK_HEIGHT = 42;
    private static final int SHEET_2_RUT_MARK_INSET = 4;
    private static final int SHEET_2_RUT_PIXEL_PERCENTAGE = 96;
    private static final int SHEET_2_RUT_FALLBACK_PIXEL_PERCENTAGE = 98;
    private static final double SHEET_2_RUT_MIN_GAP = 2.0;
    private static final double SHEET_2_RUT_MULTI_MARK_MIN_GAP = 3.5;
    private static final int SHEET_2_RUT_WEAK_FALLBACK_PIXEL_PERCENTAGE = 99;
    private static final int SHEET_2_RUT_WEAK_FALLBACK_SECOND_MIN = 99;
    private static final int[] SHEET_2_BLOCK_START_X = {198, 729};
    private static final double SHEET_2_ANSWERS_START_Y = 721;
    private static final double SHEET_2_OPTION_STEP_X = 79;
    private static final double SHEET_2_ANSWER_STEP_Y = 58;
    // Progressive vertical correction: keeps top aligned and moves lower rows slightly up.
    private static final double SHEET_2_ANSWER_BOTTOM_OFFSET_Y = -14.0;
    private static final double SHEET_2_MARK_WIDTH = 44;
    private static final double SHEET_2_MARK_HEIGHT = 48;
    private static final int SHEET_2_ANSWER_MARK_INSET = 5;
    private static final int SHEET_2_ANSWER_CENTER_MARK_INSET = 8;
    private static final int SHEET_2_ANSWER_PIXEL_PERCENTAGE = 94;
    private static final int SHEET_2_ANSWER_FALLBACK_PIXEL_PERCENTAGE = 95;
    private static final int SHEET_2_ANSWER_FALLBACK_CENTER_PIXEL_PERCENTAGE = 94;
    private static final int SHEET_2_ANSWER_STRONG_CENTER_PIXEL_PERCENTAGE = 95;
    private static final double SHEET_2_ANSWER_MIN_GAP = 5.5;
    private static final double SHEET_2_ANSWER_MIN_RELATIVE_DELTA = 5.0;
    private static final double SHEET_2_ANSWER_STRONG_MIN_GAP = 5.0;
    private static final double SHEET_2_ANSWER_STRONG_MIN_RELATIVE_DELTA = 4.5;
    private static final double SHEET_2_ANSWER_HIGH_CONF_GAP = 7.5;
    private static final double SHEET_2_ANSWER_HIGH_CONF_RELATIVE_DELTA = 7.0;
    private static final double SHEET_2_ANSWER_MULTI_MARK_MIN_GAP = 4.5;
    private static final double SHEET_2_ANSWER_MULTI_MARK_MIN_RELATIVE_DELTA = 6.0;
    private static final double SHEET_2_ANSWER_NEAR_BEST_DELTA = 2.5;
    private static final int SHEET_2_ANSWER_DARK_MULTI_PIXEL_PERCENTAGE = 50;
    private static final int SHEET_2_ANSWER_DARK_MULTI_CENTER_PIXEL_PERCENTAGE = 52;
    private static final double SHEET_2_ANSWER_DARK_MULTI_MIN_RELATIVE_DELTA = 10.0;

    private OmrUtil omrUtil;
    private Quiz quiz;
    private AnswerSheet answerSheet;
    private SheetTemplate sheetTemplate;
    private String levelName;
    private String lastRutDebug = "";
    private String lastCornerDebug = "";
    private Point pointLeftTop =  null;
    private Point pointRightTop =  null;
    private Point pointRightBottom =  null;
    private Point pointLeftBottom =  null;
    private Map<Integer, Mark> marks = new HashMap<Integer, Mark>();
    public static int PIXEL_PERCENTAGE = 90;
    public boolean isErrorDetection = false;
    public DetectionUtil(Quiz quiz)
    {
        this(quiz, null);
    }
    public DetectionUtil(Quiz quiz, String levelName)
    {
        omrUtil =  new OmrUtil();
        this.quiz =  quiz;
        this.levelName = levelName;
        this.sheetTemplate = resolveSheetTemplate(quiz, levelName);
        Log.d("TAG", "correctas: " + quiz.getCorrectas());
        if (this.sheetTemplate == SheetTemplate.ANSWER_SHEET_2) {
            answerSheet =  new AnswerSheet(SHEET_2_WIDTH, SHEET_2_HEIGHT,0.60,0.95);
            answerSheet.setNumBlocks(SHEET_2_BLOCKS);
            answerSheet.setAnswersPerBlock(SHEET_2_ANSWERS_PER_BLOCK);
        } else {
            answerSheet =  new AnswerSheet(LEGACY_SHEET_WIDTH, LEGACY_SHEET_HEIGHT,0.60,0.95);
            answerSheet.setNumBlocks(LEGACY_BLOCKS);
            answerSheet.setAnswersPerBlock(LEGACY_ANSWERS_PER_BLOCK);
        }
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
        answerSheet.setCorrects(quiz.getCorrectas());
        answerSheet.setOptionsMarkCorrects();
        Log.d(TAG, "sheetTemplate=" + this.sheetTemplate.name() + " apiTemplate=" + quiz.getTemplateHojaRespuesta() + " width=" + answerSheet.getWidth() + " height=" + answerSheet.getHeight());
        // if(!this.isErrorDetection)
        // {
        //     answerSheet.setOptionsMarkCorrects();
        // }
    }
    public Map<String, Integer> calculateSquare(Bitmap bitmap)
    {
        int cols  = bitmap.getWidth();
        int rows =  bitmap.getHeight();
        if (this.sheetTemplate == SheetTemplate.ANSWER_SHEET_2) {
            int w = (cols*18)/100;
            int posY = (rows * 71)/100;
            int initY = (rows * 20)/100;
            Map<String, Integer> calculates = new HashMap<String, Integer>();
            calculates.put("w",w);
            calculates.put("posY",posY);
            calculates.put("initY",initY);
            calculates.put("cols",cols);
            calculates.put("rows",rows);
            return calculates;
        }
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
    public String getLastRutDebug()
    {
        return lastRutDebug;
    }
    public String getLastCornerDebug()
    {
        return lastCornerDebug;
    }
    public boolean isAnswerSheet2Template()
    {
        return this.sheetTemplate == SheetTemplate.ANSWER_SHEET_2;
    }
    private void setRutDebug(String message)
    {
        this.lastRutDebug = message;
        Log.d(TAG, "rut_debug " + message);
    }
    private void setCornerDebug(String message)
    {
        this.lastCornerDebug = message;
        Log.d(TAG, "corner_debug " + message);
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
        if (this.sheetTemplate == SheetTemplate.ANSWER_SHEET_2) {
            pointLeftTop = this.findPointSheet2(edgeRoi1, 1, mapRect.get(1));
            pointRightTop = this.findPointSheet2(edgeRoi2, 2, mapRect.get(2));
            pointLeftBottom = this.findPointSheet2(edgeRoi3, 3, mapRect.get(3));
            pointRightBottom = this.findPointSheet2(edgeRoi4, 4, mapRect.get(4));
            Mat canny1 = omrUtil.applyCanny(rgb.clone().submat(mapRect.get(1)));
            Mat canny2 = omrUtil.applyCanny(rgb.clone().submat(mapRect.get(2)));
            Mat canny3 = omrUtil.applyCanny(rgb.clone().submat(mapRect.get(3)));
            Mat canny4 = omrUtil.applyCanny(rgb.clone().submat(mapRect.get(4)));
            if (pointLeftTop == null) {
                pointLeftTop = this.findPoint(canny1, 1, mapRect.get(1));
                if (pointLeftTop != null) {
                    Log.d(TAG, "sheet2 fallback point_1 used");
                }
            }
            if (pointRightTop == null) {
                pointRightTop = this.findPoint(canny2, 2, mapRect.get(2));
                if (pointRightTop != null) {
                    Log.d(TAG, "sheet2 fallback point_2 used");
                }
            }
            if (pointLeftBottom == null) {
                pointLeftBottom = this.findPoint(canny3, 3, mapRect.get(3));
                if (pointLeftBottom != null) {
                    Log.d(TAG, "sheet2 fallback point_3 used");
                }
            }
            if (pointRightBottom == null) {
                pointRightBottom = this.findPoint(canny4, 4, mapRect.get(4));
                if (pointRightBottom != null) {
                    Log.d(TAG, "sheet2 fallback point_4 used");
                }
            }
            canny1.release();
            canny2.release();
            canny3.release();
            canny4.release();
        } else {
            Mat canny1 = omrUtil.applyCanny(rgb.clone().submat(mapRect.get(1)));
            Mat canny2 = omrUtil.applyCanny(rgb.clone().submat(mapRect.get(2)));
            Mat canny3 = omrUtil.applyCanny(rgb.clone().submat(mapRect.get(3)));
            Mat canny4 = omrUtil.applyCanny(rgb.clone().submat(mapRect.get(4)));
            pointLeftTop = this.findPoint(canny1,1,mapRect.get(1));
            pointRightTop = this.findPoint(canny2,2,mapRect.get(2));
            pointLeftBottom = this.findPoint(canny3,3,mapRect.get(3));
            pointRightBottom = this.findPoint(canny4, 4,mapRect.get(4));
        }
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
            double topWidth = distance(pointLeftTop, pointRightTop);
            double bottomWidth = distance(pointLeftBottom, pointRightBottom);
            double leftHeight = distance(pointLeftTop, pointLeftBottom);
            double rightHeight = distance(pointRightTop, pointRightBottom);
            double widthAvg = (topWidth + bottomWidth) / 2.0;
            double heightAvg = (leftHeight + rightHeight) / 2.0;
            if (heightAvg <= 0 || widthAvg <= 0) {
                return false;
            }
            double ratio = widthAvg / heightAvg;
            double widthDelta = Math.abs(topWidth - bottomWidth) / widthAvg;
            double heightDelta = Math.abs(leftHeight - rightHeight) / heightAvg;
            Log.d(TAG, "shape ratio=" + ratio + " widthDelta=" + widthDelta + " heightDelta=" + heightDelta);

            if (this.sheetTemplate == SheetTemplate.ANSWER_SHEET_2) {
                boolean isValidShape = ratio > 0.74 && ratio < 0.92;
                boolean isValidParallel = widthDelta < 0.18 && heightDelta < 0.18;
                boolean isValidSize = widthAvg > (rgb.cols() * 0.45) && heightAvg > (rgb.rows() * 0.35);
                if (!isValidShape || !isValidParallel || !isValidSize) {
                    Log.d(TAG, "shape rejected sheet2: validShape=" + isValidShape + " validParallel=" + isValidParallel + " validSize=" + isValidSize);
                    setCornerDebug(
                            "invalid_shape ratio=" + String.format("%.3f", ratio)
                                    + " wDelta=" + String.format("%.3f", widthDelta)
                                    + " hDelta=" + String.format("%.3f", heightDelta)
                                    + " wAvg=" + ((int) Math.round(widthAvg))
                                    + " hAvg=" + ((int) Math.round(heightAvg))
                    );
                    return false;
                }
            }
            setCornerDebug(
                    "ok p1=" + pointLeftTop + " p2=" + pointRightTop + " p3=" + pointLeftBottom + " p4=" + pointRightBottom
                            + " ratio=" + String.format("%.3f", ratio)
            );
            return true;
        }
        setCornerDebug("missing_points p1=" + pointLeftTop + " p2=" + pointRightTop + " p3=" + pointLeftBottom + " p4=" + pointRightBottom);
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
        if (this.sheetTemplate == SheetTemplate.ANSWER_SHEET_2) {
            return this.findRutSheet2(warped);
        }
        return this.findRutLegacy(warped);
    }
    private String findRutLegacy(Mat warped)
    {
        setRutDebug("legacy_start");
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
            setRutDebug("legacy_invalid multiple_marks");
            return null;
        }

        if(rut.length() == 9)
        {
            boolean isRut = Util.isRut(rut);
            if(isRut)
            {
                setRutDebug("legacy_ok raw=" + rut);
                return rut;
            }
            //return rut;
        }
        setRutDebug("legacy_invalid checksum_or_length raw=" + rut);
        return null;
    }
    private String findRutSheet2(Mat warped)
    {
        setRutDebug("sheet2_start");
        Mat threshold = omrUtil.applyThreshold(warped.clone());
        Mat thresholdSoft = applyThresholdSheet2Soft(warped);
        try {
            StringBuilder rutBuilder = new StringBuilder();
            for (int col = 0; col < SHEET_2_RUT_COL_X.length; col++) {
                int maxRows = SHEET_2_RUT_ROW_Y.length;
                if (col < SHEET_2_RUT_COL_X.length - 1) {
                    maxRows = SHEET_2_RUT_ROW_Y.length - 1;
                }
                int markedRow = -1;
                int bestRow = -1;
                int marksCount = 0;
                StringBuilder debugValues = new StringBuilder();
                double bestPixel = 101;
                double secondPixel = 101;
                for (int row = 0; row < maxRows; row++) {
                    Rect rectMark = buildRectFromAbsolute(
                            threshold,
                            SHEET_2_RUT_COL_X[col],
                            SHEET_2_RUT_ROW_Y[row],
                            SHEET_2_RUT_MARK_WIDTH,
                            SHEET_2_RUT_MARK_HEIGHT
                    );
                    if (rectMark == null) {
                        continue;
                    }
                    double pixelBase = whitePixelPercentage(threshold, rectMark, SHEET_2_RUT_MARK_INSET);
                    double pixelSoft = whitePixelPercentage(thresholdSoft, rectMark, SHEET_2_RUT_MARK_INSET);
                    double pixel = Math.min(pixelBase, pixelSoft);
                    if (debugValues.length() > 0) {
                        debugValues.append(" ");
                    }
                    debugValues.append(row).append(":").append((int) Math.round(pixel));
                    if (pixel < bestPixel) {
                        secondPixel = bestPixel;
                        bestPixel = pixel;
                        bestRow = row;
                    } else if (pixel < secondPixel) {
                        secondPixel = pixel;
                    }
                    if (pixel <= SHEET_2_RUT_PIXEL_PERCENTAGE) {
                        marksCount++;
                        markedRow = row;
                    }
                    Imgproc.rectangle(threshold, rectMark, new Scalar(0, 0, 255), 0);
                }
                double gap = secondPixel - bestPixel;
                Log.d(TAG, "sheet2_rut col=" + col + " marks=" + marksCount + " row=" + markedRow + " best=" + ((int) Math.round(bestPixel)) + " second=" + ((int) Math.round(secondPixel)) + " gap=" + String.format("%.1f", gap) + " values=" + debugValues);
                if (marksCount == 0) {
                    boolean canUseBestCandidate = bestRow >= 0
                            && bestPixel <= SHEET_2_RUT_FALLBACK_PIXEL_PERCENTAGE
                            && gap >= SHEET_2_RUT_MIN_GAP;
                    boolean canUseWeakBestCandidate = bestRow >= 0
                            && bestPixel <= SHEET_2_RUT_WEAK_FALLBACK_PIXEL_PERCENTAGE
                            && secondPixel >= SHEET_2_RUT_WEAK_FALLBACK_SECOND_MIN;
                    if (canUseBestCandidate) {
                        markedRow = bestRow;
                        Log.d(TAG, "sheet2_rut resolve no_mark col=" + col + " selectedRow=" + markedRow + " gap=" + String.format("%.1f", gap));
                    } else if (canUseWeakBestCandidate) {
                        markedRow = bestRow;
                        Log.d(TAG, "sheet2_rut resolve weak_no_mark col=" + col + " selectedRow=" + markedRow + " best=" + ((int) Math.round(bestPixel)) + " second=" + ((int) Math.round(secondPixel)));
                    } else {
                        if (col == 0) {
                            rutBuilder.append("0");
                            continue;
                        }
                        Log.d(TAG, "sheet2_rut invalid: no mark at col=" + col);
                        setRutDebug("sheet2_invalid no_mark col=" + col + " best=" + ((int) Math.round(bestPixel)) + " second=" + ((int) Math.round(secondPixel)) + " values=" + debugValues);
                        return null;
                    }
                }
            if (marksCount > 1) {
                if (bestRow >= 0 && bestPixel <= SHEET_2_RUT_FALLBACK_PIXEL_PERCENTAGE && gap >= SHEET_2_RUT_MULTI_MARK_MIN_GAP) {
                    markedRow = bestRow;
                    Log.d(TAG, "sheet2_rut resolve multiple col=" + col + " selectedRow=" + markedRow + " gap=" + String.format("%.1f", gap));
                } else if (bestRow >= 0 && bestPixel <= SHEET_2_RUT_FALLBACK_PIXEL_PERCENTAGE) {
                    markedRow = bestRow;
                    Log.d(TAG, "sheet2_rut resolve weak_multiple col=" + col + " selectedRow=" + markedRow + " best=" + ((int) Math.round(bestPixel)) + " second=" + ((int) Math.round(secondPixel)) + " gap=" + String.format("%.1f", gap));
                } else {
                    Log.d(TAG, "sheet2_rut invalid: multiple marks at col=" + col);
                    setRutDebug("sheet2_invalid multiple_marks col=" + col + " best=" + ((int) Math.round(bestPixel)) + " second=" + ((int) Math.round(secondPixel)) + " gap=" + String.format("%.1f", gap) + " values=" + debugValues);
                    return null;
                }
                }
                if (markedRow < 0 && bestRow >= 0 && bestPixel <= SHEET_2_RUT_FALLBACK_PIXEL_PERCENTAGE && gap >= SHEET_2_RUT_MIN_GAP) {
                    markedRow = bestRow;
                    Log.d(TAG, "sheet2_rut resolve fallback col=" + col + " selectedRow=" + markedRow + " gap=" + String.format("%.1f", gap));
                }
                if (markedRow < 0) {
                    if (col == 0) {
                        rutBuilder.append("0");
                        continue;
                    }
                    Log.d(TAG, "sheet2_rut invalid: unresolved col=" + col);
                    setRutDebug("sheet2_invalid unresolved col=" + col + " best=" + ((int) Math.round(bestPixel)) + " second=" + ((int) Math.round(secondPixel)) + " values=" + debugValues);
                    return null;
                }
                if (markedRow == 10) {
                    rutBuilder.append("K");
                } else {
                    rutBuilder.append(markedRow);
                }
            }
            String rut = rutBuilder.toString();
            Log.d(TAG, "sheet2_rut raw=" + rut);
            if (rut.length() == 9) {
                boolean isRut = Util.isRut(rut);
                if (isRut) {
                    setRutDebug("sheet2_ok raw=" + rut);
                    return rut;
                }
                Log.d(TAG, "sheet2_rut invalid checksum raw=" + rut);
                setRutDebug("sheet2_invalid checksum raw=" + rut);
                return null;
            }
            setRutDebug("sheet2_invalid length raw=" + rut + " len=" + rut.length());
            return null;
        } finally {
            thresholdSoft.release();
        }
    }
    public Mat findAnswers(Mat warped)
    {
        if (this.sheetTemplate == SheetTemplate.ANSWER_SHEET_2) {
            return this.findAnswersSheet2(warped);
        }
        return this.findAnswersLegacy(warped);
    }
    private Mat findAnswersLegacy(Mat warped)
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
    private Mat findAnswersSheet2(Mat warped)
    {
        marks.clear();
        Mat enhanced = enhanceSheet2Answers(warped);
        Mat threshold = omrUtil.applyThreshold(enhanced.clone());
        Mat thresholdSoft = applyThresholdSheet2Soft(enhanced);
        Mat thresholdSoftClean = thresholdSoft.clone();
        Mat closeKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.morphologyEx(thresholdSoftClean, thresholdSoftClean, Imgproc.MORPH_CLOSE, closeKernel);
        Imgproc.medianBlur(thresholdSoftClean, thresholdSoftClean, 3);
        closeKernel.release();
        Mat answerInkMask = buildSheet2AnswerInkMask(enhanced);
        Mat thresholdCalc = new Mat();
        // Keep the darkest response between hard/soft threshold to preserve weak pencil marks.
        Core.min(threshold, thresholdSoftClean, thresholdCalc);
        Mat thresholdDebugGray = thresholdCalc.clone();
        Core.subtract(thresholdDebugGray, answerInkMask, thresholdDebugGray);
        Mat thresholdDebug = new Mat();
        Imgproc.cvtColor(thresholdDebugGray, thresholdDebug, Imgproc.COLOR_GRAY2BGR);
        Map<Integer, String> letters = new HashMap<Integer, String>();
        letters.put(1, "A");
        letters.put(2, "B");
        letters.put(3, "C");
        letters.put(4, "D");
        letters.put(5, "E");

        int sumAnswers = 0;
        int countOptions = 1;
        int sumMarks = 0;
        for (int block = 0; block < SHEET_2_BLOCK_START_X.length; block++) {
            for (int question = 1; question <= SHEET_2_ANSWERS_PER_BLOCK; question++) {
                int indexKey = question + sumAnswers;
                if (indexKey > answerSheet.getNumAnswers()) {
                    break;
                }
                boolean isOpenQuestion = answerSheet.isOpenQuestion(indexKey);
                String letterMarked = "";
                int marksCount = 0;
                double bestPixel = 101;
                double secondPixel = 101;
                int bestOption = -1;
                Rect bestRect = null;
                int bestOptionGlobalIndex = -1;
                double bestCenterPixel = 101;
                double sumPixel = 0;
                int countPixel = 0;
                int optionsCount = answerSheet.getOptionsPerAnswers();
                double[] optionPixels = new double[optionsCount + 1];
                Rect[] optionRects = new Rect[optionsCount + 1];
                for (int i = 0; i <= optionsCount; i++) {
                    optionPixels[i] = 100;
                }
                StringBuilder debugValues = new StringBuilder();
                for (int option = 1; option <= optionsCount; option++) {
                    int currentOptionGlobalIndex = countOptions;
                    double posX = SHEET_2_BLOCK_START_X[block] + ((option - 1) * SHEET_2_OPTION_STEP_X);
                    double rowIndex = question - 1;
                    double rowRatio = SHEET_2_ANSWERS_PER_BLOCK > 1
                            ? (rowIndex / (SHEET_2_ANSWERS_PER_BLOCK - 1.0))
                            : 0.0;
                    double rowCurveOffset = SHEET_2_ANSWER_BOTTOM_OFFSET_Y * rowRatio * rowRatio;
                    double posY = SHEET_2_ANSWERS_START_Y + (rowIndex * SHEET_2_ANSWER_STEP_Y) + rowCurveOffset;
                    Rect rectMark = buildRectFromAbsolute(
                            threshold,
                            posX,
                            posY,
                            SHEET_2_MARK_WIDTH,
                            SHEET_2_MARK_HEIGHT
                    );
                    if (rectMark != null) {
                        optionRects[option] = rectMark;
                        double pixelBase = whitePixelPercentage(threshold, rectMark, SHEET_2_ANSWER_MARK_INSET);
                        double pixelSoft = whitePixelPercentage(thresholdSoftClean, rectMark, SHEET_2_ANSWER_MARK_INSET);
                        double inkPixel = whitePixelPercentage(answerInkMask, rectMark, SHEET_2_ANSWER_MARK_INSET + 1);
                        double pixelCalc = whitePixelPercentage(thresholdCalc, rectMark, SHEET_2_ANSWER_MARK_INSET);
                        double pixel = Math.min(Math.min(pixelBase, pixelSoft), pixelCalc) - (inkPixel * 0.20);
                        if (pixel < 0) {
                            pixel = 0;
                        } else if (pixel > 100) {
                            pixel = 100;
                        }
                        double centerBase = whitePixelPercentage(threshold, rectMark, SHEET_2_ANSWER_CENTER_MARK_INSET);
                        double centerSoft = whitePixelPercentage(thresholdSoftClean, rectMark, SHEET_2_ANSWER_CENTER_MARK_INSET);
                        double centerInk = whitePixelPercentage(answerInkMask, rectMark, SHEET_2_ANSWER_CENTER_MARK_INSET + 1);
                        double centerCalc = whitePixelPercentage(thresholdCalc, rectMark, SHEET_2_ANSWER_CENTER_MARK_INSET);
                        double centerPixel = Math.min(Math.min(centerBase, centerSoft), centerCalc) - (centerInk * 0.25);
                        if (centerPixel < 0) {
                            centerPixel = 0;
                        } else if (centerPixel > 100) {
                            centerPixel = 100;
                        }
                        sumPixel += pixel;
                        countPixel++;
                        if (debugValues.length() > 0) {
                            debugValues.append(" ");
                        }
                        debugValues
                                .append(option)
                                .append(":")
                                .append((int) Math.round(pixel))
                                .append("/")
                                .append((int) Math.round(centerPixel))
                                .append("[")
                                .append((int) Math.round(pixelBase))
                                .append("|")
                                .append((int) Math.round(pixelSoft))
                                .append("|")
                                .append((int) Math.round(inkPixel))
                                .append("|")
                                .append((int) Math.round(pixelCalc))
                                .append("]");
                        optionPixels[option] = pixel;
                        if (pixel < bestPixel) {
                            secondPixel = bestPixel;
                            bestPixel = pixel;
                            bestCenterPixel = centerPixel;
                            bestOption = option;
                            bestRect = rectMark;
                            bestOptionGlobalIndex = currentOptionGlobalIndex;
                        } else if (pixel < secondPixel) {
                            secondPixel = pixel;
                        }
                    }
                    countOptions++;
                }
                if (bestOption > 0) {
                    double nearBestLimit = bestPixel + SHEET_2_ANSWER_NEAR_BEST_DELTA;
                    for (int option = 1; option <= optionsCount; option++) {
                        if (optionPixels[option] <= nearBestLimit) {
                            marksCount++;
                        }
                    }
                }
                double nearBestLimit = bestOption > 0 ? (bestPixel + SHEET_2_ANSWER_NEAR_BEST_DELTA) : Double.MAX_VALUE;
                double gap = secondPixel - bestPixel;
                double avgPixel = countPixel > 0 ? (sumPixel / countPixel) : 100;
                double relativeDelta = avgPixel - bestPixel;
                boolean hasHighConfidence = gap >= SHEET_2_ANSWER_HIGH_CONF_GAP || relativeDelta >= SHEET_2_ANSWER_HIGH_CONF_RELATIVE_DELTA;
                int selectedOption = -1;
                int selectedOptionGlobalIndex = -1;
                Rect selectedRect = null;
                boolean hasStrongSingle = marksCount == 1 && bestOption > 0 && bestOptionGlobalIndex > 0 &&
                        bestPixel <= SHEET_2_ANSWER_PIXEL_PERCENTAGE &&
                        bestCenterPixel <= SHEET_2_ANSWER_STRONG_CENTER_PIXEL_PERCENTAGE &&
                        ((gap >= SHEET_2_ANSWER_STRONG_MIN_GAP && relativeDelta >= SHEET_2_ANSWER_STRONG_MIN_RELATIVE_DELTA) || hasHighConfidence);
                boolean hasResolvedMultiple = marksCount > 1 && bestOption > 0 && bestOptionGlobalIndex > 0 &&
                        bestPixel <= SHEET_2_ANSWER_PIXEL_PERCENTAGE &&
                        bestCenterPixel <= SHEET_2_ANSWER_STRONG_CENTER_PIXEL_PERCENTAGE &&
                        gap >= SHEET_2_ANSWER_MULTI_MARK_MIN_GAP &&
                        relativeDelta >= SHEET_2_ANSWER_MULTI_MARK_MIN_RELATIVE_DELTA;
                boolean hasDarkMultiFallback = marksCount > 1 && bestOption > 0 && bestOptionGlobalIndex > 0 &&
                        bestPixel <= SHEET_2_ANSWER_DARK_MULTI_PIXEL_PERCENTAGE &&
                        bestCenterPixel <= SHEET_2_ANSWER_DARK_MULTI_CENTER_PIXEL_PERCENTAGE &&
                        relativeDelta >= SHEET_2_ANSWER_DARK_MULTI_MIN_RELATIVE_DELTA;
                boolean hasFallbackSingle = false;
                if (hasStrongSingle) {
                    selectedOption = bestOption;
                    selectedOptionGlobalIndex = bestOptionGlobalIndex;
                    selectedRect = bestRect;
                } else if (hasResolvedMultiple) {
                    selectedOption = bestOption;
                    selectedOptionGlobalIndex = bestOptionGlobalIndex;
                    selectedRect = bestRect;
                } else if (hasDarkMultiFallback) {
                    selectedOption = bestOption;
                    selectedOptionGlobalIndex = bestOptionGlobalIndex;
                    selectedRect = bestRect;
                } else if (hasFallbackSingle) {
                    selectedOption = bestOption;
                    selectedOptionGlobalIndex = bestOptionGlobalIndex;
                    selectedRect = bestRect;
                }
                Log.d(
                        TAG,
                        "sheet2_ans q=" + indexKey
                                + " marks=" + marksCount
                                + " bestOpt=" + bestOption
                                + " best=" + ((int) Math.round(bestPixel))
                                + " second=" + ((int) Math.round(secondPixel))
                                + " center=" + ((int) Math.round(bestCenterPixel))
                                + " gap=" + String.format("%.1f", gap)
                                + " rel=" + String.format("%.1f", relativeDelta)
                                + " sel=" + selectedOption
                                + " values=" + debugValues
                );
                for (int option = 1; option <= optionsCount; option++) {
                    Rect optionRect = optionRects[option];
                    if (optionRect == null) {
                        continue;
                    }
                    Imgproc.rectangle(thresholdDebug, optionRect, new Scalar(170, 170, 170), 1);
                    if (optionPixels[option] <= nearBestLimit) {
                        Imgproc.rectangle(thresholdDebug, optionRect, new Scalar(0, 0, 255), 1);
                    }
                    if (selectedOption == option) {
                        Imgproc.rectangle(thresholdDebug, optionRect, new Scalar(0, 255, 0), 2);
                    }
                }
                if (selectedOption > 0 && selectedOptionGlobalIndex > 0 && selectedRect != null && !isOpenQuestion) {
                    String letter = letters.get(selectedOption);
                    if (letter != null) {
                        letterMarked = letter;
                        answerSheet.setOptionMarkUser(selectedOptionGlobalIndex, letter);
                        boolean isCorrect = answerSheet.checkOptionMark(selectedOptionGlobalIndex);
                        Mark mark = new Mark(selectedRect, isCorrect);
                        marks.put(selectedOptionGlobalIndex, mark);
                        sumMarks++;
                    }
                } else {
                    letterMarked = "";
                }
                answerSheet.setAnswerLetter(indexKey, letterMarked);
            }
            sumAnswers += SHEET_2_ANSWERS_PER_BLOCK;
        }
        enhanced.release();
        threshold.release();
        thresholdSoft.release();
        thresholdSoftClean.release();
        answerInkMask.release();
        thresholdCalc.release();
        thresholdDebugGray.release();
        Log.d("sumMarks", "sumMarks:" + sumMarks);
        return thresholdDebug;
    }
    private Mat buildSheet2AnswerInkMask(Mat enhanced)
    {
        Mat gray = new Mat();
        if (enhanced.channels() > 1) {
            Imgproc.cvtColor(enhanced, gray, Imgproc.COLOR_BGR2GRAY);
        } else {
            gray = enhanced.clone();
        }
        Mat inkMask = new Mat();
        Imgproc.adaptiveThreshold(
                gray,
                inkMask,
                255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY_INV,
                35,
                3
        );
        Mat horizontalLines = new Mat();
        Mat verticalLines = new Mat();
        Mat kernelH = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(17, 1));
        Mat kernelV = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 17));
        Imgproc.morphologyEx(inkMask, horizontalLines, Imgproc.MORPH_OPEN, kernelH);
        Imgproc.morphologyEx(inkMask, verticalLines, Imgproc.MORPH_OPEN, kernelV);
        Mat linesMask = new Mat();
        Core.bitwise_or(horizontalLines, verticalLines, linesMask);
        Mat notLines = new Mat();
        Core.bitwise_not(linesMask, notLines);
        Mat inkOnly = new Mat();
        Core.bitwise_and(inkMask, notLines, inkOnly);
        Imgproc.medianBlur(inkOnly, inkOnly, 3);

        gray.release();
        inkMask.release();
        horizontalLines.release();
        verticalLines.release();
        kernelH.release();
        kernelV.release();
        linesMask.release();
        notLines.release();
        return inkOnly;
    }
    private Mat enhanceSheet2Answers(Mat warped)
    {
        Mat gray = new Mat();
        if (warped.channels() > 1) {
            Imgproc.cvtColor(warped, gray, Imgproc.COLOR_BGR2GRAY);
        } else {
            gray = warped.clone();
        }
        Imgproc.GaussianBlur(gray, gray, new Size(3, 3), 0);
        CLAHE clahe = Imgproc.createCLAHE(3.0, new Size(8, 8));
        Mat claheOut = new Mat();
        clahe.apply(gray, claheOut);
        clahe.collectGarbage();
        Mat enhancedBgr = new Mat();
        Imgproc.cvtColor(claheOut, enhancedBgr, Imgproc.COLOR_GRAY2BGR);
        gray.release();
        claheOut.release();
        return enhancedBgr;
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
        double roiArea = (double) roi.cols() * (double) roi.rows();
        double minArea = roiArea * 0.01;
        double maxDistance = Math.sqrt((double) (roi.cols() * roi.cols()) + (double) (roi.rows() * roi.rows()));
        double expectedX = (point == 2 || point == 4) ? roi.cols() : 0;
        double expectedY = (point == 3 || point == 4) ? roi.rows() : 0;
        Point best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for(int i = 0; i < contours.size(); i++)
        {
            MatOfPoint2f approxCurve = new MatOfPoint2f( contours.get(i).toArray() );
            approxPolyDP(approxCurve, approxCurve, 0.02 * arcLength(approxCurve, true), true);

            if(approxCurve.toArray().length == 4){
                Rect rect = Imgproc.boundingRect(contours.get(i));
                double ratio = (double)rect.height/(double)rect.width;
                if(ratio < 0.82 || ratio > 1.22)
                {
                    continue;
                }
                double area = (double) rect.width * (double) rect.height;
                if (area < minArea) {
                    continue;
                }
                double centerX = rect.x + (rect.width / 2.0);
                double centerY = rect.y + (rect.height / 2.0);
                double distance = Math.sqrt(
                        ((centerX - expectedX) * (centerX - expectedX)) +
                        ((centerY - expectedY) * (centerY - expectedY))
                );
                double distanceFactor = maxDistance > 0 ? (distance / maxDistance) : 0;
                double areaFactor = roiArea > 0 ? (area / roiArea) : 0;
                double score = areaFactor - (distanceFactor * 0.30);
                if (score > bestScore) {
                    bestScore = score;
                    int posX = (int) Math.round(centerX) + _rect.x;
                    int posY = (int) Math.round(centerY) + _rect.y;
                    best = new Point(posX, posY);
                }
            }
        }
        hierarchy.release();
        contours.clear();
        return best;
    }
    private Point findPointSheet2(Mat roi, int point, Rect contextRect)
    {
        Mat gray = new Mat();
        if (roi.channels() > 1) {
            Imgproc.cvtColor(roi, gray, Imgproc.COLOR_BGR2GRAY);
        } else {
            gray = roi.clone();
        }
        Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);
        Mat binary = new Mat();
        Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
        Imgproc.morphologyEx(binary, binary, Imgproc.MORPH_OPEN, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)));

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

        double roiArea = (double) roi.cols() * (double) roi.rows();
        double minArea = roiArea * 0.004;
        double maxArea = roiArea * 0.20;
        double maxDistance = Math.sqrt((double) (roi.cols() * roi.cols()) + (double) (roi.rows() * roi.rows()));
        double expectedX = (point == 2 || point == 4) ? roi.cols() : 0;
        double expectedY = (point == 3 || point == 4) ? roi.rows() : 0;

        Point best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            double area = (double) rect.width * (double) rect.height;
            if (area < minArea || area > maxArea) {
                continue;
            }
            double ratio = (double) rect.height / (double) rect.width;
            if (ratio < 0.75 || ratio > 1.30) {
                continue;
            }
            double contourArea = Imgproc.contourArea(contour);
            double fillRatio = contourArea / area;
            if (fillRatio < 0.35) {
                continue;
            }
            double centerX = rect.x + (rect.width / 2.0);
            double centerY = rect.y + (rect.height / 2.0);
            double distance = Math.sqrt(
                    ((centerX - expectedX) * (centerX - expectedX)) +
                    ((centerY - expectedY) * (centerY - expectedY))
            );
            double distanceFactor = maxDistance > 0 ? (distance / maxDistance) : 0;
            double areaFactor = roiArea > 0 ? (area / roiArea) : 0;
            double score = areaFactor + (fillRatio * 0.15) - (distanceFactor * 0.45);
            if (score > bestScore) {
                bestScore = score;
                int posX = (int) Math.round(centerX) + contextRect.x;
                int posY = (int) Math.round(centerY) + contextRect.y;
                best = new Point(posX, posY);
            }
        }

        hierarchy.release();
        contours.clear();
        binary.release();
        gray.release();
        return best;
    }
    private double distance(Point p1, Point p2)
    {
        if (p1 == null || p2 == null) {
            return 0;
        }
        double dx = p1.x - p2.x;
        double dy = p1.y - p2.y;
        return Math.sqrt((dx * dx) + (dy * dy));
    }
    private Mat applyThresholdSheet2Soft(Mat roi)
    {
        Mat gray = new Mat();
        if (roi.channels() > 1) {
            Imgproc.cvtColor(roi, gray, Imgproc.COLOR_BGR2GRAY);
        } else {
            gray = roi.clone();
        }
        Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);
        Mat threshold = new Mat();
        Imgproc.adaptiveThreshold(
                gray,
                threshold,
                255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY,
                35,
                4
        );
        gray.release();
        return threshold;
    }
    private double whitePixelPercentage(Mat context, Rect rect, int inset)
    {
        Rect evalRect = insetRect(rect, inset, context.cols(), context.rows());
        if (evalRect == null) {
            return 100.0;
        }
        int noOfWhitePixels = Core.countNonZero(context.submat(evalRect));
        return ((double) noOfWhitePixels / ((double) evalRect.width * (double) evalRect.height)) * 100;
    }
    private Rect insetRect(Rect rect, int inset, int maxX, int maxY)
    {
        int x = rect.x + inset;
        int y = rect.y + inset;
        int width = rect.width - (inset * 2);
        int height = rect.height - (inset * 2);
        if (width <= 0 || height <= 0) {
            return null;
        }
        if (x < 0 || y < 0) {
            return null;
        }
        if ((x + width) > maxX || (y + height) > maxY) {
            return null;
        }
        return new Rect(x, y, width, height);
    }
    private Rect buildRectFromAbsolute(Mat context, double x, double y, double width, double height)
    {
        int maxX = context.cols();
        int maxY = context.rows();
        int x1 = (int) Math.round(x);
        int y1 = (int) Math.round(y);
        int x2 = (int) Math.round(x + width);
        int y2 = (int) Math.round(y + height);

        if (x1 < 0) {
            x1 = 0;
        }
        if (y1 < 0) {
            y1 = 0;
        }
        if (x2 > maxX) {
            x2 = maxX;
        }
        if (y2 > maxY) {
            y2 = maxY;
        }
        if (x2 <= x1 || y2 <= y1) {
            return null;
        }
        return new Rect(new Point(x1, y1), new Point(x2, y2));
    }
    private SheetTemplate resolveSheetTemplate(Quiz quiz, String levelName)
    {
        String template = safeUpper(quiz.getTemplateHojaRespuesta());
        if ("V1".equals(template)) {
            return SheetTemplate.LEGACY;
        }
        if ("V2".equals(template)) {
            return SheetTemplate.ANSWER_SHEET_2;
        }
        Log.w(TAG, "template_hoja_respuesta desconocido: " + template + " - se usara V2");
        return SheetTemplate.ANSWER_SHEET_2;
    }
    private String safeUpper(String value)
    {
        if (value == null) {
            return "";
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

}
