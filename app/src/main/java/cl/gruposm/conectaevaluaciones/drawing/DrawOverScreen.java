package cl.gruposm.conectaevaluaciones.drawing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import cl.gruposm.conectaevaluaciones.R;

public class DrawOverScreen extends View {
    public int screenCenterX = 0;
    public int screenCenterY = 0;
    public int radius = 50;
    public int rectWidth;
    public int width;
    public int height;
    public int posY;
    public int initY;
    Paint rect1 = new Paint();
    Paint rect2 = new Paint();
    Paint rect3 = new Paint();
    Paint rect4 = new Paint();
    public Rect rectangle1 = new Rect(0,0,0,0);
    public Rect rectangle2 = new Rect(0,0,0,0);
    public Rect rectangle3 = new Rect(0,0,0,0);
    public Rect rectangle4 = new Rect(0,0,0,0);
    public DrawOverScreen(Context context) {
        super(context);
        this.screenCenterX = screenCenterX;
        this.screenCenterY = screenCenterY;
        this.radius = radius;
        this.rectWidth = 0;
        this.posY = 0;
        this.height = 1;
        this.initY = 0;

    }
    @Override
    protected void onDraw(Canvas canvas) {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(getResources().getColor(R.color.black));
        paint.setAlpha(50);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);
        paint.setColor(getResources().getColor(R.color.white));
        paint.setAlpha(50);
        canvas.drawRect(0,initY,rectWidth,rectWidth+initY,paint);
        canvas.drawRect(width-rectWidth,initY,width,rectWidth+initY,paint);
        int posY = this.posY;
        canvas.drawRect(0,posY,rectWidth,posY+rectWidth,paint);
        canvas.drawRect(width-rectWidth,posY,width,posY+rectWidth,paint);


        rect1.setStyle(Paint.Style.STROKE);
        rect1.setColor(Color.rgb(0,255,0));
        rect1.setStrokeWidth(5);
        canvas.drawRect(rectangle1, rect1);

        rect2.setStyle(Paint.Style.STROKE);
        rect2.setColor(Color.rgb(0,255,0));
        rect2.setStrokeWidth(5);
        canvas.drawRect(rectangle2, rect2);

        rect3.setStyle(Paint.Style.STROKE);
        rect3.setColor(Color.rgb(0,255,0));
        rect3.setStrokeWidth(5);
        canvas.drawRect(rectangle3, rect3);

        rect4.setStyle(Paint.Style.STROKE);
        rect4.setColor(Color.rgb(0,255,0));
        rect4.setStrokeWidth(5);
        canvas.drawRect(rectangle4, rect4);

        super.onDraw(canvas);
    }
    public void setRectWidth(int width)
    {
        this.rectWidth = width;
    }
    public void setPosY(int posY)
    {
        this.posY = posY;
    }
    public void setWidth(int width)
    {
        this.width = width;
    }
    public void setHeight(int height)
    {
        this.height = height;
    }
    public void setInitY(int initY)
    {
        this.initY = initY;
    }
}

