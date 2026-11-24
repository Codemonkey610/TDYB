package cn.lincq.accessibility.bean;

public class ScreenProperty {
    private float density;
    private int screenHeightDp;
    private int screenHeightPx;
    private int screenWidthDp;
    private int screenWidthPx;

    public ScreenProperty() {
    }

    public ScreenProperty(int i, int i2) {
        this.screenHeightPx = i2;
        this.screenWidthPx = i;
    }

    public ScreenProperty(int i, int i2, float f) {
        this.screenWidthDp = i;
        this.screenHeightDp = i2;
        this.density = f;
    }

    public float getDensity() {
        return this.density;
    }

    public int getScreenHeightDp() {
        return this.screenHeightDp;
    }

    public int getScreenHeightPx() {
        return this.screenHeightPx;
    }

    public int getScreenWidthDp() {
        return this.screenWidthDp;
    }

    public int getScreenWidthPx() {
        return this.screenWidthPx;
    }

    public void setDensity(float f) {
        this.density = f;
    }

    public void setScreenHeightDp(int i) {
        this.screenHeightDp = i;
    }

    public void setScreenHeightPx(int i) {
        this.screenHeightPx = i;
    }

    public void setScreenWidthDp(int i) {
        this.screenWidthDp = i;
    }

    public void setScreenWidthPx(int i) {
        this.screenWidthPx = i;
    }
}
