package com.byte4b.judebo.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.android.billingclient.api.*
import com.byte4b.judebo.R
import com.byte4b.judebo.adapters.SubscribesViewPagerAdapter
import com.byte4b.judebo.fragments.SubscribeFragment
import kotlinx.android.synthetic.main.activity_subscribes.*
import java.util.*

class SubscribesActivity : AppCompatActivity(R.layout.activity_subscribes) {

    private val subs10PeriodVariantsIds = listOf(
        "playmarket_month_limit_00010",
        "playmarket_halfyear_limit_00010",
        "playmarket_year_limit_00010"
    )
    private val subs50PeriodVariantsIds = listOf(
        "playmarket_month_limit_00050",
        "playmarket_halfyear_limit_00050",
        "playmarket_year_limit_00050"
    )
    private val subs200PeriodVariantsIds = listOf(
        "playmarket_month_limit_00200",
        "playmarket_halfyear_limit_00200",
        "playmarket_year_limit_00200"
    )

    var subs: MutableList<SkuDetails>? = null

    private fun SubsTry(action: () -> Unit) {
        try {
            action()
        } catch (e: Exception) {
            Log.e("testsubs", e.localizedMessage?: "subs unknow error")
        }
    }

    private val billingClient by lazy {
        BillingClient.newBuilder(this)
            .setListener(purchaseUpdateListener)
            .enablePendingPurchases()
            .build()
    }

    private val purchaseUpdateListener by lazy {
        PurchasesUpdatedListener { billingResult, purchases ->
            purchases?.forEach {
                /*val purchase = Gson().fromJson(Gson().toJson(it), PurchaseMy::class.java)
                if (purchase.zzc.nameValuePairs.productId == "subscribe_disable_ads1") {
                }*/
            }
        }
    }

    private fun queryPurchases(): List<Purchase>? {
        val purchasesResult =
            billingClient.queryPurchases(BillingClient.SkuType.SUBS)
        return purchasesResult.purchasesList
    }

    fun closeClick(v: View) = finish()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        billingClient.startConnection(object : BillingClientStateListener {

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Log.e("test", "onBillingSetupFinished: ${billingResult.responseCode}")
                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                    val skuDetailsParamsBuilder = SkuDetailsParams.newBuilder()
                    val skuList =
                        subs10PeriodVariantsIds + subs50PeriodVariantsIds + subs200PeriodVariantsIds
                    skuDetailsParamsBuilder.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)
                    skuDetailsParamsBuilder.build()

                    billingClient
                        .querySkuDetailsAsync(skuDetailsParamsBuilder.build()) { result, params ->
                            if (result.responseCode == 0) {
                                subs = params

                                fillPriceForSubs(0)
                                //params?.forEach {
                                    /*val billingFlowParams = BillingFlowParams.newBuilder()
                                        .setSkuDetails(it)
                                        .build()
                                    billingClient.launchBillingFlow(this@SubscribesActivity,
                                        billingFlowParams)*/
                                //}
                            }
                        }
                }
            }
            override fun onBillingServiceDisconnected() {}
        })

        //get my sub
        //load subs from store
        //check valid token if sub from playstore
        //  if error - send to server + show free sub
        //load subs prices + hint for 6 and 12 months
        //setOnClickListeners for buttons
        //(if > subs - delete old sub and add new)
        //else add new

        val viewPagerPendingAdapter = SubscribesViewPagerAdapter(supportFragmentManager, listOf(
                SubscribeFragment(
                    R.drawable.subscription_picture_010,
                    R.string.subsription_limit_10_title,
                    R.string.subsription_limit_10_description,
                    subs10PeriodVariantsIds
                ),
                SubscribeFragment(
                    R.drawable.subscription_picture_050,
                    R.string.subsription_limit_50_title,
                    R.string.subsription_limit_50_description,
                    subs50PeriodVariantsIds
                ),
                SubscribeFragment(
                    R.drawable.subscription_picture_200,
                    R.string.subsription_limit_200_title,
                    R.string.subsription_limit_200_description,
                    subs200PeriodVariantsIds
                )
        ))
        viewpager.adapter = viewPagerPendingAdapter
        indicator.attachToViewPager(viewpager)
        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {}

            override fun onPageSelected(position: Int) = fillPriceForSubs(position)
            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    @SuppressLint("SetTextI18n")
    private fun fillPriceForSubs(index: Int) {
        SubsTry {
            val currentFragment =
                ((viewpager.adapter as SubscribesViewPagerAdapter).fragments[index]
                        as SubscribeFragment?)
                    ?: return@SubsTry

            val firstSub = subs?.firstOrNull { it.sku == currentFragment.mySubs[0] }
            month_subs_b.text =
                "${firstSub?.price} / ${getString(R.string.subsription_period_01_month)}"


            val currencySymbol = Currency.getInstance(firstSub!!.priceCurrencyCode).symbol

            val secondSub = subs?.firstOrNull { it.sku == currentFragment.mySubs[1] }
            month6_subs_b.text =
                "${secondSub?.price} / ${getString(R.string.subsription_period_06_month)}"
            val discountHalfYearInMonth = (secondSub!!.priceAmountMicros / 6.0) / 1_000_000
            val discountHalfYearPercent =
                (discountHalfYearInMonth / (firstSub.priceAmountMicros.toDouble() / 1_000_000)) *
                        100
            discount_6month_tv.text =
                "${getString(R.string.subsription_period_save_6month_at)} " +
                        "${discountHalfYearInMonth.rounded()} ${currencySymbol}/${getString(R.string.subsription_period_save_month)}. " +
                        "${getString(R.string.subsription_period_save_is)} ${discountHalfYearPercent.rounded()}%."

            val thirdSub = subs?.firstOrNull { it.sku == currentFragment.mySubs[2] }
            year_subs_b.text =
                "${thirdSub?.price} / ${getString(R.string.subsription_period_12_month)}"
            val discountYearInMonth = (thirdSub!!.priceAmountMicros / 6.0) / 1_000_000
            val discountYearPercent =
                (discountHalfYearInMonth / (firstSub.priceAmountMicros.toDouble() / 1_000_000)) *
                        100
            discount_year_tv.text =
                "${getString(R.string.subsription_period_save_12month_at)} " +
                    "${discountYearInMonth.rounded()} ${currencySymbol}/${getString(R.string.subsription_period_save_month)}. " +
                    "${getString(R.string.subsription_period_save_is)} ${discountYearPercent.rounded()}%."
        }
    }

    private fun Double.rounded(): Double {
        return (this * 100).toInt().toDouble() / 100
    }

}