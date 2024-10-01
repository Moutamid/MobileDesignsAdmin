package com.moutamid.mobiledesignsadmin.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.mobiledesignsadmin.Constants;
import com.moutamid.mobiledesignsadmin.R;
import com.moutamid.mobiledesignsadmin.adapters.DesignAdapter;
import com.moutamid.mobiledesignsadmin.adapters.ModelAdapter;
import com.moutamid.mobiledesignsadmin.databinding.ActivityDesignBinding;
import com.moutamid.mobiledesignsadmin.models.DesignModel;
import com.moutamid.mobiledesignsadmin.models.DeviceModels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DesignActivity extends AppCompatActivity {
    ActivityDesignBinding binding;
    public String SELECTED_DEVICE = Constants.iPhone;
    ArrayList<DeviceModels> list;
    private static final String TAG = "DesignActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDesignBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.toolbar.back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.toolbar.title.setText("Model Designs");

        list = new ArrayList<>();

        binding.modelItems.setLayoutManager(new LinearLayoutManager(this));
        binding.modelItems.setHasFixedSize(false);

        binding.add.setOnClickListener(v -> startActivity(new Intent(this, AddDesignsActivity.class)));

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
                binding.models.getEditText().setText("");
                getData();
            }
        });

        binding.modelsList.setOnItemClickListener((parent, view, position, id) -> {
            DeviceModels models = list.get(position);
            getDesigns(models.id);
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
                    List<String> names = new ArrayList<>();
                    for (DeviceModels model : list) {
                        names.add(model.name);
                    }
                    ArrayAdapter<String> models = new ArrayAdapter<>(DesignActivity.this, android.R.layout.simple_spinner_dropdown_item, names);
                    binding.modelsList.setAdapter(models);
                } else {
                    binding.dataLayout.setVisibility(View.GONE);
                    binding.noLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                Constants.dismissDialog();
                Toast.makeText(DesignActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDesigns(String ID) {
        Constants.showDialog();
        Constants.databaseReference().child(SELECTED_DEVICE).child(Constants.DESIGNS).child(ID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Constants.dismissDialog();
                if (snapshot.exists()) {
                    ArrayList<DesignModel> list = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        DesignModel topicsModel = dataSnapshot.getValue(DesignModel.class);
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
                    DesignAdapter adapter = new DesignAdapter(DesignActivity.this, list);
                    binding.modelItems.setAdapter(adapter);
                } else {
                    binding.dataLayout.setVisibility(View.GONE);
                    binding.noLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                Constants.dismissDialog();
                Toast.makeText(DesignActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}