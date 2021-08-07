package com.koderkanies.mynotesapp.adapters;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.koderkanies.mynotesapp.R;
import com.koderkanies.mynotesapp.entities.Note;
import com.koderkanies.mynotesapp.listeners.NotesListeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Note> notes;
    private NotesListeners notesListeners;

    private Timer timer;
    private List<Note> noteSource;

    public NotesAdapter(List<Note> notes, NotesListeners notesListeners) {
        this.notes = notes;
        this.notesListeners = notesListeners;
        noteSource = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.note_cardview, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NotesAdapter.NoteViewHolder holder, int position) {
        holder.setNote(notes.get(position));
        holder.itemView.findViewById(R.id.note_cardView).setOnClickListener(view -> notesListeners.onNotesClicked(notes.get(position), position));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    //View holder class
    static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView textTitle;
        TextView textDateTime;
        TextView textNote;

        public NoteViewHolder(View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.titleText);
            textNote = itemView.findViewById(R.id.noteText);
            textDateTime = itemView.findViewById(R.id.dateTimeText);
        }

        void setNote(Note note) {
            textDateTime.setText(note.getDateTime());
            if (note.getTitle().trim().isEmpty()) {
                textTitle.setVisibility(View.GONE);
            } else {
                textTitle.setText(note.getTitle());
            }
            if (note.getNote().trim().isEmpty()) {
                textNote.setVisibility(View.GONE);
            } else {
                textNote.setText(note.getNote());
            }
        }
    }

    public void searchNotes(final String searchKeyword) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchKeyword.trim().isEmpty()) {
                    notes = noteSource;
                } else {
                    ArrayList<Note> temp = new ArrayList<>();
                    for (Note note : noteSource) {
                        if (note.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())
                                || note.getNote().toLowerCase().contains(searchKeyword.toLowerCase())) {
                            temp.add(note);
                        }
                    }
                    notes = temp;
                }
                new Handler(Looper.getMainLooper()).post(() -> notifyDataSetChanged());
            }
        }, 500);
    }

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
