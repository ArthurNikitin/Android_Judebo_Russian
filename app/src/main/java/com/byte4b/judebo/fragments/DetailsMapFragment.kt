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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_maps.*


class DetailsMapFragment(
    private val marker: MyMarker,
    private val isEdit: Boolean = false
) : Fragment(R.layout.fragment_maps) {

    private var map: GoogleMap? = null
    var latLng: LatLng? = null

    private fun setDraggableMarker(
        lat: Double = marker.UF_MAP_POINT_LATITUDE,
        lon: Double = marker.UF_MAP_POINT_LONGITUDE
    ) {
        val target = MarkerOptions().position(LatLng(lat, lon))

        val icon =
            requireContext().resources.getDrawable(R.drawable.map_default_marker)
                .toBitmap(100, 100)
        target.icon(BitmapDescriptorFactory.fromBitmap(icon))

        target.draggable(true)

        map?.clear()
        map?.addMarker(target)
        latLng = target.position

        map?.uiSettings?.isMapToolbarEnabled = true
        map?.uiSettings?.isZoomGesturesEnabled = true
        map?.uiSettings?.isTiltGesturesEnabled = true
        map?.uiSettings?.isZoomControlsEnabled = true
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(target.position, Setting.DEFAULT_EDIT_PAGE_ZOOM))
    }

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
        latLng = target.position
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

        if (!isEdit) {
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
        } else {
            googleMap.setOnMapClickListener {
                setDraggableMarker(it.latitude, it.longitude)
            }
            setDraggableMarker()
        }
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