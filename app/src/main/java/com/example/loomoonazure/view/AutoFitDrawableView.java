package com.example.loomoonazure.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.segway.robot.algo.dts.DTSPerson;

public class AutoFitDrawableView extends FrameLayout implements TextureView.SurfaceTextureListener {

    private class CustomView extends SurfaceView implements Handler.Callback {
        private final Paint paint;
        private final SurfaceHolder holder;

        private final Handler handler = new Handler(this);
        static final int CLEAR = 1;

        public CustomView(Context context) {
            super(context);
            holder = getHolder();
            holder.setFormat(PixelFormat.TRANSPARENT);
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.RED);
            paint.setStrokeWidth(3.0f);
            paint.setStyle(Paint.Style.STROKE);
            paint.setTextSize(40);
        }

        public void drawRect(final Rect... rects) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    preview.invalidate();
                    if (holder.getSurface().isValid()) {
                        final Canvas canvas = holder.lockCanvas();

                        if (canvas != null) {
                            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                            canvas.drawColor(Color.TRANSPARENT);
                            for (Rect rect : rects) {
                                canvas.drawRect(ratioWidth - rect.left, rect.top, ratioWidth - rect.right, rect.bottom, paint);
                            }
                            holder.unlockCanvasAndPost(canvas);
                            handler.removeMessages(CLEAR);
                            handler.sendEmptyMessageDelayed(CLEAR, 1000);
                        }
                    }
                }
            });
        }

        public void drawRect(final DTSPerson[] persons) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    preview.invalidate();
                    if (holder.getSurface().isValid()) {
                        final Canvas canvas = holder.lockCanvas();

                        if (canvas != null) {
                            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                            canvas.drawColor(Color.TRANSPARENT);
                            for (DTSPerson person : persons) {
                                Rect rect = person.getDrawingRect();
                                canvas.drawRect(ratioWidth - rect.left, rect.top, ratioWidth - rect.right, rect.bottom, paint);
                                canvas.drawText("id=" + person.getId(), ratioWidth - rect.right, rect.top, paint);
                            }
                            holder.unlockCanvasAndPost(canvas);
                            handler.removeMessages(CLEAR);
                            handler.sendEmptyMessageDelayed(CLEAR, 1000);
                        }
                    }
                }
            });
        }

        public void drawRect(final int id, final Rect rect) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    preview.invalidate();
                    if (holder.getSurface().isValid()) {
                        final Canvas canvas = holder.lockCanvas();

                        if (canvas != null) {
                            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                            canvas.drawColor(Color.TRANSPARENT);

                            canvas.drawRect(ratioWidth - rect.left, rect.top, ratioWidth - rect.right, rect.bottom, paint);
                            canvas.drawText("id=" + id, ratioWidth - rect.right, rect.top, paint);

                            holder.unlockCanvasAndPost(canvas);
                            handler.removeMessages(CLEAR);
                            handler.sendEmptyMessageDelayed(CLEAR, 1000);
                        }
                    }
                }
            });
        }

        @Override
        public boolean handleMessage(@NonNull Message message) {
            if (message.what == CLEAR) {
                if (holder.getSurface().isValid()) {
                    final Canvas canvas = holder.lockCanvas();
                    if (canvas != null) {
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                        canvas.drawColor(Color.TRANSPARENT);
                        holder.unlockCanvasAndPost(canvas);
                    }
                }
                return true;
            }
            return false;
        }
    }

    private TextureView preview;
    private CustomView overlay;
    private int ratioWidth = 0;
    private int ratioHeight = 0;
    private int rotation;
    private TextureView.SurfaceTextureListener surfaceTextureListener;

    public AutoFitDrawableView(@NonNull Context context) {
        super(context);
        init();
    }

    public AutoFitDrawableView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoFitDrawableView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutParams layoutParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        preview = new TextureView(getContext());
        overlay = new CustomView(getContext());
        preview.setSurfaceTextureListener(this);
        overlay.setZOrderOnTop(true);
        addView(preview, layoutParams);
        addView(overlay, layoutParams);
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, ratioWidth, ratioHeight);
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(viewHeight/ratioHeight, viewWidth/ratioWidth);
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        preview.setTransform(matrix);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == ratioWidth || 0 == ratioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * ratioWidth / ratioHeight) {
                setMeasuredDimension(width, width * ratioHeight / ratioWidth);
            } else {
                setMeasuredDimension(height * ratioWidth / ratioHeight, height);
            }
        }
        configureTransform(width, height);
    }

    public void setPreviewSizeAndRotation(int width, int height, int rotation) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative");
        }

        this.ratioWidth = width;
        this.ratioHeight = height;
        this.rotation = rotation;
        requestLayout();
    }

    public TextureView getPreview() {
        return preview;
    }

    public void setSurfaceTextureListenerForPreview(TextureView.SurfaceTextureListener listener) {
        surfaceTextureListener = listener;
    }

    public void drawRect(Rect... rects) {
        overlay.drawRect(rects);
    }

    public void drawRect(int id, Rect rect) {
        overlay.drawRect(id, rect);
    }

    public void drawRect(DTSPerson[] persons) {
        overlay.drawRect(persons);
    }

    // TextureView.SurfaceTextureListener
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        configureTransform(width, height);
        TextureView.SurfaceTextureListener listener = surfaceTextureListener;
        if (listener != null) {
            listener.onSurfaceTextureAvailable(surfaceTexture, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        TextureView.SurfaceTextureListener listener = surfaceTextureListener;
        if (listener != null) {
            listener.onSurfaceTextureSizeChanged(surfaceTexture, width,height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        TextureView.SurfaceTextureListener listener = surfaceTextureListener;
        if (listener != null) {
            return listener.onSurfaceTextureDestroyed(surfaceTexture);
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        TextureView.SurfaceTextureListener listener = surfaceTextureListener;
        if (listener != null) {
            listener.onSurfaceTextureUpdated(surfaceTexture);
        }

    }
}
