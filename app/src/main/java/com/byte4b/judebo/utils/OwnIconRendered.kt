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
import android.util.Log
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
import com.google.gson.Gson
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import kotlinx.android.synthetic.main.cluster_icon.view.*
import kotlinx.android.synthetic.main.marker_item.view.*
import kotlinx.android.synthetic.main.marker_without_salary.view.*


class OwnIconRendered(
    val context: Context?, map: GoogleMap?,
    private val clusterManager: ClusterManager<AbstractMarker>?
) : DefaultClusterRenderer<AbstractMarker>(context, map, clusterManager) {

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
        return view.toBitmap()
    }

    override fun onClusterUpdated(cluster: Cluster<AbstractMarker>, marker: Marker) {
        try { marker.setIcon(BitmapDescriptorFactory.fromBitmap(getClusterIcon(cluster))) }
        catch (e: Exception) {}
    }

    private fun View.toBitmap(): Bitmap {
        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        measure(measureSpec, measureSpec)
        layout(0, 0, measuredWidth, measuredHeight)
        val r = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        r.eraseColor(Color.TRANSPARENT)
        val canvas = Canvas(r)
        draw(canvas)
        return r
    }

    val handler = Handler {
        clusterManager?.cluster()
        true
    }

    override fun onBeforeClusterItemRendered(item: AbstractMarker, markerOptions: MarkerOptions) {
        try {
            if (item.marker.UF_GROSS_PER_MONTH.isEmpty()
                || item.marker.UF_GROSS_PER_MONTH == "0"
            ) {
                markerOptions.anchor(.5f, 1f)
                markerOptions.infoWindowAnchor(000.5f, .5f)
            } else {
                markerOptions.anchor(0f, .5f)
                markerOptions.infoWindowAnchor(000.1f, .5f)
            }
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getMarkerIcon(item)))
        } catch (e: Exception) {
            Log.e("debug", Gson().toJson(item))
        }
        super.onBeforeClusterItemRendered(item, markerOptions)
    }

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
            if (currency?.name == setting.currency
                || (setting.currency == "" && currency?.name == "USD")
            ) {
                view.salary_tv2.text = data.UF_GROSS_PER_MONTH.round()
                view.salaryVal_tv2.text = " ${currency?.name ?: ""}"
                if (isRtl(context!!))
                    view.salary_tv2.setLeftDrawable(currency?.icon ?: R.drawable.iusd)
                else
                    view.salary_tv2.setRightDrawable(currency?.icon ?: R.drawable.iusd)
                view.secondContainer2.visibility = View.GONE
            } else {
                view.salary_tv2.text = data.UF_GROSS_PER_MONTH.round()
                view.salaryVal_tv2.text = " ${currency?.name ?: ""}"
                if (isRtl(context!!))
                    view.salary_tv2.setLeftDrawable(currency?.icon ?: R.drawable.iusd)
                else
                    view.salary_tv2.setRightDrawable(currency?.icon ?: R.drawable.iusd)

                view.secondContainer2.visibility = View.VISIBLE

                val currencyFromSetting =
                    if (setting.currency.isNullOrEmpty()) "USD" else setting.currency!!
                val currency2 = currencies.firstOrNull { it.name == currencyFromSetting }
                val convertedSalary =
                    data.UF_GROSS_PER_MONTH.toDouble() * (currency2?.rate
                        ?: 1) / (currency?.rate ?: 1)
                if (convertedSalary == 0.0)
                    view.secondContainer2.visibility = View.GONE
                view.secondSalary_tv2.text =
                    if (isRtl(context!!)) "${convertedSalary.toString().round()}≈"
                    else "≈${convertedSalary.toString().round()}"
                view.secondSalaryVal_tv2.text = currency2?.name ?: "USD"

                if (isRtl(context))
                    view.secondSalary_tv2.setLeftDrawable(currency2?.icon ?: R.drawable.iusd)
                else
                    view.secondSalary_tv2.setRightDrawable(currency2?.icon ?: R.drawable.iusd)
            }
        } catch (e: Exception) {
        }
        return view
    }

    @SuppressLint("WrongConstant")
    private fun getMarkerIcon(item: AbstractMarker): Bitmap {
        if (!(item.marker.UF_GROSS_PER_MONTH.isEmpty() || item.marker.UF_GROSS_PER_MONTH == "0")) {
            var view =
                (context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                    .inflate(R.layout.marker_item, null)
            view.marker_title.text = item.marker.NAME
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
            if (item.marker.UF_GROSS_PER_MONTH.isEmpty()
                || item.marker.UF_GROSS_PER_MONTH == "0"
            ) {
                marker.setAnchor(.5f, 1f)
                marker.setInfoWindowAnchor(000.5f, .5f)
            } else {
                marker.setAnchor(0f, .5f)
                marker.setInfoWindowAnchor(000.1f, .5f)
            }
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerIcon(item)))
        } catch (e: Exception) {
            Log.e("debug", Gson().toJson(item))
        }
        super.onClusterItemUpdated(item, marker)
    }

}