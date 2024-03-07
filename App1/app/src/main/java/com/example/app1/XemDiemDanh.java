package com.example.app1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class XemDiemDanh extends AppCompatActivity {

    TextView tvNgay;
    ListView lst;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xem_diem_danh);

        db=FirebaseFirestore.getInstance();

        tvNgay=(TextView) findViewById(R.id.textViewNgay);
        lst=(ListView) findViewById(R.id.listView);

        Intent intent = getIntent();
        if (intent != null) {

            // Lấy giá trị của "selectedDate" từ Intent
            String selectedDate = intent.getStringExtra("selectedDate");
            ArrayList<DiemDanhTheoNgay> diemDanhTheoNgays =new ArrayList<DiemDanhTheoNgay>();
            tvNgay.setText(selectedDate);
            // 1. Truy vấn tất cả maNV từ bảng DiemDanh
            db.collection("DiemDanh")
                    .whereEqualTo("idNgay", selectedDate)  // formattedDate là giá trị ngày bạn quan tâm
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // 2. Lưu trữ danh sách maNV từ bảng DiemDanh
                            List<String> maNVList = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                String maNV = document.getString("MaNV");
                                maNVList.add(maNV);
                            }


                            // 3. Truy vấn tất cả bản ghi từ bảng NhanVien
                            db.collection("NhanVien")
                                    .get()
                                    .addOnCompleteListener(nvTask -> {
                                        if (nvTask.isSuccessful()) {
                                            // 4. Duyệt qua danh sách bản ghi từ bảng NhanVien
                                            for (DocumentSnapshot nvDocument : nvTask.getResult()) {
                                                String maNV = nvDocument.getId();
                                                String ten= nvDocument.getString("hoTen");
                                                if(maNVList.contains(maNV)){
                                                    diemDanhTheoNgays.add(new DiemDanhTheoNgay(maNV,ten,"Có mặt"));
                                                }
                                                else {
                                                    diemDanhTheoNgays.add(new DiemDanhTheoNgay(maNV,ten,"Vắng mặt"));
                                                }

                                            }

                                            DDTheoNgayAdapter ddTheoNgayAdapter =  new DDTheoNgayAdapter(
                                                    XemDiemDanh.this,R.layout.item,diemDanhTheoNgays
                                            );
                                            lst.setAdapter(ddTheoNgayAdapter);

                                        } else {
                                            // Xử lý lỗi khi truy vấn bảng NhanVien
                                            Toast.makeText(XemDiemDanh.this,"Lỗi",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // Xử lý lỗi khi truy vấn bảng DiemDanh
                            Toast.makeText(XemDiemDanh.this,"Ngày nghỉ",Toast.LENGTH_SHORT).show();
                        }
                    });

        }

    }
}