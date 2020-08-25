package com.byte4b.judebo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.byte4b.judebo.R
import com.byte4b.judebo.activities.LanguagesActivity
import com.byte4b.judebo.models.Language
import kotlinx.android.synthetic.main.item_edit_skill.view.*

class EditLanguagesAdapter(
    private val ctx: LanguagesActivity,
    private val languages: List<Language>,
    private val isSelectedItems: Boolean
) : RecyclerView.Adapter<EditLanguagesAdapter.Holder>() {

    override fun getItemCount() = languages.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(LayoutInflater.from(ctx).inflate(R.layout.item_edit_skill, parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        languages[position].apply {
            holder.name.text = name
            holder.check.setImageResource(
                if (isSelectedItems) R.drawable.button_delete
                else R.drawable.button_plus_gray
            )

            holder.view.setOnClickListener {
                if (isSelectedItems)
                    ctx.deleteSkill(id)
                else
                    ctx.addSkill(id)
            }
        }
    }

    class Holder(val view: View) : RecyclerView.ViewHolder(view) {
        val check = view.check!!
        val name = view.name!!
    }

}