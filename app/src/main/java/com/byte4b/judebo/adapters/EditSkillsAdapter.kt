package com.byte4b.judebo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.byte4b.judebo.R
import com.byte4b.judebo.activities.SkillsActivity
import com.byte4b.judebo.models.Skill
import kotlinx.android.synthetic.main.item_edit_skill.view.*

class EditSkillsAdapter(
    private val ctx: SkillsActivity,
    private val skills: List<Skill>,
    private val isSelectedItems: Boolean
) : RecyclerView.Adapter<EditSkillsAdapter.Holder>() {

    override fun getItemCount() = skills.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(LayoutInflater.from(ctx).inflate(R.layout.item_edit_skill, parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        skills[position].apply {
            holder.name.text = name
            holder.check.setImageResource(
                if (isSelectedItems) R.drawable.edit_tags_search_delete
                else R.drawable.edit_tags_search_add
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