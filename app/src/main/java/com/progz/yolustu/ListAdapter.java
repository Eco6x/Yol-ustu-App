package com.progz.yolustu;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ItemViewHolder> {

    // This is a temporary list until Abdulkadir finishes the database
    private List<ShoppingItem> shoppingList = new ArrayList<>();

    public void addItem(ShoppingItem item) {
        shoppingList.add(item);
        notifyItemInserted(shoppingList.size() - 1);
    }

    public int getItemsCount() {
        return shoppingList.size();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shopping_item_row, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ShoppingItem currentItem = shoppingList.get(position);
        
        holder.textViewItemName.setText(currentItem.getName());
        holder.chipMarket.setText(currentItem.getMarket());
        
        // Remove listener temporarily to avoid triggering it while manually changing the state
        holder.checkboxItem.setOnCheckedChangeListener(null);
        holder.checkboxItem.setChecked(currentItem.isCompleted());
        
        applyStrikethrough(holder.textViewItemName, currentItem.isCompleted());

        holder.checkboxItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentItem.setCompleted(isChecked);
            applyStrikethrough(holder.textViewItemName, isChecked);
        });
        
        holder.imageViewDelete.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                shoppingList.remove(currentPos);
                notifyItemRemoved(currentPos);
            }
        });
    }
    
    private void applyStrikethrough(TextView textView, boolean isStriked) {
        if (isStriked) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    @Override
    public int getItemCount() {
        return shoppingList.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        MaterialCheckBox checkboxItem;
        TextView textViewItemName;
        Chip chipMarket;
        ImageView imageViewDelete;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            checkboxItem = itemView.findViewById(R.id.checkboxItem);
            textViewItemName = itemView.findViewById(R.id.textViewItemName);
            chipMarket = itemView.findViewById(R.id.chipMarket);
            imageViewDelete = itemView.findViewById(R.id.imageViewDelete);
        }
    }
}