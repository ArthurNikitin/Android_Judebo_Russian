package com.byte4b.judebo.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.byte4b.judebo.R
import com.byte4b.judebo.activities.SkillsActivity
import com.byte4b.judebo.activities.VocationEditActivity
import com.byte4b.judebo.models.Vocation
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item_skill.view.*

class SkillsAdapter(
    private val ctx: Context,
    private val skills: List<String>,
    private val isDetails: Boolean = false,
    private val isEditor: Boolean = false,
    private val vocation: Vocation? = null
) : RecyclerView.Adapter<SkillsAdapter.Holder>() {

    override fun getItemCount() = skills.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(LayoutInflater.from(ctx).inflate(R.layout.item_skill, parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.skillTitle.text = skills[position]
        if (isDetails) holder.skillTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)

        holder.skillImage.setImageResource(R.drawable.item_detail_tags)

        holder.view.setOnClickListener {
            if (!isEditor) return@setOnClickListener
            try {
                val selectIntent = Intent((ctx as Activity), SkillsActivity::class.java)
                selectIntent.putExtra("data", Gson().toJson(vocation))
                ctx.startActivityForResult(selectIntent, VocationEditActivity.REQUEST_SKILLS)
            } catch (e: Exception) {}
        }
    }

    class Holder(val view: View) : RecyclerView.ViewHolder(view) {
        val skillTitle = view.skill_tv!!
        val skillImage = view.imageView2!!
    }

}