package com.byte4b.judebo.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.byte4b.judebo.*
import com.byte4b.judebo.R
import com.byte4b.judebo.activities.DetailsActivity
import com.byte4b.judebo.adapters.ClusterAdapter
import com.byte4b.judebo.adapters.LanguagesAdapter
import com.byte4b.judebo.adapters.SkillsAdapter
import com.byte4b.judebo.models.AbstractMarker
import com.byte4b.judebo.models.MyMarker
import com.byte4b.judebo.models.currencies
import com.byte4b.judebo.models.languages
import com.byte4b.judebo.services.ApiServiceImpl
import com.byte4b.judebo.utils.OwnIconRendered
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
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.cluster_icon.view.*
import kotlinx.android.synthetic.main.fragment_maps.*
import kotlinx.android.synthetic.main.marker_item.view.*
import kotlinx.android.synthetic.main.preview.*
import kotlinx.android.synthetic.main.preview.view.*
import kotlin.math.abs


class MapsFragment : Fragment(R.layout.fragment_maps), ServiceListener {

    private val setting by lazy { Setting(ctx) }
    private val ctx by lazy { requireActivity() }
    private var map: GoogleMap? = null
    private var markers: List<MyMarker>? = null
    private var clusterManager: ClusterManager<AbstractMarker>? = null

    private fun addMyLocationTarget() {
        if (map != null) {
            val location = ctx.getLocation()
            if (location != null) {
                val me = LatLng(location.latitude, location.longitude)
                map?.addMarker(MarkerOptions().position(me)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.me)))
            } else {
                val me = LatLng(setting.defaultLatitude, setting.defaultLongitude)
                map?.addMarker(MarkerOptions().position(me)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.me)))
            }
        }
    }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        googleMap.uiSettings.isRotateGesturesEnabled = false
        googleMap.setMaxZoomPreference(setting.maxZoom)
        googleMap.setMinZoomPreference(setting.minZoom)

        clusterManager = ClusterManager(activity!!.applicationContext, map)
        clusterManager?.renderer = OwnIconRendered(ctx, map, clusterManager)
        val alg = clusterManager!!.algorithm
        alg.maxDistanceBetweenClusteredItems = (setting.cluster_radius * 2).toInt()
        clusterManager?.algorithm = alg
        clusterManager?.setOnClusterClickListener {
            if (googleMap.cameraPosition.zoom != setting.maxZoom) {
                var zoom = googleMap.cameraPosition.zoom + 1
                if (zoom > setting.maxZoom) zoom = setting.maxZoom
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it.position, zoom))
            } else {
                if (it.items.isNotEmpty()) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(it.position))
                    clusterPreview_cv.visibility = View.VISIBLE
                    clusterContainer_rv.layoutManager = LinearLayoutManager(ctx)
                    clusterContainer_rv.adapter = ClusterAdapter(ctx, it.items.map { it.marker })
                }
            }
            true
        }

        googleMap.setOnMapClickListener { clusterPreview_cv.visibility = View.GONE }
        googleMap.setOnCameraMoveStartedListener { clusterPreview_cv.visibility = View.GONE }

        clusterManager?.markerCollection?.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoContents(marker: Marker) = getPreview(marker)
            override fun getInfoWindow(marker: Marker) = getPreview(marker)
        })

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
                ApiServiceImpl(this).getNearbyMarkers(
                    if (setting.language == "") "en" else setting.language!!,
                    position.latitude + setting.max_search_latitude_size / 2,
                    position.longitude + setting.max_search_longitude_size / 2,
                    position.latitude - setting.max_search_latitude_size / 2,
                    position.longitude - setting.max_search_longitude_size / 2
                )
            }
            true
        }
        googleMap.setOnCameraMoveListener { clusterManager?.cluster() }

        clusterManager?.markerCollection?.setOnInfoWindowClickListener { marker ->
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

    @SuppressLint("SetTextI18n")
    private fun getPreview(marker: Marker): View {
        val view = ctx.layoutInflater.inflate(R.layout.preview, null)
        val data = markers?.first {
            marker.position.latitude == it.UF_MAP_POINT_LATITUDE
                    && marker.position.longitude == it.UF_MAP_POINT_LONGITUDE
        } ?: return view
        try {
            view.title_tv.text = data.NAME
            if (!data.UF_LOGO_IMAGE.isNullOrEmpty()) {
                try {
                    Picasso.get()
                        .load(data.UF_PREVIEW_IMAGE)
                        .placeholder(R.drawable.default_logo_preview)
                        .error(R.drawable.default_logo_preview)
                        .into(view.logo_iv)
                } catch (e: Exception) {}
            }

            val currency = currencies.firstOrNull { it.id == data.UF_GROSS_CURRENCY_ID }

            try {

                if (data.UF_GROSS_PER_MONTH.isEmpty() || data.UF_GROSS_PER_MONTH == "0") {
                    view.secondContainer.visibility = View.GONE
                    view.salaryContainer.visibility = View.GONE
                } else {
                    view.secondContainer.visibility = View.VISIBLE
                    view.salaryContainer.visibility = View.VISIBLE
                }
                if (currency?.name == setting.currency
                    || (setting.currency == "" && currency?.name == "USD")
                ) {
                    view.salary_tv.text = data.UF_GROSS_PER_MONTH.round()
                    view.salaryVal_tv.text = " ${currency?.name ?: ""}"
                    view.salary_tv.setRightDrawable(currency?.icon ?: R.drawable.iusd)
                    view.secondContainer.visibility = View.GONE
                } else {
                    view.salary_tv.text = data.UF_GROSS_PER_MONTH.round()
                    view.salaryVal_tv.text = " ${currency?.name ?: ""}"
                    view.salary_tv.setRightDrawable(currency?.icon ?: R.drawable.iusd)
                    view.secondContainer.visibility = View.VISIBLE
                    val currencyFromSetting =
                        if (setting.currency.isNullOrEmpty()) "USD" else setting.currency!!
                    val currency2 = currencies.firstOrNull { it.name == currencyFromSetting }
                    val convertedSalary =
                        data.UF_GROSS_PER_MONTH.toDouble() * (currency2?.rate
                            ?: 1) / (currency?.rate ?: 1)
                    if (convertedSalary == 0.0)
                        secondContainer.visibility = View.GONE
                    view.secondSalary_tv.text =
                        "≈${convertedSalary.toString().round()}"
                    view.secondSalaryVal_tv.text = currency2?.name ?: "USD"
                    view.secondSalary_tv.setRightDrawable(currency2?.icon ?: R.drawable.iusd)
                }
            } catch (e: Exception) {}
            view.more_tv.text = "#${data.UF_JOBS_ID} ${getString(R.string.button_detail_title)}"
            try {
                view.langs_rv.layoutManager =
                    LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)
                view.langs_rv.adapter = LanguagesAdapter(ctx, data.UF_LANGUAGE_ID_ALL.split(",").map {
                    languages.first { lang -> lang.id == it.toInt() }
                })
            } catch (e:Exception) {}
            view.place_tv.text = data.COMPANY

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
        clusterManager?.clearItems()
        clusterManager?.addItems((list ?: listOf()).map {
            AbstractMarker(it.UF_MAP_POINT_LATITUDE, it.UF_MAP_POINT_LONGITUDE, it)
        })
        clusterManager?.cluster()
        markers = list
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