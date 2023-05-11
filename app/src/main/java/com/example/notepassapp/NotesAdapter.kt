package com.example.notepassapp

import android.graphics.BlurMaskFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.notepassapp.databinding.NoteItemBinding
import com.example.notepassapp.persistence.entity.NoteEntity


class NotesAdapter(private val onNotesAdapterAction: (NoteAdapterAction) -> Unit) :
    ListAdapter<NoteEntity, NotesAdapter.NotesViewHolder>(NotesDiffCallback()),Filterable {
    var originalList = listOf<NoteEntity>()
    override fun getFilter(): Filter {
        return object:Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = mutableListOf<NoteEntity>()
                val filterPattern = constraint.toString().lowercase().trim()
                if (filterPattern.isEmpty()){
                    filteredList.addAll(originalList)
                }
                else{
                    originalList.filter { it.password == null }.forEach{
                        if (it.title.lowercase().contains(filterPattern)) {
                            filteredList.add(it)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            override fun publishResults(constraint: CharSequence?, result: FilterResults?) {
                submitList(result?.values as List<NoteEntity>)
            }
        }
    }
    fun submitOriginalList(list: List<NoteEntity>){
        originalList = list
        submitList(list)
    }
    class NotesViewHolder(private val itemBinding: NoteItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(noteEntity: NoteEntity, onNotesAdapterAction: (NoteAdapterAction) -> Unit) {
            itemBinding.itemLock.isVisible = noteEntity.password != null
            itemBinding.itemTitle.text = noteEntity.title
            itemBinding.itemContent.text = noteEntity.content
            if (noteEntity.password != null) {
                addBlurryEffect(itemBinding)
            } else {
                itemBinding.itemTitle.paint.maskFilter = null
                itemBinding.itemContent.paint.maskFilter = null
            }

            itemBinding.itemRemove.setOnClickListener {
                onNotesAdapterAction(
                    NoteAdapterAction.OnDeleteNote(
                        noteEntity
                    )
                )
            }
            itemBinding.itemEdit.setOnClickListener {
                onNotesAdapterAction(
                    NoteAdapterAction.OnEditNote(
                        noteEntity
                    )
                )
            }
        }

        private fun addBlurryEffect(itemBinding: NoteItemBinding) {
            val titleRadius = itemBinding.itemTitle.textSize / 3
            val contentRadius = itemBinding.itemContent.textSize / 3
            val filterTitle = BlurMaskFilter(titleRadius, BlurMaskFilter.Blur.NORMAL)
            val filterContent = BlurMaskFilter(contentRadius, BlurMaskFilter.Blur.NORMAL)
            itemBinding.itemTitle.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            itemBinding.itemTitle.paint.maskFilter = filterTitle
            itemBinding.itemContent.paint.maskFilter = filterContent
        }
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        holder.bind(currentList[position], onNotesAdapterAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val binding = NoteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotesViewHolder(binding)
    }
}

private class NotesDiffCallback : DiffUtil.ItemCallback<NoteEntity>() {
    override fun areContentsTheSame(oldItem: NoteEntity, newItem: NoteEntity): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: NoteEntity, newItem: NoteEntity): Boolean {
        return oldItem.id == newItem.id
    }
}

sealed interface NoteAdapterAction {
    data class OnDeleteNote(val note: NoteEntity) : NoteAdapterAction
    data class OnEditNote(val note: NoteEntity) : NoteAdapterAction
}