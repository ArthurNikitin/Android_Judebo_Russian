package com.byte4b.judebo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.byte4b.judebo.R
import com.byte4b.judebo.models.Currency
import kotlinx.android.synthetic.main.currency_item.view.*

class CurrencyAdapter(ctx: Context, val list: Array<Currency>, val currentCurrency: String) :
    ArrayAdapter<Currency>(ctx, R.layout.currency_item, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inf = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inf.inflate(R.layout.currency_item, parent, false)
        view.icon.setImageResource(list[position].icon)
        view.name.text = list[position].name
        view.check.visibility =
            if (list[position].name == currentCurrency) View.VISIBLE
            else View.INVISIBLE
        return view
    }

}