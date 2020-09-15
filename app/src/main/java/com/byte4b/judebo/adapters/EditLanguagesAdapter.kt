package com.byte4b.judebo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.byte4b.judebo.R
import com.byte4b.judebo.activities.LanguagesActivity
import com.byte4b.judebo.models.Language
import kotlinx.android.synthetic.main.edit_vocation_language.view.*

class EditLanguagesAdapter(
    private val ctx: LanguagesActivity,
    private val languages: List<Pair<Language, Boolean>>
) : RecyclerView.Adapter<EditLanguagesAdapter.Holder>() {

    override fun getItemCount() = languages.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(LayoutInflater.from(ctx).inflate(R.layout.edit_vocation_language, parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        languages[position].apply {
            holder.name.text = first.name
            holder.icon.setImageResource(first.flag)
            holder.action.setImageResource(
                if (second) R.drawable.edit_tags_search_delete
                else R.drawable.edit_jobs_add_tags_and_langs
            )
            holder.view.setBackgroundColor(
                ctx.resources.getColor(
                    if (second) R.color.settings_backgroud_active_line
                    else R.color.white
                )
            )
            holder.check.visibility =
                if (second) View.VISIBLE
                else View.INVISIBLE


            holder.view.setOnClickListener {
                if (second)
                    ctx.deleteSkill(first.id)
                else
                    ctx.addSkill(first.id)
            }
        }
    }

    class Holder(val view: View) : RecyclerView.ViewHolder(view) {
        val check = view.check!!
        val action = view.action!!
        val name = view.name!!
        val icon = view.icon!!
    }

}