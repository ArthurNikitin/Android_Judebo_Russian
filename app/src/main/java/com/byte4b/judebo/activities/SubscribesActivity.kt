package com.byte4b.judebo.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.android.billingclient.api.*
import com.byte4b.judebo.R
import com.byte4b.judebo.adapters.SubscribesViewPagerAdapter
import com.byte4b.judebo.fragments.SubscribeFragment
import com.byte4b.judebo.models.SubAnswer
import com.byte4b.judebo.services.ApiServiceImpl
import com.byte4b.judebo.startActivity
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.view.ServiceListener
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_subscribes.*
import java.util.*
import kotlin.math.roundToInt


class SubscribesActivity : AppCompatActivity(R.layout.activity_subscribes), ServiceListener {

    var subs: MutableList<SkuDetails>? = null
    private val setting by lazy { Setting(this) }

    private fun SubsTry(action: () -> Unit) {
        try {
            action()
        } catch (e: Exception) {
            Log.e("testsubs", e.localizedMessage ?: "subs unknow error")
        }
    }

    private val billingClient by lazy {
        BillingClient.newBuilder(this)
            .setListener(purchaseUpdateListener)
            .enablePendingPurchases()
            .build()
    }

    private val purchaseUpdateListener by lazy {
        PurchasesUpdatedListener { _, purchases ->
            Log.e("test", "add sub: purchase size = ${purchases?.size}")
            purchases?.forEach {
                    if (it != null) {
                        setting.subscribeInfo = SubAnswer(
                            MESSAGE = "SUCCESS",
                            STATUS = "SUCCESS",
                            SUBSCRIPTION_BILL_TOKEN = it.purchaseToken,
                            SUBSCRIPTION_END = it.purchaseTime + when {
                                it.sku.contains("year") -> 12 * 30 * 24 * 60 * 60 * 1000L
                                it.sku.contains("half") -> 6 * 30 * 24 * 60 * 60 * 1000L
                                else -> 30 * 24 * 60 * 60 * 1000L
                            },
                            SUBSCRIPTION_STORE_ID = it.sku,
                            SUBSCRIPTION_LIMIT = it.sku.substringAfter("0").toIntOrNull(),
                            SUBSCRIPTION_NAME = ""
                        )
                        ApiServiceImpl(this).setSubs(
                            setting.getCurrentLanguage().locale,
                            setting.token ?: "",
                            setting.email ?: "",
                            it.sku, setting.subscribeInfo?.SUBSCRIPTION_END.toString(), it.purchaseToken
                        )
                    } else {
                        ApiServiceImpl(this).setSubs(
                            setting.getCurrentLanguage().locale,
                            setting.token ?: "",
                            setting.email ?: "",
                            null, null, it?.purchaseToken
                        )
                    }
            }
        }
    }

    fun closeClick(v: View) = finish()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        billingClient.startConnection(object : BillingClientStateListener {

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Log.e("test", "onBillingSetupFinished: ${billingResult.responseCode}")
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val skuDetailsParamsBuilder = SkuDetailsParams.newBuilder()
                    val skuList =
                        Setting.subs10PeriodVariantsIds +
                                Setting.subs50PeriodVariantsIds +
                                Setting.subs200PeriodVariantsIds
                    skuDetailsParamsBuilder.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)
                    skuDetailsParamsBuilder.build()

                    billingClient
                        .querySkuDetailsAsync(skuDetailsParamsBuilder.build()) { result, params ->
                            if (result.responseCode == 0) {
                                subs = params
                                fillPriceForSubs(0)
                            }
                        }
                }
            }

            override fun onBillingServiceDisconnected() {}
        })

        ApiServiceImpl(this).checkMySub(
            setting.getCurrentLanguage().locale,
            setting.token ?: "",
            setting.email ?: ""
        )

        val viewPagerPendingAdapter = SubscribesViewPagerAdapter(
            supportFragmentManager, listOf(
                SubscribeFragment(
                    R.drawable.subscription_picture_010,
                    R.string.subsription_limit_10_title,
                    R.string.subsription_limit_10_description,
                    Setting.subs10PeriodVariantsIds
                ),
                SubscribeFragment(
                    R.drawable.subscription_picture_050,
                    R.string.subsription_limit_50_title,
                    R.string.subsription_limit_50_description,
                    Setting.subs50PeriodVariantsIds
                ),
                SubscribeFragment(
                    R.drawable.subscription_picture_200,
                    R.string.subsription_limit_200_title,
                    R.string.subsription_limit_200_description,
                    Setting.subs200PeriodVariantsIds
                )
            )
        )
        viewpager.adapter = viewPagerPendingAdapter
        indicator.attachToViewPager(viewpager)
        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) = fillPriceForSubs(position)
            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun queryPurchases() =
        billingClient.queryPurchases(BillingClient.SkuType.SUBS).purchasesList

    override fun onMySubLoaded(result: SubAnswer?) {
        if (!setting.toLogin) {
            if (result?.STATUS == "success") {
                if (result.SUBSCRIPTION_STORE_ID?.startsWith("playmarket") == true) {
                    val mySub = queryPurchases()?.firstOrNull {
                        it.sku == result.SUBSCRIPTION_STORE_ID && it.purchaseToken == result.SUBSCRIPTION_BILL_TOKEN
                    }
                    if (mySub != null) {
                        setting.subscribeInfo = result
                    } else {
                        ApiServiceImpl(this).setSubs(
                            setting.getCurrentLanguage().locale,
                            setting.token ?: "",
                            setting.email ?: "",
                            null, null, null
                        )
                    }
                } else {
                    setting.subscribeInfo = result
                }

                //add it to main activity for each onCreate
            } else if (result != null) {
                Toasty.error(this, result.MESSAGE).show()
            } else
                Toasty.error(this, R.string.error_no_internet).show()
        } else {
            setting.toLogin = false
            if (result?.STATUS == "success") {
                if (result.SUBSCRIPTION_STORE_ID?.startsWith("playmarket") == true) {
                    val mySub = queryPurchases()?.firstOrNull {
                        it.sku == result.SUBSCRIPTION_STORE_ID
                                && it.purchaseToken == result.SUBSCRIPTION_BILL_TOKEN
                    }
                    if (mySub != null) {
                        setting.subscribeInfo = result
                        Toasty.success(this, R.string.subsription_restore_subs_success).show()
                    } else {
                        ApiServiceImpl(this).setSubs(
                            setting.getCurrentLanguage().locale,
                            setting.token ?: "",
                            setting.email ?: "",
                            "0", "0", "0"
                        )
                        setting.subscribeInfo = null
                        Toasty.error(this, R.string.subsription_restore_subs_error).show()
                    }
                } else {
                    Toasty.success(this, R.string.subsription_restore_subs_success).show()
                }
            } else
                Toasty.error(this, R.string.subsription_restore_subs_error).show()
        }
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
                        "${discountHalfYearInMonth.rounded()} ${currencySymbol}/${getString(R.string.subsription_period_save_month)}.\n" +
                        "${getString(R.string.subsription_period_save_is)} ${100 - discountHalfYearPercent.toInt()}%."

            val thirdSub = subs?.firstOrNull { it.sku == currentFragment.mySubs[2] }
            year_subs_b.text =
                "${thirdSub?.price} / ${getString(R.string.subsription_period_12_month)}"
            val discountYearInMonth = (thirdSub!!.priceAmountMicros / 12.0) / 1_000_000
            val discountYearPercent =
                (discountHalfYearInMonth / (firstSub.priceAmountMicros.toDouble() / 1_000_000)) *
                        100
            discount_year_tv.text =
                "${getString(R.string.subsription_period_save_12month_at)} " +
                    "${discountYearInMonth.rounded()} ${currencySymbol}/${getString(R.string.subsription_period_save_month)}.\n" +
                    "${getString(R.string.subsription_period_save_is)} ${100 - discountYearPercent.toInt()}%."
        }
    }

    private fun Double.rounded(): Double {
        return (this * 100).roundToInt().toDouble() / 100
    }

    fun monthClick(v: View) {
        SubsTry {
            if (!checkSubs()) showCancelDialog()
            else {
                val currentFragment =
                    (viewpager.adapter as SubscribesViewPagerAdapter).fragments[viewpager.currentItem]
                            as SubscribeFragment
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(subs!!.first { it.sku == currentFragment.mySubs[0] })
                    .build()
                billingClient.launchBillingFlow(
                    this@SubscribesActivity,
                    billingFlowParams
                )
            }
        }
    }

    fun halfYearClick(v: View) {
        SubsTry {
            if (!checkSubs()) showCancelDialog()
            else {
                val currentFragment =
                    (viewpager.adapter as SubscribesViewPagerAdapter).fragments[viewpager.currentItem]
                            as SubscribeFragment
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(subs!!.first { it.sku == currentFragment.mySubs[1] })
                    .build()
                billingClient.launchBillingFlow(
                    this@SubscribesActivity,
                    billingFlowParams
                )
            }
        }
    }

    fun yearClick(v: View) {
        SubsTry {
            if (!checkSubs()) showCancelDialog()
            else {
                val currentFragment =
                    (viewpager.adapter as SubscribesViewPagerAdapter).fragments[viewpager.currentItem]
                            as SubscribeFragment
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(subs!!.first { it.sku == currentFragment.mySubs[2] })
                    .build()
                billingClient.launchBillingFlow(
                    this@SubscribesActivity,
                    billingFlowParams
                )
            }
        }
    }

    fun restoreClick(v: View) {
        if (setting.isAuth) {
            startActivity<SubscribesActivity> { }
            setting.toLogin = true
            finish()
        } else {
            setting.toLogin = true
            finish()
        }
    }

    private fun showCancelDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.subscription_need_cancel_current_subs)
            .setPositiveButton(R.string.request_geolocation_ok) { dialog, _ ->
                dialog.dismiss()
                startActivity(
                    Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://play.google.com/store/apps/details?id=com.byte4b.judebo")
                    }
                )
            }
            .setNegativeButton(R.string.request_geolocation_cancel) { d, _ -> d.cancel() }
            .show()
    }

    private fun checkSubs() = queryPurchases().isNullOrEmpty()

}