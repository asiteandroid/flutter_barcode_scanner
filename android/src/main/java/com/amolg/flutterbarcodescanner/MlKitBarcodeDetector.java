package com.amolg.flutterbarcodescanner;

import android.graphics.Point;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

/**
 * Mobile Vision {@link Detector} that decodes with ML Kit's bundled barcode model.
 * <p>
 * Mobile Vision's own BarcodeDetector needs Google Play Services to download a native module,
 * which fails on many devices (e.g. API 33 images, offline, low storage) and leaves the detector
 * non-operational. ML Kit's model ships inside the APK, so scanning works on every API level.
 * The existing CameraSource / MultiProcessor / tracker pipeline is unchanged.
 */
class MlKitBarcodeDetector extends Detector<Barcode> {
    private final BarcodeScanner scanner = BarcodeScanning.getClient();

    /**
     * Called on the camera processing thread, never the main thread.
     * <p>
     * ML Kit finishes every process() task with a step on the main thread, so this wait needs the
     * main thread to be free. CameraSource.stop() interrupts this thread before joining it,
     * otherwise onPause would deadlock.
     */
    @Override
    public SparseArray<Barcode> detect(Frame frame) {
        Frame.Metadata m = frame.getMetadata();
        InputImage image = InputImage.fromByteBuffer(frame.getGrayscaleImageData(),
                m.getWidth(), m.getHeight(), m.getRotation() * 90, InputImage.IMAGE_FORMAT_NV21);
        try {
            return toVision(Tasks.await(scanner.process(image)));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // from CameraSource.stop(); run loop exits on !mActive
        } catch (Exception e) {
            Log.w("MlKitBarcodeDetector", "Barcode detection failed", e);
        }
        return new SparseArray<>();
    }

    @Override
    public void release() {
        super.release();
        scanner.close();
    }

    /** Converts ML Kit results to the Mobile Vision type the rest of the plugin uses. */
    static SparseArray<Barcode> toVision(List<com.google.mlkit.vision.barcode.common.Barcode> results) {
        SparseArray<Barcode> out = new SparseArray<>();
        for (com.google.mlkit.vision.barcode.common.Barcode b : results) {
            if (b.getRawValue() == null) continue;
            Barcode v = new Barcode();
            v.rawValue = b.getRawValue();
            v.displayValue = b.getDisplayValue() != null ? b.getDisplayValue() : v.rawValue;
            v.cornerPoints = b.getCornerPoints() != null ? b.getCornerPoints() : new Point[0];
            // Keyed by value so the tracker treats a different code as a new item.
            out.put(v.rawValue.hashCode(), v);
        }
        return out;
    }
}
