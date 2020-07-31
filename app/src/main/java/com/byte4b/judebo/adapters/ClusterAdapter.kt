package com.byte4b.judebo.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byte4b.judebo.R
import com.byte4b.judebo.activities.DetailsActivity
import com.byte4b.judebo.models.MyMarker
import com.byte4b.judebo.models.currencies
import com.byte4b.judebo.models.languages
import com.byte4b.judebo.round
import com.byte4b.judebo.setRightDrawable
import com.byte4b.judebo.startActivity
import com.byte4b.judebo.utils.Setting
import com.google.android.flexbox.*
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_skill.view.*
import kotlinx.android.synthetic.main.preview.*
import kotlinx.android.synthetic.main.preview.view.*

class ClusterAdapter(
    private val ctx: Context,
    private val markers: List<MyMarker>
) : RecyclerView.Adapter<ClusterAdapter.Holder>() {

    private val setting by lazy { Setting(ctx) }

    override fun getItemCount() = markers.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(LayoutInflater.from(ctx).inflate(R.layout.preview, parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        with(markers[position]) {
            try {
                holder.view.setOnClickListener {
                    ctx.startActivity<DetailsActivity> { putExtra("marker", Gson().toJson(this@with)) }
                }
                holder.moreButton.setOnClickListener {
                    ctx.startActivity<DetailsActivity> { putExtra("marker", Gson().toJson(this@with)) }
                }
                holder.titleView.setOnClickListener {
                    ctx.startActivity<DetailsActivity> { putExtra("marker", Gson().toJson(this@with)) }
                }

                holder.titleView.text = NAME
                if (!UF_LOGO_IMAGE.isNullOrEmpty()) {
                    try {
                        Picasso.get()
                            .load(UF_PREVIEW_IMAGE)
                            .placeholder(R.drawable.default_logo_preview)
                            .error(R.drawable.default_logo_preview)
                            .into(holder.logoView)
                    } catch (e: Exception) { }
                }

                val currency = currencies.firstOrNull { it.id == UF_GROSS_CURRENCY_ID }

                try {
                    if (UF_GROSS_PER_MONTH.isEmpty() || UF_GROSS_PER_MONTH == "0") {
                        holder.convertedSalaryContainer.visibility = View.GONE
                        holder.originSalaryContainer.visibility = View.GONE
                    } else {
                        holder.convertedSalaryContainer.visibility = View.VISIBLE
                        holder.originSalaryContainer.visibility = View.VISIBLE
                    }
                    if (currency?.name == setting.currency
                        || (setting.currency == "" && currency?.name == "USD")
                    ) {
                        holder.originSalaryValue.text = UF_GROSS_PER_MONTH.round()
                        holder.originSalaryCurrency.text = " ${currency?.name ?: ""}"
                        holder.originSalaryValue.setRightDrawable(currency?.icon ?: R.drawable.iusd)
                        holder.convertedSalaryContainer.visibility = View.GONE
                    } else {
                        holder.originSalaryValue.text = UF_GROSS_PER_MONTH.round()
                        holder.originSalaryCurrency.text = " ${currency?.name ?: ""}"
                        holder.originSalaryValue.setRightDrawable(currency?.icon ?: R.drawable.iusd)
                        holder.convertedSalaryContainer.visibility = View.VISIBLE
                        val currencyFromSetting =
                            if (setting.currency.isNullOrEmpty()) "USD" else setting.currency!!
                        val currency2 = currencies.firstOrNull { it.name == currencyFromSetting }
                        val convertedSalary = UF_GROSS_PER_MONTH.toDouble() *
                                (currency2?.rate ?: 1) / (currency?.rate ?: 1)
                        if (convertedSalary == 0.0)
                            holder.convertedSalaryContainer.visibility = View.GONE
                        holder.convertedSalaryValue.text =
                            "≈${convertedSalary.toString().round()}"
                        holder.convertedSalaryCurrency.text = currency2?.name ?: "USD"
                        holder.convertedSalaryValue.setRightDrawable(currency2?.icon ?: R.drawable.iusd)
                    }
                } catch (e: Exception) {
                }
                holder.moreButton.text = "#${UF_JOBS_ID} ${ctx.getString(R.string.button_detail_title)}"
                try {
                    holder.languagesList.layoutManager =
                        LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)
                    holder.languagesList.adapter =
                        LanguagesAdapter(ctx, UF_LANGUAGE_ID_ALL.split(",").map {
                            languages.first { lang -> lang.id == it.toInt() }
                        })
                } catch (e: Exception) {
                }
                holder.placeView.text = COMPANY

                val layoutManager = FlexboxLayoutManager(ctx)
                layoutManager.flexWrap = FlexWrap.WRAP
                layoutManager.flexDirection = FlexDirection.ROW
                layoutManager.justifyContent = JustifyContent.FLEX_START
                layoutManager.alignItems = AlignItems.FLEX_START

                holder.filtersList.layoutManager = layoutManager
                if (UF_SKILLS_ID_ALL == "") {
                    holder.filtersList.visibility = View.GONE
                } else {
                    holder.filtersList.visibility = View.VISIBLE
                    holder.filtersList.adapter = SkillsAdapter(ctx, ALL_SKILLS_NAME.split(","))
                }
            } catch (e: Exception) {
                Log.e("test", e.localizedMessage ?: "error")
            }
        }
    }

    class Holder(val view: View) : RecyclerView.ViewHolder(view) {
        val moreButton = view.more_tv!!
        val titleView = view.title_tv!!
        val logoView = view.logo_iv!!
        val placeView = view.place_tv!!
        val filtersList = view.filters_tv!!
        val languagesList = view.langs_rv!!
        val originSalaryContainer = view.salaryContainer!!
        val convertedSalaryContainer = view.secondContainer!!
        val originSalaryValue = view.salary_tv!!
        val originSalaryCurrency = view.salaryVal_tv!!
        val convertedSalaryValue = view.secondSalary_tv!!
        val convertedSalaryCurrency = view.secondSalaryVal_tv!!
    }

}