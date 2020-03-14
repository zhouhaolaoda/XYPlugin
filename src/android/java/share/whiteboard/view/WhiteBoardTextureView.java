package com.xylink.sdk.sample.share.whiteboard.view;

import android.content.Context;
import android.log.L;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.AttributeSet;
import android.util.LongSparseArray;
import android.util.ThreadedHandler;
import android.view.MotionEvent;
import android.view.View;

import com.ainemo.sdk.model.BaseMessage;
import com.ainemo.sdk.model.LineMessage;
import com.ainemo.sdk.otf.WhiteboardGLTextureView;
import com.ainemo.util.JsonUtil;
import com.xylink.sdk.sample.share.whiteboard.message.Line;
import com.xylink.sdk.sample.share.whiteboard.message.PenType;
import com.xylink.sdk.sample.share.whiteboard.message.Point;
import com.xylink.sdk.sample.share.whiteboard.message.ShaderHelper;
import com.xylink.sdk.sample.share.whiteboard.message.Vec2f;
import com.xylink.sdk.sample.share.whiteboard.message.VetexDataBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

public class WhiteBoardTextureView extends WhiteboardGLTextureView {

    private static String TAG = WhiteBoardTextureView.class.getSimpleName();
    private static Logger mLogger = Logger.getLogger(WhiteBoardTextureView.class.getSimpleName());

    private final static String HANDLE_THREAD_NAME = "WhiteBoardTextureView thread";

    private static final int MSG_WHITEBOARD_MESSAGE_ARRIVAL = 1;
    private static final int MSG_WHITEBOARD_CLOSE = 2;
    private static final int MSG_WHITEBOARD_MESSAGES_ARRIVAL = 3;

    private static final int DEFAUL_WHITE_BOARD_WIDTH = 1280;
    private static final int DEFAUL_WHITE_BOARD_HEIGHT = 720;

    private static final int PENCIL_WIDTH = 2;
    private static final int MAKRER_WIDTH = 15;
    private static final int ERASE_WIDTH = 50;

    private final float[] mtrxProjection = new float[16];
    private final float[] mtrxView = new float[16];
    private final float[] mtrxProjectionAndView = new float[16];

    private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

    private Renderer mRenderer;
    private LongSparseArray<Line> allLines = new LongSparseArray<Line>();
    private List<VetexDataBuffer> allDataBuffers = Collections.synchronizedList(new ArrayList<VetexDataBuffer>());
    private String mDeviceUrl = "";

    private List<Point> unDrawnPoints = Collections.synchronizedList(new ArrayList<Point>());
    private Line mLocalLine = null;
    int viewWidth = -1;
    int viewHeight = -1;

    private final int MAX_POINT = 10;
    private final int MAX_POINT_PER_DRAW = 1024;
    private final int MAX_SEGMENT_PER_DRAW = MAX_POINT_PER_DRAW / (MAX_POINT + 8);

    private static final float EDGE_THICK = 3.0f;

    private float previousX;
    private float previousY;

    private int whiteboardWidth = DEFAUL_WHITE_BOARD_WIDTH;
    private int whiteboardHeight = DEFAUL_WHITE_BOARD_HEIGHT;

    private WhiteBoardViewListener mListener = null;

    private long mLocalColor = 0xff6666L;
    private PenType mCurrentLocalPenType = PenType.OPAQUE;
    private int mCurrentLocalWidth = PENCIL_WIDTH;

    private boolean mReDraw;

    private Point mFistLocalPoint;

    private float mAspectRatio;

    private ThreadedHandler handler;

    private volatile boolean mStopped = true;

    private Point[] mPointToDraw = new Point[4];

    private FloatBuffer vertexs = null;

    private FloatBuffer colors = null;

    private ShortBuffer indices = null;

    private ArrayList<VetexDataBuffer> mNeedToDraw;

    public interface WhiteBoardViewListener {
        void onWhiteBoardMessageSend(String text);
    }

    public WhiteBoardTextureView(Context context) {
        super(context);
        init(context, null);
    }

    public WhiteBoardTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void setWhiteBoardViewListener(WhiteBoardViewListener listener) {
        mListener = listener;
    }

    private void init(Context context, AttributeSet attrs) {
        handler = ThreadedHandler.create(HANDLE_THREAD_NAME,
                Process.THREAD_PRIORITY_BACKGROUND, new MessageHandleCallback());

        setEGLContextFactory(new ContextFactory());
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mRenderer = new Renderer();
        setRenderer(mRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        setOpaque(false);

        ByteBuffer vbb = ByteBuffer
                .allocateDirect(MAX_POINT_PER_DRAW * 10 * 3 * 4);
        vbb.order(ByteOrder.nativeOrder());
        vertexs = vbb.asFloatBuffer();
        // color
        ByteBuffer cbb = ByteBuffer
                .allocateDirect(MAX_POINT_PER_DRAW * 10 * 4 * 4);
        cbb.order(ByteOrder.nativeOrder());
        colors = cbb.asFloatBuffer();
        // indices
        ByteBuffer ibb = ByteBuffer
                .allocateDirect(MAX_POINT_PER_DRAW * 24 * 2);
        ibb.order(ByteOrder.nativeOrder());
        indices = ibb.asShortBuffer();

        mNeedToDraw = new ArrayList<VetexDataBuffer>();
    }

    public void start() {
        mLogger.info("start");
    }

    public void pause() {
        mLogger.info("pause");
        onPause();
    }

    public void resume() {
        mLogger.info("resume");
        onResume();
    }

    public void close() {
        mLogger.info("close");
        mStopped = true;

        Message msg = Message.obtain();
        msg.what = MSG_WHITEBOARD_CLOSE;
        handler.sendMessage(msg);
    }

    public void destroy() {
        mLogger.info("destroy");
        handler.stop();
    }

    public void setWhiteBoardResolution(int width, int height) {
        L.i(TAG, "setWhiteBoardResolution " + width + "x" + height);
        if (width <= 0 || height <= 0) {
            L.i(TAG, "setWhiteBoardResolution error");
            this.whiteboardWidth = DEFAUL_WHITE_BOARD_WIDTH;
            this.whiteboardHeight = DEFAUL_WHITE_BOARD_HEIGHT;
        } else {
            this.whiteboardWidth = width;
            this.whiteboardHeight = height;
        }
        mStopped = false;
    }

    public void setDeviceUrl(String url) {
        L.i(TAG, "setDeviceUrl " + url);
        mDeviceUrl = url;
    }

    public long getLocalColor() {
        return mLocalColor;
    }

    public void setLocalColor(long localColor) {
        this.mLocalColor = localColor;
    }

    public PenType getmCurrentLocalPenType() {
        return mCurrentLocalPenType;
    }

    public void setCurrentLocalPenType(PenType penType) {
        this.mCurrentLocalPenType = penType;
        mCurrentLocalWidth = PENCIL_WIDTH;
        if (PenType.TRANSLUCENT == mCurrentLocalPenType) {
            mCurrentLocalWidth = MAKRER_WIDTH;
        } else if (PenType.ERASER == mCurrentLocalPenType) {
            mCurrentLocalWidth = ERASE_WIDTH;
        }
    }

    public void clear() {
        onClearLines();
        if (null != mListener) {
            BaseMessage message = new BaseMessage(BaseMessage.ClearLines);
            mListener.onWhiteBoardMessageSend(JsonUtil.toJson(message));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        int speWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        int speHeight = View.MeasureSpec.getSize(heightMeasureSpec);

        L.i(TAG, "onMeasure spec " + speWidth + "x" + speHeight
                + " whiteboard " + whiteboardWidth + "x" + whiteboardHeight);

        if (whiteboardHeight <= 0 || whiteboardWidth <= 0) {
            L.w(TAG, "invalidate whiteboard resulotion");
        }

        int width = speWidth;
        int height = speHeight;
        if (whiteboardHeight * speWidth < whiteboardWidth * speHeight) {
            // scale algin with width
            width = speWidth;
            height = width * whiteboardHeight / whiteboardWidth;
        } else {
            // scale algin with height
            height = speHeight;
            width = height * whiteboardWidth / whiteboardHeight;
        }
        L.i(TAG, "width " + width + " height " + height);
        setMeasuredDimension(width, height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public void onTouch(int action, float x, float y) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                touchDown(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                break;
            case MotionEvent.ACTION_UP:
                touchUp(x, y);
                break;
            default:
        }

    }

    private void touchDown(float xf, float yf) {
        previousX = xf;
        previousY = yf;

        // convert coordinate from view to white board
        int x = (int) (xf * Point.NORMAL_SCALE / viewWidth);
        int y = (int) (yf * Point.NORMAL_SCALE / viewHeight);

        mFistLocalPoint = new Point(x, y);
        mLocalLine = new Line(local2OpenGlColor(), mCurrentLocalWidth);
    }

    private void touchMove(float xf, float yf) {
        float deltaX = Math.abs(xf - previousX);
        float deltaY = Math.abs(yf - previousY);
        if (deltaX < 4 && deltaY < 4) {
            return;
        }

        if (null == mLocalLine) {
            return;
        }
        previousX = xf;
        previousY = yf;

        // convert coordinate from view to white board
        int x = (int) (xf * Point.NORMAL_SCALE / viewWidth);
        int y = (int) (yf * Point.NORMAL_SCALE / viewHeight);

        Point p = new Point(x, y);
        L.i(TAG, "touchMove point " + p.getX() + " " + p.getY());

        if (null != mFistLocalPoint) {
            mFistLocalPoint.add2UnDrawn(mLocalLine);
            unDrawnPoints.add(mFistLocalPoint);
            mLocalLine.add2Notify(mFistLocalPoint);
            mFistLocalPoint = null;
        }

        boolean needRender = false;
        p.add2UnDrawn(mLocalLine);
        mLocalLine.add2Notify(p);
        unDrawnPoints.add(p);
        needRender = mLocalLine.canBeRender();
        requestRender();
        if (needRender) {
            this.requestRender();
        }
        notifyLine(mLocalLine);
    }

    private void touchUp(float xf, float yf) {

        if (null == mLocalLine) {
            return;
        }

        int x = (int) (xf * Point.NORMAL_SCALE / viewWidth);
        int y = (int) (yf * Point.NORMAL_SCALE / viewHeight);

        Point p = new Point(x, y);
        boolean needRender = false;
        p.add2UnDrawn(mLocalLine);
        mLocalLine.add2Notify(p);
        mLocalLine.setCompleted(true);
        unDrawnPoints.add(p);
        needRender = mLocalLine.canBeRender();

        if (needRender) {
            requestRender();
        }
        notifyLine(mLocalLine);

        mLocalLine = null;
    }

    private void touchDown(MotionEvent event) {
        touchDown(event.getX(), event.getY());
    }

    private void touchMove(MotionEvent event) {
        touchMove(event.getX(), event.getY());
    }

    private void touchUp(MotionEvent event) {
        touchUp(event.getX(), event.getY());
    }

    private void notifyLine(Line line) {
        LineMessage message = line.notify(mLocalColor, mCurrentLocalPenType);

        if (null != message && null != mListener) {
            String json = message.toJson();
            if (null != json && !json.isEmpty()) {
                mListener.onWhiteBoardMessageSend(json);
            }
        }
    }

    private float[] local2OpenGlColor() {
        float[] color = new float[4];
        long localColor = mLocalColor << 8;
        color[0] = ((float) ((localColor & 0xff000000L) >> 24)) / 255;
        color[1] = ((float) ((localColor & 0x00ff0000L) >> 16)) / 255;
        color[2] = ((float) ((localColor & 0x0000ff00L) >> 8)) / 255;
        color[3] = Line.PENCIL_APLAH;
        if (PenType.TRANSLUCENT == mCurrentLocalPenType) {
            color[3] = Line.MAKRER_ALPHA;
        } else if (PenType.ERASER == mCurrentLocalPenType) {
            color[0] = 0.0f;
            color[1] = 0.0f;
            color[2] = 0.0f;
            color[3] = Line.ERASE_ALPHA;
        }

        mLogger.info("color " + color[0] + " " + color[1] + " " + color[2] + " " + color[3]);
        return color;
    }

    private class ContextFactory extends WhiteboardGLTextureView.EGLContextFactory {

        public EGLContext createContext(EGL10 egl, EGLDisplay display,
                                        EGLConfig eglConfig) {
            int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};
            EGLContext context = egl.eglCreateContext(display, eglConfig,
                    EGL10.EGL_NO_CONTEXT, attrib_list);
            return context;
        }

        public void destroyContext(EGL10 egl, EGLDisplay display,
                                   EGLContext context) {
            egl.eglDestroyContext(display, context);
        }
    }

    private float bezierCurve(float t, float P0, float P1, float P2, float P3) {
        // Cubic bezier Curve
        double point = ((1 - t) * (1 - t) * (1 - t) * P0)
                + (3 * (1 - t) * (1 - t) * t * P1)
                + (3 * (1 - t) * t * t * P2) + (t * t * t * P3);
        return (float) point;
    }

    private float bezierCurve(float t, float P0, float P1, float P2) {
        // Quadratic bezier Curve
        double point = ((1 - t) * (1 - t) * P0)
                + 2 * (1 - t) * t * P1
                + t * t * P2;
        return (float) point;
    }

    private class Renderer extends WhiteboardGLTextureView.Renderer {
        private short mIndex = 0;
        Vec2f p21 = new Vec2f(0.0f, 0.0f);
        Vec2f normal = new Vec2f(0.0f, 0.0f);
        Vec2f miter1 = new Vec2f(0.0f, 0.0f);
        Vec2f miter2 = new Vec2f(0.0f, 0.0f);
        Vec2f leftBottom = new Vec2f(0.0f, 0.0f);
        Vec2f leftTop = new Vec2f(0.0f, 0.0f);
        Vec2f rightBottom = new Vec2f(0.0f, 0.0f);
        Vec2f rightTop = new Vec2f(0.0f, 0.0f);
        Vec2f delta = new Vec2f(0.0f, 0.0f);
        Vec2f leftBottomMid = new Vec2f(0.0f, 0.0f);
        Vec2f leftTopMid = new Vec2f(0.0f, 0.0f);
        Vec2f rightBottomMid = new Vec2f(0.0f, 0.0f);
        Vec2f rightTopMid = new Vec2f(0.0f, 0.0f);

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            L.i(TAG, "onSurfaceCreated");
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            EGL14.eglSurfaceAttrib(EGL14.eglGetCurrentDisplay(),
                    EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW),
                    EGL14.EGL_SWAP_BEHAVIOR, EGL14.EGL_BUFFER_PRESERVED);

            int vertexShader = ShaderHelper.loadShader(GLES20.GL_VERTEX_SHADER,
                    ShaderHelper.vs_vertext);
            int fragmentShader = ShaderHelper.loadShader(
                    GLES20.GL_FRAGMENT_SHADER, ShaderHelper.fs_color);

            ShaderHelper.sp_color = GLES20.glCreateProgram(); // create empty OpenGL ES Program
            GLES20.glAttachShader(ShaderHelper.sp_color, vertexShader); // add the vertex shader to program
            GLES20.glAttachShader(ShaderHelper.sp_color, fragmentShader); // add the fragment shader to program
            GLES20.glLinkProgram(ShaderHelper.sp_color); // creates OpenGL ES program executables

            // Set our shader programm
            GLES20.glUseProgram(ShaderHelper.sp_color);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            L.i(TAG, "onSurfaceChanged " + width + " " + height);
            GLES20.glViewport(0, 0, width, height);
            GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            EGL14.eglSurfaceAttrib(EGL14.eglGetCurrentDisplay(),
                    EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW),
                    EGL14.EGL_SWAP_BEHAVIOR, EGL14.EGL_BUFFER_PRESERVED);
            // Clear our matrices
            for (int i = 0; i < 16; i++) {
                mtrxProjection[i] = 0.0f;
                mtrxView[i] = 0.0f;
                mtrxProjectionAndView[i] = 0.0f;
            }

            mAspectRatio = (float) height / (float) width;
            Matrix.orthoM(mtrxProjection, 0, -1.0f, 1.0f, -mAspectRatio,
                    mAspectRatio, 0, 50);
            //Matrix.orthoM(mtrxProjection, 0, -1.0f, 1.0f, -1.0f, 1.0f, 0, 50);
            // Set the camera position (View matrix)
            Matrix.setLookAtM(mtrxView, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f,
                    0.0f);
            // Calculate the projection and view transformation
            Matrix.multiplyMM(mtrxProjectionAndView, 0, mtrxProjection, 0,
                    mtrxView, 0);
            viewWidth = width;
            viewHeight = height;

            mReDraw = true;
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            mLogger.info("WhiteBoard-->onDrawFrame");
            try {
                mNeedToDraw.clear();
                // gl.glLoadIdentity();
                if (mReDraw) {
                    GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
                    redraw();
                    mNeedToDraw.clear();
                    mReDraw = false;
                }

                Point point = getPointToDraw();
                while (null != point) {
                    Line line = point.getLine();
                    if (null != line) {
                        int pointCount = line.getPointsToDrawn(mPointToDraw);
                        switch (pointCount) {
                            case 4:
                                drawCurveWithCubicBezier(line, mPointToDraw);
                                break;
                            case 3:
                                drawCurveWithQuadraticBezier(line, mPointToDraw);
                                break;
                            case 2:
                                drawLine(line, mPointToDraw);
                                break;
                            default:
                        }
                    }
                    point = getPointToDraw();
                }
                if (!mNeedToDraw.isEmpty()) {
                    draw();
                } else {
                    //if no any operation is done, some part of line will be lost.
                    //hotfix: render last part of line if no operation is done.
                    if (allDataBuffers.size() > 0) {
                        VetexDataBuffer buffer = allDataBuffers.get(allDataBuffers.size() - 1);
                        if (null != buffer) {
                            mNeedToDraw.add(buffer);
                            draw();
                        }
                    }
                }
            } catch (Exception ex) {
                mLogger.info("onDrawFrame Exception " + ex.getMessage());
            }
        }

        private Point getPointToDraw() {
            Point p = null;
            synchronized (unDrawnPoints) {
                Iterator<Point> iter = unDrawnPoints.iterator();
                while (iter.hasNext()) {
                    Point point = iter.next();
                    Line line = point.getLine();
                    if (null == line) {
                        iter.remove();
                        continue;
                    }
                    if (line.isDrawn(point)) {
                        iter.remove();
                        continue;
                    }

                    if (!line.canBeRender()) {
                        continue;
                    }
                    p = point;
                    break;
                }
            }
            return p;
        }

        private void drawCurveWithCubicBezier(Line line, Point[] pointsToDraw) {
            Vec2f vp0 = pointsToDraw[0].toOpenGLCoordinate(mAspectRatio);
            Vec2f vp1 = pointsToDraw[1].toOpenGLCoordinate(mAspectRatio);
            Vec2f vp2 = pointsToDraw[2].toOpenGLCoordinate(mAspectRatio);
            Vec2f vp3 = pointsToDraw[3].toOpenGLCoordinate(mAspectRatio);

            VetexDataBuffer buffer = new VetexDataBuffer(MAX_POINT,
                    line.getColor()[3]);
            mIndex = 0;

            int t = MAX_POINT;
            ArrayList<Vec2f> points = new ArrayList<Vec2f>(MAX_POINT + 1);
            if (null != line.getLastPointsDrawn()) {
                points.addAll(line.getLastPointsDrawn());
            }

            for (int i = 1; i <= t; i++) {
                float pos = (float) i / (float) t;
                float x = bezierCurve(pos, vp0.x, vp1.x,
                        vp2.x, vp3.x);
                float y = bezierCurve(pos, vp0.y, vp1.y,
                        vp2.y, vp3.y);
                points.add(new Vec2f(x, y));
            }

            int start = 0;
            if (null != line.getLastPointsDrawn()
                    && !line.getLastPointsDrawn().isEmpty()) {
                start = 1;
            }
            // draw all segments
            for (int i = start; i < points.size() - 2; ++i) {
                int a = ((i - 1) < 0) ? 0 : (i - 1);
                int b = i;
                int c = ((i + 1) >= points.size()) ? points.size() - 1
                        : (i + 1);
                int d = ((i + 2) >= points.size()) ? points.size() - 1
                        : (i + 2);

                createSegmentFromLine(buffer, points.get(a), points.get(b),
                        points.get(c), points.get(d), line);
            }

            line.getLastPointsDrawn().clear();
            line.getLastPointsDrawn().add(points.get(points.size() - 3));
            line.getLastPointsDrawn().add(points.get(points.size() - 2));
            line.getLastPointsDrawn().add(points.get(points.size() - 1));

            addNewBufferToDraw(buffer);
        }

        private void drawCurveWithQuadraticBezier(Line line, Point[] pointsToDraw) {
            Vec2f vp0 = pointsToDraw[0].toOpenGLCoordinate(mAspectRatio);
            Vec2f vp1 = pointsToDraw[1].toOpenGLCoordinate(mAspectRatio);
            Vec2f vp2 = pointsToDraw[2].toOpenGLCoordinate(mAspectRatio);

            VetexDataBuffer buffer = new VetexDataBuffer(MAX_POINT,
                    line.getColor()[3]);
            mIndex = 0;

            int t = MAX_POINT;
            ArrayList<Vec2f> points = new ArrayList<Vec2f>(MAX_POINT + 1);
            if (null != line.getLastPointsDrawn()) {
                points.addAll(line.getLastPointsDrawn());
            }

            for (int i = 1; i <= t; i++) {
                float pos = (float) i / (float) t;
                float x = bezierCurve(pos, vp0.x, vp1.x,
                        vp2.x);
                float y = bezierCurve(pos, vp0.y, vp1.y,
                        vp2.y);
                points.add(new Vec2f(x, y));
            }

            int start = 0;
            if (null != line.getLastPointsDrawn()
                    && !line.getLastPointsDrawn().isEmpty()) {
                start = 1;
            }
            // draw all segments
            for (int i = start; i < points.size() - 2; ++i) {
                int a = ((i - 1) < 0) ? 0 : (i - 1);
                int b = i;
                int c = ((i + 1) >= points.size()) ? points.size() - 1
                        : (i + 1);
                int d = ((i + 2) >= points.size()) ? points.size() - 1
                        : (i + 2);

                createSegmentFromLine(buffer, points.get(a), points.get(b),
                        points.get(c), points.get(d), line);
            }

            line.getLastPointsDrawn().clear();
            line.getLastPointsDrawn().add(points.get(points.size() - 3));
            line.getLastPointsDrawn().add(points.get(points.size() - 2));
            line.getLastPointsDrawn().add(points.get(points.size() - 1));

            addNewBufferToDraw(buffer);
        }

        private void drawLine(Line line, Point[] pointsToDraw) {
            Vec2f vp0 = pointsToDraw[0].toOpenGLCoordinate(mAspectRatio);
            Vec2f vp1 = pointsToDraw[1].toOpenGLCoordinate(mAspectRatio);

            VetexDataBuffer buffer = new VetexDataBuffer(2,
                    line.getColor()[3]);
            mIndex = 0;

            ArrayList<Vec2f> points = new ArrayList<Vec2f>(2);
            if (null != line.getLastPointsDrawn()) {
                points.addAll(line.getLastPointsDrawn());
            }
            points.add(new Vec2f(vp0.x, vp0.y));
            points.add(new Vec2f(vp1.x, vp1.y));

            int start = 0;
            if (null != line.getLastPointsDrawn()
                    && !line.getLastPointsDrawn().isEmpty()) {
                start = 1;
            }
            // draw all segments
            for (int i = start; i < points.size() - 2; ++i) {
                int a = ((i - 1) < 0) ? 0 : (i - 1);
                int b = i;
                int c = ((i + 1) >= points.size()) ? points.size() - 1
                        : (i + 1);
                int d = ((i + 2) >= points.size()) ? points.size() - 1
                        : (i + 2);

                createSegmentFromLine(buffer, points.get(a), points.get(b),
                        points.get(c), points.get(d), line);
            }

            line.getLastPointsDrawn().clear();
            addNewBufferToDraw(buffer);
        }

        private void addNewBufferToDraw(VetexDataBuffer buffer) {
            mNeedToDraw.add(buffer);
            allDataBuffers.add(buffer);
        }

        private void redraw() {
            synchronized (allDataBuffers) {
                Iterator<VetexDataBuffer> iter = allDataBuffers.iterator();
                while (iter.hasNext()) {
                    VetexDataBuffer buffer = iter.next();
                    mNeedToDraw.add(buffer);
                }
            }
            draw();
        }

        private void draw() {
            float alpha = -1.0f;
            int count = 0;
            vertexs.rewind();
            colors.rewind();
            indices.rewind();

            for (VetexDataBuffer buffer : mNeedToDraw) {
                if (alpha == -1.0f) {
                    alpha = buffer.getAlpha();
                    buffer.commit(vertexs, colors, indices);
                } else if (alpha == buffer.getAlpha() && count < MAX_SEGMENT_PER_DRAW) {
                    buffer.commit(vertexs, colors, indices);
                } else {
                    //to draw
                    drawPoints(alpha);
                    count = 0;
                    //add to buffer;
                    alpha = buffer.getAlpha();
                    buffer.commit(vertexs, colors, indices);
                }
                count++;
            }

            if (count > 0) {
                drawPoints(alpha);
            }
        }

        private void drawPoints(float alpha) {
            int indexCount = indices.position();
            vertexs.rewind();
            colors.rewind();
            indices.rewind();
            GLES20.glEnable(GLES20.GL_BLEND);
            if (Line.PENCIL_APLAH == alpha) {
                GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,
                        GLES20.GL_ONE_MINUS_SRC_ALPHA);
            } else if (Line.ERASE_ALPHA == alpha) {
                // GLES20.glBlendFunc( GLES20.GL_ONE , GLES20.GL_ZERO);
                GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_SRC_ALPHA);
            } else {
                GLES20.glBlendFuncSeparate(GLES20.GL_ONE, GLES20.GL_ZERO, GLES20.GL_ONE, GLES20.GL_ZERO);
//				GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ZERO);
            }

            // get handle to vertex shader's vPosition member and add vertices
            int mPositionHandle = GLES20.glGetAttribLocation(
                    ShaderHelper.sp_color, "vPosition");
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT,
                    false, 0, vertexs);
            GLES20.glEnableVertexAttribArray(mPositionHandle);

            // Get handle to texture coordinates location and load the texture
            // uvs
            int mTexCoordLoc = GLES20.glGetAttribLocation(
                    ShaderHelper.sp_color, "a_vColor");
            GLES20.glVertexAttribPointer(mTexCoordLoc, 4, GLES20.GL_FLOAT,
                    false, 0, colors);
            GLES20.glEnableVertexAttribArray(mTexCoordLoc);

            // Get handle to shape's transformation matrix and add our matrix
            int mtrxhandle = GLES20.glGetUniformLocation(ShaderHelper.sp_color,
                    "uMVPMatrix");
            GLES20.glUniformMatrix4fv(mtrxhandle, 1, false,
                    mtrxProjectionAndView, 0);

            // Draw the triangle
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexCount,
                    GLES20.GL_UNSIGNED_SHORT, indices);

            // Disable vertex array
            GLES20.glDisableVertexAttribArray(mPositionHandle);
            GLES20.glDisableVertexAttribArray(mTexCoordLoc);
            GLES20.glDisable(GLES20.GL_BLEND);

            vertexs.rewind();
            colors.rewind();
            indices.rewind();
        }

        private void createSegmentFromLine(VetexDataBuffer buffer, Vec2f p0,
                                           Vec2f p1, Vec2f p2, Vec2f p3, Line line) {
//			mLogger.info("thickness " + thickness + " realThickNess " + realThickNess + " addEdge " + addEdge);
            float[] color = line.getColor();
            buffer.setAlpha(color[3]);

            float[] edgeColor = new float[4];
            edgeColor[0] = color[0];
            edgeColor[1] = color[1];
            edgeColor[2] = color[2];
            edgeColor[3] = 0.0f;

            float edgeThinkness = EDGE_THICK / viewWidth;
            float thickness = ((float) line.getWidth()) / Point.NORMAL_SCALE;
            float realThickNess = thickness + edgeThinkness;
            boolean addEdge = true;
            if (color[3] < Line.PENCIL_APLAH) {
                edgeColor[3] = color[3];
                edgeThinkness /= 2;
                realThickNess = thickness;
                addEdge = false;
            }

//			mLogger.info("thickness " + thickness + " realThickNess " + realThickNess + " addEdge " + addEdge);

            // skip if zero length
            if (p1 == p2 || p1.equals(p2))
                return;

            // 1) define the line between the two points
            p21.x = p2.x - p1.x;
            p21.y = p2.y - p1.y;
            p21.normalized();

            // 2) find the normal vector of this line
            normal.x = -p21.y;
            normal.y = p21.x;
            normal.normalized();

            // 3) find the tangent vector at both the end points:
            // -if there are no segments before or after this one, use the line
            // itself
            // -otherwise, add the two normalized lines and average them by
            // normalizing again
            Vec2f tangent1 = getTangent(p0, p1, p21);
            Vec2f tangent2 = getTangent(p2, p3, p21);

            // 4) find the miter line, which is the normal of the tangent
            miter1.x = -tangent1.y;
            miter1.y = tangent1.x;
            miter2.x = -tangent2.y;
            miter2.y = tangent2.x;
            // find length of miter by projecting the miter onto the normal,
            // take the length of the projection, invert it and multiply it by
            // the thickness:
            // length = thickness * ( 1 / |normal|.|miter| )
            float length1 = realThickNess / Math.abs(normal.dot(miter1));
            if (length1 > realThickNess * 1.5f) {
                length1 = realThickNess * 1.5f;
            }
            float length2 = realThickNess / Math.abs(normal.dot(miter2));
            if (length2 > realThickNess * 1.5f) {
                length2 = realThickNess * 1.5f;
            }

//			 mLogger.info("czj p0 " + p0);
//			 mLogger.info("czj p1 " + p1);
//			 mLogger.info("czj p2 " + p2);
//			 mLogger.info("czj p3 " + p3);

            leftBottom.x = p1.x - length1 * miter1.x;
            leftBottom.y = p1.y - length1 * miter1.y;
            leftTop.x = p1.x + length1 * miter1.x;
            leftTop.y = p1.y + length1 * miter1.y;
            rightBottom.x = p2.x - length2 * miter2.x;
            rightBottom.y = p2.y - length2 * miter2.y;
            rightTop.x = p2.x + length2 * miter2.x;
            rightTop.y = p2.y + length2 * miter2.y;

//			 mLogger.info("czj leftBottom " + leftBottom);
//			 mLogger.info("czj rightBottom " + rightBottom);
//			 mLogger.info("czj leftTop " + leftTop);
//			 mLogger.info("czj rightTop " + rightTop);
            buffer.addPoint(p1);
            buffer.addColor(color);
            buffer.addPoint(p2);
            buffer.addColor(color);
            buffer.addPoint(leftBottom);
            buffer.addColor(edgeColor);
            buffer.addPoint(leftTop);
            buffer.addColor(edgeColor);
            buffer.addPoint(rightTop);
            buffer.addColor(edgeColor);
            buffer.addPoint(rightBottom);
            buffer.addColor(edgeColor);

            int count = 6;
            if (addEdge) {

                delta.x = leftTop.x - leftBottom.x;
                delta.y = leftTop.y - leftBottom.y;
                delta.normalized();
                delta.scale(edgeThinkness);

                leftBottomMid.x = leftBottom.x + delta.x;
                leftBottomMid.y = leftBottom.y + delta.y;
                leftTopMid.x = leftTop.x - delta.x;
                leftTopMid.y = leftTop.y - delta.y;

                delta.x = rightTop.x - rightBottom.x;
                delta.y = rightTop.y - rightBottom.y;
                delta.normalized();
                delta.scale(edgeThinkness);

                rightBottomMid.x = rightBottom.x + delta.x;
                rightBottomMid.y = rightBottom.y + delta.y;
                rightTopMid.x = rightTop.x - delta.x;
                rightTopMid.y = rightTop.y - delta.y;

//				 mLogger.info("czj leftBottomMid " + leftBottomMid);
//				 mLogger.info("czj leftTopMid " + leftTopMid);
//				 mLogger.info("czj rightBottomMid " + rightBottomMid);
//				 mLogger.info("czj rightTopMid " + rightTopMid);

                buffer.addPoint(leftBottomMid);
                buffer.addColor(color);
                buffer.addPoint(leftTopMid);
                buffer.addColor(color);

                buffer.addPoint(rightTopMid);
                buffer.addColor(color);
                buffer.addPoint(rightBottomMid);
                buffer.addColor(color);
                count += 4;

                buffer.addIndex((short) (mIndex + 7));
                buffer.addIndex((short) (mIndex + 6));
                buffer.addIndex((short) (mIndex + 9));
                buffer.addIndex((short) (mIndex + 9));
                buffer.addIndex((short) (mIndex + 7));
                buffer.addIndex((short) (mIndex + 8));

                buffer.addIndex((short) (mIndex + 7));
                buffer.addIndex((short) (mIndex + 8));
                buffer.addIndex((short) (mIndex + 4));
                buffer.addIndex((short) (mIndex + 4));
                buffer.addIndex((short) (mIndex + 7));
                buffer.addIndex((short) (mIndex + 3));

                buffer.addIndex((short) (mIndex + 6));
                buffer.addIndex((short) (mIndex + 2));
                buffer.addIndex((short) (mIndex + 5));
                buffer.addIndex((short) (mIndex + 5));
                buffer.addIndex((short) (mIndex + 6));
                buffer.addIndex((short) (mIndex + 9));

            } else {
                buffer.addIndex(mIndex);
                buffer.addIndex((short) (mIndex + 2));
                buffer.addIndex((short) (mIndex + 5));
                buffer.addIndex((short) (mIndex + 5));
                buffer.addIndex((short) (mIndex + 0));
                buffer.addIndex((short) (mIndex + 1));

                buffer.addIndex(mIndex);
                buffer.addIndex((short) (mIndex + 3));
                buffer.addIndex((short) (mIndex + 4));
                buffer.addIndex((short) (mIndex + 4));
                buffer.addIndex((short) (mIndex + 0));
                buffer.addIndex((short) (mIndex + 1));
            }
            mIndex += count;
        }

        private Vec2f getTangent(Vec2f p0, Vec2f p1, Vec2f line) {
            if (p0.equals(p1)) {
                return line;
            }
            Vec2f p = Vec2f.minus(p1, p0).normalized();
            p.x += line.x;
            p.y += line.y;

            if (p.x == 0.0f && p.y == 0.0f) {
                return line;
            }
            return p.normalized();
        }
    }

    public void onMessage(String text) {
        Message msg = Message.obtain();
        msg.what = MSG_WHITEBOARD_MESSAGE_ARRIVAL;
        msg.obj = text;
        handler.sendMessage(msg);
    }

    public void onMessages(Object obj) {
        Message msg = Message.obtain();
        msg.what = MSG_WHITEBOARD_MESSAGES_ARRIVAL;
        msg.obj = obj;
        handler.sendMessage(msg);
    }

    private boolean onLineArrival(LineMessage lm) {
        try {
            if (null == lm.getP() || lm.getP().isEmpty()) {
                mLogger.warning("not point in the line, ignore");
                return false;
            }

            if (null != lm && null != lm.getId()) {
                Line line = null;
                boolean needRender = false;
                line = allLines.get(lm.getSeq());
                if (null == line) {
                    LineMessage.PointInfo pi = lm.getP().get(0);
                    if (0 == pi.getW() || pi.getC().isEmpty()) {
                        mLogger.warning("can not find color and width for new line");
                        return false;
                    }
                    line = new Line(toOpenglColor(pi.getC()),
                            pi.getW(), lm.getSeq());
                    allLines.put(lm.getSeq(), line);
                }

                if (line.isCompleted()) {
                    mLogger.warning("duplicated line found " + line.getSeq());
                    return false;
                }

                LineMessage.PointInfo last = lm.getP().get(lm.getP().size() - 1);
                if (last.getW() == 0) {
                    //mark line is completed
                    line.setCompleted(true);
                }

                if (0 == mDeviceUrl.compareToIgnoreCase(lm.getId())) {
                    // this is line from myself.
                    if (1 == lm.getC()) {
                        // get all lines. shall render it
                        needRender = addAndRenderLine(lm, line);

                    } else {
                        // it is already render.
                        addButNotRenderLine(lm, line);
                    }
                } else {
                    needRender = addAndRenderLine(lm, line);
                }
                return needRender;

            } else {
                mLogger.warning("receive error line ");
            }
        } catch (Exception ex) {

        }
        return false;
    }

    private void onClearLines() {
        allDataBuffers.clear();
        unDrawnPoints.clear();
        allLines.clear();
        mReDraw = true;
        requestRender();
    }

    private boolean addAndRenderLine(LineMessage lm, Line line) {
        for (LineMessage.PointInfo pi : lm.getP()) {
            Point p = new Point(pi.getX(), pi.getY());

            //remove remote point which is too dense.
            if (!line.isCompleted()) {
                Point lastPoint = line.getLastPoint();
                if (null != lastPoint) {
                    int deltaX = Math.abs(lastPoint.getX() - p.getX());
                    int deltaY = Math.abs(lastPoint.getY() - p.getY());
                    if (deltaX < 4 && deltaY < 4) {
                        continue;
                    }
                }
            }
            p.add2UnDrawn(line);
            unDrawnPoints.add(p);
        }
        return line.canBeRender();
    }

    private void addButNotRenderLine(LineMessage lm, Line line) {
        for (LineMessage.PointInfo pi : lm.getP()) {
            Point p = new Point(pi.getX(), pi.getY(), line);
            line.move2Drawn(p);
        }
    }

    private float[] toOpenglColor(String text) {
        Long color = Long.valueOf(text.substring(1), 16);
        float[] colorf = new float[4];
        colorf[0] = ((float) ((color & 0xff000000) >> 24)) / 255;
        colorf[1] = ((float) ((color & 0x00ff0000) >> 16)) / 255;
        colorf[2] = ((float) ((color & 0x0000ff00) >> 8)) / 255;
        colorf[3] = ((float) ((color & 0x000000ff))) / 255;
        return colorf;
    }

    private class MessageHandleCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case MSG_WHITEBOARD_MESSAGE_ARRIVAL:
                        handleMessageArrival(msg);
                        break;
                    case MSG_WHITEBOARD_MESSAGES_ARRIVAL:
                        L.i("WhiteBoard-->MSG_WHITEBOARD_MESSAGES_ARRIVAL message arrive start to draw line");
                        handleMessagesArrival(msg);
                        break;
                    case MSG_WHITEBOARD_CLOSE:
                        mLogger.info("clear all lines");
                        onClearLines();
                        allLines.clear();
                        handler.removeMessages(MSG_WHITEBOARD_MESSAGES_ARRIVAL);
                        break;
                    default:
                }
            } catch (Exception ex) {
                mLogger.warning("fail to handle message " + ex.toString());
            }
            return false;
        }

        private void handleMessageArrival(Message msg) {
            String text = (String) msg.obj;
            L.i("WhiteBoard--> clear line: " + text);
            BaseMessage bm = JsonUtil.toObject(text, BaseMessage.class);
            if (null != bm) {
                switch (bm.getType()) {
                    case BaseMessage.ClearLines: {
                        onClearLines();
                    }
                    default:
                        break;
                }
            }
        }

        private void handleMessagesArrival(Message msg) {
            try {
                ArrayList<String> messages = (ArrayList<String>) msg.obj;
                boolean needRender = false;
                for (String text : messages) {
                    try {
                        LineMessage lm = JsonUtil.toObject(text, LineMessage.class);
                        if (null != lm) {
                            boolean needRenderOnce = onLineArrival(lm);
                            if (!needRender && needRenderOnce) {
                                needRender = needRenderOnce;
                            }
                        }
                    } catch (Exception ex) {
                    }
                }
                if (needRender) {
                    requestRender();
                }
            } catch (Exception ex) {

            }
        }
    }
}
