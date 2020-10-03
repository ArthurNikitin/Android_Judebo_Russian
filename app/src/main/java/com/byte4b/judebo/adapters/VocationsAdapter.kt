package com.byte4b.judebo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.util.Base64.decode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
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
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.item_vocation.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random


class VocationsAdapter(
    private val ctx: Context,
    private var vocations: List<Vocation>,
    private val parent: CreatorFragment,
    private val isMaxVocations: Boolean
) : RecyclerView.Adapter<VocationsAdapter.Holder>(), ServiceListener {

    init {
        vocations = vocations.sortedByDescending { it.UF_MODIFED ?: 0 }
    }

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
            if (isMaxVocations) {
                holder.copyLeft.setImageResource(R.drawable.button_copy_deny)
                holder.copyRight.setImageResource(R.drawable.button_copy_deny)
            }


            with(vocations[position]) {

                holder.copy1.setOnClickListener { if (!isMaxVocations) copyVocations(this) }
                holder.copy2.setOnClickListener { if (!isMaxVocations) copyVocations(this) }

                holder.del1.setOnClickListener { deleteVocation(this) }
                holder.del2.setOnClickListener { deleteVocation(this) }

                holder.main.setOnClickListener {
                    if (holder.swiper.openStatus == SwipeLayout.Status.Close) {
                        ctx.startActivity<VocationEditActivity> {
                            putExtra("appId", UF_APP_JOB_ID?.toLongOrNull())
                            putExtra("jobId", UF_JOBS_ID)
                        }
                    } else
                        holder.swiper.close()
                }
                holder.nameView.text = NAME
                if (UF_JOBS_ID == null) {
                    holder.idView.visibility = View.GONE
                } else {
                    holder.idView.text = "#$UF_JOBS_ID"
                    holder.idView.visibility = View.VISIBLE
                }

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
                            .addListener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    try {
                                        val btm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                            Base64.getDecoder().decode(UF_LOGO_IMAGE!!)
                                        else
                                            decode(UF_LOGO_IMAGE!!, android.util.Base64.DEFAULT)
                                        Handler().postDelayed({
                                            Glide.with(ctx)
                                                .load(BitmapFactory.decodeByteArray(btm, 0, btm.size))
                                                .circleCrop()
                                                .into(holder.iconView)
                                        }, 12)
                                    } catch (e: Exception) {}
                                    return true
                                }

                                override fun onResourceReady(r: Drawable?, m: Any?,
                                                             t: Target<Drawable>?, d: DataSource?,
                                                             i: Boolean) = false
                            })
                            .into(holder.iconView)
                    }
                } catch (e: Exception) {}
                val editDate = getDate(UF_MODIFED)
                val disableDate = getDate(UF_DISABLE)
                val format = SimpleDateFormat("dd MMM", Locale(setting.getCurrentLanguage().locale))

                holder.editDateView.text = format.format(editDate) + " - "
                holder.disableLabel.text = format.format(disableDate)
                holder.company.text = COMPANY

                if ((UF_ACTIVE != 1.toByte()) || ((UF_DISABLE?:0) < Calendar.getInstance().timestamp)) {
                    holder.isNotActiveView.visibility = View.VISIBLE

                    if ((UF_DISABLE?:0) < Calendar.getInstance().timestamp) {
                        holder.editDateView
                            .setTextColor(ctx.resources.getColor(android.R.color.holo_red_dark))
                        holder.editDateView.setTypeface(null, Typeface.BOLD)

                        holder.disableLabel
                            .setTextColor(ctx.resources.getColor(android.R.color.holo_red_dark))
                        holder.disableLabel.setTypeface(null, Typeface.BOLD)
                    }

                    holder.leftCorners.setImageResource(R.drawable.corners_left_red)
                    holder.rightCorners.setImageResource(R.drawable.corners_right_red)
                    holder.main.setBackgroundResource(R.color.jobs_list_not_active_background)
                    holder.errorView.visibility = View.VISIBLE

                } else {
                    holder.isNotActiveView.visibility = View.INVISIBLE

                    if ((UF_DISABLE?:0) < Calendar.getInstance().timestamp) {
                        holder.editDateView
                            .setTextColor(ctx.resources.getColor(R.color.grey))
                        holder.editDateView.setTypeface(null, Typeface.NORMAL)

                        holder.disableLabel
                            .setTextColor(ctx.resources.getColor(R.color.grey))
                        holder.disableLabel.setTypeface(null, Typeface.NORMAL)
                    }

                    holder.rightCorners.setImageResource(R.drawable.corners_right)
                    holder.leftCorners.setImageResource(R.drawable.corners_left)
                    holder.main.setBackgroundResource(R.color.white)
                    holder.errorView.visibility = View.INVISIBLE
                }

                val currency = currencies.firstOrNull { it.id == UF_GROSS_CURRENCY_ID }
                if (UF_GROSS_PER_MONTH == null) {
                    holder.salaryView.visibility = View.INVISIBLE
                } else {
                    holder.salaryView.visibility = View.VISIBLE
                    holder.salaryView.text = "$UF_GROSS_PER_MONTH ${currency?.name ?: "USD"}"
                }
            }
        } catch (e: Exception) {}
    }

    @SuppressLint("SimpleDateFormat")
    private fun copyVocations(vocation: Vocation) {
        try {
            Realm.getDefaultInstance().executeTransaction {
                val vocationRealm = it.where<VocationRealm>()
                    .equalTo("UF_APP_JOB_ID", vocation.UF_APP_JOB_ID?.toLong())
                    .findFirst()
                    ?: return@executeTransaction

                val copyVocation = VocationRealm()
                copyVocation.isHided = vocationRealm.isHided

                val now = Calendar.getInstance()

                copyVocation.NAME = (vocationRealm.NAME ?: "") + "-2"
                copyVocation.UF_JOBS_ID = null
                copyVocation.UF_MODIFED = now.timestamp
                copyVocation.UF_APP_JOB_ID = getNewJobAppId().toLong()

                now.add(Calendar.DATE, Setting.JOB_LIFETIME_IN_DAYS)
                copyVocation.UF_DISABLE = now.timestamp

                copyVocation.UF_ACTIVE = vocationRealm.UF_ACTIVE
                copyVocation.COMPANY = vocationRealm.COMPANY
                copyVocation. DETAIL_TEXT = vocationRealm.DETAIL_TEXT
                copyVocation. UF_CONTACT_EMAIL = vocationRealm.UF_CONTACT_EMAIL
                copyVocation. UF_CONTACT_PHONE = vocationRealm.UF_CONTACT_PHONE
                copyVocation. UF_DETAIL_IMAGE = vocationRealm.UF_DETAIL_IMAGE
                copyVocation. UF_GOLD_PER_MONTH = vocationRealm.UF_GOLD_PER_MONTH
                copyVocation. UF_GROSS_CURRENCY_ID = vocationRealm.UF_GROSS_CURRENCY_ID
                copyVocation. UF_GROSS_PER_MONTH = vocationRealm.UF_GROSS_PER_MONTH
                copyVocation. UF_LANGUAGE_ID_ALL = vocationRealm.UF_LANGUAGE_ID_ALL
                copyVocation. UF_LOGO_IMAGE = vocationRealm.UF_LOGO_IMAGE
                copyVocation. UF_MAP_POINT = vocationRealm.UF_MAP_POINT
                copyVocation. UF_PREVIEW_IMAGE = vocationRealm.UF_PREVIEW_IMAGE
                copyVocation. UF_SKILLS_ID_ALL = vocationRealm.UF_SKILLS_ID_ALL
                copyVocation. UF_TYPE_OF_JOB_ID = vocationRealm.UF_TYPE_OF_JOB_ID
                it.insertOrUpdate(copyVocation)


                //val newVocation = vocationRealm.toBasicVersion().toRealmVersion()
                //newVocation.apply {
                //    val now = Calendar.getInstance()

                //    NAME = (NAME ?: "") + "-2"
                //    UF_JOBS_ID = null
                 //   UF_MODIFED = now.timestamp
                //    UF_APP_JOB_ID = getNewJobAppId().toLong()

                //    now.add(Calendar.DATE, Setting.JOB_LIFETIME_IN_DAYS)
                //    UF_DISABLE = now.timestamp
                //}
                //it.copyToRealm(newVocation)
                ApiServiceImpl(this).addMyVocation(
                    setting.getCurrentLanguage().locale,
                    token = setting.token ?: "",
                    login = setting.email ?: "",
                    vocation = copyVocation.toBasicVersion()
                )
                ctx.startActivity<VocationEditActivity> {
                    putExtra("appId", (copyVocation.UF_APP_JOB_ID ?: 0))
                    putExtra("jobId", copyVocation.UF_JOBS_ID ?: 1)
                }
            }
        } catch (e: Exception) {
        }
    }

    override fun onVocationAdded(success: Boolean) = parent.onRefresh()

    private fun getNewJobAppId(): String {
        var random = Random.nextLong(0, 99999999).toString()
        random = "0".repeat(8 - random.length) + random
        return "${Calendar.getInstance().timestamp}$random"
    }

    @SuppressLint("SimpleDateFormat")
    private fun deleteVocation(vocation: Vocation) {
        AlertDialog.Builder(ctx)
            .setTitle(R.string.request_request_delete_title)
            .setMessage(R.string.request_request_delete_message)
            .setPositiveButton(R.string.request_request_delete_ok) { dialog, _ ->

                Realm.getDefaultInstance().executeTransaction {
                    val vocationRealm = it.where<VocationRealm>()
                        .equalTo("UF_APP_JOB_ID", vocation.UF_APP_JOB_ID?.toLong())
                        .findFirst()

                    //Log.e("test list", "${vocationRealm?.UF_APP_JOB_ID} ")

                    try {
                        vocationRealm?.isHided = true
                        vocationRealm?.COMPANY = null
                        vocationRealm?.DETAIL_TEXT = null
                        vocationRealm?.UF_CONTACT_EMAIL = null
                        vocationRealm?.UF_CONTACT_PHONE = null
                        vocationRealm?.UF_DETAIL_IMAGE = null
                        vocationRealm?.UF_DISABLE = null
                        vocationRealm?.UF_GOLD_PER_MONTH = null
                        vocationRealm?.UF_GROSS_CURRENCY_ID = null
                        vocationRealm?.UF_GROSS_PER_MONTH = null
                        vocationRealm?.UF_LOGO_IMAGE = null
                        vocationRealm?.UF_MAP_POINT = null
                        vocationRealm?.UF_PREVIEW_IMAGE = null

                        vocationRealm?.UF_LANGUAGE_ID_ALL = null
                        vocationRealm?.UF_SKILLS_ID_ALL = null
                        vocationRealm?.UF_TYPE_OF_JOB_ID = null

                        vocationRealm?.UF_MODIFED = Calendar.getInstance().timestamp
                            //it.insertOrUpdate(vocationRealm!!)
                    } catch (e: Exception) {
                        //Log.e("check", e.localizedMessage ?: "set null error")
                    }
                    parent.showList()
                    try {
                        ApiServiceImpl(this).deleteVocation(
                            setting.getCurrentLanguage().locale,
                            token = setting.token ?: "",
                            login = setting.email ?: "",
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

    override fun onVocationDeleted(success: Boolean) = parent.showList()

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
        val company = view.company_tv!!
        //for limit icon
        val copyLeft = view.copy12!!
        val copyRight = view.copy22!!

        val errorView = view.error_tv!!
        val isNotActiveView = view.notActive_iv!!
        val disableLabel = view.disable_tv!!

        val leftCorners = view.left_corners!!
        val rightCorners = view.right_corners!!
    }

}