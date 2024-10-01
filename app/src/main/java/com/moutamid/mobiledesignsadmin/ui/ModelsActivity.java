package com.moutamid.mobiledesignsadmin.ui;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.mobiledesignsadmin.Constants;
import com.moutamid.mobiledesignsadmin.R;
import com.moutamid.mobiledesignsadmin.adapters.ModelAdapter;
import com.moutamid.mobiledesignsadmin.databinding.ActivityModelsBinding;
import com.moutamid.mobiledesignsadmin.models.DeviceModels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

public class ModelsActivity extends AppCompatActivity {
    ActivityModelsBinding binding;
    public String SELECTED_DEVICE = Constants.iPhone;
    ArrayList<DeviceModels> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityModelsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        list = new ArrayList<>();

        binding.modelItems.setLayoutManager(new LinearLayoutManager(this));
        binding.modelItems.setHasFixedSize(false);

        binding.add.setOnClickListener(v -> showDialog());

        binding.toolbar.back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.toolbar.title.setText("Device Models");

        binding.devices.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.iphone) {
                    SELECTED_DEVICE = Constants.iPhone;
                } else if (checkedId == R.id.samsung) {
                    SELECTED_DEVICE = Constants.Samsung;
                } else if (checkedId == R.id.watch) {
                    SELECTED_DEVICE = Constants.Apple_Watch;
                }
                getData();
            }
        });

    }

    private void showDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_model);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.show();

        TextInputLayout topic = dialog.findViewById(R.id.name);
        Button complete = dialog.findViewById(R.id.complete);

        complete.setOnClickListener(v -> {
            String name = topic.getEditText().getText().toString().trim();
            if (!name.isEmpty()) {
                dialog.dismiss();
                Constants.showDialog();

                DeviceModels model = new DeviceModels(UUID.randomUUID().toString(), name);
                Constants.databaseReference().child(SELECTED_DEVICE).child(Constants.MODELS).child(model.id).setValue(model)
                        .addOnSuccessListener(unused -> {
                            Constants.dismissDialog();
                            Toast.makeText(ModelsActivity.this, "Model Added Successfully", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Constants.dismissDialog();
                            Toast.makeText(ModelsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                topic.setErrorEnabled(true);
                topic.setError("Model name is empty");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Constants.initDialog(this);
        getData();
    }

    private void getData() {
        Constants.showDialog();
        Constants.databaseReference().child(SELECTED_DEVICE).child(Constants.MODELS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Constants.dismissDialog();
                if (snapshot.exists()) {
                    list.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        DeviceModels topicsModel = dataSnapshot.getValue(DeviceModels.class);
                        list.add(topicsModel);
                    }
                    if (!list.isEmpty()) {
                        Collections.reverse(list);
                        binding.dataLayout.setVisibility(View.VISIBLE);
                        binding.noLayout.setVisibility(View.GONE);
                    } else {
                        binding.dataLayout.setVisibility(View.GONE);
                        binding.noLayout.setVisibility(View.VISIBLE);
                    }

                    ModelAdapter adapter = new ModelAdapter(ModelsActivity.this, list, SELECTED_DEVICE);
                    binding.modelItems.setAdapter(adapter);
                } else {
                    binding.dataLayout.setVisibility(View.GONE);
                    binding.noLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                Constants.dismissDialog();
                Toast.makeText(ModelsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}