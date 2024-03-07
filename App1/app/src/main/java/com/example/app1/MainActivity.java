package com.example.app1;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    Button btnThem,btnQuetMa,btnXem;
    ImageView imvQuetQR;

    private FirebaseFirestore db;
    private boolean isProcessing = false; // Biến kiểm soát quá trình xử lý

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnThem=(Button) findViewById(R.id.buttonThemNV);
        btnQuetMa=(Button) findViewById(R.id.buttonQuetMa);
        btnXem=(Button) findViewById(R.id.buttonXem);

        db = FirebaseFirestore.getInstance();

        Intent it = getIntent();
        String qrCode_maNV = it.getStringExtra("resutl");
        if (qrCode_maNV != null) {
            handleValidQRCode(qrCode_maNV);
        }


        btnThem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ThemNV.class);
                startActivity(intent);
            }
        });

        btnQuetMa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,QuetQR.class);
                startActivity(intent);
            }
        });

        btnXem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }


    private void handleValidQRCode(String qrCode_maNV) {

        Timestamp currentTimestamp = Timestamp.now();
        CollectionReference collectionReference = db.collection("NgayDiemDanh");

        collectionReference.limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                    Timestamp time = documentSnapshot.getTimestamp("time");

                    if (isAfter(currentTimestamp, time)) {
                        isProcessing = true; // Đánh dấu rằng quá trình xử lý đã bắt đầu
                        checkAndDeleteDocumentsThenAdd(qrCode_maNV);

                    } else {
                        checkEmployeeExistence(qrCode_maNV);
                    }
                }
            }
        });

    }

    private void checkAndDeleteDocumentsThenAdd(String qrCode_maNV) {
        CollectionReference collectionReference = db.collection("NgayDiemDanh");

        collectionReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult()) {
                    document.getReference().delete();
                }
                addEmployeeToCollection(qrCode_maNV);

            } else {
                showToast("Điểm danh thất bại");
                isProcessing = false; // Đặt lại biến kiểm soát khi quá trình xử lý không thành công
            }
        });
    }

    private void checkEmployeeExistence(String qrCode_maNV) {
        CollectionReference collectionReference = db.collection("NgayDiemDanh");

        collectionReference.document(qrCode_maNV).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    showToast( qrCode_maNV+"Đã điểm danh trong ngày");

                } else {
                    addEmployeeToCollection(qrCode_maNV);
                }
            } else {
                // Xử lý khi có lỗi xảy ra
                showToast("Điểm danh thất bại");

            }
        });
    }

    //Thêm vào bàng NgayDiemDanh
    private void addEmployeeToCollection(String qrCode_maNV) {

        Map<String, Object> data = new HashMap<>();
        data.put("time", FieldValue.serverTimestamp());

        DocumentReference documentReference = db.collection("NgayDiemDanh").document(qrCode_maNV);
        documentReference.set(data)
                .addOnSuccessListener(aVoid -> {
                    // Xử lý khi dữ liệu được thêm thành công
                    addNgayDiemDanhtoCollection(qrCode_maNV);

                })
                .addOnFailureListener(e -> {
                    // Xử lý khi có lỗi xảy ra
                    showToast("Điểm danh thất bại");
                });
    }

    //Thêm vào bảng DiemDanh
    private void addNgayDiemDanhtoCollection(String qrCode_maNV) {
        Map<String, Object> data = new HashMap<>();
        Date currentDate = new Date();
// Chuyển đổi đối tượng Date thành chuỗi ngày tháng năm
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(currentDate);

        data.put("idNgay", formattedDate);
        data.put("time", FieldValue.serverTimestamp());
        data.put("MaNV", qrCode_maNV);


        db.collection("DiemDanh")
                .add(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast(qrCode_maNV+"Đã điểm danh");
                        // Dừng quét và quay về màn hình ban đầu sau khi thêm dữ liệu thành công
                        isProcessing = false;
                        // (Gọi hàm chuyển về màn hình ban đầu nếu cần)
                    } else {
                        showToast("Điểm danh thất bại");
                        isProcessing = false; // Đặt lại biến kiểm soát khi quá trình xử lý không thành công
                    }
                });
    }


    private boolean isAfter(Timestamp timestamp1, Timestamp timestamp2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(timestamp1.toDate());
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(timestamp2.toDate());
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        return cal1.after(cal2);
    }



    private void showToast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }


    //Sử dụng cho btnXem

    private void showDatePickerDialog() {
        // Lấy ngày, tháng, năm hiện tại
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Tạo DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDayOfMonth) {
                        // Xử lý khi người dùng chọn ngày
                        handleDateSelection(selectedYear, selectedMonth, selectedDayOfMonth);
                    }
                },
                year, month, dayOfMonth);

        // Hiển thị DatePickerDialog
        datePickerDialog.show();
    }

    private void handleDateSelection(int year, int month, int dayOfMonth) {
        // Xử lý dữ liệu ngày tháng
        String selectedDate = dayOfMonth + "-" + (month + 1) + "-" + year;


        // Chuyển sang màn hình mới với dữ liệu ngày tháng
            Intent intent = new Intent(MainActivity.this, XemDiemDanh.class);
            intent.putExtra("selectedDate", selectedDate);
            startActivity(intent);
    }


}