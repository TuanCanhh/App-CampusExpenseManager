package com.example.qlct;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.qlct.Model.BudgetItem;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private TextView totalBudgetTextView;
    private BarChart barChart;
    private PieChart pieChart;
    private SwipeRefreshLayout swipeRefreshLayout;

    private FirebaseFirestore db;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Ánh xạ các thành phần giao diện
        totalBudgetTextView = rootView.findViewById(R.id.totalBudgetTextView);
        barChart = rootView.findViewById(R.id.barChart);
        pieChart = rootView.findViewById(R.id.pieChart);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return rootView;
        }

        // Thiết lập SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::loadBudgetData);

        // Tải dữ liệu ban đầu
        loadBudgetData();

        return rootView;
    }

    private void loadBudgetData() {
        swipeRefreshLayout.setRefreshing(true);

        db.collection("users")
                .document(userId)
                .collection("budgets")
                .get()
                .addOnCompleteListener(task -> {
                    swipeRefreshLayout.setRefreshing(false);

                    if (task.isSuccessful() && task.getResult() != null) {
                        double totalBudget = 0;
                        ArrayList<BarEntry> entries = new ArrayList<>();
                        ArrayList<String> labels = new ArrayList<>();
                        ArrayList<BudgetItem> budgetItems = new ArrayList<>();

                        int index = 0;

                        for (DocumentSnapshot doc : task.getResult()) {
                            String group = doc.getString("group");
                            String amount = doc.getString("amount");
                            String type = doc.getString("type");
                            try {
                                double amountValue = Double.parseDouble(amount);
                                totalBudget += amountValue;

                                // Thêm dữ liệu vào BarChart
                                entries.add(new BarEntry(index++, (float) amountValue));
                                labels.add(group != null ? group : "Unknown");

                                // Thêm dữ liệu vào PieChart
                                budgetItems.add(new BudgetItem(doc.getId(), group, amount, "", type));

                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }

                        totalBudgetTextView.setText("Total Budget: " + totalBudget);

                        // Cập nhật BarChart
                        BarDataSet barDataSet = new BarDataSet(entries, "Budgets");
                        BarData barData = new BarData(barDataSet);
                        barChart.setData(barData);
                        barChart.invalidate();

                        // Hiển thị chú thích dưới các cột của BarChart
                        XAxis xAxis = barChart.getXAxis();
                        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                        xAxis.setGranularity(1f);
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                        // Cập nhật PieChart
                        setupPieChart(budgetItems);

                    } else {
                        Toast.makeText(getContext(), "Error loading data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupPieChart(ArrayList<BudgetItem> budgetItems) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        for (BudgetItem item : budgetItems) {
            try {
                float amount = Float.parseFloat(item.getAmount());
                entries.add(new PieEntry(amount, item.getGroup())); // Sử dụng group làm nhãn cho PieChart
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "Budget Distribution");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Màu sắc cho PieChart
        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);

        pieChart.setData(data);
        pieChart.invalidate(); // Làm mới PieChart
        pieChart.setUsePercentValues(true); // Hiển thị dưới dạng phần trăm
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setEntryLabelColor(Color.BLACK);
    }





}
