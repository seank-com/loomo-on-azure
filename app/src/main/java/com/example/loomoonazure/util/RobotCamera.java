package com.example.loomoonazure.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class RobotCamera {
    private static final String TAG = "RobotCamera";

    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_LOCK = 1;
    private static final int STATE_WAITING_PRECAPTURE = 2;
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;
    private static final int STATE_PICTURE_TAKEN = 4;

    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    private Activity activity;
    private String cameraId;

//    private SurfaceTexture surfaceTexture;
    private RobotTracking robotTracking;

    private CameraCaptureSession captureSession;
    private CameraDevice cameraDevice;
    private CameraDevice.StateCallback stateCallback;

    private HandlerThread backgroundThread;
    private Handler backgroundHandler;

    private ImageReader imageReader;
    private ImageReader.OnImageAvailableListener onImageAvailableListener;

    private CaptureRequest.Builder previewRequestBuilder;
    private CaptureRequest previewRequest;
    private int state;
    private Semaphore cameraOpenCloseLock;
    private CameraCaptureSession.CaptureCallback captureCallback;
    private int photoCount;

    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    private void createCameraPreviewSession() {
        RobotCamera that = this;

        Log.d(TAG, String.format("createCameraPreviewSession threadId=%d", Thread.currentThread().getId()));

        try {
            Thread.sleep(30000);

            //Surface surface = new Surface(surfaceTexture);
            Surface dtsSurface = robotTracking.getSurface();

            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(dtsSurface);

            cameraDevice.createCaptureSession(Arrays.asList(dtsSurface, imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.d(TAG, String.format("onConfigured threadId=%d", Thread.currentThread().getId()));

                    // The camera is already closed
                    if (null == that.cameraDevice) {
                        return;
                    }

                    // When the session is ready, we start displaying the preview.
                    that.captureSession = cameraCaptureSession;
                    try {
                        // Auto focus should be continuous for camera preview.
                        that.previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                        // Finally, we start displaying the camera preview.
                        that.previewRequest = that.previewRequestBuilder.build();
                        that.captureSession.setRepeatingRequest(that.previewRequest, that.captureCallback, that.backgroundHandler);

                        robotTracking.beginTracking();
                    } catch (CameraAccessException e) {
                        Log.d(TAG, "CameraAccessException", e);
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.d(TAG, String.format("onConfigureFailed threadId=%d", Thread.currentThread().getId()));
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.d(TAG, "CameraAccessException", e);
        } catch (InterruptedException e) {
            Log.d(TAG, "InterruptedException", e);
        }
    }

    private void lockFocus() {
        Log.d(TAG, String.format("lockFocus threadId=%d", Thread.currentThread().getId()));
        try {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            state = STATE_WAITING_LOCK;
            captureSession.capture(previewRequestBuilder.build(), captureCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            Log.d(TAG, "CameraAccessException", e);
        }
    }

    private void runPrecaptureSequence() {
        Log.d(TAG, String.format("runPrecaptureSequence threadId=%d", Thread.currentThread().getId()));
        try {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            state = STATE_WAITING_PRECAPTURE;
            captureSession.capture(previewRequestBuilder.build(), captureCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            Log.d(TAG, "CameraAccessException", e);
        }
    }

    private void captureStillPicture () {
        Log.d(TAG, String.format("captureStillPicture threadId=%d", Thread.currentThread().getId()));
        try {
            if (cameraDevice == null) {
                return;
            }

            CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());

            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            // Might need to fix orientation here. See this link for ideas how
            // https://github.com/android/camera-samples/blob/master/Camera2BasicJava/Application/src/main/java/com/example/android/camera2basic/Camera2BasicFragment.java#L828

            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    Log.d(TAG, String.format("onCaptureCompleted threadId=%d", Thread.currentThread().getId()));

                    unlockFocus();
                }
            };

            captureSession.stopRepeating();
            captureSession.abortCaptures();
            captureSession.capture(captureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            Log.d(TAG, "CameraAccessException", e);
        }
    }

    private void unlockFocus() {
        Log.d(TAG, String.format("unlockFocus threadId=%d", Thread.currentThread().getId()));
        try {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            captureSession.capture(previewRequestBuilder.build(), captureCallback, backgroundHandler);
            state = STATE_PREVIEW;
            captureSession.setRepeatingRequest(previewRequest, captureCallback, backgroundHandler);

        } catch (CameraAccessException e) {
            Log.d(TAG, "CameraAccessException", e);
        }
    }

    public RobotCamera(Activity activity) {
        RobotCamera that = this;

        Log.d(TAG, String.format("constructor threadId=%d", Thread.currentThread().getId()));

        this.activity = activity;
        this.state = STATE_PREVIEW;
        this.photoCount = 0;

        this.cameraOpenCloseLock = new Semaphore(1);

//        this.surfaceTexture = new SurfaceTexture(10);

        this.stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice cameraDevice) {
                Log.d(TAG, String.format("onOpened threadId=%d", Thread.currentThread().getId()));
                // This method is called when the camera is opened.  We start camera preview here.
                that.cameraOpenCloseLock.release();
                that.cameraDevice = cameraDevice;
                that.createCameraPreviewSession();
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                Log.d(TAG, String.format("onDisconnected threadId=%d", Thread.currentThread().getId()));
                that.cameraOpenCloseLock.release();
                that.cameraDevice.close();
                that.cameraDevice = null;
            }

            @Override
            public void onError(@NonNull CameraDevice cameraDevice, int error) {
                Log.d(TAG, String.format("onError threadId=%d", Thread.currentThread().getId()));
                that.cameraOpenCloseLock.release();
                that.cameraDevice.close();
                that.cameraDevice = null;
            }
        };

        this.onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Log.d(TAG, String.format("onImageAvailable threadId=%d", Thread.currentThread().getId()));
                that.photoCount += 1;
                that.backgroundHandler.post(new ImagePoster(reader.acquireNextImage(), that.photoCount));
            }
        };

        this.captureCallback = new CameraCaptureSession.CaptureCallback() {
            private void process(CaptureResult result) {
                Log.d(TAG, String.format("process threadId=%d", Thread.currentThread().getId()));
                switch (that.state) {
                    case STATE_PREVIEW: {
                        // We have nothing to do when the camera preview is working normally.
                        break;
                    }
                    case STATE_WAITING_LOCK: {
                        Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                        if (afState == null) {
                            that.captureStillPicture();
                        } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                                CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                            // CONTROL_AE_STATE can be null on some devices
                            Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                            if (aeState == null ||
                                    aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                                that.state = STATE_PICTURE_TAKEN;
                                that.captureStillPicture();
                            } else {
                                that.runPrecaptureSequence();
                            }
                        }
                        break;
                    }
                    case STATE_WAITING_PRECAPTURE: {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                                aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                            that.state = STATE_WAITING_NON_PRECAPTURE;
                        }
                        break;
                    }
                    case STATE_WAITING_NON_PRECAPTURE: {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                            that.state = STATE_PICTURE_TAKEN;
                            that.captureStillPicture();
                        }
                        break;
                    }
                }
            }

            @Override
            public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                            @NonNull CaptureRequest request,
                                            @NonNull CaptureResult partialResult) {
                Log.d(TAG, String.format("onCaptureProgressed threadId=%d", Thread.currentThread().getId()));
                process(partialResult);
            }

            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                           @NonNull CaptureRequest request,
                                           @NonNull TotalCaptureResult result) {
                Log.d(TAG, String.format("onCaptureCompleted threadId=%d", Thread.currentThread().getId()));
                process(result);
            }
        };
    }

    public void start(RobotTracking robotTracking) {
        Log.d(TAG, String.format("start threadId=%d", Thread.currentThread().getId()));

        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                Log.e(TAG, "tryAcquire timed out");
                return;
            }

            backgroundThread = new HandlerThread("CameraBackground");
            backgroundThread.start();
            backgroundHandler = new Handler(this.backgroundThread.getLooper());

            CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);

            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);

                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing == null && facing != CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                Log.d(TAG, String.format("start: facing = %d", facing));

                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                List<Size> outputSizes = Arrays.asList(map.getOutputSizes(ImageFormat.JPEG));
                CompareSizesByArea comparer = new CompareSizesByArea();

                Size max = Collections.max(outputSizes, comparer);
//                Size min = Collections.min(outputSizes, comparer);
//
//                surfaceTexture.setDefaultBufferSize(min.getWidth(), min.getHeight());
                this.robotTracking = robotTracking;

                imageReader = ImageReader.newInstance(max.getWidth(), max.getHeight(), ImageFormat.JPEG, 2);
                imageReader.setOnImageAvailableListener(onImageAvailableListener, backgroundHandler);

                this.cameraId = cameraId;

                manager.openCamera(cameraId, stateCallback, backgroundHandler);
                return;
            }
        } catch (CameraAccessException e) {
            Log.d(TAG, "CameraAccessException", e);
        } catch (InterruptedException e) {
            Log.d(TAG, "InterruptedException", e);
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            Log.d(TAG, "NullPointerException", e);
        }

        cameraOpenCloseLock.release();
    }

    public void takePicture() {
        lockFocus();
    }

    public void stop() {
        Log.d(TAG, String.format("stop threadId=%d", Thread.currentThread().getId()));

        try {
            cameraOpenCloseLock.acquire();

            if (backgroundThread != null) {
                backgroundThread.quitSafely();
                backgroundThread.join();
                backgroundThread = null;
            }

            backgroundHandler = null;

            if (captureSession != null) {
                captureSession.close();
                captureSession = null;
            }
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (imageReader != null) {
                imageReader.close();
                imageReader = null;
            }

            robotTracking = null;
        } catch (InterruptedException e) {
            Log.d(TAG, "InterruptedException", e);
        } finally {
            cameraOpenCloseLock.release();
        }
    }
}
