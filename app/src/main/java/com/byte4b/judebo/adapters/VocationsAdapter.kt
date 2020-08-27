package com.byte4b.judebo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.byte4b.judebo.R
import com.byte4b.judebo.activities.VocationEditActivity
import com.byte4b.judebo.fragments.CreatorFragment
import com.byte4b.judebo.models.Vocation
import com.byte4b.judebo.models.VocationRealm
import com.byte4b.judebo.models.currencies
import com.byte4b.judebo.services.ApiServiceImpl
import com.byte4b.judebo.startActivity
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.view.ServiceListener
import com.daimajia.swipe.SwipeLayout
import com.google.gson.Gson
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.item_vocation.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class VocationsAdapter(
    private val ctx: Context,
    private val vocations: List<Vocation>,
    private val parent: CreatorFragment
) : RecyclerView.Adapter<VocationsAdapter.Holder>(), ServiceListener {

    private val setting by lazy { Setting(ctx) }
    private var lastSwipedPosition = -1

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                notifyItemChanged(lastSwipedPosition)
            }
        })
    }

    override fun getItemCount() = vocations.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(LayoutInflater.from(ctx).inflate(R.layout.item_vocation, parent, false))

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        try {
            with(vocations[position]) {

                holder.copy1.setOnClickListener { copyVocations(this) }
                holder.copy2.setOnClickListener { copyVocations(this) }

                holder.del1.setOnClickListener { deleteVocation(this) }
                holder.del2.setOnClickListener { deleteVocation(this) }

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
                with(holder.swiper) {
                    isLeftSwipeEnabled = true
                    isRightSwipeEnabled = true
                    close(false)
                    addSwipeListener(object : SwipeLayout.SwipeListener {
                        override fun onOpen(layout: SwipeLayout?) {
                            lastSwipedPosition = position
                        }

                        override fun onUpdate(
                            layout: SwipeLayout?,
                            leftOffset: Int,
                            topOffset: Int
                        ) {}

                        override fun onStartOpen(layout: SwipeLayout?) {
                            if (lastSwipedPosition != position)
                                notifyItemChanged(lastSwipedPosition)
                        }

                        override fun onStartClose(layout: SwipeLayout?) {}
                        override fun onHandRelease(
                            layout: SwipeLayout?,
                            xvel: Float,
                            yvel: Float
                        ) {}

                        override fun onClose(layout: SwipeLayout?) {}
                    })

                    addDrag(SwipeLayout.DragEdge.Left, holder.left)
                    addDrag(SwipeLayout.DragEdge.Right, holder.right)
                }

                try {
                    if (!UF_LOGO_IMAGE.isNullOrEmpty()) {
                        Glide.with(holder.view)
                            .load(UF_LOGO_IMAGE)
                            .circleCrop()
                            .placeholder(R.drawable.map_default_marker)
                            .into(holder.iconView)
                    }
                } catch (e: Exception) {
                }
                val editDate = SimpleDateFormat("dd.mm.yyyy hh:mm:ss")
                    .parse(UF_MODIFED ?: "")
                val editString =
                    if (editDate != null)
                        SimpleDateFormat("dd MMM", Locale(Setting(ctx).getCurrentLanguage().locale)).format(editDate)
                    else
                        "Empty"

                holder.editDateView.text = editString
                val currency = currencies.firstOrNull { it.id == UF_GROSS_CURRENCY_ID }
                holder.salaryView.text = "$UF_GROSS_PER_MONTH ${currency?.name ?: "USD"}"
            }
        } catch (e: Exception) {
            Log.e("test", e.localizedMessage ?: "error")
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun copyVocations(vocation: Vocation) {
        Realm.getDefaultInstance().executeTransaction {
            val vocationRealm = it.where<VocationRealm>()
                .equalTo("UF_JOBS_ID", vocation.UF_JOBS_ID)
                .findFirst()
                ?: return@executeTransaction
            val newVocation = vocationRealm.toBasicVersion().toRealmVersion()
            newVocation.apply {
                val now = Calendar.getInstance()

                UF_JOBS_ID = null

                val format = SimpleDateFormat("dd.mm.yyyy hh:mm:ss")
                UF_MODIFED = format.format(now.time)

                UF_APP_JOB_ID = getNewJobAppId()

                now.add(Calendar.DATE, Setting.JOB_LIFETIME_IN_DAYS.toInt())
                UF_DISABLE = format.format(now.time)
            }
        }
        //todo: update query to server
        //todo: reload list
    }

    private fun getNewJobAppId(): String {
        var random = Random.nextLong(0, 99999999).toString()
        random = "0".repeat(8 - random.length) + random
        Log.e("test", random)
        return "${Calendar.getInstance().timeInMillis / 1000}$random"
    }

    @SuppressLint("SimpleDateFormat")
    private fun deleteVocation(vocation: Vocation) {
        Log.e("test", "del")
        AlertDialog.Builder(ctx)
            .setTitle(R.string.request_request_delete_title)
            .setMessage(R.string.request_request_delete_message)
            .setPositiveButton(R.string.request_request_delete_ok) { dialog, _ ->

                Realm.getDefaultInstance().executeTransaction {
                    val vocationRealm = it.where<VocationRealm>()
                        .equalTo("UF_JOBS_ID", vocation.UF_JOBS_ID)
                        .findFirst()
                    vocationRealm?.apply {
                        isHided = true
                        val format = SimpleDateFormat("dd.mm.yyyy hh:mm:ss")
                        UF_MODIFED = format.format(Calendar.getInstance().time)

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
                ApiServiceImpl(this).deleteVocation(
                    setting.getCurrentLanguage().locale,
                    token = "Z4pjjs5t7rt6uJc2uOLWx5Zb",
                    login = "judebo.com@gmail.com",
                    vocation = vocation
                )
                parent.onRefresh()
                //todo: update query to server
                //todo: reload list

                dialog.dismiss()
            }
            .setNegativeButton(R.string.request_request_delete_cancel) { d, _ -> d.cancel() }
            .show()
    }

    class Holder(val view: View) : RecyclerView.ViewHolder(view) {
        val iconView = view.icon_iv!!
        val nameView = view.name_tv!!
        val salaryView = view.salary_tv!!
        val idView = view.id_tv!!
        val editDateView = view.edit_tv!!
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