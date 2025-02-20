package com.example.notes.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.notes.R;
import com.example.notes.activities.NoteActivity;
import com.example.notes.models.AppDatabase;
import com.example.notes.models.Note;
import com.example.notes.utils.Formatting;

import java.io.File;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NotesViewHolder> {

    public static AppDatabase sAppDatabase;
    private List<Note> mNoteList;
    private Context mContext;

    Formatting formatting;

    public NoteAdapter(List<Note> noteList, Context context) {
        this.mNoteList = noteList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final Context mContext = viewGroup.getContext();

        View mView;
        mView = LayoutInflater.from(mContext).inflate(R.layout.item_note_list, viewGroup, false);
        final NotesViewHolder notesViewHolder = new NotesViewHolder(mView);

        notesViewHolder.mLinearLayout_note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, NoteActivity.class);
                intent.putExtra("noteId", mNoteList.get(notesViewHolder.getAdapterPosition()).get_id());
                intent.putExtra("categoryId", mNoteList.get(notesViewHolder.getAdapterPosition()).getCategoryId());
                mContext.startActivity(intent);
            }
        });

        notesViewHolder.mLinearLayout_note.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final AlertDialog alertDialog =new AlertDialog.Builder(mContext).create();
                alertDialog.setTitle("Are you want to delete this");
                alertDialog.setCancelable(false);
                alertDialog.setMessage("By deleting this, item will permanently be deleted. Are you still want to delete this?");
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // DATABASE
                        sAppDatabase = Room.databaseBuilder(mContext, AppDatabase.class, "unotes")
                                .allowMainThreadQueries() // it will allow the database works on the main thread
                                .fallbackToDestructiveMigration() // because i wont implement now migrations
                                .build();

                        Note currentNote = mNoteList.get(notesViewHolder.getAdapterPosition());
                        sAppDatabase.mNoteDAO().deleteNote(currentNote);
                        alertDialog.dismiss();
                        mNoteList.remove(notesViewHolder.getAdapterPosition());
                        notifyDataSetChanged();

                        sAppDatabase.close();
                    }
                });
                alertDialog.show();
                return false;
            }
        });

        return notesViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NoteAdapter.NotesViewHolder notesViewHolder, int position) {
        // DATABASE
        sAppDatabase = Room.databaseBuilder(mContext, AppDatabase.class, "unotes")
                .allowMainThreadQueries() // it will allow the database works on the main thread
                .fallbackToDestructiveMigration() // because i wont implement now migrations
                .build();

        Note currentNote = mNoteList.get(position);
        String lastImage = sAppDatabase.mAttachmentDAO().getLastImageByNoteId(currentNote.get_id());

        formatting = new Formatting();

        notesViewHolder.lblNoteTitle.setText(currentNote.getTitle());
        notesViewHolder.lblCreatedDate.setText(formatting.getDateMediumFormatter(currentNote.getCreatedDate()) + " - Last update: " + formatting.getDateLongFormatter(currentNote.getUpdatedDate()));

        if(lastImage != null){
            File imgFile = new File(lastImage);
            notesViewHolder.imgNote.setVisibility(View.VISIBLE);
            notesViewHolder.imgNote.setImageBitmap(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
        } else {
            notesViewHolder.imgNote.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return mNoteList.size();
    }

    public static class NotesViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout mLinearLayout_note;
        private TextView lblNoteTitle;
        private TextView lblCreatedDate;
        private ImageView imgNote;

        public NotesViewHolder(@NonNull View itemView) {
            super(itemView);

            this.mLinearLayout_note = itemView.findViewById(R.id.noteId);
            this.lblNoteTitle = itemView.findViewById(R.id.lblNoteTitle);
            this.lblCreatedDate = itemView.findViewById(R.id.lblCreatedDate);
            this.imgNote = itemView.findViewById(R.id.imgNote);
        }

    }

}
