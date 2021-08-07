package com.koderkanies.mynotesapp.listeners;

import com.koderkanies.mynotesapp.entities.Note;

public interface NotesListeners {
    void onNotesClicked(Note note, int position);
}
