package com.example.app2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    Button btnXemtt;
    EditText edtMaNV;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnXemtt = (Button) findViewById(R.id.buttonXemTT);
        edtMaNV = (EditText) findViewById(R.id.editTextmaNV);

        db=FirebaseFirestore.getInstance();

        btnXemtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String maNV=edtMaNV.getText().toString().trim();
                if(TextUtils.isEmpty(maNV)){
                    Toast.makeText(MainActivity.this,"Điền mã nhân viên",Toast.LENGTH_SHORT).show();

                }

                db.collection("NhanVien")
                        .document(maNV)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    // ID đã tồn tại trong collection
                                    // Thực hiện xử lý tương ứng
                                    Intent intent = new Intent(MainActivity.this,HIenThiThongTin.class);
                                    intent.putExtra("dataMain",maNV);
                                    startActivity(intent);

                                } else {
                                    // ID không tồn tại trong collection
                                    // Thực hiện xử lý tương ứng
                                    Toast.makeText(MainActivity.this,"Không tồn tại mã nhân viên",Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Xử lý khi có lỗi xảy ra
                                Toast.makeText(MainActivity.this,"Lỗi",Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}