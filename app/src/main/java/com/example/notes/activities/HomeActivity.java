package com.example.notes.activities;


import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.notes.R;
import com.example.notes.adapters.CategoryAdapter;
import com.example.notes.models.AppDatabase;
import com.example.notes.models.Category;
import com.example.notes.utils.Formatting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    public static AppDatabase sAppDatabase;
    private RecyclerView mRecyclerView;

    // use a linear layout manager
    private RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);

    Category category;
    private List<Category> categories;
    private List<Category> mCategoryList;

    Formatting formatting;

    CategoryAdapter categoryAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mRecyclerView = findViewById(R.id.rvCategories);

        // DATABASE
        sAppDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "unotes")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        categories = HomeActivity.sAppDatabase.mCategoryDAO().getCategories();

        mCategoryList = new ArrayList<>();

        for(Category category : categories){
            mCategoryList.add(category);
        }

        mRecyclerView.setLayoutManager(mLayoutManager);

        // LOADING RECYCLERVIEW AND SPECIFY AN ADAPTER
        categoryAdapter = new CategoryAdapter(mCategoryList, this);
        mRecyclerView.setAdapter(categoryAdapter);

        category = new Category();

    }

    @Override
    protected void onStart() {
        super.onStart();
        categoryAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.category_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.btnMenuAddCategory:
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                View view = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
                final EditText txtCategoryName = view.findViewById(R.id.txtCategoryName);
                Button btnAddCategory = view.findViewById(R.id.btnAddCategory_dialog);

                builder.setView(view);
                final AlertDialog dialog = builder.create();

                btnAddCategory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!txtCategoryName.getText().toString().isEmpty()){

                            formatting = new Formatting();
                            String categoryId = formatting.getDateTimeForIdFormatter(new Date());

                            category.set_id(categoryId);
                            category.setName(txtCategoryName.getText().toString());
                            category.setCreatedDate(new Date());
                            category.setUpdatedDate(new Date());

                            HomeActivity.sAppDatabase.mCategoryDAO().addCategory(category);

                            Toast.makeText(HomeActivity.this, "Category added: " + txtCategoryName.getText().toString(), Toast.LENGTH_LONG).show();

                            mCategoryList.add(category);
                            categoryAdapter.notifyDataSetChanged();

                            dialog.cancel();

                        } else {
                            Toast.makeText(HomeActivity.this, getString(R.string.error_category_add), Toast.LENGTH_LONG).show();
                        }
                    }
                });


                dialog.show();

                break;

        }

        return super.onOptionsItemSelected(item);

    }
}
