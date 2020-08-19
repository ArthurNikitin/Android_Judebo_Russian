package com.byte4b.judebo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.byte4b.judebo.R
import com.byte4b.judebo.activities.VocationEditActivity
import com.byte4b.judebo.models.Vocation
import com.byte4b.judebo.models.currencies
import com.byte4b.judebo.startActivity
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_vocation.view.*

class VocationsAdapter(
    private val ctx: Context,
    private val vocations: List<Vocation>
) : RecyclerView.Adapter<VocationsAdapter.Holder>() {

    override fun getItemCount() = vocations.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(LayoutInflater.from(ctx).inflate(R.layout.item_vocation, parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        with(vocations[position]) {
            holder.view.setOnClickListener {
                ctx.startActivity<VocationEditActivity> {
                    putExtra("data", Gson().toJson(this@with))
                }
            }
            holder.nameView.text = NAME
            holder.idView.text = "#$UF_JOBS_ID"
            try {
                if (UF_LOGO_IMAGE.isNotEmpty()) {
                    Glide.with(holder.view)
                        .load(UF_LOGO_IMAGE)
                        .circleCrop()
                        .placeholder(R.drawable.map_default_marker)
                        .into(holder.iconView)
                }
            } catch (e: Exception) {}
            holder.editDateView.text = UF_MODIFED.split(" ").firstOrNull() ?: "Empty"
            holder.deleteDateView.text = UF_DISABLE.split(" ").firstOrNull() ?: "Empty"
            val currency = currencies.firstOrNull { it.id == UF_GROSS_CURRENCY_ID }
            holder.salaryView.text = "$UF_GROSS_PER_MONTH ${currency?.name ?: "USD"}"
        }
    }

    class Holder(val view: View) : RecyclerView.ViewHolder(view) {
        val iconView = view.icon_iv!!
        val nameView = view.name_tv!!
        val salaryView = view.salary_tv!!
        val idView = view.id_tv!!
        val editDateView = view.edit_tv!!
        val deleteDateView = view.delete_tv!!
    }

}