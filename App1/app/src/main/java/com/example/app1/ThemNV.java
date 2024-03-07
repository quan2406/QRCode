package com.example.app1;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ThemNV extends AppCompatActivity {
    Button btnTaoQR ;
    EditText edtTen,edtNgaySinh,edtMaNV;
    ImageView imgvQR;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_nv);

        btnTaoQR = (Button) findViewById(R.id.btnTaoMa);
        edtTen = (EditText) findViewById(R.id.editTextHoTen);
        edtNgaySinh = (EditText) findViewById(R.id.editTextNgaySinh);
        // Sự kiện khi nhấn vào EditTextNgaySinh
        edtNgaySinh.setOnClickListener(v -> showDatePickerDialog());
        edtMaNV = (EditText) findViewById(R.id.editTextMaNV);
        imgvQR = (ImageView) findViewById(R.id.imgvQR);

        db=FirebaseFirestore.getInstance();

        btnTaoQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ten=edtTen.getText().toString();
                String gioiTinh=edtNgaySinh.getText().toString();
                String maNV=edtMaNV.getText().toString();

                if(ten!=null && gioiTinh!=null && maNV!=null ){
                    Bitmap qrCode =generateQRCode(maNV,300,300);
                    imgvQR.setImageBitmap(qrCode);
                    db.collection("NhanVien")
                            .document(maNV)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        // Tài liệu có tồn tại với ID đã cho
                                        // Thực hiện xử lý tương ứng
                                        Toast.makeText(ThemNV.this,"Đã tồn tại mã nhân viên",Toast.LENGTH_LONG).show();
                                    } else {
                                        // Không có tài liệu với ID đã cho
                                        // Thực hiện xử lý tương ứng
                                        Map<String,Object> data=new HashMap<>();
                                        String dataQR = convertBitmapToBase64(qrCode);
                                        data.put("hoTen",ten);
                                        data.put("ngaySinh",gioiTinh);
                                        data.put("dataQR",dataQR);

                                        DocumentReference documentReference = db.collection("NhanVien").document(maNV);
                                        // Thêm dữ liệu vào Firestore
                                        documentReference.set(data)
                                                .addOnSuccessListener(aVoid -> {
                                                    // Xử lý khi dữ liệu được thêm thành công
                                                    Toast.makeText(ThemNV.this,"Đã thêm dữ liệu thành công",Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Xử lý khi có lỗi xảy ra
                                                    Toast.makeText(ThemNV.this,"lỗi khi thêm ",Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                } else {
                                    // Xử lý khi có lỗi xảy ra
                                }
                            });

                }
                else {
                    Toast.makeText(ThemNV.this,"Điền đây dủ các trường dữ liệu",Toast.LENGTH_LONG).show();
                }

            }
        });
    }


    public static String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
    private Bitmap generateQRCode(String data, int width, int height) {
        Writer qrCodeWriter = new QRCodeWriter();

        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);
            int bitMatrixWidth = bitMatrix.getWidth();
            int bitMatrixHeight = bitMatrix.getHeight();
            int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

            for (int y = 0; y < bitMatrixHeight; y++) {
                for (int x = 0; x < bitMatrixWidth; x++) {
                    pixels[y * bitMatrixWidth + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }


            Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, bitMatrixWidth, 0, 0, bitMatrixWidth, bitMatrixHeight);

            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showDatePickerDialog() {
        // Lấy ngày, tháng, năm hiện tại
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Tạo DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                    // Xử lý khi người dùng chọn ngày
                    String selectedDate = selectedDayOfMonth + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    edtNgaySinh.setText(selectedDate);
                },
                year, month, dayOfMonth);

        // Hiển thị DatePickerDialog
        datePickerDialog.show();
    }


}