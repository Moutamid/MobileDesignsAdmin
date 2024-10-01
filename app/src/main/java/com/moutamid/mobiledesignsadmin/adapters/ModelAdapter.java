package com.moutamid.mobiledesignsadmin.adapters;

import android.app.Dialog;
import android.content.Context;
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

import java.util.ArrayList;

public class ModelAdapter extends RecyclerView.Adapter<ModelAdapter.ModelVH> {
    Context context;
    ArrayList<DeviceModels> list;
    String SELECTED_DEVICE;

    public ModelAdapter(Context context, ArrayList<DeviceModels> list, String SELECTED_DEVICE) {
        this.context = context;
        this.list = list;
        this.SELECTED_DEVICE = SELECTED_DEVICE;
    }

    @NonNull
    @Override
    public ModelVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ModelVH(LayoutInflater.from(context).inflate(R.layout.model_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ModelVH holder, int position) {
        DeviceModels model = list.get(holder.getAdapterPosition());
        holder.device.setText(SELECTED_DEVICE.replace("_", " "));
        holder.model.setText(model.name);

        holder.itemView.setOnLongClickListener(v -> {
            Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.add_model);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setCancelable(true);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setGravity(Gravity.CENTER);
            dialog.show();

            TextInputLayout topic = dialog.findViewById(R.id.name);
            Button complete = dialog.findViewById(R.id.complete);
            topic.getEditText().setText(model.name);
            complete.setOnClickListener(v1 -> {
                String topicName = topic.getEditText().getText().toString();
                if (!topicName.isEmpty()) {
                    dialog.dismiss();
                    Constants.showDialog();
                    model.name = topicName;
                    Constants.databaseReference().child(SELECTED_DEVICE).child(Constants.MODELS).child(model.id).setValue(model)
                            .addOnSuccessListener(unused -> {
                                Constants.dismissDialog();
                                Toast.makeText(context, "Model Updated Successfully", Toast.LENGTH_SHORT).show();
                            }).addOnFailureListener(e -> {
                                Constants.dismissDialog();
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    topic.setErrorEnabled(true);
                    topic.setError("Model name is empty");
                }
            });

            return false;
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

                        Constants.databaseReference().child(SELECTED_DEVICE).child(Constants.MODELS).child(model.id).removeValue()
                                .addOnSuccessListener(unused -> {
                                    Constants.dismissDialog();
                                    Toast.makeText(context, "Model Deleted Successfully", Toast.LENGTH_SHORT).show();
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

    public class ModelVH extends RecyclerView.ViewHolder {
        TextView device, model;
        MaterialCardView delete;
        public ModelVH(@NonNull View itemView) {
            super(itemView);
            device = itemView.findViewById(R.id.device);
            model = itemView.findViewById(R.id.model);
            delete = itemView.findViewById(R.id.delete);
        }
    }

}
