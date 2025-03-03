package com.example.estok.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estok.Model.StockItem;
import com.example.estok.R;
import com.example.estok.StockAdd;

import java.util.List;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {


    private Context context;
    private List<StockItem> stockItemList;

    public StockAdapter(Context context, List<StockItem> stockItemList) {
        this.context = context;
        this.stockItemList = stockItemList;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_stock, parent, false);
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        StockItem item = stockItemList.get(position);

        holder.itemName.setText(item.getName());
        holder.quantity.setText(String.valueOf(item.getCount()));
        holder.unit.setText(item.getUnit());

        // Set click listener for the item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StockAdd.class);
            intent.putExtra("itemName", item.getName());
            intent.putExtra("itemCount", item.getCount());
            intent.putExtra("itemUnit", item.getUnit());
            intent.putExtra("documentId", item.getDocumentId()); // Pass the document ID
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return stockItemList.size();
    }

    public static class StockViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, quantity, unit;

        public StockViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            quantity = itemView.findViewById(R.id.quantity);
            unit = itemView.findViewById(R.id.Unit);
        }
    }
}