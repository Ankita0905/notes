package com.example.notes.models;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Category.class, Note.class, Attachment.class}, version = 10)
@TypeConverters({DateTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract CategoryDAO mCategoryDAO();
    public abstract NoteDAO mNoteDAO();
    public abstract AttachmentDAO mAttachmentDAO();

}
