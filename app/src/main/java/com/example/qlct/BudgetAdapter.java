package com.example.qlct;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.qlct.Model.BudgetItem;

import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {

    private List<BudgetItem> budgetList;
    private Context context;
    private OnBudgetItemClickListener listener;

    public BudgetAdapter(List<BudgetItem> budgetList, Context context, OnBudgetItemClickListener listener) {
        this.budgetList = budgetList;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BudgetItem item = budgetList.get(position);
        holder.groupTextView.setText(item.getGroup());
        holder.amountTextView.setText("$" + item.getAmount());
        holder.dateTextView.setText(item.getDate());

        holder.itemView.setOnClickListener(v -> listener.onBudgetItemClick(item.getId(), item.getGroup(), item.getAmount(), item.getDate()));
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView groupTextView, amountTextView, dateTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            groupTextView = itemView.findViewById(R.id.groupTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }

    public interface OnBudgetItemClickListener {
        void onBudgetItemClick(String documentId, String group, String amount, String date);
    }
}
