package com.example.app1;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.text.CollationElementIterator;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class QuetQR extends AppCompatActivity {

    private CameraSource cameraSource;
    private BarcodeDetector barcodeDetector;
    private SurfaceView cameraPreview;

    private FirebaseFirestore db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quet_qr);
        cameraPreview = findViewById(R.id.camera_preview);
        db = FirebaseFirestore.getInstance();

        // Kiểm tra quyền CAMERA và yêu cầu quyền nếu chưa được cấp
        if (checkCameraPermission()) {
            initializeCamera();
        } else {
            requestCameraPermission();
        }
    }

    private void initializeCamera() {
        barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build();
        cameraSource = new CameraSource.Builder(this, barcodeDetector).setAutoFocusEnabled(true).build();

        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                startCameraPreview();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                stopCameraPreview();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() > 0) {
                    String qrCode_maNV = barcodes.valueAt(0).displayValue;

                    isValidQRCode(qrCode_maNV, isValid -> {
                        if (isValid) {
                            Intent intent =new Intent(QuetQR.this,MainActivity.class);
                            intent.putExtra("resutl",qrCode_maNV);
                            startActivity(intent);

                        } else {
                            Toast.makeText(QuetQR.this,"Mã không hợp lệ",Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
            }
        });
    }

    private void startCameraPreview() {
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            cameraSource.start(cameraPreview.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopCameraPreview() {
        cameraSource.stop();
        //Intent intent =new Intent(QuetQR.this,MainActivity.class);
        //startActivity(intent);
        //finish();
    }



    private void isValidQRCode(String qrCode, QRCodeValidationCallback callback) {
        db.collection("NhanVien")
                .document(qrCode)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        callback.onValidationResult(document.exists());
                    } else {
                        callback.onValidationResult(false);
                    }
                });
    }



    private boolean checkCameraPermission() {
        int cameraPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        return cameraPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 100);
    }



    public interface QRCodeValidationCallback {
        void onValidationResult(boolean isValid);
    }



}