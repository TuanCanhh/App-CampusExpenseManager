package com.example.qlct;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DialogDetail extends AppCompatActivity {

    private EditText etGroup, etAmount, etDate;
    private Button btnDelete, btnEdit;
    private FirebaseFirestore db;
    private String documentId;
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_detail);

        // Ánh xạ các thành phần
        etGroup = findViewById(R.id.etGroup);
        etAmount = findViewById(R.id.etAmount);
        etDate = findViewById(R.id.etDate);
        btnDelete = findViewById(R.id.btn_delete);
        btnEdit = findViewById(R.id.btn_edit);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Lấy dữ liệu từ Intent
        documentId = getIntent().getStringExtra("id");
        String group = getIntent().getStringExtra("group");
        String amount = getIntent().getStringExtra("amount");
        String date = getIntent().getStringExtra("date");

        // Hiển thị dữ liệu lên EditText
        etGroup.setText(group);
        etAmount.setText(amount);
        etDate.setText(date);

        // Xử lý nút Edit
        btnEdit.setOnClickListener(v -> {
            String updatedGroup = etGroup.getText().toString();
            String updatedAmount = etAmount.getText().toString();
            String updatedDate = etDate.getText().toString();

            if (updatedGroup.isEmpty() || updatedAmount.isEmpty() || updatedDate.isEmpty()) {
                Toast.makeText(DialogDetail.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users")
                    .document(userId)
                    .collection("budgets")
                    .document(documentId)
                    .update(
                            "group", updatedGroup,
                            "amount", updatedAmount,
                            "date", updatedDate
                    )
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(DialogDetail.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(DialogDetail.this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        // Xử lý nút Delete
        btnDelete.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(DialogDetail.this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa mục này không?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        db.collection("users")
                                .document(userId)
                                .collection("budgets")
                                .document(documentId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(DialogDetail.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent();
                                    setResult(RESULT_OK, intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(DialogDetail.this, "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Không", null)
                    .show();
        });
    }
}
