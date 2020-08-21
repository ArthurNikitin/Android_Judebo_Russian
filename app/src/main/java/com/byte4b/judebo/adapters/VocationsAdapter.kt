package com.byte4b.judebo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.byte4b.judebo.R
import com.byte4b.judebo.activities.VocationEditActivity
import com.byte4b.judebo.models.Vocation
import com.byte4b.judebo.models.VocationRealm
import com.byte4b.judebo.models.currencies
import com.byte4b.judebo.startActivity
import com.byte4b.judebo.utils.Setting
import com.daimajia.swipe.SwipeLayout
import com.google.gson.Gson
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.item_vocation.view.*
import java.text.SimpleDateFormat
import java.util.*

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
            holder.main.setOnClickListener {
                if (holder.swiper.openStatus == SwipeLayout.Status.Close) {
                    ctx.startActivity<VocationEditActivity> {
                        putExtra("data", Gson().toJson(this@with))
                    }
                } else
                    holder.swiper.close()
            }
            holder.nameView.text = NAME
            holder.idView.text = "#$UF_JOBS_ID"
            holder.swiper.isLeftSwipeEnabled = true
            holder.swiper.isRightSwipeEnabled = true

            holder.swiper.addDrag(SwipeLayout.DragEdge.Left, holder.left)
            holder.swiper.addDrag(SwipeLayout.DragEdge.Right, holder.right)

            try {
                if (!UF_LOGO_IMAGE.isNullOrEmpty()) {
                    Glide.with(holder.view)
                        .load(UF_LOGO_IMAGE)
                        .circleCrop()
                        .placeholder(R.drawable.map_default_marker)
                        .into(holder.iconView)
                }
            } catch (e: Exception) {}
            holder.editDateView.text = UF_MODIFED?.split(" ")?.firstOrNull() ?: "Empty"
            holder.deleteDateView.text = UF_DISABLE?.split(" ")?.firstOrNull() ?: "Empty"
            val currency = currencies.firstOrNull { it.id == UF_GROSS_CURRENCY_ID }
            holder.salaryView.text = "$UF_GROSS_PER_MONTH ${currency?.name ?: "USD"}"

            holder.copy1.setOnClickListener { copyVocations(this) }
            holder.copy2.setOnClickListener { copyVocations(this) }

            holder.del1.setOnClickListener { deleteVocation(this) }
            holder.del2.setOnClickListener { deleteVocation(this) }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun copyVocations(vocation: Vocation) {
        Realm.getDefaultInstance().executeTransaction {
            val vocationRealm = it.where<VocationRealm>()
                .equalTo("UF_JOBS_IВ", vocation.UF_JOBS_ID)
                .findFirst()
                ?: return@executeTransaction
            val newVocation = vocationRealm.toBasicVersion().toRealmVersion()
            newVocation.apply {
                val now = Calendar.getInstance()

                UF_JOBS_ID = null

                val format = SimpleDateFormat("dd.mm.yyyy hh:mm:ss")
                UF_MODIFED = format.format(now.time)
                Log.e("test", UF_MODIFED.toString())

                UF_APP_JOB_ID = UUID.randomUUID().toString() + getMaxId(it) + 1

                now.add(Calendar.DATE, Setting.JOB_LIFETIME_IN_DAYS.toInt())
                UF_DISABLE = format.format(now.time)
            }
        }
        //todo: update query to server
        //todo: reload list
    }

    inline fun getMaxId(realm: Realm): Int {
        return realm.where<VocationRealm>().max("ID")?.toInt() ?: 0
    }

    @SuppressLint("SimpleDateFormat")
    private fun deleteVocation(vocation: Vocation) {
        Realm.getDefaultInstance().executeTransaction {
            val vocationRealm = it.where<VocationRealm>()
                .equalTo("UF_JOBS_ID", vocation.UF_JOBS_ID)
                .findFirst()
            vocationRealm?.apply {
                isHided = true
                val format = SimpleDateFormat("dd.mm.yyyy hh:mm:ss")
//17.08.2021 11:46:20
                UF_MODIFED = format.format(Calendar.getInstance().time)
                Log.e("test", UF_MODIFED.toString())

                AUTO_TRANSLATE = null
                COMPANY = null
                DETAIL_TEXT = null
                NAME = null
                UF_CONTACT_EMAIL = null
                UF_CONTACT_PHONE = null
                UF_DETAIL_IMAGE = null
                UF_DISABLE = null
                UF_GOLD_GROSS_MONTH = null
                UF_GOLD_PER_MONTH = null
                UF_GROSS_CURRENCY_ID = null
                UF_GROSS_PER_MONTH = null
                UF_LANGUAGE_ID_ALL = null
                UF_LOGO_IMAGE = null
                UF_MAP_POINT = null
                UF_MAP_RENDERED = null
                UF_PREVIEW_IMAGE = null
                UF_SKILLS_ID_ALL = null
                UF_TYPE_OF_JOB_ID = null
                UF_USER_ID = null
            }
        }
        //todo: update query to server
        //todo: reload list
    }

    class Holder(val view: View) : RecyclerView.ViewHolder(view) {
        val iconView = view.icon_iv!!
        val nameView = view.name_tv!!
        val salaryView = view.salary_tv!!
        val idView = view.id_tv!!
        val editDateView = view.edit_tv!!
        val deleteDateView = view.delete_tv!!
        val swiper = view.side_swiper!!
        val left = view.left1!!
        val right = view.right1!!
        val main = view.container!!
        val copy1 = view.copy1!!
        val copy2 = view.copy2!!
        val del1 = view.delete1!!
        val del2 = view.delete2!!
    }

}