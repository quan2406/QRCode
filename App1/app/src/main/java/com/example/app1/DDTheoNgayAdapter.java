package com.example.app1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class DDTheoNgayAdapter extends ArrayAdapter<DiemDanhTheoNgay> {

    public DDTheoNgayAdapter(@NonNull Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public DDTheoNgayAdapter(@NonNull Context context, int resource, @NonNull List<DiemDanhTheoNgay> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v=convertView;
        if (v==null){
            LayoutInflater vl =LayoutInflater.from(getContext());
            v=vl.inflate(R.layout.item,null);
        }

        DiemDanhTheoNgay diemDanhTheoNgay=getItem(position);
        if(diemDanhTheoNgay!=null){
            TextView txtMaNV=(TextView) v.findViewById(R.id.textFieldMaNV);
            txtMaNV.setText("Mã nhân viên : "+diemDanhTheoNgay.maNV);
            TextView txtTen=(TextView) v.findViewById(R.id.textFieldTen);
            txtTen.setText("Tên nhân viên : "+diemDanhTheoNgay.ten);
            TextView txtDd=(TextView) v.findViewById(R.id.textFieldDd);
            txtDd.setText(diemDanhTheoNgay.diemDanh);
        }
        return  v;
    }
}
