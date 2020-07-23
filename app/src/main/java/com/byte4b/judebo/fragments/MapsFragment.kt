package com.byte4b.judebo.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.byte4b.judebo.R
import com.byte4b.judebo.activities.DetailsActivity
import com.byte4b.judebo.adapters.SkillsAdapter
import com.byte4b.judebo.getLocation
import com.byte4b.judebo.models.MyMarker
import com.byte4b.judebo.models.currencies
import com.byte4b.judebo.models.languages
import com.byte4b.judebo.services.ApiServiceImpl
import com.byte4b.judebo.startActivity
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.view.ServiceListener
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.flexbox.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_maps.*
import kotlinx.android.synthetic.main.preview.view.*
import kotlin.math.abs


class MapsFragment : Fragment(R.layout.fragment_maps), ServiceListener {

    private val setting by lazy { Setting(ctx) }
    private val ctx by lazy { requireActivity() }
    private var map: GoogleMap? = null
    private var markers: List<MyMarker>? = null

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

        googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoContents(marker: Marker) = getPreview(marker)
            override fun getInfoWindow(marker: Marker) = getPreview(marker)
        })

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
                ApiServiceImpl(this).getNearbyMarkers(
                    if (setting.language == "") "en" else setting.language!!,
                    position.latitude - setting.max_search_latitude_size / 2,
                    position.longitude - setting.max_search_longitude_size / 2,
                    position.latitude + setting.max_search_latitude_size / 2,
                    position.longitude + setting.max_search_longitude_size / 2
                )
            }
            true
        }
        googleMap.setOnInfoWindowClickListener {marker ->
            val data = markers?.first {
                marker.position.latitude == it.UF_MAP_POINT_LATITUDE
                        && marker.position.longitude == it.UF_MAP_POINT_LONGITUDE
            } ?: return@setOnInfoWindowClickListener
            ctx.startActivity<DetailsActivity> { putExtra("marker", Gson().toJson(data)) }
        }
        Thread {
            while (true) {
                if (isResumed)
                    handler.sendEmptyMessage(0)
                Thread.sleep(setting.search_request_pause * 1000L)
            }
        }.start()
    }

    private fun getPreview(marker: Marker): View {
        val view = ctx.layoutInflater.inflate(R.layout.preview, null)
        val data = markers?.first {
            marker.position.latitude == it.UF_MAP_POINT_LATITUDE
                    && marker.position.longitude == it.UF_MAP_POINT_LONGITUDE
        } ?: return view
        try {
            view.title_tv.text = data.NAME
            if (data.UF_LOGO_IMAGE.isNotEmpty()) {
                Picasso.get()
                    .load(data.UF_PREVIEW_IMAGE)
                    .placeholder(R.drawable.big_logo_setting)
                    .error(R.drawable.big_logo_setting)
                    .into(view.logo_iv)
            }

            val currency = currencies.firstOrNull { it.id == data.UF_GROSS_CURRENCY_ID }
            val lang = languages.firstOrNull { currency?.name == it.currency }

            if (lang?.locale == setting.language) {
                view.salary_tv.text = data.UF_GROSS_PER_MONTH
            } else {
                view.salary_tv.text =
                    "${data.UF_GROSS_PER_MONTH} (${data.UF_GROSS_PER_MONTH.toDouble() / (currency?.rate ?: 1)} ${currency?.name ?: "USD"})"
            }
            view.place_tv.text = data.COMPANY

            if (currency != null) {
                Picasso.get()
                    .load(lang?.flag ?: R.drawable.en)
                    .placeholder(R.drawable.en)
                    .error(R.drawable.en)
                    .into(view.currency_iv)
            }

            view.currencyTitle_tv.text = lang?.locale?.toUpperCase() ?: "EN"

            val layoutManager = FlexboxLayoutManager(ctx)
            layoutManager.flexWrap = FlexWrap.WRAP
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.justifyContent = JustifyContent.FLEX_START
            layoutManager.alignItems = AlignItems.FLEX_START

            view.filters_tv.layoutManager = layoutManager
            if (data.UF_SKILLS_ID_ALL == "") {
                view.filters_tv.visibility = View.GONE
            } else {
                view.filters_tv.visibility = View.VISIBLE
                view.filters_tv.adapter = SkillsAdapter(ctx, data.ALL_SKILLS_NAME.split(","))
            }
        } catch (e: Exception) {
            Log.e("test", e.localizedMessage?: "error")
        }
        return view
    }

    override fun onNearbyMarkersLoaded(list: List<MyMarker>?) {
        list?.apply {
            markers = list
            forEach {
                map?.addMarker(MarkerOptions()
                    .position(LatLng(it.UF_MAP_POINT_LATITUDE, it.UF_MAP_POINT_LONGITUDE))
                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.green_marker))
                )
            }
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
                    val location = ctx.getLocation()
                    if (location != null) {
                        map?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(55.5, 37.3),//stub LatLng(location.latitude, location.longitude),
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