package com.moutamid.mobiledesignsadmin.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.database.DataSnapshot;
import com.moutamid.mobiledesignsadmin.Constants;
import com.moutamid.mobiledesignsadmin.R;
import com.moutamid.mobiledesignsadmin.databinding.ActivityEditDesignBinding;
import com.moutamid.mobiledesignsadmin.models.DesignModel;
import com.moutamid.mobiledesignsadmin.models.DeviceModels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class EditDesignActivity extends AppCompatActivity {
    ActivityEditDesignBinding binding;
    Uri imageUri = Uri.EMPTY;
    public String SELECTED_DEVICE = "";
    public String MODEL_ID = "";
    String id = "";
    DesignModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEditDesignBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        id = getIntent().getStringExtra(Constants.ID);
        SELECTED_DEVICE = getIntent().getStringExtra(Constants.DEVICE);
        MODEL_ID = getIntent().getStringExtra(Constants.MODEL_ID);

        Constants.databaseReference().child(SELECTED_DEVICE).child(Constants.DESIGNS).child(MODEL_ID).child(id)
                .get().addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        model = dataSnapshot.getValue(DesignModel.class);
                        binding.name.getEditText().setText(model.name);
                        binding.description.getEditText().setText(model.description);
                        Glide.with(this).load(model.image).into(binding.image);
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });

        binding.toolbar.back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.toolbar.title.setText("Edit Design");

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

        binding.upload.setOnClickListener(v -> {
            if (valid()) {
                Constants.showDialog();
                if (imageUri != Uri.EMPTY) uploadImage();
                else uploadData(model.image);
            }
        });

    }

    private void uploadData(String link) {
        model.name = binding.name.getEditText().getText().toString();
        model.description = binding.description.getEditText().getText().toString();
        model.image = link;
        Constants.databaseReference().child(SELECTED_DEVICE).child(Constants.DESIGNS).child(MODEL_ID).child(model.id)
                .setValue(model).addOnSuccessListener(command -> {
                    Constants.dismissDialog();
                    Toast.makeText(this, "Design Updated", Toast.LENGTH_SHORT).show();
                    getOnBackPressedDispatcher().onBackPressed();
                }).addOnFailureListener(e -> {
                    Constants.dismissDialog();
                    Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void uploadImage() {
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

    private boolean valid() {
        if (binding.name.getEditText().getText().toString().isEmpty()) {
            Toast.makeText(this, "Name is empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.description.getEditText().getText().toString().isEmpty()) {
            Toast.makeText(this, "Description is empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Constants.initDialog(this);
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