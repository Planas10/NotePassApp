package com.example.notepassapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.notepassapp.databinding.ActivityMainBinding
import com.example.notepassapp.persistence.DatabaseProvider
import com.example.notepassapp.persistence.dao.NoteDao
import com.example.notepassapp.persistence.entity.NoteEntity
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var menu: Menu
    private val noteDao: NoteDao by lazy { DatabaseProvider.getDatabase(this).noteDao() }
    private val adapter: NotesAdapter by lazy {
        NotesAdapter {
            when (it) {
                is NoteAdapterAction.OnDeleteNote -> {
                    if (it.note.password != null) {
                        askPassword { password ->
                            if (it.note.password == password) {
                                onDeleteNote(it.note)
                            } else {
                                Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        onDeleteNote(it.note)
                    }
                }
                is NoteAdapterAction.OnEditNote -> {
                    if (it.note.password != null) {
                        askPassword { password ->
                            if (it.note.password == password) {
                                startActivity(NoteActivity.getIntentWithArguments(this, it.note))
                            } else {
                                Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        startActivity(NoteActivity.getIntentWithArguments(this, it.note))
                    }
                }
            }
        }
    }

    private fun askPassword(onOk: (password: String) -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Insert Password")
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(input)
        builder.setPositiveButton("OK") { _, _ ->
            onOk(input.text.toString())
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fillListView()
        binding.buttonAdd.setOnClickListener {
            startActivity(NoteActivity.getIntent(this))
        }
    }

    override fun onResume() {
        super.onResume()
        fillListView()
    }

    private fun fillListView() {
        lifecycleScope.launch {
            noteDao.getAllNotes().collect { notes ->
                adapter.submitOriginalList(notes)
            }
        }
        binding.listViewNotes.adapter = adapter
        binding.editTextSearch.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter.filter.filter(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    private fun onDeleteNote(noteEntity: NoteEntity) {
        lifecycleScope.launch {
            noteDao.deleteNote(noteEntity)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.dark_theme_menu,menu)
        updateMenu()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.dark_mode->{
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
                recreate()
                true
            }
            R.id.light_mode->{
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
                recreate()
                true
            }
            else->super.onOptionsItemSelected(item)
        }
    }
    private fun updateMenu(){
        when(AppCompatDelegate.getDefaultNightMode()){
            AppCompatDelegate.MODE_NIGHT_NO->{
                menu.findItem(R.id.light_mode).isVisible = false
                menu.findItem(R.id.dark_mode).isVisible = true
            }
            AppCompatDelegate.MODE_NIGHT_YES->{
                menu.findItem(R.id.light_mode).isVisible = true
                menu.findItem(R.id.dark_mode).isVisible = false
            }
            else->{
                menu.findItem(R.id.light_mode).isVisible = false
                menu.findItem(R.id.dark_mode).isVisible = true
            }

        }
    }
}