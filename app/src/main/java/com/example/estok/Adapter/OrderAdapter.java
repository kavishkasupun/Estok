package com.example.estok.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estok.Model.OrderItem;
import com.example.estok.R;
import com.example.estok.ViewOrder;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {


    private List<OrderItem> orderItemList;
    private ViewOrder viewOrderActivity;

    public OrderAdapter(List<OrderItem> orderItemList, ViewOrder viewOrderActivity) {
        this.orderItemList = orderItemList;
        this.viewOrderActivity = viewOrderActivity;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderItem orderItem = orderItemList.get(position);

        // Bind data to TextViews
        holder.itemName.setText(orderItem.getItemName());
        holder.optionName.setText(orderItem.getOptionName());
        holder.quantity.setText(String.valueOf(orderItem.getQuantity()));
        holder.unit.setText(orderItem.getUnit());
        holder.userNumber.setText(orderItem.getUserNumber());
        holder.jobId.setText(orderItem.getJobId());
        holder.submitDate.setText(orderItem.getFormattedTime());
        holder.submitBy.setText(orderItem.getSubmitBy());

        // Disable buttons if the order has been processed
        holder.btnAccept.setEnabled(!orderItem.isProcessed());
        holder.btnReject.setEnabled(!orderItem.isProcessed());

        // Handle button clicks
        holder.btnAccept.setOnClickListener(v -> {
            viewOrderActivity.onAcceptButtonClick(orderItem, position);
        });

        holder.btnReject.setOnClickListener(v -> {
            viewOrderActivity.onRejectButtonClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return orderItemList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, optionName, quantity, unit, userNumber, jobId, submitDate, submitBy;
        Button btnAccept, btnReject;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);

            // Map TextViews to their IDs
            itemName = itemView.findViewById(R.id.item_name);
            optionName = itemView.findViewById(R.id.option_name);
            quantity = itemView.findViewById(R.id.quantity);
            unit = itemView.findViewById(R.id.Unit);
            userNumber = itemView.findViewById(R.id.User_Number);
            jobId = itemView.findViewById(R.id.Job_Id);
            submitDate = itemView.findViewById(R.id.submite_date);
            submitBy = itemView.findViewById(R.id.Submite_by);

            // Map buttons to their IDs
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnReject = itemView.findViewById(R.id.btn_reject);
        }
    }
}