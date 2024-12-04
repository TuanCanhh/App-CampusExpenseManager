package com.example.qlct;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qlct.Model.BudgetItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class HistoryFragment extends Fragment {

    private TextView totalExpenseTextView;
    private RecyclerView recyclerView;
    private BudgetAdapter adapter;
    private ArrayList<BudgetItem> budgetItems;  // Danh sách các mục ngân sách
    private FirebaseFirestore db;
    private String uid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout fragment
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);

        // Ánh xạ các thành phần giao diện
        totalExpenseTextView = rootView.findViewById(R.id.totalExpense);  // Thay thế totalBudgetTextView
        recyclerView = rootView.findViewById(R.id.recyclerView);

        // Cấu hình RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        budgetItems = new ArrayList<>();

        adapter = new BudgetAdapter(budgetItems, getContext(), new BudgetAdapter.OnBudgetItemClickListener() {
            @Override
            public void onBudgetItemClick(String documentId, String group, String amount, String date) {
                // Xử lý sự kiện khi người dùng click vào một mục ngân sách
                Log.d("HistoryFragment", "Clicked on: " + group + ", Amount: " + amount);
            }
        });
        recyclerView.setAdapter(adapter);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Lấy thông tin người dùng hiện tại
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Tải dữ liệu ngân sách từ Firestore
        loadBudgetData(uid);

        return rootView;
    }

    // Phương thức tải dữ liệu ngân sách từ Firestore
    private void loadBudgetData(String userId) {
        db.collection("users")
                .document(userId)
                .collection("expenses") // Nếu bạn có collection chi tiêu là "expenses"
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        budgetItems.clear();

                        double totalExpense = 0; // Biến lưu tổng chi tiêu

                        // Lấy danh sách các mục chi tiêu từ Firestore và tính tổng chi tiêu
                        for (DocumentSnapshot doc : task.getResult()) {
                            BudgetItem item = doc.toObject(BudgetItem.class);
                            if (item != null) {
                                item.setId(doc.getId());  // Lấy ID của document từ Firestore
                                budgetItems.add(item);

                                // Tính tổng chi tiêu
                                try {
                                    totalExpense += Double.parseDouble(item.getAmount());
                                } catch (NumberFormatException e) {
                                    Log.e("HistoryFragment", "Error parsing amount", e);
                                }
                            }
                        }

                        // Cập nhật tổng chi tiêu lên TextView
                        totalExpenseTextView.setText("Total Expense: $" + totalExpense);

                        // Cập nhật RecyclerView với dữ liệu mới
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.w("HistoryFragment", "Error getting documents.", task.getException());
                    }
                });
    }
}
