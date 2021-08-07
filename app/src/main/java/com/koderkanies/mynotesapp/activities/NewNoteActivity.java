package com.koderkanies.mynotesapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.koderkanies.mynotesapp.R;
import com.koderkanies.mynotesapp.database.NotesDatabase;
import com.koderkanies.mynotesapp.entities.Note;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewNoteActivity extends AppCompatActivity {

    private EditText mEditTitle;
    private EditText mEditNote;
    private TextView mDateTimeText;

    private AlertDialog dialogDelete;
    private AlertDialog dialogDiscard;

    private Note alreadyAvailableNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        //Making Back icon clickable and proceeding back to main activity
        ImageView mBackImage = findViewById(R.id.back_image);
        mBackImage.setOnClickListener(view -> {
            if (isNoteChanged()) {
                showDiscardNoteDialog();
            } else {
                onBackPressed();
            }
        });

        //Initialize new_note_activity layout components
        mEditTitle = findViewById(R.id.title);
        mEditNote = findViewById(R.id.note);
        mDateTimeText = findViewById(R.id.date_time_text);

        mDateTimeText.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(new Date()));

        //Call saveNote() from Save icon on new_note_activity layout
        ImageView mSaveImage = findViewById(R.id.save_image);
        mSaveImage.setOnClickListener(view -> saveNote());

        if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            alreadyAvailableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }

        if (alreadyAvailableNote != null) {
            ImageView mDeleteImage = findViewById(R.id.delete_image);
            mDeleteImage.setVisibility(View.VISIBLE);
            mDeleteImage.setOnClickListener(view -> showDeleteNoteDialog());
        }

    }

    private void showDeleteNoteDialog() {
        if (dialogDelete == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(NewNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.delete_warning_layout, findViewById(R.id.delete_dialog));
            builder.setView(view);
            dialogDelete = builder.create();
            if (dialogDelete.getWindow() != null) {
                dialogDelete.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    class DeleteNoteAsyncTask extends AsyncTask<Void, Void, Void> {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NotesDatabase.getDatabase(getApplicationContext()).noteDao().deleteNote(alreadyAvailableNote);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void unused) {
                            super.onPostExecute(unused);
                            Intent intent = new Intent();
                            intent.putExtra("isNoteDeleted", true);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }

                    new DeleteNoteAsyncTask().execute();
                }
            });
            view.findViewById(R.id.cancel).setOnClickListener(view1 -> dialogDelete.dismiss());
            view.findViewById(R.id.close_icon).setOnClickListener(view12 -> dialogDelete.dismiss());
        }

        dialogDelete.show();
    }

    private void showDiscardNoteDialog() {
        if (dialogDiscard == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(NewNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.discard_warning_layout, findViewById(R.id.discard_dialog));
            builder.setView(view);
            dialogDiscard = builder.create();
            if (dialogDiscard.getWindow() != null) {
                dialogDiscard.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.discard).setOnClickListener(view13 -> onBackPressed());
            view.findViewById(R.id.cancel).setOnClickListener(view1 -> dialogDiscard.dismiss());
            view.findViewById(R.id.close_icon).setOnClickListener(view12 -> dialogDiscard.dismiss());
        }

        dialogDiscard.show();

    }

    private void setViewOrUpdateNote() {
        mEditTitle.setText(alreadyAvailableNote.getTitle());
        mEditNote.setText(alreadyAvailableNote.getNote());
        mDateTimeText.setText(alreadyAvailableNote.getDateTime());
    }

    private void saveNote() {
        boolean isEmpty = false;
        if (mEditTitle.getText().toString().trim().isEmpty() && mEditNote.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note can't be empty", Toast.LENGTH_SHORT).show();
            isEmpty = true;
        }

        //To save only those notes which are not empty
        if (!isEmpty) {

            final Note note = new Note();
            note.setTitle(mEditTitle.getText().toString());
            note.setNote(mEditNote.getText().toString());
            note.setDateTime(mDateTimeText.getText().toString());

            if (alreadyAvailableNote != null) {
                note.setId(alreadyAvailableNote.getId());
            }

            //Create Async task to save notes (Room work on background thread)
            @SuppressLint("StaticFieldLeak")
            class SaveNoteAsyncTask extends AsyncTask<Void, Void, Void> {
                @Override
                protected Void doInBackground(Void... voids) {
                    NotesDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
            new SaveNoteAsyncTask().execute();
        }
    }

    private boolean isNoteChanged() {
        if (alreadyAvailableNote == null) {
            Log.d("NOTE TYPE", "Note is NEW NOTE");
            if (mEditTitle.getText().toString().trim().isEmpty() && mEditNote.getText().toString().trim().isEmpty()) {
                Log.d("NOTE CHANGE", "Note not changed");
                return false;
            } else {
                Log.d("NOTE CHANGE", "Note is changed");
                return true;
            }
        } else if (alreadyAvailableNote != null) {
            Log.d("NOTE TYPE", "Note is OLD NOTE");
            if (mEditTitle.getText().toString().equals(alreadyAvailableNote.getTitle())
                    && mEditNote.getText().toString().equals(alreadyAvailableNote.getNote())) {
                Log.d("NOTE CHANGE", "Note not changed");
                return false;
            } else {
                Log.d("NOTE CHANGE", "Note is changed");
                return true;
            }
        }
        return true;
    }

}