package com.byte4b.judebo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.util.Base64.decode
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.byte4b.judebo.*
import com.byte4b.judebo.activities.MainActivity
import com.byte4b.judebo.activities.VocationEditActivity
import com.byte4b.judebo.fragments.CreatorFragment
import com.byte4b.judebo.models.Vocation
import com.byte4b.judebo.models.VocationRealm
import com.byte4b.judebo.models.currencies
import com.byte4b.judebo.services.ApiServiceImpl
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.view.ServiceListener
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

    private val isRtlConfig by lazy { setting.isLocaleSettingRtl }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(LayoutInflater.from(ctx).inflate(if (!isRtlConfig) R.layout.item_vocation else R.layout.item_vocation_rtl, parent, false))

    @SuppressLint("SetTextI18n", "SimpleDateFormat", "ClickableViewAccessibility")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        try {
            if (isMaxVocations) {
                holder.copyLeft.setImageResource(R.drawable.jobs_list_copy_deny)
                holder.copyRight.setImageResource(R.drawable.jobs_list_copy_deny)
            }

            with(vocations[position]) {

                holder.copy1.setOnClickListener { if (!isMaxVocations) copyVocations(this) }
                holder.copy2.setOnClickListener { if (!isMaxVocations) copyVocations(this) }

                holder.del1.setOnClickListener { deleteVocation(this) }
                holder.del2.setOnClickListener { deleteVocation(this) }

                holder.main.setOnClickListener {
                    if (holder.swiper.isClosed) {
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

                val isCanShowOnMap = (UF_JOBS_ID != null && UF_MAP_POINT != null && UF_ACTIVE != 0.toByte())
                resetIcons(holder, isCanShowOnMap)
                with(holder.swiper) {
                    close(false)
                    setOnTouchListener { _, event ->
                        if (lastSwipedPosition != position && lastSwipedPosition != -1)
                            notifyItemChanged(lastSwipedPosition)
                        false
                    }
                    var opened = false
                    openRight(false)
                    setOnActionsListener(object : com.zerobranch.layout.SwipeLayout.SwipeActionsListener {
                        override fun onOpen(direction: Int, isContinuous: Boolean) {
                            //opened = if (!opened) {
//                                if (direction == 1) {
//
//                                    Handler().postDelayed({
//                                        holder.hideLeftIcons()
//                                        setSeeParams(holder, isCanShowOnMap)
//                                    }, 210)
//                                } else if (direction == 2) {
//                                    Handler().postDelayed({
//                                        holder.hideRightIcons()
//                                        setSeeParams(holder, isCanShowOnMap)
//                                        openRightCompletely(false)
//                                    }, 210)
//                                }
//                                true
//                            } else {
//                                false
//                            }
                            lastSwipedPosition = position
                        }
                        override fun onClose() {
//                            if (opened)
//                                notifyItemChanged(position)
                        }
                    })
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

                    if (isRtl(ctx)) {
                        holder.leftCorners.setImageResource(R.drawable.corners_right_red)
                        holder.rightCorners.setImageResource(R.drawable.corners_left_red)
                    } else {
                        holder.leftCorners.setImageResource(R.drawable.corners_left_red)
                        holder.rightCorners.setImageResource(R.drawable.corners_right_red)
                    }
                    holder.cont.setBackgroundResource(R.color.jobs_list_not_active_background)
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

                    if (isRtl(ctx)) {
                        holder.leftCorners.setImageResource(R.drawable.corners_right)
                        holder.rightCorners.setImageResource(R.drawable.corners_left)
                    } else {
                        holder.rightCorners.setImageResource(R.drawable.corners_right)
                        holder.leftCorners.setImageResource(R.drawable.corners_left)
                    }

                    holder.cont.setBackgroundResource(R.color.white)
                    holder.errorView.visibility = View.INVISIBLE
                    try {
                        holder.seeLeft.setOnClickListener { open(vocations[position]) }
                        holder.seeRight.setOnClickListener { open(vocations[position]) }
                    } catch (e: Exception) {}
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

    private fun setSeeParams(holder: Holder, isCanShowOnMap: Boolean) {
        if (holder.seeLeft.isVisible) holder.seeLeft.visibility = if (!isCanShowOnMap) View.INVISIBLE else View.VISIBLE
        if (holder.seeRight.isVisible) holder.seeRight.visibility = if (!isCanShowOnMap) View.INVISIBLE else View.VISIBLE
        if (holder.lastDiv.isVisible) holder.lastDiv.visibility = if (!isCanShowOnMap) View.INVISIBLE else View.VISIBLE
        if (holder.firstDiv.isVisible) holder.firstDiv.visibility = if (!isCanShowOnMap) View.INVISIBLE else View.VISIBLE
    }



    private fun resetIcons(holder: Holder, isCanShowOnMap: Boolean) {
        holder.seeLeft.visibility = if (!isCanShowOnMap) View.INVISIBLE else View.VISIBLE
        holder.seeRight.visibility = if (!isCanShowOnMap) View.INVISIBLE else View.VISIBLE
        holder.lastDiv.visibility = if (!isCanShowOnMap) View.INVISIBLE else View.VISIBLE
        holder.firstDiv.visibility = if (!isCanShowOnMap) View.INVISIBLE else View.VISIBLE
    }

    private fun open(vocation: Vocation) {
        try {
            Log.e("test1", "from adapter: id=${vocation.UF_JOBS_ID} map=${vocation.UF_MAP_POINT}")
            if (vocation.UF_JOBS_ID != null) {
                val (lat, lon) = vocation.UF_MAP_POINT!!.split(",").map { it.trim().toDouble() }
                Log.e("test1", "problem with get activity")
                (parent.requireActivity() as MainActivity).openVocationOnMap(
                    vocation.UF_JOBS_ID!!.toInt(),
                    lat, lon
                )
            }
        } catch(e: Exception) {
            Log.e("test1", "adapter error: ${e.localizedMessage}")
        }
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
                copyVocation.DETAIL_TEXT = vocationRealm.DETAIL_TEXT
                copyVocation.UF_CONTACT_EMAIL = vocationRealm.UF_CONTACT_EMAIL
                copyVocation.UF_CONTACT_PHONE = vocationRealm.UF_CONTACT_PHONE
                copyVocation.UF_GOLD_PER_MONTH = vocationRealm.UF_GOLD_PER_MONTH
                copyVocation.UF_GROSS_CURRENCY_ID = vocationRealm.UF_GROSS_CURRENCY_ID
                copyVocation.UF_GROSS_PER_MONTH = vocationRealm.UF_GROSS_PER_MONTH
                copyVocation.UF_LANGUAGE_ID_ALL = vocationRealm.UF_LANGUAGE_ID_ALL
                copyVocation.UF_MAP_POINT = vocationRealm.UF_MAP_POINT
                copyVocation.UF_SKILLS_ID_ALL = vocationRealm.UF_SKILLS_ID_ALL
                copyVocation.UF_TYPE_OF_JOB_ID = vocationRealm.UF_TYPE_OF_JOB_ID


                copyVocation.UF_LOGO_IMAGE = vocationRealm.UF_LOGO_IMAGE
                copyVocation.UF_PREVIEW_IMAGE = vocationRealm.UF_PREVIEW_IMAGE
                copyVocation.UF_DETAIL_IMAGE = vocationRealm.UF_DETAIL_IMAGE

                check {
                    val iv = ImageView(ctx)

                    val errorHandler = Handler { _ ->
                        Handler().postDelayed({
                            Realm.getDefaultInstance().executeTransaction {
                                it.insertOrUpdate(copyVocation);
                            }
                        }, 100)
                        addCopyVocation(copyVocation)
                        true
                    }

                    val successHandler = Handler { _ ->
                        val resource = iv.drawable
                        Log.e("test", "ready")

                        val bitmap = resource?.toBitmap(
                            Setting.MAX_IMG_CROP_HEIGHT,
                            Setting.MAX_IMG_CROP_HEIGHT
                        ) ?: return@Handler  true

                        copyVocation.UF_DETAIL_IMAGE = toBase64(bitmap)
                        val previewBitmap = resource.toBitmap(
                            Setting.MAX_IMG_CROP_HEIGHT_PREVIEW,
                            Setting.MAX_IMG_CROP_HEIGHT_PREVIEW
                        );

                        copyVocation.UF_PREVIEW_IMAGE = toBase64(previewBitmap)

                        val logoBitmap = resource.toBitmap(
                            Setting.MAX_IMG_CROP_HEIGHT_LOGO,
                            Setting.MAX_IMG_CROP_HEIGHT_LOGO
                        );

                        copyVocation.UF_LOGO_IMAGE = toBase64(logoBitmap)
                        Log.e("test", "base 64 ready")

                        check {
                            Handler().postDelayed({
                                Realm.getDefaultInstance().executeTransaction {
                                    it.insertOrUpdate(copyVocation);
                                }
                            }, 100)
                            addCopyVocation(vocationRealm)
                        }

                        Log.e("test", "finla")
                        true;
                    };

                    if (!vocationRealm.UF_DETAIL_IMAGE.isNullOrEmpty()) {
                        Log.e("test", "start load")
                        Glide.with(ctx)
                            .load(vocationRealm.UF_DETAIL_IMAGE)
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    //errorHandler.sendEmptyMessage(0)
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    Log.e("test", "first lvl")
                                    iv.setImageDrawable(resource)
                                    check { successHandler.sendEmptyMessage(0) }
                                    return false
                                }
                            })
                            .submit()//into(ImageView(ctx))
                    } else {
                        it.insertOrUpdate(copyVocation)
                        addCopyVocation(copyVocation)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("test", e.localizedMessage ?: "copy error")
        }
    }

    private fun addCopyVocation(copyVocation: VocationRealm) {
        check {
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
    }

    private fun check(tag: String = "", action: () -> Unit) {
        try {
            action()
        } catch (e: Exception) {
            Log.e("test$tag", e.localizedMessage ?: tag)
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
        val cont = view.cont!!
        val copy1 = view.copy1!!
        val copy2 = view.copy2!!
        val del1 = view.delete1!!
        val del2 = view.delete2!!
        val company = view.company_tv!!
        //for limit icon
        val copyLeft = view.copy1!!
        val copyRight = view.copy2!!

        val errorView = view.error_tv!!
        val isNotActiveView = view.notActive_iv!!
        val disableLabel = view.disable_tv!!

        val leftCorners = view.left_corners!!
        val rightCorners = view.right_corners!!

        val seeLeft = view.see1!!
        val seeRight = view.see2!!

        val firstDiv = view.first_div!!
        val lastDiv = view.last_div!!

        fun hideRightIcons() {
            seeLeft.visibility = View.VISIBLE
            seeRight.visibility = View.GONE
            lastDiv.visibility = View.GONE
           firstDiv.visibility = View.VISIBLE
        }

        fun hideLeftIcons() {
            seeLeft.visibility = View.GONE
            seeRight.visibility = View.VISIBLE
            lastDiv.visibility = View.VISIBLE
            firstDiv.visibility = View.GONE
        }
    }

}