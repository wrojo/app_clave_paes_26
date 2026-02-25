package cl.gruposm.conectaevaluaciones.object;

import org.opencv.core.Rect;

public class Mark {
    Rect rect;
    boolean isCorrect;

    public Mark(Rect rect, boolean isCorrect) {
        this.rect = rect;
        this.isCorrect = isCorrect;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }
}
