package com.moutamid.mobiledesignsadmin.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.moutamid.mobiledesignsadmin.Constants;
import com.moutamid.mobiledesignsadmin.R;
import com.moutamid.mobiledesignsadmin.databinding.ActivityOrderDetailBinding;
import com.moutamid.mobiledesignsadmin.models.DesignModel;
import com.moutamid.mobiledesignsadmin.models.DeviceModels;
import com.moutamid.mobiledesignsadmin.models.OrderModel;

import java.util.ArrayList;

public class OrderDetailActivity extends AppCompatActivity {
    ActivityOrderDetailBinding binding;
    OrderModel model;
    String UID;
    private static final String TAG = "OrderDetailActivity";
    DesignModel design;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityOrderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.toolbar.back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.toolbar.title.setText("Order");

        Constants.initDialog(this);
        Constants.showDialog();
        UID = getIntent().getStringExtra(Constants.ID);

        Constants.databaseReference().child(Constants.ORDERS).child(UID)
                .get().addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        model = dataSnapshot.getValue(OrderModel.class);
                        getDesignData();
                    } else {
                        Constants.dismissDialog();
                        Toast.makeText(this, "Data not found", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    Constants.dismissDialog();
                    Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });

        binding.confirm.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setCancelable(true)
                    .setTitle("Confirm Order")
                    .setMessage("Are you sure you want to complete this order.")
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("Yes", ((dialog, which) -> {
                        dialog.dismiss();
                        Constants.showDialog();
                        Constants.databaseReference().child(Constants.ORDERS).child(UID).removeValue()
                                .addOnSuccessListener(unused -> {
                                    Constants.dismissDialog();
                                    Toast.makeText(this, "Order Completed Successfully", Toast.LENGTH_SHORT).show();
                                    getOnBackPressedDispatcher().onBackPressed();
                                }).addOnFailureListener(e -> {
                                    Constants.dismissDialog();
                                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }))
                    .show();
        });

        binding.delete.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setCancelable(true)
                    .setTitle("Delete Order")
                    .setMessage("Are you sure you want to delete this order.")
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("Yes", ((dialog, which) -> {
                        dialog.dismiss();
                        Constants.showDialog();
                        Constants.databaseReference().child(Constants.ORDERS).child(UID).removeValue()
                                .addOnSuccessListener(unused -> {
                                    Constants.dismissDialog();
                                    Toast.makeText(this, "Order Deleted Successfully", Toast.LENGTH_SHORT).show();
                                    getOnBackPressedDispatcher().onBackPressed();
                                }).addOnFailureListener(e -> {
                                    Constants.dismissDialog();
                                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }))
                    .show();
        });

    }

    private void getDesignData() {
        Constants.databaseReference().child(model.device).child(Constants.DESIGNS).child(model.modelID).child(model.productID)
                .get().addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        design = dataSnapshot.getValue(DesignModel.class);
                        Glide.with(this).load(design.image).into(binding.image);
                        binding.name.setText(design.name);
                        binding.desc.setText(design.description);
                        binding.deviceName.setText(design.device);
                        getModelName();
                    } else {
                        Constants.dismissDialog();
                        Toast.makeText(this, "Data not found", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    Constants.dismissDialog();
                    Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void getModelName() {
        Constants.databaseReference().child(model.device).child(Constants.MODELS).child(model.modelID)
                .get().addOnSuccessListener(dataSnapshot -> {
                    Constants.dismissDialog();
                    if (dataSnapshot.exists()) {
                        DeviceModels deviceModels = dataSnapshot.getValue(DeviceModels.class);
                        binding.deviceModel.setText(deviceModels.name);
                        updateUI();
                    } else {
                        Toast.makeText(this, "Data not found", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    Constants.dismissDialog();
                    Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI() {
        binding.quantity.setText("Quantity: x" + model.quantity);
        binding.price.setText(String.format("$%.2f", model.price));
        binding.subtotal.setText(String.format("$%.2f", (model.price * model.quantity)));

        ArrayList<String> rows = new ArrayList<>();
        rows.add("Order ID~" + model.UID);
        rows.add("Name~" + model.personName);
        rows.add("Phone Number~" + model.number);
        rows.add("Email~" + model.email);
        rows.add("Address~" + model.address);
        updateTable(rows);
    }

    private void updateTable(ArrayList<String> rows) {
            TableLayout tableLayout = new TableLayout(this);
            tableLayout.setLayoutParams(new TableLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            for (int i = 0; i < rows.size(); i++) {
                String s = rows.get(i);
                String[] columns = s.split("~");

                TableRow tableRow = new TableRow(this);
                tableRow.setLayoutParams(new TableRow.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                for (String col : columns) {
                    TextView textView = new TextView(this);
                    textView.setText(col);
                    textView.setLayoutParams(new TableRow.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            1f));
//                    textView.setGravity(Gravity.CENTER);
                    textView.setTextSize(13);
                    textView.setPadding(21, 21, 21, 21);
                    tableRow.addView(textView);
                }
                if (i % 2 == 0) {
                    tableRow.setBackgroundColor(getColor(R.color.grey));
                } else {
                    tableRow.setBackgroundColor(getColor(R.color.blueLight));
                }
                tableLayout.addView(tableRow);
                Log.d(TAG, "updateViews: tableLayout");
            }
            binding.tableView.addView(tableLayout);
            Log.d(TAG, "updateViews: added");
    }
}