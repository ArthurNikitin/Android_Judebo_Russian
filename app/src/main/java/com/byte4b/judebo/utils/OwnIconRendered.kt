package com.byte4b.judebo.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.byte4b.judebo.R
import com.byte4b.judebo.models.AbstractMarker
import com.byte4b.judebo.models.MyMarker
import com.byte4b.judebo.models.currencies
import com.byte4b.judebo.round
import com.byte4b.judebo.setRightDrawable
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import kotlinx.android.synthetic.main.cluster_icon.view.*
import kotlinx.android.synthetic.main.marker_item.view.*

class OwnIconRendered(
    val context: Context?, map: GoogleMap?,
    val clusterManager: ClusterManager<AbstractMarker>?
) : DefaultClusterRenderer<AbstractMarker>(context, map, clusterManager) {

    private val drawables = mutableMapOf<String, Drawable>()
    private val setting by lazy { Setting(context!!) }

    override fun onBeforeClusterRendered(
        cluster: Cluster<AbstractMarker>,
        markerOptions: MarkerOptions
    ) {
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getClusterIcon(cluster)))
    }

    private fun getClusterIcon(cluster: Cluster<AbstractMarker>): Bitmap {
        val view = (context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.cluster_icon, null)
        val size =
            when (cluster.items.size) {
                in setting.cluster_sizes[0] -> 15
                in setting.cluster_sizes[1] -> 25
                in setting.cluster_sizes[2] -> 35
                in setting.cluster_sizes[3] -> 45
                in setting.cluster_sizes[4] -> 55
                in setting.cluster_sizes[5] -> 65
                in setting.cluster_sizes[6] -> 75
                in setting.cluster_sizes[7] -> 85
                in setting.cluster_sizes[8] -> 95
                else -> 100
            }
        val params = view.img.layoutParams
        params.height = size
        params.width = size
        view.img.layoutParams = params
        view.size.text = cluster.items.size.toString()
        return view.toBitmap()
    }

    override fun onClusterUpdated(cluster: Cluster<AbstractMarker>, marker: Marker) {
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(getClusterIcon(cluster)))
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
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getMarkerIcon(item)))
        super.onBeforeClusterItemRendered(item, markerOptions)
    }

    @SuppressLint("SetTextI18n")
    private fun getViewWithSalaryStub(view: View, data: MyMarker): View {
        try {
            val currency = currencies.firstOrNull { it.id == data.UF_GROSS_CURRENCY_ID }

            if (data.UF_GROSS_PER_MONTH.isEmpty() || data.UF_GROSS_PER_MONTH == "0") {
                view.secondContainer4.visibility = View.GONE
                view.salaryContainer4.visibility = View.GONE
            } else {
                view.secondContainer4.visibility = View.INVISIBLE
                view.salaryContainer4.visibility = View.INVISIBLE
            }
            if (currency?.name == setting.currency
                || (setting.currency == "" && currency?.name == "USD")
            ) {
                view.salary_tv4.text = data.UF_GROSS_PER_MONTH.round()
                view.salaryVal_tv4.text = " ${currency?.name ?: ""}"
                view.salary_tv4.setRightDrawable(currency?.icon ?: R.drawable.iusd)
                view.secondContainer4.visibility = View.GONE
            } else {
                view.salary_tv4.text = data.UF_GROSS_PER_MONTH.round()
                view.salaryVal_tv4.text = " ${currency?.name ?: ""}"
                view.salary_tv4.setRightDrawable(currency?.icon ?: R.drawable.iusd)
                view.secondContainer4.visibility = View.INVISIBLE
                val currencyFromSetting =
                    if (setting.currency.isNullOrEmpty()) "USD" else setting.currency!!
                val currency2 = currencies.firstOrNull { it.name == currencyFromSetting }
                val convertedSalary =
                    data.UF_GROSS_PER_MONTH.toDouble() * (currency2?.rate
                        ?: 1) / (currency?.rate ?: 1)
                if (convertedSalary == 0.0)
                    view.secondContainer4.visibility = View.GONE
                view.secondSalary_tv4.text =
                    "≈${convertedSalary.toString().round()}"
                view.secondSalaryVal_tv4.text = currency2?.name ?: "USD"
                view.secondSalary_tv4.setRightDrawable(currency2?.icon ?: R.drawable.iusd)
            }
        } catch (e: Exception) {
        }
        return view
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
                view.salary_tv2.setRightDrawable(currency?.icon ?: R.drawable.iusd)
                view.secondContainer2.visibility = View.GONE
            } else {
                view.salary_tv2.text = data.UF_GROSS_PER_MONTH.round()
                view.salaryVal_tv2.text = " ${currency?.name ?: ""}"
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
                    "≈${convertedSalary.toString().round()}"
                view.secondSalaryVal_tv2.text = currency2?.name ?: "USD"
                view.secondSalary_tv2.setRightDrawable(currency2?.icon ?: R.drawable.iusd)
            }
        } catch (e: Exception) {
        }
        return view
    }

    private fun getMarkerIcon(item: AbstractMarker): Bitmap {
        var view = (context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.marker_item, null)
        view.marker_title.text = item.marker.NAME
        view = getViewWithSalaryMath(view, item.marker)
        view = getViewWithSalaryStub(view, item.marker)
        try {
            val logoUrl = item.marker.UF_LOGO_IMAGE
            if (!logoUrl.isNullOrEmpty()) {

                if (drawables.containsKey(logoUrl))
                    view.marker_icon.setImageDrawable(drawables[logoUrl])
                else {
                    Thread {
                        Glide.with(context)
                            .load(logoUrl)
                            .centerInside()
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
        } catch (e: Exception) { }
        return view.toBitmap()
    }

    override fun onClusterItemUpdated(item: AbstractMarker, marker: Marker) {
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerIcon(item)))
        super.onClusterItemUpdated(item, marker)
    }

}