package com.moutamid.mobiledesignsadmin.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.mobiledesignsadmin.Constants;
import com.moutamid.mobiledesignsadmin.R;
import com.moutamid.mobiledesignsadmin.databinding.ActivityAddDesignsBinding;
import com.moutamid.mobiledesignsadmin.models.DesignModel;
import com.moutamid.mobiledesignsadmin.models.DeviceModels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class AddDesignsActivity extends AppCompatActivity {
    ActivityAddDesignsBinding binding;
    Uri imageUri = Uri.EMPTY;
    public String SELECTED_DEVICE = Constants.iPhone;
    public String MODEL_ID = "";
    ArrayList<DeviceModels> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAddDesignsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.toolbar.back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.toolbar.title.setText("Add Design");

        binding.gallery.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .galleryOnly()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start();
        });

        binding.camera.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .cameraOnly()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start();
        });

        binding.devices.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.iphone) {
                SELECTED_DEVICE = Constants.iPhone;
            } else if (checkedId == R.id.samsung) {
                SELECTED_DEVICE = Constants.Samsung;
            } else if (checkedId == R.id.watch) {
                SELECTED_DEVICE = Constants.Apple_Watch;
            }
            binding.models.getEditText().setText("");
            getData();
        });

        binding.upload.setOnClickListener(v -> {
            if (valid()) {
                Optional<DeviceModels> optionalDevice = list.stream()
                        .filter(deviceModels -> deviceModels.name.equalsIgnoreCase(binding.models.getEditText().getText().toString().trim()))
                        .findFirst();
                if (optionalDevice.isPresent()) {
                    DeviceModels device = optionalDevice.get();
                    MODEL_ID = device.id;
                    uploadImage();
                } else {
                    Toast.makeText(this, "Device not found! Make Sure name is correct", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void uploadImage() {
        Constants.showDialog();
        Constants.storageReference("Images").child(new SimpleDateFormat("ddMMyyyyhhmmss", Locale.getDefault()).format(new Date()))
                .putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> uploadData(uri.toString()));
                })
                .addOnFailureListener(e -> {
                    Constants.dismissDialog();
                    Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadData(String link) {
        DesignModel model = new DesignModel();
        model.id = UUID.randomUUID().toString();
        model.name = binding.name.getEditText().getText().toString();
        model.description = binding.description.getEditText().getText().toString();
        model.modelID = MODEL_ID;
        model.device = SELECTED_DEVICE;
        model.image = link;
        Constants.databaseReference().child(SELECTED_DEVICE).child(Constants.DESIGNS).child(MODEL_ID).child(model.id)
                .setValue(model).addOnSuccessListener(command -> {
                    Constants.dismissDialog();
                    Toast.makeText(this, "Design Uploaded", Toast.LENGTH_SHORT).show();
                    getOnBackPressedDispatcher().onBackPressed();
                }).addOnFailureListener(e -> {
                    Constants.dismissDialog();
                    Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean valid() {
        if (binding.name.getEditText().getText().toString().isEmpty()){
            Toast.makeText(this, "Name is empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.models.getEditText().getText().toString().isEmpty()){
            Toast.makeText(this, "Device Model is empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.description.getEditText().getText().toString().isEmpty()){
            Toast.makeText(this, "Description is empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (imageUri == Uri.EMPTY){
            Toast.makeText(this, "Select a image", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Constants.initDialog(this);
        list = new ArrayList<>();
        getData();
    }

    private void getData() {
        Constants.showDialog();
        Constants.databaseReference().child(SELECTED_DEVICE).child(Constants.MODELS)
                .get().addOnSuccessListener(snapshot -> {
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
                        ArrayAdapter<String> models = new ArrayAdapter<>(AddDesignsActivity.this, android.R.layout.simple_spinner_dropdown_item, names);
                        binding.modelsList.setAdapter(models);
                    }
                }).addOnFailureListener(e -> {
                    Constants.dismissDialog();
                    Toast.makeText(AddDesignsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(binding.image);
        }
    }
}