package com.example.qlct;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qlct.Model.BudgetItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetFragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayout emptyLayout;
    private BudgetAdapter adapter;
    private List<BudgetItem> budgetList;
    private FirebaseFirestore db;
    private String uid;
    private EditText etGroup, etAmount, etDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout fragment
        View rootView = inflater.inflate(R.layout.fragment_budget, container, false);

        // Khởi tạo các thành phần giao diện người dùng
        recyclerView = rootView.findViewById(R.id.recyclerView);
        emptyLayout = rootView.findViewById(R.id.emptyLayout);

        // Cấu hình RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        budgetList = new ArrayList<>();

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Lấy thông tin người dùng hiện tại
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Bạn cần đăng nhập để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
            return rootView;
        }
        uid = user.getUid();

        // Cấu hình adapter với callback
        adapter = new BudgetAdapter(budgetList, getContext(), new BudgetAdapter.OnBudgetItemClickListener() {
            @Override
            public void onBudgetItemClick(String documentId, String group, String amount, String date) {
                openDialogDetail(documentId, group, amount, date);
            }
        });
        recyclerView.setAdapter(adapter);

        // Xử lý sự kiện nhấn nút "Tạo mới"
        Button btnCreateNew = rootView.findViewById(R.id.btn_createNew);
        btnCreateNew.setOnClickListener(v -> openCreateNewDialog());

        return rootView;
    }

    // Phương thức để tải dữ liệu ngân sách từ Firestore
    private void loadBudgetData() {
        db.collection("users")
                .document(uid)
                .collection("budgets")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        budgetList.clear();

                        for (DocumentSnapshot doc : task.getResult()) {
                            BudgetItem item = doc.toObject(BudgetItem.class);
                            if (item != null) {
                                item.setId(doc.getId());
                                budgetList.add(item);
                            }
                        }

                        // Cập nhật giao diện dựa trên sự có mặt của dữ liệu
                        if (budgetList.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            emptyLayout.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyLayout.setVisibility(View.GONE);
                        }

                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Phương thức mở hộp thoại tạo ngân sách mới
    private void openCreateNewDialog() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_layout_budget);
        dialog.setCancelable(true);

        // Khởi tạo các thành phần trong hộp thoại
        etGroup = dialog.findViewById(R.id.etGroup);
        etAmount = dialog.findViewById(R.id.etAmount);
        etDate = dialog.findViewById(R.id.etDate);
        Button btnSave = dialog.findViewById(R.id.btn_save);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        // Thay đổi kích thước của hộp thoại
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;  // Full width
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;  // Wrap content based on content size
        dialog.getWindow().setAttributes(params);

        // Xử lý sự kiện nhấn nút lưu
        btnSave.setOnClickListener(v -> {
            // Lấy dữ liệu từ các trường nhập liệu
            String group = etGroup.getText().toString().trim();
            String amount = etAmount.getText().toString().trim();
            String date = etDate.getText().toString().trim();

            // Kiểm tra dữ liệu đầu vào, nếu không hợp lệ, thông báo lỗi
            if (group.isEmpty() || amount.isEmpty() || date.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra xem số tiền có hợp lệ không
            try {
                Double.parseDouble(amount);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Số tiền phải là một giá trị hợp lệ.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lưu vào Firestore
            saveToFirestore(uid, group, amount, date);
            dialog.dismiss();  // Đóng dialog sau khi lưu thành công
        });

        // Xử lý sự kiện nhấn nút hủy
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    // Phương thức lưu ngân sách vào Firestore
    private void saveToFirestore(String userId, String group, String amount, String date) {
        Map<String, Object> budgetData = new HashMap<>();
        budgetData.put("group", group);
        budgetData.put("amount", amount);
        budgetData.put("date", date);

        // Thêm dữ liệu vào Firestore
        db.collection("users")
                .document(userId)
                .collection("budgets")
                .add(budgetData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Thêm ngân sách thành công", Toast.LENGTH_SHORT).show();
                    loadBudgetData();  // Tải lại dữ liệu ngân sách sau khi thêm mới
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi thêm ngân sách: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Phương thức mở hộp thoại chi tiết để chỉnh sửa mục ngân sách
    private void openDialogDetail(String documentId, String group, String amount, String date) {
        Intent intent = new Intent(getContext(), DialogDetail.class);
        intent.putExtra("id", documentId);
        intent.putExtra("group", group);
        intent.putExtra("amount", amount);
        intent.putExtra("date", date);
        startActivityForResult(intent, 100); // 100 là mã yêu cầu
    }

    // Xử lý kết quả trả về từ activity DialogDetail
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == getActivity().RESULT_OK) {
            // Tải lại dữ liệu sau khi chỉnh sửa hoặc xóa
            loadBudgetData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải lại dữ liệu khi fragment được quay lại
        loadBudgetData();
    }
}
