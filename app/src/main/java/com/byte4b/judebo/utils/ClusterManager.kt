package com.byte4b.judebo.utils

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
import com.byte4b.judebo.fragments.AbstractMarker
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
        view.img.setImageResource(
            when (cluster.items.size) {
                in setting.cluster_sizes[0] -> R.drawable.cluster_01
                in setting.cluster_sizes[1] -> R.drawable.cluster_02
                in setting.cluster_sizes[2] -> R.drawable.cluster_03
                in setting.cluster_sizes[3] -> R.drawable.cluster_04
                in setting.cluster_sizes[4] -> R.drawable.cluster_05
                in setting.cluster_sizes[5] -> R.drawable.cluster_06
                in setting.cluster_sizes[6] -> R.drawable.cluster_07
                in setting.cluster_sizes[7] -> R.drawable.cluster_08
                in setting.cluster_sizes[8] -> R.drawable.cluster_09
                else -> R.drawable.cluster_10
            }
        )
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

    private fun getMarkerIcon(item: AbstractMarker): Bitmap {
        val view = (context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.marker_item, null)
        view.marker_title.text = item.marker.NAME

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