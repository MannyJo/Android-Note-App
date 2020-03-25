package com.mansangjo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.mansangjo.adapters.NoteRecyclerAdapter;
import com.mansangjo.models.Note;
import com.mansangjo.persistence.NoteRepository;
import com.mansangjo.utils.VerticalSpacingItemDecorator;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        NoteRecyclerAdapter.OnNoteListener,
        View.OnClickListener
{

    private static final String TAG = "MainActivity";

    // UI components
    private RecyclerView mRecyclerView;

    // vars
    private ArrayList<Note> mNotes = new ArrayList<>();
    private NoteRecyclerAdapter mNoteRecyclerAdapter;
    private NoteRepository mNoteRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: started");

        mRecyclerView = findViewById(R.id.recyclerView);

        findViewById(R.id.fab).setOnClickListener(this);

        mNoteRepository = new NoteRepository(this);
        initRecyclerView();
//        insertFakeNotes();
        retrieveNotes();

        Log.d(TAG, "onCreate: thread : " + Thread.currentThread().getName());

//        This will the app to crash, because this is trying to insert data on the main thread.
//        This is why I am using AsyncTask. It inserts on a background thread.
//        mNoteRepository.insertNoteTask(new Note());

        setSupportActionBar((Toolbar) findViewById(R.id.noteToolBar));
        setTitle("Notes");
    }

    private void retrieveNotes() {
        mNoteRepository.retrieveNotesTask().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(List<Note> notes) {
                if(mNotes.size() > 0) {
                    mNotes.clear();
                }
                if(notes != null) {
                    mNotes.addAll(notes);
                }
                mNoteRecyclerAdapter.notifyDataSetChanged();
            }
        });
    }

    private void insertFakeNotes() {
        for(int i=0; i< 1000; i++) {
            Note note = new Note();
            note.setTitle("title #" + i);
            note.setContent("content #" + i);
            note.setTimestamp("Jan. 2020");
            mNotes.add(note);
        }
        mNoteRecyclerAdapter.notifyDataSetChanged();
    }

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        VerticalSpacingItemDecorator itemDecorator = new VerticalSpacingItemDecorator(10);
        mRecyclerView.addItemDecoration(itemDecorator);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);
        mNoteRecyclerAdapter = new NoteRecyclerAdapter(mNotes, this);
        mRecyclerView.setAdapter(mNoteRecyclerAdapter);
    }

    @Override
    public void onNoteClick(int position) {
        Log.d(TAG, "onNoteClick: clicked #" + position);

        Intent intent = new Intent(this, NoteActivity.class);
        intent.putExtra("selected_note", mNotes.get(position));
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, NoteActivity.class);
        startActivity(intent);
    }

    private void deleteNote(Note note) {
        mNotes.remove(note);
        mNoteRecyclerAdapter.notifyDataSetChanged();

        mNoteRepository.deleteNote(note);
    }

    private ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            deleteNote(mNotes.get(viewHolder.getAdapterPosition()));
        }
    };
}
