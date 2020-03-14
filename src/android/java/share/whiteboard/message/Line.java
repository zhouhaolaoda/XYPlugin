package com.xylink.sdk.sample.share.whiteboard.message;

import com.ainemo.sdk.model.LineMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Line {

    public static final float PENCIL_APLAH = 1.0f;
    public static final float MAKRER_ALPHA = 0.5f;
    public static final float ERASE_ALPHA = 0.0f;

    private static final float[] DEFAULT_COLOR = {1.0f, 0.0f, 0.0f, 1.0f};
    private static final int DEFAULT_WIDTH = 200;
    private List<Point> PointsNotDrawn;
    private List<Point> PointsDrawn;
    private List<Point> pointsToNotify;
    private boolean everNotify;
    private ArrayList<Vec2f> lastPointsDrawn;
    private float[] color;

    private int width;

    private boolean active;

    private long seq;

    private boolean cached;

    private boolean completed;


    public Line(float[] color, int width) {
        PointsDrawn = Collections.synchronizedList(new ArrayList<Point>());
        PointsNotDrawn = Collections.synchronizedList(new ArrayList<Point>());
        this.color = color;
        this.width = width;
        seq = -1;
        completed = false;
        lastPointsDrawn = new ArrayList<Vec2f>(3);
        pointsToNotify = new ArrayList<Point>(4);
        everNotify = false;
    }

    public Line() {
        this(DEFAULT_COLOR, DEFAULT_WIDTH);
    }

    public Line(float[] color, int width, long seq) {
        this(color, width);
        this.seq = seq;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public boolean isCached() {
        return cached;
    }

    public void setCached(boolean cached) {
        this.cached = cached;
    }

    public float[] getColor() {
        return color;
    }

    public void setColor(float[] color) {
        this.color = color;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public ArrayList<Vec2f> getLastPointsDrawn() {
        return lastPointsDrawn;
    }

    public boolean isEverNotify() {
        return everNotify;
    }

    public void setEverNotify(boolean everNotify) {
        this.everNotify = everNotify;
    }

    public boolean canBeRender() {
        boolean enough = false;
        if (PointsDrawn.isEmpty()
                && PointsNotDrawn.size() > 4
                || !PointsDrawn.isEmpty()
                && PointsNotDrawn.size() > 3) {
            enough = true;
        } else if (isCompleted()) {
            if (PointsDrawn.isEmpty()
                    && PointsNotDrawn.size() >= 2
                    || !PointsDrawn.isEmpty()
                    && PointsNotDrawn.size() >= 1) {
                enough = true;
            }
        }
        return enough;
    }

    public synchronized Point getLastPoint() {
        Point lastPoint = null;
        if (PointsNotDrawn.size() > 0) {
            lastPoint = PointsNotDrawn.get(PointsNotDrawn.size() - 1);
        } else if (PointsDrawn.size() > 0) {
            lastPoint = PointsDrawn.get(PointsDrawn.size() - 1);
        }
        return lastPoint;
    }

    public synchronized void addUndrawnPoint(Point point) {
        PointsNotDrawn.add(point);
    }

    public synchronized void move2Drawn(Point point) {
        PointsNotDrawn.remove(point);
        PointsDrawn.add(point);
    }

    public void move2DrawnInternal(Point point) {
        PointsNotDrawn.remove(point);
        PointsDrawn.add(point);
    }

    public synchronized boolean isDrawn(Point point) {
        return PointsDrawn.contains(point);
    }

    public synchronized int getPointsToDrawn(Point[] points) {
        int count = 0;
        if (PointsDrawn.isEmpty()
                && PointsNotDrawn.size() > 4) {
            points[0] = PointsNotDrawn.remove(0);
            points[1] = PointsNotDrawn.remove(0);
            points[2] = PointsNotDrawn.remove(0);
            points[3] = PointsNotDrawn.remove(0);
            Point p4 = PointsNotDrawn.get(0);

            count = 4;
            move2DrawnInternal(points[0]);
            move2DrawnInternal(points[1]);
            move2DrawnInternal(points[2]);
            move2DrawnInternal(points[3]);

            int x = (points[2].getX() + p4.getX()) / 2;
            int y = (points[2].getY() + p4.getY()) / 2;
            points[3] = new Point(x, y);
        } else if (!PointsDrawn.isEmpty()
                && PointsNotDrawn.size() > 3) {
            Point lastControlPoint = PointsDrawn.get(
                    PointsDrawn.size() - 2);
            points[1] = PointsNotDrawn.remove(0);
            int x = (lastControlPoint.getX() + points[1].getX()) / 2;
            int y = (lastControlPoint.getY() + points[1].getY()) / 2;
            points[0] = new Point(x, y);

            points[2] = PointsNotDrawn.remove(0);
            points[3] = PointsNotDrawn.remove(0);
            Point p4 = PointsNotDrawn.get(0);

            count = 4;
            move2DrawnInternal(points[1]);
            move2DrawnInternal(points[2]);
            move2DrawnInternal(points[3]);

            x = (points[2].getX() + p4.getX()) / 2;
            y = (points[2].getY() + p4.getY()) / 2;
            points[3] = new Point(x, y);
        } else if (isCompleted()) {
            if (PointsDrawn.isEmpty()
                    && PointsNotDrawn.size() > 2) {
                points[0] = PointsNotDrawn.remove(0);
                points[1] = PointsNotDrawn.remove(0);
                points[2] = PointsNotDrawn.get(0);

                count = 3;
                move2DrawnInternal(points[0]);
                move2DrawnInternal(points[1]);

                int x = (points[1].getX() + points[2].getX()) / 2;
                int y = (points[1].getY() + points[2].getY()) / 2;
                points[2] = new Point(x, y);

            } else if (!PointsDrawn.isEmpty()
                    && PointsNotDrawn.size() > 1) {
                Point lastControlPoint = PointsDrawn.get(
                        PointsDrawn.size() - 1);
                points[1] = PointsNotDrawn.remove(0);
                int x = (lastControlPoint.getX() + points[1].getX()) / 2;
                int y = (lastControlPoint.getY() + points[1].getY()) / 2;
                points[0] = new Point(x, y);

                points[2] = PointsNotDrawn.get(0);

                count = 3;
                move2DrawnInternal(points[1]);

                x = (points[1].getX() + points[2].getX()) / 2;
                y = (points[1].getY() + points[2].getY()) / 2;
                points[2] = new Point(x, y);
            } else if (PointsDrawn.isEmpty()
                    && PointsNotDrawn.size() > 1) {
                points[0] = PointsNotDrawn.remove(0);
                points[1] = PointsNotDrawn.remove(0);

                count = 2;
                move2DrawnInternal(points[0]);
                move2DrawnInternal(points[1]);

            } else if (!PointsDrawn.isEmpty()
                    && PointsNotDrawn.size() >= 1) {
                points[0] = PointsDrawn.get(
                        PointsDrawn.size() - 1);
                points[1] = PointsNotDrawn.remove(0);
                count = 2;
                move2DrawnInternal(points[1]);
            }
        }
        return count;
    }

    public void add2Notify(Point point) {
        pointsToNotify.add(point);
    }

    public LineMessage notify(long color, PenType penType) {
        boolean enough = !completed
                && (!everNotify && (pointsToNotify.size() > 4) || everNotify && (pointsToNotify.size() > 3))
                || completed
                && (!everNotify && (pointsToNotify.size() > 1) || everNotify && (pointsToNotify.size() > 0));

        if (!enough) {
            return null;
        }

        LineMessage message = new LineMessage();
        for (int i = 0; i < pointsToNotify.size(); i++) {
            Point point = pointsToNotify.get(i);
            LineMessage.PointInfo pi = new LineMessage.PointInfo();
            pi.setX(point.getX());
            pi.setY(point.getY());
            if (!everNotify && i == 0) {
                //first point
                pi.setW(getWidth());
                long c = color << 8;
                long alpha = 0x000000ffL;
                if (PenType.TRANSLUCENT == penType) {
                    alpha = (int) (MAKRER_ALPHA * 255);
                } else if (PenType.ERASER == penType) {
                    c = 0L;
                    alpha = 0;
                }
                c = c | alpha;
                pi.setC(toWhiteBoardColor(c));
            } else if (isCompleted() && i == pointsToNotify.size() - 1) {
                //last point
                pi.setW(0);
            }
            message.getP().add(pi);
        }
        pointsToNotify.clear();
        everNotify = true;
        return message;
    }

    private String toWhiteBoardColor(long color) {
        return "#" + Integer.toHexString((int) color);
    }
}
