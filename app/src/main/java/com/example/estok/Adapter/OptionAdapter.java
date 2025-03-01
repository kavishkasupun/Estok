package com.example.estok.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estok.Items;
import com.example.estok.R;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class OptionAdapter extends RecyclerView.Adapter<OptionAdapter.OptionViewHolder> {

    private List<DocumentSnapshot> optionList;
    private Context context;

    public OptionAdapter(Context context, List<DocumentSnapshot> optionList) {
        this.context = context;
        this.optionList = optionList;
    }

    @NonNull
    @Override
    public OptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);
        return new OptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OptionViewHolder holder, int position) {
        String optionName = optionList.get(position).getId(); // Get document name
        holder.optionName.setText(optionName);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, Items.class);
            intent.putExtra("optionName", optionName); // Pass document name
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return optionList.size();
    }

    public static class OptionViewHolder extends RecyclerView.ViewHolder {
        TextView optionName;

        public OptionViewHolder(@NonNull View itemView) {
            super(itemView);
            optionName = itemView.findViewById(R.id.Option_name);
        }
    }
}