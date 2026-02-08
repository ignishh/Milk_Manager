package com.ignishers.milkmanager2;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import java.util.Stack;

import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ignishers.milkmanager2.DAO.CustomerDAO;
import com.ignishers.milkmanager2.DAO.DBHelper;
import com.ignishers.milkmanager2.DAO.DailyTransactionDAO;
import com.ignishers.milkmanager2.DAO.RouteGroupDAO;
import com.ignishers.milkmanager2.adapter.NavigationAdapter;
import com.ignishers.milkmanager2.model.Customer;
import com.ignishers.milkmanager2.model.NavigationManager;
import com.ignishers.milkmanager2.model.RouteGroup;
import com.ignishers.milkmanager2.utils.DialogUtils; 

import java.util.List;

public class DashboardActivity extends AppCompatActivity implements AddCustomerDialogFragment.OnCustomerCreatedListener {


    DBHelper dbHelper;
    RouteGroupDAO groupDAO;
    CustomerDAO customerDAO;
    DailyTransactionDAO dailyTransactionDAO;
    NavigationManager navManager;
    RecyclerView recyclerView;
    NavigationAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DBHelper(this);
        groupDAO = new RouteGroupDAO(this);
        groupDAO.ensureRootRouteExists();
        customerDAO = new CustomerDAO(this);
        dailyTransactionDAO = new DailyTransactionDAO(this);
        navManager = new NavigationManager();


        recyclerView = findViewById(R.id.recyclerViewMain);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Dynamic Animation
        int resId = R.anim.layout_animation_fall_down;
        android.view.animation.LayoutAnimationController animation = android.view.animation.AnimationUtils.loadLayoutAnimation(this, resId);
        recyclerView.setLayoutAnimation(animation);
        
        adapter = new NavigationAdapter(new NavigationAdapter.OnItemClickListener() {
            @Override
            public void onFolderClick(long groupId) {
                onFolderClicked(groupId);
            }

            @Override
            public void onCustomerClick(long customerId) {
                onCustomerClicked(customerId);
            }

            @Override
            public void onFolderLongClick(RouteGroup group) {
                handleFolderOptions(group);
            }

            @Override
            public void onCustomerLongClick(Customer customer) {
                handleCustomerOptions(customer);
            }
        });
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Long currentGroupId = navManager.getCurrentGroup();
            long groupId = currentGroupId == null ? 0 : currentGroupId;
            
            AddCustomerDialogFragment.newInstance(groupId)
                    .show(getSupportFragmentManager(), "CreateCustomer");
        });

        FloatingActionButton fab2 = findViewById(R.id.fab2);
        fab2.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Create New Route Group");

            final EditText input = new EditText(this);
            input.setHint("Enter Group Name (e.g. Street 5)");
            builder.setView(input);

            builder.setPositiveButton("Create", (dialog, which) -> {
                String name = input.getText().toString().trim();

                if (!name.isEmpty()) {
                    Long currentParent = navManager.getCurrentGroup();
                    if(currentParent == null) currentParent = 0L;

                    boolean success = groupDAO.insertGroup(name, currentParent);

                    if (success) {
                        Toast.makeText(DashboardActivity.this, "Route Created!", Toast.LENGTH_SHORT).show();
                        loadCurrentLevel();
                    } else {
                        Toast.makeText(DashboardActivity.this, "Error creating route", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        });


        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNavigationDrawer(v);
            }
        });

        toolbar.setOnClickListener(v -> {
            if (!navManager.isAtRoot()) {
                navManager.clear();
                loadCurrentLevel();
                Toast.makeText(DashboardActivity.this, "Returned to Dashboard", Toast.LENGTH_SHORT).show();
            }
        });

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {
                        if (!navManager.isAtRoot()) {
                            navManager.goBack();
                            loadCurrentLevel();
                        } else {
                            setEnabled(false);
                            getOnBackPressedDispatcher().onBackPressed();
                        }
                    }
                }
        );


        loadCurrentLevel();
    }

    private void onFolderClicked(long groupId) {
        navManager.enterGroup(groupId);
        loadCurrentLevel();
    }

    private void onCustomerClicked(long customerId) {
        Intent intent = new Intent(this, PointOfSaleActivity.class);
        intent.putExtra("customerId", customerId);
        startActivity(intent);
    }

    // NEW: Open Bottom Sheet Options instead of old Dialog
    private void handleFolderOptions(RouteGroup group) {
        showOptionsBottomSheet(
            group.name,
            "Folder Options",
            false,
            null,
            () -> attemptDeleteFolder(group)
        );
    }

    private void attemptDeleteFolder(RouteGroup group) {
        List<RouteGroup> subGroups = groupDAO.getChildGroups(group.id);
        List<Customer> customers = customerDAO.getCustomersByGroup(group.id);

        if (subGroups.isEmpty() && customers.isEmpty()) {
            DialogUtils.showWarningDialog(this,
                    "Delete Folder?",
                    "Are you sure you want to delete '" + group.name + "'? This action cannot be undone.",
                    "Delete",
                    () -> {
                        groupDAO.deleteGroup(group.id);
                        Toast.makeText(this, "Folder Deleted", Toast.LENGTH_SHORT).show();
                        loadCurrentLevel();
                    });
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Cannot Delete")
                    .setMessage("Folder is not empty. Please remove all sub-folders and customers first.")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    // NEW: Open Bottom Sheet Options
    private void handleCustomerOptions(Customer customer) {
         showOptionsBottomSheet(
            customer.name,
            "Customer Options",
            true,
            () -> initiateMoveCustomer(customer),
            () -> attemptDeleteCustomer(customer)
        );
    }
    
    // NEW: Helper to show Bottom Sheet
    private void showOptionsBottomSheet(String title, String subtitle, boolean canMove, Runnable onMove, Runnable onDelete) {
        com.google.android.material.bottomsheet.BottomSheetDialog sheet = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_options_bottom_sheet, null);
        sheet.setContentView(view);
        
        ((TextView)view.findViewById(R.id.tvSheetTitle)).setText(title);
        ((TextView)view.findViewById(R.id.tvSheetSubtitle)).setText(subtitle);
        
        View moveOption = view.findViewById(R.id.optionMove);
        if (canMove) {
            moveOption.setVisibility(View.VISIBLE);
            moveOption.setOnClickListener(v -> {
                sheet.dismiss();
                if (onMove != null) onMove.run();
            });
        } else {
            moveOption.setVisibility(View.GONE);
        }
        
        View deleteOption = view.findViewById(R.id.optionDelete);
        deleteOption.setOnClickListener(v -> {
            sheet.dismiss();
            if (onDelete != null) onDelete.run();
        });
        
        sheet.show();
    }

    private void initiateMoveCustomer(Customer customer) {
        com.ignishers.milkmanager2.utils.CustomerClipboard.copy(customer.id, customer.name);
        Toast.makeText(this, "Customer Cut! Navigate to new folder and Click Paste.", Toast.LENGTH_LONG).show();
        checkClipboard(); // Update UI
    }

    private void attemptDeleteCustomer(Customer customer) {
        // Double check dues from DB to be sure
        Customer freshCust = customerDAO.getCustomer(String.valueOf(customer.id));
        if (freshCust == null) return;

        // Tolerance for floating point
        if (Math.abs(freshCust.currentDue) > 0.01) {
            new AlertDialog.Builder(this)
                    .setTitle("Cannot Delete")
                    .setMessage("Customer has pending dues (" + freshCust.currentDue + "). Please clear dues before deleting.")
                    .setPositiveButton("OK", null)
                    .show();
        } else {
            DialogUtils.showWarningDialog(this,
                    "Delete Customer?",
                    "Are you sure? This will delete '" + customer.name + "' and ALL their transactions permanently.",
                    "Delete Forever",
                    () -> {
                        dailyTransactionDAO.deleteAllForCustomer(customer.id);
                        customerDAO.deleteCustomer(customer.id);
                        Toast.makeText(this, "Customer Deleted", Toast.LENGTH_SHORT).show();
                        loadCurrentLevel();
                    });
        }
    }

    private void loadCurrentLevel() {
        Long currentGroupId = navManager.getCurrentGroup();

        List<RouteGroup> groups;
        List<Customer> customers;

        if (currentGroupId == null) {
            groups = groupDAO.getRootGroups();
            customers = customerDAO.getCustomersByGroup(null); // Fetch root (null) customers
        } else {
            groups = groupDAO.getChildGroups(currentGroupId);
            customers = customerDAO.getCustomersByGroup(currentGroupId);
        }

        adapter.submit(groups, customers);
        updateBreadcrumbs();
    }

    private void updateBreadcrumbs() {
        StringBuilder pathBuilder = new StringBuilder("/");
        String title = "Dashboard";

        Stack<Long> stack = navManager.getPathStack();
        for (Long groupId : stack) {
            RouteGroup group = groupDAO.getGroupById(groupId);
            if (group != null) {
                pathBuilder.append(" > ").append(group.name);
                title = group.name;
            }
        }

        TextView pathView = findViewById(R.id.textPath);
        if (pathView != null) {
            pathView.setText(pathBuilder.toString());
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            String date = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEE, dd MMM yyyy"));
            getSupportActionBar().setSubtitle(date);
        }
    }

    private void showNavigationDrawer(View anchorView) {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        
        toolbar.setNavigationIcon(androidx.appcompat.content.res.AppCompatResources.getDrawable(this, R.drawable.avd_dots_to_lines));
        android.graphics.drawable.Drawable icon = toolbar.getNavigationIcon();
        if (icon instanceof android.graphics.drawable.Animatable) {
            ((android.graphics.drawable.Animatable) icon).start();
        }
    
        toolbar.postDelayed(() -> {
             toolbar.setNavigationIcon(R.drawable.vec_menu_lines);
        }, 450);

        MainMenuBottomSheet bottomSheet = new MainMenuBottomSheet();
        bottomSheet.setDismissAction(() -> {
            toolbar.setNavigationIcon(androidx.appcompat.content.res.AppCompatResources.getDrawable(this, R.drawable.avd_lines_to_dots));
            android.graphics.drawable.Drawable closeIcon = toolbar.getNavigationIcon();
            if (closeIcon instanceof android.graphics.drawable.Animatable) {
                ((android.graphics.drawable.Animatable) closeIcon).start();
            }
            toolbar.postDelayed(() -> {
                 toolbar.setNavigationIcon(R.drawable.vec_menu_dots);
            }, 450);
        });
        
        bottomSheet.setListener(new MainMenuBottomSheet.OnMenuItemClickListener() {
            @Override
            public void onSalesRecordClick() {
                Intent intent = new Intent(DashboardActivity.this, SalesReportActivity.class);
                startActivity(intent);
            }

            @Override
            public void onMilkPriceClick() {
                new SetMilkPriceDialog().show(getSupportFragmentManager(), "SetPrice");
            }

            @Override
            public void onSettingsClick() {
                Toast.makeText(DashboardActivity.this, "Settings Comming Soon", Toast.LENGTH_SHORT).show();
            }
        });
        
        bottomSheet.show(getSupportFragmentManager(), "MainMenu");
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        checkClipboard();
    }
    
    private void checkClipboard() {
        if (com.ignishers.milkmanager2.utils.CustomerClipboard.hasClip()) {
             FloatingActionButton fabPaste = findViewById(R.id.fabPaste);
             fabPaste.setVisibility(View.VISIBLE);
             
             String custName = com.ignishers.milkmanager2.utils.CustomerClipboard.getCustomerName();
             fabPaste.setOnClickListener(v -> showPasteDialog(custName));
        } else {
             FloatingActionButton fabPaste = findViewById(R.id.fabPaste);
             if (fabPaste != null) fabPaste.setVisibility(View.GONE);
        }
        invalidateOptionsMenu();
    }
    
    private void showPasteDialog(String custName) {
         new AlertDialog.Builder(this)
             .setTitle("Move Customer Here?")
             .setMessage("Move '" + custName + "' to current folder?")
             .setPositiveButton("Paste", (dialog, which) -> {
                 Long currentGroupId = navManager.getCurrentGroup();
                 long targetRouteId = (currentGroupId == null) ? 0 : currentGroupId;
                 
                 customerDAO.updateCustomerRoute(com.ignishers.milkmanager2.utils.CustomerClipboard.getCustomerId(), targetRouteId);
                 
                 com.ignishers.milkmanager2.utils.CustomerClipboard.clear();
                 checkClipboard(); 
                 loadCurrentLevel(); 
                 
                 Toast.makeText(this, "Customer Moved Successfully", Toast.LENGTH_SHORT).show();
             })
             .setNegativeButton("Cancel", null)
             .show();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        android.view.MenuItem cancelItem = menu.findItem(R.id.action_cancel_move);
        if (cancelItem != null) {
            cancelItem.setVisible(com.ignishers.milkmanager2.utils.CustomerClipboard.hasClip());
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_cancel_move) {
            com.ignishers.milkmanager2.utils.CustomerClipboard.clear();
            checkClipboard();
            Toast.makeText(this, "Move Cancelled", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        loadCurrentLevel();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCustomerCreated() {
        loadCurrentLevel();
    }
}
