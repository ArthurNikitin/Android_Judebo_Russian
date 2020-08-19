package com.byte4b.judebo.fragments

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.byte4b.judebo.R
import com.byte4b.judebo.models.MyMarker
import com.byte4b.judebo.utils.Setting
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_maps.*


class DetailsMapFragment(val marker: MyMarker) : Fragment(R.layout.fragment_maps) {

    private var map: GoogleMap? = null

    private fun setMarker(drawable: Drawable? = null) {
        val target = MarkerOptions()
            .position(LatLng(marker.UF_MAP_POINT_LATITUDE, marker.UF_MAP_POINT_LONGITUDE))

        val icon =
            requireContext().resources.getDrawable(R.drawable.map_default_marker)
                .toBitmap(100, 100)

        if (drawable != null) {
            target.icon(BitmapDescriptorFactory.fromBitmap(drawable.toBitmap(100, 100)))
            target.anchor(.5f, .5f)
        } else {
            target.icon(BitmapDescriptorFactory.fromBitmap(icon))
        }

        map?.clear()

        map?.addMarker(target)
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(target.position, Setting.BASIC_ZOOM))
    }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        googleMap.uiSettings.isRotateGesturesEnabled = false
        googleMap.setMaxZoomPreference(Setting.DETAILS_MAX_ZOOM)
        googleMap.setMinZoomPreference(Setting.DETAILS_MIN_ZOOM)
        googleMap.isTrafficEnabled = false
        googleMap.isBuildingsEnabled = true
        googleMap.isIndoorEnabled = false
        googleMap.uiSettings.isScrollGesturesEnabled = false
        googleMap.uiSettings.isCompassEnabled = false
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isZoomGesturesEnabled = false
        googleMap.uiSettings.isTiltGesturesEnabled = false
        googleMap.uiSettings.isMapToolbarEnabled = false

        setMarker()
        Glide.with(requireContext())
            .load(marker.UF_LOGO_IMAGE)
            .centerInside()
            .circleCrop()
            .placeholder(R.drawable.map_default_marker)
            .into(object : SimpleTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    setMarker(resource)
                }
            })
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        imageView6.visibility = View.GONE
        myGeo_iv.visibility = View.GONE
    }

}