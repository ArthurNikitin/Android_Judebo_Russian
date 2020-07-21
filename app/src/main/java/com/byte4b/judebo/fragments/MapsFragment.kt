package com.byte4b.judebo.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.fragment.app.Fragment
import com.byte4b.judebo.R
import com.byte4b.judebo.getLocation
import com.byte4b.judebo.utils.Setting
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import kotlinx.android.synthetic.main.fragment_maps.*
import kotlin.math.abs


class MapsFragment : Fragment(R.layout.fragment_maps) {

    private val setting by lazy { Setting(ctx) }
    private val ctx by lazy { requireActivity() }
    private var map: GoogleMap? = null

    private fun addMyLocationTarget() {
        if (map != null) {
            val location = ctx.getLocation()
            if (location != null) {
                val me = LatLng(location.latitude, location.longitude)
                map?.addMarker(MarkerOptions().position(me)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.green_marker)))
            } else {
                val me = LatLng(setting.defaultLatitude, setting.defaultLongitude)
                map?.addMarker(MarkerOptions().position(me)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.green_marker)))
            }

        }
    }

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

        addMyLocationTarget()

        var lastPolygonLatitude = 0.0
        var lastPolygonLongitude = 0.0
        val handler = Handler {
            //draw here
            val position = googleMap.cameraPosition.target
            if (position != null
                && (abs(lastPolygonLatitude - position.latitude) > setting.search_request_min_move_delta
                        || abs(lastPolygonLongitude - position.longitude) > setting.search_request_min_move_delta)) {
                val polygon = PolygonOptions().add(
                    LatLng(position.latitude + setting.max_search_latitude_size / 2,
                        position.longitude + setting.max_search_longitude_size / 2),
                    LatLng(position.latitude - setting.max_search_latitude_size / 2,
                        position.longitude + setting.max_search_longitude_size / 2),
                    LatLng(position.latitude - setting.max_search_latitude_size / 2,
                        position.longitude - setting.max_search_longitude_size / 2),
                    LatLng(position.latitude + setting.max_search_latitude_size / 2,
                        position.longitude - setting.max_search_longitude_size / 2),
                    LatLng(position.latitude + setting.max_search_latitude_size / 2,
                        position.longitude + setting.max_search_longitude_size / 2)
                ).strokeColor(ctx.resources.getColor(R.color.search_polygon_square))
                googleMap.clear()
                addMyLocationTarget()
                googleMap.addPolygon(polygon)
                googleMap.cameraPosition.target.apply {
                    lastPolygonLatitude = latitude
                    lastPolygonLongitude = longitude
                }
            }
            true
        }
        Thread {
            while (true) {
                if (isResumed)
                    handler.sendEmptyMessage(0)
                Thread.sleep(setting.search_request_pause * 1000L)
            }
        }.start()
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
                    val location = ctx.getLocation()
                    if (location != null) {
                        map?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(location.latitude, location.longitude),
                                setting.basicZoom
                            )
                        )
                        addMyLocationTarget()
                    } else {
                        map?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(setting.defaultLatitude, setting.defaultLongitude),
                                setting.basicZoom
                            )
                        )
                        addMyLocationTarget()
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