package com.moutamid.mobiledesignsadmin.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.moutamid.mobiledesignsadmin.Constants;
import com.moutamid.mobiledesignsadmin.R;
import com.moutamid.mobiledesignsadmin.models.DesignModel;
import com.moutamid.mobiledesignsadmin.ui.AddDesignsActivity;
import com.moutamid.mobiledesignsadmin.ui.EditDesignActivity;

import java.util.ArrayList;

public class DesignAdapter extends RecyclerView.Adapter<DesignAdapter.DesignVH> {
    Context context;
    ArrayList<DesignModel> list;

    public DesignAdapter(Context context, ArrayList<DesignModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public DesignVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DesignVH(LayoutInflater.from(context).inflate(R.layout.design_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DesignVH holder, int position) {
        DesignModel model = list.get(holder.getAdapterPosition());
        Glide.with(context).load(model.image).into(holder.image);
        holder.name.setText(model.name);
        holder.desc.setText(model.description);
        holder.price.setText(String.format("$%.2f", model.price));

        holder.edit.setOnClickListener(v -> {
            context.startActivity(new Intent(context, EditDesignActivity.class)
                    .putExtra(Constants.DEVICE, model.device)
                    .putExtra(Constants.ID, model.id)
                    .putExtra(Constants.MODEL_ID, model.modelID));
        });

        holder.delete.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(context)
                    .setCancelable(true)
                    .setTitle("Delete " + model.name)
                    .setMessage("Are you sure you want to delete this Model.")
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("Yes", ((dialog, which) -> {
                        dialog.dismiss();
                        Constants.showDialog();
                        Constants.databaseReference().child(model.device).child(Constants.DESIGNS).child(model.modelID).child(model.id).removeValue()
                                .addOnSuccessListener(unused -> {
                                    Constants.dismissDialog();
                                    Toast.makeText(context, "Design Deleted Successfully", Toast.LENGTH_SHORT).show();
                                }).addOnFailureListener(e -> {
                                    Constants.dismissDialog();
                                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }))
                    .show();
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class DesignVH extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, desc, price;
        Button edit, delete;
        public DesignVH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            price = itemView.findViewById(R.id.price);
            name = itemView.findViewById(R.id.name);
            desc = itemView.findViewById(R.id.desc);
            edit = itemView.findViewById(R.id.edit);
            delete = itemView.findViewById(R.id.delete);
        }
    }

}
