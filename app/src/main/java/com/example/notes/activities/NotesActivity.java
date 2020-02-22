package com.example.notes.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.notes.R;
import com.example.notes.adapters.NoteAdapter;
import com.example.notes.models.AppDatabase;
import com.example.notes.models.Category;
import com.example.notes.models.Note;

import java.util.ArrayList;
import java.util.List;

public class NotesActivity extends AppCompatActivity {

    public static AppDatabase sAppDatabase;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    private List<Note> notes;
    private List<Note> mNoteList;
    private String categoryId;
    private static String searchTerm;
    private Category category;

    NoteAdapter noteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        mRecyclerView = findViewById(R.id.rvNotes);

        // DATABASE
        sAppDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "unotes")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        categoryId = getIntent().getStringExtra("categoryId");

        notes = sAppDatabase.mNoteDAO().getNotesByCategory(categoryId);
        category = sAppDatabase.mCategoryDAO().getCategoryById(categoryId);

        mNoteList = new ArrayList<>();

        for(Note note : notes){
            mNoteList.add(note);
        }

        // USE A LINEAR LAYOUT MANAGER
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // LOADING RECYCLERVIEW AND SPECIFY AN ADAPTER
        noteAdapter = new NoteAdapter(mNoteList, this);
        mRecyclerView.setAdapter(noteAdapter);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(category.getName());

        // FOR SEARCH
        searchTerm = "";
        handleIntent(getIntent());
    }

    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        noteAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notes_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.search:
                return true;

            case R.id.btnMenuAddNote:
                Intent intent = new Intent(NotesActivity.this, NoteActivity.class);
                intent.putExtra("categoryId", categoryId);
                startActivity(intent);
                break;

            case R.id.btnOrderTitleAsc:
                notes = NotesActivity.sAppDatabase.mNoteDAO().getNotesOrderByTitleAsc(categoryId, "%"+searchTerm+"%");
                getUpdatedNotesAfterResultQuery();
                break;

            case R.id.btnOrderTitleDesc:
                notes = NotesActivity.sAppDatabase.mNoteDAO().getNotesOrderByTitleDesc(categoryId, "%"+searchTerm+"%");
                getUpdatedNotesAfterResultQuery();
                break;

            case R.id.btnOrderDateDesc:
                notes = NotesActivity.sAppDatabase.mNoteDAO().getNotesOrderByDateDesc(categoryId, "%"+searchTerm+"%");
                getUpdatedNotesAfterResultQuery();
                break;

            case R.id.btnOrderDateAsc:
                notes = NotesActivity.sAppDatabase.mNoteDAO().getNotesOrderByDateAsc(categoryId, "%"+searchTerm+"%");
                getUpdatedNotesAfterResultQuery();
                break;

        }

        return super.onOptionsItemSelected(item);

    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            searchTerm = intent.getStringExtra(SearchManager.QUERY);
            notes = NotesActivity.sAppDatabase.mNoteDAO().getNotesBySearch(categoryId, "%" + searchTerm + "%");
            getUpdatedNotesAfterResultQuery();
        }

    }

    private void getUpdatedNotesAfterResultQuery(){
        mNoteList = new ArrayList<>();
        for(Note note : notes){
            mNoteList.add(note);
        }
        noteAdapter = new NoteAdapter(mNoteList, this);
        mRecyclerView.setAdapter(noteAdapter);
        noteAdapter.notifyDataSetChanged();
    }
}
