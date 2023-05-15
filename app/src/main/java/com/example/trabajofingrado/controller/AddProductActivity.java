package com.example.trabajofingrado.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.SearchView;

import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.RecipeProductRecyclerAdapter;
import com.example.trabajofingrado.model.Product;
import com.example.trabajofingrado.utilities.Utils;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class AddProductActivity extends BaseActivity {
    // Fields
    private ArrayList<Product> productList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecipeProductRecyclerAdapter recyclerAdapter;
    private RecyclerView.ViewHolder viewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Bind the views
        this.bindViews();

        // TODO NOT A GOOD SOLUTION TO SET 0 TO THIS PARAMETER
        // Configure the drawer layout
        this.setDrawerLayout(0);

        // Configure the recyclerView and their adapter
        this.setRecyclerView();

        // Configure the listener
        this.setListener();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.product_search_filter_menu, menu);

        // Configure the searchView
        this.setSearchView(menu);

        return super.onCreateOptionsMenu(menu);
    }

    private void setSearchView(Menu menu) {
        MenuItem productSearchItem = menu.findItem(R.id.search_bar_products);
        SearchView searchView = (SearchView) productSearchItem.getActionView();
        searchView.setQueryHint("Search by name");
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                recyclerAdapter.getFilter().filter(s);
                return false;
            }
        });
    }

    // Auxiliary methods
    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        // Instance the views
        this.recyclerView = findViewById(R.id.rvProducts);
        this.drawerLayout = findViewById(R.id.drawer_layout_add_product);
        this.toolbar = findViewById(R.id.toolbar_add_product);
    }

    private void fillProductList(){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(Utils.PRODUCT_PATH);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                    Product product = dataSnapshot1.getValue(Product.class);
                    productList.add(product);
                }
                recyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(AddProductActivity.this);
            }
        });
    }

    private void setRecyclerView() {
        // Instance the adapter
        this.recyclerAdapter = new RecipeProductRecyclerAdapter(productList);

        // Instance the layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        // Configure the recycler view
        this.recyclerView.setAdapter(recyclerAdapter);
        this.recyclerView.setLayoutManager(layoutManager);

        // Set the data
        this.fillProductList();
    }

    private void setListener() {
        this.recyclerAdapter.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder = (RecyclerView.ViewHolder) view.getTag();
                Product product = productList.get(viewHolder.getAdapterPosition());
                Intent returnIntent = new Intent();
                returnIntent.putExtra("name", product.getName());
                returnIntent.putExtra("unitType", product.getUnitType());

                switch (getIntent().getStringExtra("action")){
                    case "add":
                        returnIntent.putExtra("action", "add");
                        break;
                    case "modify":
                        returnIntent.putExtra("action", "modify");
                        break;
                }

                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }
}