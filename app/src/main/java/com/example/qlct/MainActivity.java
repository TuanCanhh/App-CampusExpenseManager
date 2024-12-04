package com.example.qlct;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;


import com.example.qlct.Model.ExpenseItem;
import com.example.qlct.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    Button button;
    FirebaseAuth mAuth;
    ActivityMainBinding binding;
    private String uid;
    private FirebaseFirestore db;
    private BudgetAdapter adapter;
    private List<ExpenseItem> expenseList;
    private RecyclerView recyclerView;
    private LinearLayout emptyLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());
        binding.bottomNavigationView.setBackground(null);
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.home) {
                selectedFragment = new HomeFragment();
            }
            else if (item.getItemId() == R.id.shorts) {
                selectedFragment = new HistoryFragment();
//            }
//             else if (item.getItemId() == R.id.createBudget) {
//                selectedFragment = new BudgetFragment();
//               showDialog01();

            } else if (item.getItemId() == R.id.subscriptions) {
                selectedFragment = new BudgetFragment();
            } else if (item.getItemId() == R.id.library) {
                selectedFragment = new ProfileFragment();
            }

            // Thay thế fragment hiện tại bằng fragment mới
            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
            }

            return true;
        });
        FloatingActionButton create=findViewById(R.id.fab_create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog01();
            }
        });


    }
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == R.id.fab_create) {
//            // Mở DialogLayout khi bấm vào dấu "+"
//            Log.d("Navigation", "Create clicked");
//            showDialog01();
//            return true;
//        } else {
//            return super.onOptionsItemSelected(item);
//        }
//    }


//    private void openDialogLayout() {
//        // Tạo Intent để mở activity DialogLayout
//        Intent intent = new Intent(this, DialogLayout.class);
//        startActivity(intent);
//    }

    // Method to show dialog for creating a new budget
    private void showDialog01() {
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.dialog_layout_expense);
        dialog.setCancelable(true);

        // Initialize dialog components
        EditText etGroup = dialog.findViewById(R.id.etGroup);
        EditText etAmount = dialog.findViewById(R.id.etAmount);
        EditText etDate = dialog.findViewById(R.id.etDate);
        Button btnSave = dialog.findViewById(R.id.btn_save);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        // Handle save button
        btnSave.setOnClickListener(v -> {
            String group = etGroup.getText().toString().trim();
            String amount = etAmount.getText().toString().trim();
            String date = etDate.getText().toString().trim();

            if (group.isEmpty() || amount.isEmpty() || date.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please fill in all the required information.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Initialize Firestore
            db = FirebaseFirestore.getInstance();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            uid = user.getUid();
            // Save to Firestore
            saveToFirestore(uid, group, amount, date);
            dialog.dismiss();
        });

        // Handle cancel button
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        // Adjust the width and height of the dialog
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;  // Full width
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;  // Wrap content based on content size
        dialog.getWindow().setAttributes(params);
        dialog.show();
    }

    // Method to save new budget to Firestore
    private void saveToFirestore(String userId, String group, String amount, String date) {
        Map<String, Object> expense = new HashMap<>();
        expense.put("group", group);
        expense.put("amount", amount);
        expense.put("date", date);

        db.collection("users")
                .document(userId)
                .collection("expenses")
                .add(expense)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(MainActivity.this, "Add expense successfully", Toast.LENGTH_SHORT).show();
//                    loadExpenseData(); // Reload data
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error when adding the expense " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    // Method to load budget data from Firestore
    private void loadExpenseData() {
        db.collection("users")
                .document(uid)
                .collection("expenses")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        expenseList.clear();
                        for (DocumentSnapshot doc : task.getResult()) {
                            ExpenseItem item = doc.toObject(ExpenseItem.class);
                            if (item != null) {
                                item.setId(doc.getId());
                                expenseList.add(item);
                            }
                        }

                        // Update UI based on data availability
                        if (expenseList.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            emptyLayout.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyLayout.setVisibility(View.GONE);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MainActivity.this, "Unable to load data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}