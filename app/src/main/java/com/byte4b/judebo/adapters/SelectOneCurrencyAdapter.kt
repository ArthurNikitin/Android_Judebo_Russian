package com.byte4b.judebo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.byte4b.judebo.R
import com.byte4b.judebo.activities.SelectAppCurrency
import com.byte4b.judebo.models.Currency
import kotlinx.android.synthetic.main.edit_vocation_language.view.*

class SelectOneCurrencyAdapter(
    private val ctx: SelectAppCurrency,
    private val languages: List<Currency>,
    private val selectedId: Int
) : RecyclerView.Adapter<SelectOneCurrencyAdapter.Holder>() {

    override fun getItemCount() = languages.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(LayoutInflater.from(ctx).inflate(R.layout.edit_vocation_language, parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        languages[position].apply {
            holder.name.text = name
            holder.icon.setImageResource(icon)
            holder.view.setBackgroundColor(
                ctx.resources.getColor(
                    if (id == selectedId) R.color.settings_backgroud_active_line
                    else R.color.white
                )
            )
            holder.check.visibility =
                if (id == selectedId) View.VISIBLE
                else View.INVISIBLE


            holder.view.setOnClickListener {
                if (id != selectedId)
                    ctx.saveClick(id)
            }
        }
    }

    class Holder(val view: View) : RecyclerView.ViewHolder(view) {
        val check = view.check!!
        val name = view.name!!
        val icon = view.icon!!
    }

}