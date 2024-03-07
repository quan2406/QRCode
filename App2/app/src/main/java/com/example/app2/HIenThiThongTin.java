package com.example.app2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class HIenThiThongTin extends AppCompatActivity {
    TextView tvMaNV,tvHoTen,tvGioiTinh;
    ImageView imvQR;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hien_thi_thong_tin);

        tvMaNV = (TextView) findViewById(R.id.textViewMaNV);
        tvHoTen=(TextView) findViewById(R.id.textViewHoTen);
        tvGioiTinh=(TextView) findViewById(R.id.textViewGioiTinh);
        imvQR=(ImageView) findViewById(R.id.imageViewQR);
        db=FirebaseFirestore.getInstance();

        String maNV;

        Intent intent =getIntent();
        if(intent.hasExtra("dataMain")){
            maNV = intent.getStringExtra("dataMain");

            DocumentReference docRef = db.collection("NhanVien").document(maNV);
            // Lấy dữ liệu từ Firestore
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        tvMaNV.setText("Mã nhân viên : " + maNV);
                        // Tài liệu tồn tại
                        // Lấy ra dữ liệu các trường dựa trên key của chúng
                        String ngaySinh = document.getString("ngaSinh");
                        String hoTen=document.getString("hoTen");
                        String qrCode=document.getString("dataQR");

                        if (hoTen != null) {
                            // Thực hiện xử lý với gioiTinh
                            tvHoTen.setText("Họ tên : "+hoTen);
                        } else {
                            // Trường gioiTinh không tồn tại hoặc có giá trị null
                        }

                        // Sử dụng giá trị gioiTinh theo nhu cầu của bạn
                        if (ngaySinh != null) {
                            // Thực hiện xử lý với gioiTinh
                            tvGioiTinh.setText("Giới tính : "+ngaySinh);
                        } else {

                        }

                        if (qrCode != null) {
                            Bitmap qrBit=convertBase64ToBitmap(qrCode);
                            imvQR.setImageBitmap(qrBit);
                        } else {

                        }

                    } else {
                        // Tài liệu không tồn tại
                        Toast.makeText(HIenThiThongTin.this,"Không tồn tại nhân viên",Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Xử lý khi có lỗi xảy ra
                    Toast.makeText(HIenThiThongTin.this,"Lỗi",Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    public static Bitmap convertBase64ToBitmap(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}