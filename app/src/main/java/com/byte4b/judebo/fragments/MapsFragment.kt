package com.byte4b.judebo.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.byte4b.judebo.R
import com.byte4b.judebo.isHavePermission
import com.byte4b.judebo.utils.Setting
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_maps.*
import kotlin.math.abs


class MapsFragment : Fragment(R.layout.fragment_maps) {

    private val setting by lazy { Setting(ctx) }
    private val ctx by lazy { requireActivity() }
    private var map: GoogleMap? = null

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        googleMap.uiSettings.isRotateGesturesEnabled = false
        googleMap.setMaxZoomPreference(setting.maxZoom)
        googleMap.setMinZoomPreference(setting.minZoom)

        if (setting.lastMapCameraPosition.latitude != 0.0) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                setting.lastMapCameraPosition, setting.basicZoom
            ))
        }

        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (location != null) {
            val me = LatLng(location.latitude, location.longitude)
            map!!.addMarker(
                MarkerOptions().position(me)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.green_marker))
            )
        }
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        askPermission(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION) {
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync(callback)

            myGeo_iv.setOnClickListener {
                if (map != null) {
                    val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (location != null) {
                        map?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(location.latitude, location.longitude),
                                setting.basicZoom
                            )
                        )
                        val me = LatLng(location.latitude, location.longitude)
                        map!!.addMarker(MarkerOptions().position(me)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.green_marker)))
                    } else {
                        map?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(setting.defaultLatitude, setting.defaultLongitude),
                                setting.basicZoom
                            )
                        )
                        val me = LatLng(setting.defaultLatitude, setting.defaultLongitude)
                        map!!.addMarker(MarkerOptions().position(me)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.green_marker)))
                    }

                }
            }
        }.onDeclined {
            if (it.hasDenied())
                it.askAgain()
            if (it.hasForeverDenied())
                it.goToSettings()
        }
    }

    override fun onStop() {
        map?.cameraPosition?.apply {
            setting.lastMapCameraPosition = target
        }
        super.onStop()
    }

}