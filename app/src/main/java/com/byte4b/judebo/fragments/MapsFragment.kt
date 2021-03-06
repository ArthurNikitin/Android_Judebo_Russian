package com.byte4b.judebo.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.byte4b.judebo.*
import com.byte4b.judebo.R
import com.byte4b.judebo.activities.DetailsActivity
import com.byte4b.judebo.activities.filter.FilterActivity
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
import com.byte4b.judebo.utils.getLastRate
import com.byte4b.judebo.view.ServiceListener
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.flexbox.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.maps.android.clustering.ClusterManager
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_maps.*
import kotlinx.android.synthetic.main.preview.view.*
import java.util.*
import kotlin.math.pow


class MapsFragment : Fragment(R.layout.fragment_maps), ServiceListener {

    companion object {
        const val REQUEST_FILTER = 107
    }

    private val setting by lazy { Setting(ctx) }
    private val ctx by lazy { requireActivity() }
    var map: GoogleMap? = null
    private var markers: List<MyMarker>? = null
    private var clusterManager: ClusterManager<AbstractMarker>? = null
    private var renderer: OwnIconRendered? = null
    private var isFromSetting = false
    private var isMustBeSetLocation = false

    var showJobId: Int? = null
    var positionShow: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(if (!setting.isLocaleSettingRtl) R.layout.fragment_maps else R.layout.fragment_maps_rtl, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val locale = Locale(setting.getCurrentLanguage().locale)
        requireActivity().baseContext.resources.apply {
            Locale.setDefault(locale)
            val config = configuration
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            updateConfiguration(config, displayMetrics)
        }
        super.onCreate(savedInstanceState)
    }


    private var locationFromUrl: LatLng? = null
    private var jobIdFromUrl: Int? = null
    fun showVocationFromUrl(jobId: Int, latLng: LatLng) {
        Log.e("test1", "showVocationFromUrl id=$jobId loc=$latLng")
        try {
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, Setting.MAX_ZOOM))
            jobIdFromUrl = jobId
            locationFromUrl = latLng
        } catch (e: Exception) {
            Log.e("test1", "show error: ${e.localizedMessage}")
        }
    }

    private fun addMyLocationTarget() {
        if (map != null) {
            val location = ctx.getLocation()
            if (location != null) {
                val me = LatLng(location.latitude, location.longitude)
                map?.addMarker(
                    MarkerOptions().position(me)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.me))
                )
            } else {
                val me = LatLng(Setting.DEFAULT_LATITUDE, Setting.DEFAULT_LONGITUDE)
                map?.addMarker(
                    MarkerOptions().position(me)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.me))
                )
            }
        }
    }

    private fun showMe() {
        if (map != null) {
            val location = ctx.getLocation()
            Log.e("test", "showMe: "+(location == null).toString())
            if (location != null) {
                Log.e("test", "showMe location: ${location.latitude}, ${location.longitude}")
                map?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude, location.longitude),
                        Setting.BASIC_ZOOM
                    )
                )
                addMyLocationTarget()
            } else {
                map?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(Setting.DEFAULT_LATITUDE, Setting.DEFAULT_LONGITUDE),
                        Setting.BASIC_ZOOM
                    )
                )
                addMyLocationTarget()
            }
        } else
            Log.e("test", "showMe: map is null")
    }

    //private var clusterManager: ClusterManager? = null
    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        showMe()
        Handler().postDelayed({showMe()}, 600)
        if (isMustBeSetLocation) {
            //showMe()
            isMustBeSetLocation = false
        }

        googleMap.uiSettings.isRotateGesturesEnabled = false
        googleMap.setMaxZoomPreference(Setting.MAX_ZOOM)
        googleMap.setMinZoomPreference(Setting.MIN_ZOOM)
        googleMap.isTrafficEnabled = false
        googleMap.isBuildingsEnabled = true
        googleMap.isIndoorEnabled = false

        clusterManager = ClusterManager(requireActivity().applicationContext, map)
        renderer = OwnIconRendered(ctx, map, clusterManager)
        clusterManager?.renderer = renderer
        val alg = clusterManager!!.algorithm
        alg.maxDistanceBetweenClusteredItems = Setting.CLUSTER_RADIUS.toInt()
        clusterManager?.algorithm = alg
        clusterManager?.setOnClusterClickListener {
            if (googleMap.cameraPosition.zoom != Setting.MAX_ZOOM) {
                var zoom = googleMap.cameraPosition.zoom + 1
                if (zoom > Setting.MAX_ZOOM) zoom = Setting.MAX_ZOOM
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
        clusterManager?.setOnClusterItemClickListener {
            Handler().postDelayed({
                if (googleMap.cameraPosition.zoom != Setting.MAX_ZOOM) {
                    var zoom = googleMap.cameraPosition.zoom + 1
                    if (zoom > Setting.MAX_ZOOM) zoom = Setting.MAX_ZOOM
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it.position, zoom))
                }
            }, 100)
            false
        }

        googleMap.setOnMapClickListener { clusterPreview_cv.visibility = View.GONE }
        googleMap.setOnCameraMoveStartedListener { clusterPreview_cv.visibility = View.GONE }

        clusterManager?.markerCollection?.setInfoWindowAdapter(object :
            GoogleMap.InfoWindowAdapter {
            override fun getInfoContents(marker: Marker) = getPreview(marker)
            override fun getInfoWindow(marker: Marker) = getPreview(marker)
        })

        if (setting.lastMapCameraPosition.latitude != 0.0) {
            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    setting.lastMapCameraPosition, Setting.BASIC_ZOOM
                )
            )
        }

        addMyLocationTarget()

        var lastPolygonLatitude = 0.0
        var lastPolygonLongitude = 0.0
        val handler = Handler {
            //draw here
            val position = googleMap.cameraPosition.target
            if (position != null
                    && ((lastPolygonLatitude - position.latitude).pow(2.0) +
                        (lastPolygonLongitude - position.longitude).pow(2.0))
                        > Setting.SEARCH_REQUEST_MIN_MOVE_DELTA.pow(2.0)) {

                    val polygon = PolygonOptions().add(
                        LatLng(
                            position.latitude + Setting.MAX_SEARCH_LATITUDE_SIZE,
                            position.longitude + Setting.MAX_SEARCH_LONGITUDE_SIZE
                        ),
                        LatLng(
                            position.latitude - Setting.MAX_SEARCH_LATITUDE_SIZE,
                            position.longitude + Setting.MAX_SEARCH_LONGITUDE_SIZE
                        ),
                        LatLng(
                            position.latitude - Setting.MAX_SEARCH_LATITUDE_SIZE,
                            position.longitude - Setting.MAX_SEARCH_LONGITUDE_SIZE
                        ),
                        LatLng(
                            position.latitude + Setting.MAX_SEARCH_LATITUDE_SIZE,
                            position.longitude - Setting.MAX_SEARCH_LONGITUDE_SIZE
                        ),
                        LatLng(
                            position.latitude + Setting.MAX_SEARCH_LATITUDE_SIZE,
                            position.longitude + Setting.MAX_SEARCH_LONGITUDE_SIZE
                        )
                    ).strokeColor(ctx.resources.getColor(R.color.search_polygon_square))
                googleMap.clear()
                addMyLocationTarget()
                googleMap.addPolygon(polygon)
                googleMap.cameraPosition.target.apply {
                    lastPolygonLatitude = latitude
                    lastPolygonLongitude = longitude
                }
                try {
                    refresher.isRefreshing = true
                ApiServiceImpl(this).getNearbyMarkers(
                    setting.getCurrentLanguage().locale,
                    position.latitude + Setting.MAX_SEARCH_LATITUDE_SIZE / 1,
                    position.longitude + Setting.MAX_SEARCH_LONGITUDE_SIZE / 1,
                    position.latitude - Setting.MAX_SEARCH_LATITUDE_SIZE / 1,
                    position.longitude - Setting.MAX_SEARCH_LONGITUDE_SIZE / 1
                )
                } catch (e: Exception) {
                }
            }
            true
        }
        googleMap.setOnCameraMoveListener { clusterManager?.cluster() }

        clusterManager?.markerCollection?.setOnInfoWindowClickListener { marker ->
            Try {
                val data = markers?.first {
                    marker.position.latitude == it.UF_MAP_POINT_LATITUDE
                            && marker.position.longitude == it.UF_MAP_POINT_LONGITUDE
                } ?: return@Try
                ctx.startActivity<DetailsActivity> { putExtra("marker", Gson().toJson(data)) }
            }
        }
        Thread {
            while (true) {
                if (isResumed)
                    handler.sendEmptyMessage(0)
                Thread.sleep(Setting.SEARCH_REQUEST_PAUSE_SECONDS * 1000L)
            }
        }.start()

        if (showJobId != null && positionShow != null) {
            jobIdFromUrl = showJobId
            locationFromUrl = positionShow
            showVocationFromUrl(showJobId!!, positionShow!!)
            showJobId = null
            positionShow = null
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getPreview(marker: Marker): View? {
        try {
            val view = ctx.layoutInflater.inflate(R.layout.preview, null)
            val data = markers?.first {
                marker.position.latitude == it.UF_MAP_POINT_LATITUDE
                        && marker.position.longitude == it.UF_MAP_POINT_LONGITUDE
            } ?: return view
            try {
                view.title_tv.text = data.NAME
                if (!data.UF_LOGO_IMAGE.isNullOrEmpty()) {
                    renderer?.apply {
                        view.logo_iv.setImageDrawable(renderer!!.drawables[data.UF_LOGO_IMAGE])
                    }
                }

                //val currency = setting.getCurrentCurrency()
                val currency = currencies.firstOrNull { it.id == data.UF_GROSS_CURRENCY_ID }

                try {

                    if (data.UF_GROSS_PER_MONTH.isEmpty() || data.UF_GROSS_PER_MONTH == "0") {
                        view.secondContainer.visibility = View.GONE
                        view.salaryContainer.visibility = View.GONE
                    } else {
                        view.secondContainer.visibility = View.VISIBLE
                        view.salaryContainer.visibility = View.VISIBLE
                    }

                    if (currency?.name == setting.getCurrentCurrency().name) {
                        view.salary_tv.text = data.UF_GROSS_PER_MONTH.round()
                        view.salaryVal_tv.text = " ${currency.name}"
                        view.salary_tv.setRightDrawable(currency.icon)
                        view.secondContainer.visibility = View.GONE
                    } else {
                        view.salary_tv.text = data.UF_GROSS_PER_MONTH.round().trim()
                        view.salaryVal_tv.text = " ${currency?.name ?: ""}"
                        view.salary_tv.setRightDrawable(currency?.icon ?: R.drawable.iusd)

                        val currency2 = setting.getCurrentCurrency()
                        val convertedSalary =
                            (data.UF_GROSS_PER_MONTH.toDouble() * currency2.getLastRate(realm) / (currency?.getLastRate(
                                realm
                            ) ?: 1))
                                .toString().round().trim()
                        view.secondSalary_tv.text = "≈${convertedSalary}"
                        view.secondContainer.visibility =
                            if (convertedSalary == "0") View.GONE
                            else View.VISIBLE
                        view.secondSalaryVal_tv.text = currency2.name
                        view.secondSalary_tv.setRightDrawable(currency2.icon)
                    }
                } catch (e: Exception) {
                }
                view.more_tv.text = "#${data.UF_JOBS_ID} ${getString(R.string.button_detail_title)}"

                val layoutManager = FlexboxLayoutManager(ctx)
                layoutManager.flexWrap = FlexWrap.WRAP
                layoutManager.flexDirection = FlexDirection.ROW
                layoutManager.justifyContent =
                    if (isRtl) JustifyContent.FLEX_END else JustifyContent.FLEX_START
                layoutManager.alignItems = if (isRtl) AlignItems.FLEX_END else AlignItems.FLEX_START

                try {
                    view.langs_rv.layoutManager = //layoutManager
                        LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, isRtl)
                    view.langs_rv.adapter = LanguagesAdapter(
                        ctx,
                        data.UF_LANGUAGE_ID_ALL.split(",").map {
                            languages.first { lang -> lang.id == it.toInt() }
                        })
                } catch (e: Exception) {
                }
                view.place_tv.text = data.COMPANY


                view.filters_tv.layoutManager = layoutManager
                if (data.UF_SKILLS_ID_ALL == "" || data.UF_SKILLS_ID_ALL == Setting.DEFAULT_SKILL_ID_ALWAYS_HIDDEN) {
                    view.filters_tv.visibility = View.GONE
                } else {
                    view.filters_tv.visibility = View.VISIBLE
                    view.filters_tv.adapter = SkillsAdapter(ctx,
                        data.ALL_SKILLS_NAME
                            .split(",")
                            .filterNot { it == Setting.DEFAULT_SKILL_ID_ALWAYS_HIDDEN }
                    )
                }
            } catch (e: Exception) {
            }
            return view
        } catch (e: Exception) {
            return null
        }
    }

    override fun onNearbyMarkersLoaded(list: List<MyMarker>?) {
        try {
            refresher.isRefreshing = false
        } catch (e: Exception) {}
        markers = list
        showMarkers(setting.isFilterActive)

        Handler().postDelayed({
            Log.e("test", "data job: $showJobId " + jobIdFromUrl.toString())
            if (jobIdFromUrl != null) {
                clusterManager?.markerCollection?.markers
                    ?.firstOrNull { it.snippet == jobIdFromUrl.toString() }?.apply {
                        map?.animateCamera(CameraUpdateFactory.newLatLng(position))
                        showInfoWindow()
                    }
            }
        }, 1000)
    }

    override fun onResume() {
        super.onResume()
        if (isFromSetting) {
            showMe()
            isFromSetting = false
        }
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isHavePermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            && isHavePermission(Manifest.permission.ACCESS_FINE_LOCATION))
            mapInit()
        else
            Try {
                askPermission(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) {
                    mapInit()
                }.onDeclined {
                    if (it.hasDenied())
                        it.askAgain()
                    if (it.hasForeverDenied())
                        it.goToSettings()
                }
            }

        filter_iv.setImageResource(
            if (setting.isFilterActive) R.drawable.search_filter_active
            else R.drawable.search_filter_not_active
        )
        filter_iv.setOnClickListener {
            requireActivity().startActivityForResult(
                Intent(requireActivity(), FilterActivity::class.java),
                REQUEST_FILTER
            )
        }
    }

    private fun mapInit() {
        Try {
            val mapFragment =
                childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync(callback)

            if (setting.lastMapCameraPosition.latitude == 0.0
                && setting.lastMapCameraPosition.longitude == 0.0
            ) {
                isMustBeSetLocation = true
                showMe()
            }

            myGeo_iv.setOnClickListener {
                val locationManager = ctx.getSystemService(LOCATION_SERVICE) as LocationManager
                val enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                if (!enabled) {
                    AlertDialog.Builder(ctx)
                        .setTitle(R.string.request_geolocation_title)
                        .setMessage(R.string.request_geolocation_message)
                        .setPositiveButton(R.string.request_geolocation_ok) { dialogInterface, _ ->
                            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            dialogInterface.dismiss()
                            isFromSetting = true
                        }
                        .setNegativeButton(R.string.request_geolocation_cancel) { d, _ -> d.cancel() }
                        .show()
                } else {
                    Handler().postDelayed({ showMe() }, 500)
                }
            }
        }
    }

    private val realm by lazy { Realm.getDefaultInstance() }
    private fun showMarkers(isFilterEnabled: Boolean) {
        try {
            Log.e("test", "1")
            val currency = setting.getCurrentCurrency()
            Log.e("test", "2")
            currency.getLastRate(realm)
            Log.e("test", "3")
            val settingLangs = setting.filterLanguagesIds
            Log.e("test", "4")
            val settingSkills = setting.filterSkillsIds
            Log.e("test", "5")
            val isJobTypeFilterEnabled = setting.filterJobType != ""
            Log.e("test", "6")
            val jobType = setting.filterJobType ?: ""
            Log.e("test", "7")
            val isSalaryFilterEnabled =
                (setting.filterSalary != "") && (setting.filterSalary != Setting.DEFAULT_FILTER_RANGE_PARAMS)
            Log.e("test", "8")
            val minSalaryGold = setting.filterSalary.split("-").first().toInt() * //0-123
                    Setting.DEFAULT_MAX.toDouble()
            Log.e("test", "9")
            val maxData = setting.filterSalary.split("-").last()
            Log.e("test", "10")
            val maxSalaryGold =
                (if (maxData == "∞") 1_000_000_000.0
                else maxData.toInt() * Setting.DEFAULT_MAX.toDouble())
            Log.e("test", "11")

            clusterManager?.clearItems()
            clusterManager?.addItems((markers ?: listOf()).filter { marker ->
                val languagesIds = marker.UF_LANGUAGE_ID_ALL.split(",")
                val skillsIds = marker.UF_SKILLS_ID_ALL.split(",")
                val salaryGold = marker.UF_GOLD_PER_MONTH.toDoubleOrNull() ?: .0

                //predicates for filter
                (isFilterEnabled && (
                        ((isSalaryFilterEnabled && (salaryGold > minSalaryGold && salaryGold < maxSalaryGold)) || (!isSalaryFilterEnabled))//salary predicate
                                && settingLangs.filter { it in languagesIds }.size == settingLangs.size//languages predicate
                                && settingSkills.filter { it in skillsIds }.size == settingSkills.size//skills predicate
                                && ((isJobTypeFilterEnabled && (marker.UF_TYPE_OF_JOB_NAME == jobType)) || (!isJobTypeFilterEnabled))//job type predicate
                        ))
                        || (!isFilterEnabled)
            }.map {
                AbstractMarker(it.UF_MAP_POINT_LATITUDE, it.UF_MAP_POINT_LONGITUDE, it)
            })
            clusterManager?.cluster()
        } catch (e: Exception) {
            Log.e("test", e.localizedMessage ?: "Unknow Error")
        }
    }

    fun onResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            setting.isFilterActive = true
            filter_iv.setImageResource(R.drawable.search_filter_active)
            showMarkers(true)
        } else if (resultCode == Activity.RESULT_CANCELED) {
            setting.isFilterActive = false
            filter_iv.setImageResource(R.drawable.search_filter_not_active)
            showMarkers(false)
        }
    }

    override fun onStop() {
        map?.cameraPosition?.apply {
            setting.lastMapCameraPosition = target
        }
        super.onStop()
    }

}