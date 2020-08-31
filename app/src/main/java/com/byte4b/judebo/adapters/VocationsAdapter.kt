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
import com.byte4b.judebo.getDate
import com.byte4b.judebo.models.Vocation
import com.byte4b.judebo.models.VocationRealm
import com.byte4b.judebo.models.currencies
import com.byte4b.judebo.services.ApiServiceImpl
import com.byte4b.judebo.startActivity
import com.byte4b.judebo.timestamp
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
                val editDate = getDate(UF_MODIFED)
                val editString =
                    SimpleDateFormat("dd MMM", Locale(Setting(ctx).getCurrentLanguage().locale)).format(editDate)

                holder.editDateView.text = editString
                val currency = currencies.firstOrNull { it.id == UF_GROSS_CURRENCY_ID }
                holder.salaryView.text = "$UF_GROSS_PER_MONTH ${currency?.name ?: "USD"}"
            }
        } catch (e: Exception) {
            Log.e("test", "adapter: " + (e.localizedMessage ?: "error"))
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
                UF_MODIFED = now.timestamp.toString()
                UF_APP_JOB_ID = getNewJobAppId().toLong()

                now.add(Calendar.DATE, Setting.JOB_LIFETIME_IN_DAYS.toInt())
                UF_DISABLE = now.timestamp.toString()
            }
            it.copyToRealm(newVocation)
            ApiServiceImpl(this).addMyVocation(
                setting.getCurrentLanguage().locale,
                token = "Z4pjjs5t7rt6uJc2uOLWx5Zb",
                login = "judebo.com@gmail.com",
                vocation = newVocation.toBasicVersion()
            )
        }
    }

    override fun onVocationAdded(success: Boolean) {
        Log.e("check", "is added: $success")
        parent.onRefresh()
    }

    private fun getNewJobAppId(): String {
        var random = Random.nextLong(0, 99999999).toString()
        random = "0".repeat(8 - random.length) + random
        return "${Calendar.getInstance().timestamp}$random"
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

                    try {
                        vocationRealm?.isHided = true
                        vocationRealm?.AUTO_TRANSLATE = null
                        vocationRealm?.COMPANY = null
                        vocationRealm?.DETAIL_TEXT = null
                        vocationRealm?.NAME = null
                        vocationRealm?.UF_CONTACT_EMAIL = null
                        vocationRealm?.UF_CONTACT_PHONE = null
                        vocationRealm?.UF_DETAIL_IMAGE = null
                        vocationRealm?.UF_DISABLE = null
                        vocationRealm?.UF_GOLD_GROSS_MONTH = null
                        vocationRealm?.UF_GOLD_PER_MONTH = null
                        vocationRealm?.UF_GROSS_CURRENCY_ID = null
                        vocationRealm?.UF_GROSS_PER_MONTH = null
                        vocationRealm?.UF_LOGO_IMAGE = null
                        vocationRealm?.UF_MAP_POINT = null
                        vocationRealm?.UF_PREVIEW_IMAGE = null

                        vocationRealm?.UF_LANGUAGE_ID_ALL = null
                        vocationRealm?.UF_SKILLS_ID_ALL = null
                        vocationRealm?.UF_TYPE_OF_JOB_ID = null

                        vocationRealm?.UF_MODIFED = Calendar.getInstance().timestamp.toString()
                    } catch (e: Exception) {
                        Log.e("test", e.localizedMessage ?: "ErrorMe")
                    }
                    try {
                        ApiServiceImpl(this).deleteVocation(
                            setting.getCurrentLanguage().locale,
                            token = "Z4pjjs5t7rt6uJc2uOLWx5Zb",
                            login = "judebo.com@gmail.com",
                            vocation = vocationRealm!!.toBasicVersion()
                        )
                    } catch (e: Exception) {
                        onVocationDeleted(false)
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.request_request_delete_cancel) { d, _ -> d.cancel() }
            .show()
    }

    override fun onVocationDeleted(success: Boolean) {
        Log.e("check", "is deleted: $success")
        parent.onRefresh()
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