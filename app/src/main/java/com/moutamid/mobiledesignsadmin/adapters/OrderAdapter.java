package com.moutamid.mobiledesignsadmin.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.moutamid.mobiledesignsadmin.Constants;
import com.moutamid.mobiledesignsadmin.R;
import com.moutamid.mobiledesignsadmin.models.DeviceModels;
import com.moutamid.mobiledesignsadmin.models.OrderModel;
import com.moutamid.mobiledesignsadmin.ui.OrderDetailActivity;

import java.util.ArrayList;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ModelVH> {
    Context context;
    ArrayList<OrderModel> list;

    public OrderAdapter(Context context, ArrayList<OrderModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ModelVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ModelVH(LayoutInflater.from(context).inflate(R.layout.orders_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ModelVH holder, int position) {
        OrderModel model = list.get(holder.getAdapterPosition());
        holder.product.setText(model.productName + " | x" + model.quantity);
        holder.itemView.setOnClickListener(v -> {
            context.startActivity(new Intent(context, OrderDetailActivity.class).putExtra(Constants.ID, model.UID));
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ModelVH extends RecyclerView.ViewHolder {
        TextView product;
        public ModelVH(@NonNull View itemView) {
            super(itemView);
            product = itemView.findViewById(R.id.product);
        }
    }

}
