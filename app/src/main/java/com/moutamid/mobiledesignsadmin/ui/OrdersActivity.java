package com.moutamid.mobiledesignsadmin.ui;

import android.os.Bundle;
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
import com.moutamid.mobiledesignsadmin.adapters.OrderAdapter;
import com.moutamid.mobiledesignsadmin.databinding.ActivityOrdersBinding;
import com.moutamid.mobiledesignsadmin.models.OrderModel;

import java.util.ArrayList;

public class OrdersActivity extends AppCompatActivity {
    ActivityOrdersBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.toolbar.back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.toolbar.title.setText("New Orders");

        binding.orders.setLayoutManager(new LinearLayoutManager(this));
        binding.orders.setHasFixedSize(false);

        Constants.initDialog(this);
        Constants.showDialog();
        ArrayList<OrderModel> list = new ArrayList<>();
        Constants.databaseReference().child(Constants.ORDERS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Constants.dismissDialog();
                if (snapshot.exists()) {
                    list.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        OrderModel model = dataSnapshot.getValue(OrderModel.class);
                        list.add(model);
                    }
                    OrderAdapter adapter = new OrderAdapter(OrdersActivity.this, list);
                    binding.orders.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Constants.dismissDialog();
                Toast.makeText(OrdersActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}