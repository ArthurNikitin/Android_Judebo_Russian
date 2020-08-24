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
import com.byte4b.judebo.models.Language
import kotlinx.android.synthetic.main.item_lang.view.*

class LanguagesAdapter(
    private val ctx: Context,
    private val langs: List<Language>,
    private val isDetails: Boolean = false,
    private val isEditor: Boolean = false
) : RecyclerView.Adapter<LanguagesAdapter.Holder>() {

    override fun getItemCount() = langs.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(LayoutInflater.from(ctx).inflate(R.layout.item_lang, parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        with(langs[position]) {
            holder.title.text = if (isDetails) title else locale.toUpperCase()
            holder.icon.setImageResource(flag)
            holder.title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        }

        holder.view.setOnClickListener {
            if (!isEditor) return@setOnClickListener
            (ctx as Activity).startActivityForResult(
                Intent(ctx, SkillsActivity::class.java), VocationEditActivity.REQUEST_SKILLS
            )
        }
    }

    class Holder(val view: View) : RecyclerView.ViewHolder(view) {
        val title = view.currencyTitle_tv!!
        val icon = view.currency_iv!!
    }

}