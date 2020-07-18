package com.byte4b.judebo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.byte4b.judebo.R
import com.byte4b.judebo.models.Language
import kotlinx.android.synthetic.main.currency_item.view.*

class LanguageAdapter(ctx: Context, val list: Array<Language>, val currentLanguage: String) :
    ArrayAdapter<Language>(ctx, R.layout.currency_item, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inf = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inf.inflate(R.layout.currency_item, parent, false)
        view.icon.setImageResource(list[position].flag)
        view.name.text = list[position].title
        view.check.visibility =
            if (list[position].locale == currentLanguage) View.VISIBLE
            else View.INVISIBLE
        return view
    }

}