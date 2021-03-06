package com.byte4b.judebo.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Handler
import android.text.Html
import android.util.LayoutDirection
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.byte4b.judebo.*
import com.byte4b.judebo.models.AbstractMarker
import com.byte4b.judebo.models.MyMarker
import com.byte4b.judebo.models.currencies
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import io.realm.Realm
import kotlinx.android.synthetic.main.cluster_icon.view.*
import kotlinx.android.synthetic.main.marker_item.view.*
import kotlinx.android.synthetic.main.marker_without_salary.view.*

class OwnIconRendered(
    val context: Context?, map: GoogleMap?,
    private val clusterManager: ClusterManager<AbstractMarker>?
) : DefaultClusterRenderer<AbstractMarker>(context, map, clusterManager) {

    private val realm by lazy { Realm.getDefaultInstance() }
    val drawables = mutableMapOf<String, Drawable>()
    private val setting by lazy { Setting(context!!) }

    override fun onBeforeClusterRendered(
        cluster: Cluster<AbstractMarker>,
        markerOptions: MarkerOptions
    ) {
        try {
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getClusterIcon(cluster)))
        } catch (e: Exception) {}
    }

    private fun getClusterIcon(cluster: Cluster<AbstractMarker>): Bitmap {
        val view = (context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.cluster_icon, null)
        val size =
            when (cluster.items.size) {
                //размеры кластера здесь
                in Setting.CLUSTER_SIZES[0] -> 40
                in Setting.CLUSTER_SIZES[1] -> 45
                in Setting.CLUSTER_SIZES[2] -> 50
                in Setting.CLUSTER_SIZES[3] -> 55
                in Setting.CLUSTER_SIZES[4] -> 60
                in Setting.CLUSTER_SIZES[5] -> 65
                in Setting.CLUSTER_SIZES[6] -> 70
                in Setting.CLUSTER_SIZES[7] -> 75
                in Setting.CLUSTER_SIZES[8] -> 80
                else -> 85
            }
        val params = view.img.layoutParams
        params.height = size
        params.width = size
        view.img.layoutParams = params
        view.size.text = cluster.items.size.toString()
        return view.toBitmap()[0] as Bitmap
    }

    override fun onClusterUpdated(cluster: Cluster<AbstractMarker>, marker: Marker) {
        try { marker.setIcon(BitmapDescriptorFactory.fromBitmap(getClusterIcon(cluster))) }
        catch (e: Exception) {}
    }

    private fun View.toBitmap(): List<Any> {
        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        measure(measureSpec, measureSpec)
        layout(0, 0, measuredWidth, measuredHeight)
        val r = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        r.eraseColor(Color.TRANSPARENT)
        val canvas = Canvas(r)
        draw(canvas)
        try {
            return listOf(r, width, marker_icon.width, height, marker_icon.height)
        } catch (e: Exception) {
            return  listOf(r, width, width, height, height)
        }
    }

    val handler = Handler {
        clusterManager?.cluster()
        true
    }

    private val isRtlNow by lazy { isRtl(context!!) }

    override fun onBeforeClusterItemRendered(item: AbstractMarker, marker: MarkerOptions) {
        try {
            marker.snippet(item.marker.UF_JOBS_ID.toString())
            val (icon, containerWidth, iconWidth, containerHeight, iconHeight) = getMarkerIcon(item)

            var iconCenterHorizontal = ((((iconWidth as Int) / 2f)) / containerWidth as Int) / 2
            if (isRtlNow) iconCenterHorizontal = 1f - iconCenterHorizontal
            val iconCenterVertical =
                if (item.marker.UF_LOGO_IMAGE.isNullOrEmpty()) 1f
                else 1f - (((iconHeight as Int) / 2f) / containerHeight as Int)

            marker.anchor(iconCenterHorizontal, iconCenterVertical)
            marker.infoWindowAnchor(iconCenterHorizontal, .5f)

            marker.icon(BitmapDescriptorFactory.fromBitmap(icon as Bitmap))
        } catch (e: Exception) {
        }
        super.onBeforeClusterItemRendered(item, marker)
    }

    override fun getMarker(clusterItem: AbstractMarker?): Marker {
        return super.getMarker(clusterItem).apply { tag = clusterItem?.marker?.UF_JOBS_ID }
    }



    val k = 0.7
    val size = (Setting.CURRENCY_ICON_SIZE * k).toInt()

    @SuppressLint("SetTextI18n")
    private fun getViewWithSalaryMath(view: View, data: MyMarker): View {
        try {
            val currency = currencies.firstOrNull { it.id == data.UF_GROSS_CURRENCY_ID }

            if (data.UF_GROSS_PER_MONTH.isEmpty() || data.UF_GROSS_PER_MONTH == "0") {
                view.secondContainer2.visibility = View.GONE
                view.salaryContainer2.visibility = View.GONE
            } else {
                view.secondContainer2.visibility = View.VISIBLE
                view.salaryContainer2.visibility = View.VISIBLE
            }
            if (currency?.name == setting.getCurrentCurrency().name) {
                view.salary_tv2.text = data.UF_GROSS_PER_MONTH.round()
                view.salaryVal_tv2.text = " ${currency.name}"
                if (isRtl(context!!))
                    view.salary_tv2.setLeftDrawableMap(currency.icon, size)
                else
                    view.salary_tv2.setRightDrawableMap(currency.icon, size)
                view.secondContainer2.visibility = View.GONE
            } else {
                view.salary_tv2.text = data.UF_GROSS_PER_MONTH.round().trim()
                view.salaryVal_tv2.text = " ${currency?.name ?: ""}"
                if (isRtl(context!!))
                    view.salary_tv2.setLeftDrawableMap(currency?.icon ?: R.drawable.iusd, size)
                else
                    view.salary_tv2.setRightDrawableMap(currency?.icon ?: R.drawable.iusd, size)

                view.secondContainer2.visibility = View.VISIBLE

                val currency2 = setting.getCurrentCurrency()
                val convertedSalary =
                    (data.UF_GROSS_PER_MONTH.toDouble() * currency2.getLastRate(realm) / (currency?.getLastRate(realm) ?: 1))
                        .toString().round().trim()
                view.secondSalary_tv2.text = "≈${convertedSalary}"
                view.secondContainer2.visibility =
                    if (convertedSalary == "0") View.GONE
                    else View.VISIBLE
                view.secondSalaryVal_tv2.text = currency2.name

                if (isRtl(context))
                    view.secondSalary_tv2.setLeftDrawableMap(currency2.icon, size)
                else
                    view.secondSalary_tv2.setRightDrawableMap(currency2.icon, size)
            }
        } catch (e: Exception) {
        }
        return view
    }

    @SuppressLint("WrongConstant")
    private fun getMarkerIcon(item: AbstractMarker): List<Any> {
        if (!(item.marker.UF_GROSS_PER_MONTH.isEmpty() || item.marker.UF_GROSS_PER_MONTH == "0")) {
            var view =
                (context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                    .inflate(R.layout.marker_item, null)
            view.marker_title.text = Html.fromHtml("<div style='text-shadow: 0px 0px 4px #FFFFFF;'>${item.marker.NAME}</div>")//item.marker.NAME
            view = getViewWithSalaryMath(view, item.marker)

            if (isRtl(context)) {
                view.gravity_container.layoutDirection = LayoutDirection.RTL
                view.marker_title.gravity = Gravity.RIGHT
            }

            try {
                val logoUrl = item.marker.UF_LOGO_IMAGE
                if (!logoUrl.isNullOrEmpty()) {

                    if (drawables.containsKey(logoUrl)) {
                        view.marker_icon.setImageDrawable(drawables[logoUrl])
                    } else {
                        Thread {
                            Glide.with(context)
                                .load(logoUrl)
                                .centerInside()
                                .circleCrop()
                                .placeholder(R.drawable.map_default_marker)
                                .into(object : SimpleTarget<Drawable>() {
                                    override fun onResourceReady(
                                        resource: Drawable,
                                        transition: Transition<in Drawable>?
                                    ) {
                                        drawables[logoUrl] = resource
                                        handler.sendEmptyMessage(0)
                                    }
                                })
                        }.start()
                        clusterManager?.updateItem(item)
                    }
                }
            } catch (e: Exception) {
            }
            return view.toBitmap()
        } else {
            val view =
                (context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                    .inflate(R.layout.marker_without_salary, null)
            view.marker_title_short.text = item.marker.NAME

            if (isRtl(context)) {
                view.marker_icon_short.scaleType = ImageView.ScaleType.FIT_END
                view.marker_title_short.gravity = Gravity.RIGHT
            }
            try {
                val logoUrl = item.marker.UF_LOGO_IMAGE
                if (!logoUrl.isNullOrEmpty()) {

                    if (drawables.containsKey(logoUrl)) {
                        view.marker_icon_short.setImageDrawable(drawables[logoUrl])
                    } else {
                        Thread {
                            Glide.with(context)
                                .load(logoUrl)
                                .centerInside()
                                .circleCrop()
                                .placeholder(R.drawable.map_default_marker)
                                .into(object : SimpleTarget<Drawable>() {
                                    override fun onResourceReady(
                                        resource: Drawable,
                                        transition: Transition<in Drawable>?
                                    ) {
                                        drawables[logoUrl] = resource
                                        handler.sendEmptyMessage(0)
                                    }
                                })
                        }.start()
                        clusterManager?.updateItem(item)
                    }
                }
            } catch (e: Exception) {
            }
            return view.toBitmap()
        }
    }

    override fun onClusterItemUpdated(item: AbstractMarker, marker: Marker) {
        try {
            val (icon, containerWidth, iconWidth, containerHeight, iconHeight) = getMarkerIcon(item)

            var iconCenterHorizontal = ((((iconWidth as Int) / 2f)) / containerWidth as Int) / 2
            if (isRtlNow) iconCenterHorizontal = 1f - iconCenterHorizontal
            val iconCenterVertical =
                if (item.marker.UF_LOGO_IMAGE.isNullOrEmpty()) 1f
                else 1f - (((iconHeight as Int) / 2f) / containerHeight as Int)

//            if (item.marker.UF_GROSS_PER_MONTH.isEmpty()
//                || item.marker.UF_GROSS_PER_MONTH == "0"
//            ) {
//                Log.e("test", "true: ${item.marker.UF_JOBS_ID}")
//                marker.setAnchor(iconCenterHorizontal, iconCenterVertical)
//                marker.setInfoWindowAnchor(000.5f, .5f)
//            } else {
                marker.setAnchor(iconCenterHorizontal, iconCenterVertical)
                marker.setInfoWindowAnchor(iconCenterHorizontal, .5f)
//            }
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon as Bitmap))
        } catch (e: Exception) {}
        super.onClusterItemUpdated(item, marker)
    }

}