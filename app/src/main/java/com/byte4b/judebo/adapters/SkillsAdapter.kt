package com.byte4b.judebo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.byte4b.judebo.R
import kotlinx.android.synthetic.main.item_skill.view.*

class SkillsAdapter(
    private val ctx: Context,
    private val skills: List<String>
) : RecyclerView.Adapter<SkillsAdapter.Holder>() {

    override fun getItemCount() = skills.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(LayoutInflater.from(ctx).inflate(R.layout.item_skill, parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.skillTitle.text = skills[position]
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val skillTitle = view.skill_tv!!
    }

}