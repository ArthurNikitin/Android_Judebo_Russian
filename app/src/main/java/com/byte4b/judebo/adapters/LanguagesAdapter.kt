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
import com.byte4b.judebo.activities.LanguagesActivity
import com.byte4b.judebo.activities.VocationEditActivity
import com.byte4b.judebo.models.Language
import com.byte4b.judebo.models.Vocation
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item_lang.view.*

class LanguagesAdapter(
    private val ctx: Context,
    private val langs: List<Language>,
    private val isDetails: Boolean = false,
    private val isEditor: Boolean = false,
    private val vocation: Vocation? = null
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
            try {
                val selectIntent = Intent((ctx as Activity), LanguagesActivity::class.java)
                selectIntent.putExtra("data", Gson().toJson(vocation))
                ctx.startActivityForResult(selectIntent, VocationEditActivity.REQUEST_LANGUAGES)
            } catch (e: Exception) {}
        }
    }

    class Holder(val view: View) : RecyclerView.ViewHolder(view) {
        val title = view.currencyTitle_tv!!
        val icon = view.currency_iv!!
    }

}