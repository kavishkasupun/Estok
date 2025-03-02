package com.example.estok.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estok.Model.AcceptOrderItem;
import com.example.estok.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AcceptOrderAdapter extends RecyclerView.Adapter<AcceptOrderAdapter.AcceptOrderViewHolder> {

    private List<AcceptOrderItem> acceptOrderList;

    public AcceptOrderAdapter(List<AcceptOrderItem> acceptOrderList) {
        this.acceptOrderList = acceptOrderList;
    }

    @NonNull
    @Override
    public AcceptOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.accept_order, parent, false);
        return new AcceptOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AcceptOrderViewHolder holder, int position) {
        AcceptOrderItem item = acceptOrderList.get(position);

        holder.itemName.setText(item.getItemName());
        holder.optionName.setText(item.getOptionName());
        holder.quantity.setText(String.valueOf(item.getQuantity()));
        holder.unit.setText(item.getUnit());
        holder.orderId.setText("Order Id: " + item.getOrderId());
        holder.userNumber.setText("Number: " + item.getUserNumber());
        holder.jobId.setText("Job Id: " + item.getJobId());

        // Format the timestamp
        if (item.getFormattedTime() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy 'at' HH:mm:ss", Locale.getDefault());
            String formattedDate = sdf.format(item.getFormattedTime().toDate());
            holder.submitDate.setText(formattedDate);
        } else {
            holder.submitDate.setText("N/A");
        }

        holder.submitBy.setText("Submitted By: " + item.getSubmitBy());
    }

    @Override
    public int getItemCount() {
        return acceptOrderList.size();
    }

    public static class AcceptOrderViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, optionName, quantity, unit, orderId, userNumber, jobId, submitDate, submitBy;

        public AcceptOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            optionName = itemView.findViewById(R.id.option_name);
            quantity = itemView.findViewById(R.id.quantity);
            unit = itemView.findViewById(R.id.Unit);
            orderId = itemView.findViewById(R.id.Order_Id);
            userNumber = itemView.findViewById(R.id.User_Number);
            jobId = itemView.findViewById(R.id.Job_Id);
            submitDate = itemView.findViewById(R.id.submite_date);
            submitBy = itemView.findViewById(R.id.Submite_by);
        }
    }
}