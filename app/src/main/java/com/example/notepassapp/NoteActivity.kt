package com.example.notepassapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import com.example.notepassapp.databinding.ActivityNoteBinding
import com.example.notepassapp.persistence.DatabaseProvider
import com.example.notepassapp.persistence.dao.NoteDao
import com.example.notepassapp.persistence.entity.NoteEntity
import kotlinx.coroutines.launch

class NoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteBinding
    private var editMode = false
    private var password = ""
    private val noteDao: NoteDao by lazy { DatabaseProvider.getDatabase(this).noteDao() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val note = intent.extras?.getParcelable(NOTE_ARG) as? NoteEntity
        if (note != null) {
            binding.editTextTitle.setText(note.title)
            binding.editTextDescription.setText(note.content)
            note.password?.let {
                binding.imageLock.setImageResource(R.drawable.ic_lock_locked)
            } ?: binding.imageLock.setImageResource(R.drawable.ic_lock_open)
            editMode = true
        }
        binding.imageLock.setOnClickListener {
            if (note?.password != null) {
                //Estic editant
                val title = if (note.password.isEmpty()) {
                    "Set password"
                } else {
                    "Insert password"
                }
                openPasswordDialog(title) { newPassword ->
                    password = if (note.password == newPassword) {
                        binding.imageLock.setImageResource(R.drawable.ic_lock_open)
                        NO_PASSWORD
                    } else {
                        Toast.makeText(it.context, "Incorrect password", Toast.LENGTH_SHORT).show()
                        note.password
                    }
                }
            } else {
                //Estic creant nota nova
                openPasswordDialog("Set password") { newPassword ->
                    if (newPassword.isNotEmpty()) {
                        password = newPassword
                        binding.imageLock.setImageResource(R.drawable.ic_lock_locked)
                    }
                }
            }
        }
        binding.imageButtonCheck.setOnClickListener {
            val noteTitle = binding.editTextTitle.text.toString()
            val noteDescription = binding.editTextDescription.text.toString()
            if (noteTitle.isNotEmpty() || noteDescription.isNotEmpty()) {
                if (editMode) {
                    updateNote(note, noteTitle, noteDescription)
                } else {
                    insertNote(noteTitle, noteDescription)
                }
            }
        }
    }

    private fun openPasswordDialog(title: String, onOk: (password: String) -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        val input = EditText(this)
        input.setHint(R.string.password)
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

    private fun insertNote(noteTitle: String, noteDescription: String) {
        lifecycleScope.launch {
            noteDao.insertNote(
                noteEntity = NoteEntity(title = noteTitle,
                    content = noteDescription,
                    password = password.ifEmpty { null }),
            )
            finish()
        }
    }

    private fun updateNote(note: NoteEntity?, noteTitle: String, noteDescription: String) {
        lifecycleScope.launch {
            note?.let {
                noteDao.updateNote(
                    it.copy(
                        id = it.id,
                        title = noteTitle,
                        content = noteDescription,
                        password = if (password.isEmpty()) {
                            note.password
                        } else {
                            if (note.password == null) {
                                password
                            } else null
                        }
                    )
                )
                finish()
            }
        }
    }

    companion object {
        fun getIntent(context: Context) = Intent(context, NoteActivity::class.java)
        fun getIntentWithArguments(context: Context, note: NoteEntity) =
            Intent(context, NoteActivity::class.java).apply {
                putExtra(NOTE_ARG, note)
            }
    }
}

private const val NOTE_ARG = "note_arg"
const val NO_PASSWORD = "noPassword"