package com.example.notes.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.notes.R;
import com.example.notes.activities.NotesActivity;
import com.example.notes.models.AppDatabase;
import com.example.notes.models.Category;

import java.util.Date;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoriesViewHolder> {

    public static AppDatabase sAppDatabase;
    private List<Category> mCategoryList;
    private Context mContext;

    public CategoryAdapter(List<Category> categoryList, Context context) {
        this.mCategoryList = categoryList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public CategoriesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        final Context mContext = viewGroup.getContext();

        // DATABASE
        sAppDatabase = Room.databaseBuilder(mContext, AppDatabase.class, "unotes")
                .allowMainThreadQueries() // it will allow the database works on the main thread
                .fallbackToDestructiveMigration() // because i wont implement now migrations
                .build();

        View mView;
        mView = LayoutInflater.from(mContext).inflate(R.layout.item_category_list, viewGroup, false);
        final CategoriesViewHolder categoriesViewHolder = new CategoriesViewHolder(mView);



        categoriesViewHolder.mLinearLayout_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, NotesActivity.class);
                intent.putExtra("categoryId", mCategoryList.get(categoriesViewHolder.getAdapterPosition()).get_id());
                mContext.startActivity(intent);
            }
        });

        categoriesViewHolder.mLinearLayout_category.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                int count = sAppDatabase.mNoteDAO().getNumOfNotesByCategory(mCategoryList.get(categoriesViewHolder.getAdapterPosition()).get_id());

                final AlertDialog alertDialog =new AlertDialog.Builder(mContext).create();

                alertDialog.setCancelable(false);
                if(count > 0) {
                    alertDialog.setTitle("You cannot delete this");
                    alertDialog.setMessage("This Category has Notes associated. You need to delete the notes first before delete this category.");
                } else {
                    alertDialog.setTitle("Are you want to delete this");
                    alertDialog.setMessage("By deleting this, item will permanently be deleted, and all notes associate as well. Are you still want to delete this?");
                }

                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                if(count == 0) {
                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Category currentCategory = mCategoryList.get(categoriesViewHolder.getAdapterPosition());
                            sAppDatabase.mCategoryDAO().deleteCategory(currentCategory);
                            alertDialog.dismiss();
                            mCategoryList.remove(categoriesViewHolder.getAdapterPosition());
                            notifyDataSetChanged();
                        }
                    });
                }
                alertDialog.show();
                return false;
            }
        });

        categoriesViewHolder.btnEditCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                view = inflater.inflate(R.layout.dialog_add_category, null);
                final EditText txtCategoryName = view.findViewById(R.id.txtCategoryName);
                Button btnAddCategory = view.findViewById(R.id.btnAddCategory_dialog);

                final Category currentCategory = mCategoryList.get(categoriesViewHolder.getAdapterPosition());

                txtCategoryName.setText(currentCategory.getName());

                builder.setView(view);
                final AlertDialog dialog = builder.create();

                btnAddCategory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!txtCategoryName.getText().toString().isEmpty()){

                            currentCategory.setName(txtCategoryName.getText().toString());
                            currentCategory.setUpdatedDate(new Date());

                            // DATABASE
//                            sAppDatabase = Room.databaseBuilder(mContext, AppDatabase.class, "unotes")
//                                    .allowMainThreadQueries() // it will allow the database works on the main thread
//                                    .fallbackToDestructiveMigration() // because i wont implement now migrations
//                                    .build();

                            sAppDatabase.mCategoryDAO().updateCategory(currentCategory);

                            Toast.makeText(mContext, "Category edited: " + txtCategoryName.getText().toString(), Toast.LENGTH_LONG).show();

                            notifyDataSetChanged();
//                            sAppDatabase.close();

                            dialog.cancel();

                        } else {
                            Toast.makeText(mContext, "Error editing Category", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                dialog.show();
            }
        });

        sAppDatabase.close();

        return categoriesViewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull final CategoriesViewHolder categoriesViewHolder, int position) {

        // DATABASE
        sAppDatabase = Room.databaseBuilder(mContext, AppDatabase.class, "unotes")
                .allowMainThreadQueries() // it will allow the database works on the main thread
                .fallbackToDestructiveMigration() // because i wont implement now migrations
                .build();

        Category currentCategory = mCategoryList.get(position);
        int count = sAppDatabase.mNoteDAO().getNumOfNotesByCategory(currentCategory.get_id());

        categoriesViewHolder.lblCategoryName.setText(currentCategory.getName());
        categoriesViewHolder.lblNumOfNotes.setText(count + " notes");

        sAppDatabase.close();
    }

    @Override
    public int getItemCount() {
        return mCategoryList.size();
    }

    public static class CategoriesViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout mLinearLayout_category;
        private TextView lblCategoryName;
        private TextView lblNumOfNotes;
        private ImageButton btnEditCategory;

        public CategoriesViewHolder(@NonNull View itemView) {
            super(itemView);

            this.mLinearLayout_category = itemView.findViewById(R.id.categoryId);
            this.lblCategoryName = itemView.findViewById(R.id.lblCategoryName);
            this.lblNumOfNotes = itemView.findViewById(R.id.lblNumOfNotes);
            this.btnEditCategory = itemView.findViewById(R.id.btnEditCategory);
        }

    }

}
