package com.byte4b.judebo.services

import android.util.Log
import com.byte4b.judebo.api.getAPI
import com.byte4b.judebo.api.secretKey
import com.byte4b.judebo.models.*
import com.byte4b.judebo.models.request.CreateSkillRequest
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.view.ServiceListener
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.random.Random

class ApiServiceImpl(val listener: ServiceListener?) : ApiService {

    private fun check(action: () -> Unit) {
        try {
            action()
            Log.e("test", "check complited")
        } catch (e: Exception) {
            Log.e("test", e.localizedMessage ?: "Error")
        }
    }

    override fun getNearbyMarkers(
        locale: String,
        northEastLatitude: Double,
        northEastLongitude: Double,
        southWestLatitude: Double,
        southWestLongitude: Double
    ) {
        getAPI(locale).getNearbyTargets("$northEastLatitude,$northEastLongitude",
        "$southWestLatitude,$southWestLongitude", secretKey)
            .enqueue(object : Callback<JsonObject> {
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    check { listener?.onNearbyMarkersLoaded(null) }
                }

                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (response.isSuccessful) {
                        try {
                            val objects = response.body()?.entrySet()?.map {
                                val result = Gson().fromJson(it.value, MyMarker::class.java)

                                result.UF_MAP_POINT_LATITUDE +=
                                    Random.nextInt(-8, 8) * Setting.VALUE_INFINITESIMAL

                                result.UF_MAP_POINT_LONGITUDE +=
                                    2 * Random.nextInt(-8, 8) * Setting.VALUE_INFINITESIMAL

                                result
                            }
                            listener?.onNearbyMarkersLoaded(objects)
                        } catch (e: Exception) {
                            Log.e("test", e.localizedMessage ?: "parse error" + " ${Gson().toJson(response.body())}")
                            check { listener?.onNearbyMarkersLoaded(null) }
                        }
                    } else
                        check { listener?.onNearbyMarkersLoaded(null) }
                }

            })
    }

    override fun getSkills(locale: String) {
        getAPI(locale)
            .getSkills(secretKey)
            .enqueue(object : Callback<List<Skill>> {
                override fun onFailure(call: Call<List<Skill>>, t: Throwable) {
                    listener?.onSkillsLoaded(null)
                }

                override fun onResponse(call: Call<List<Skill>>, response: Response<List<Skill>>) {
                    if (response.isSuccessful)
                        listener?.onSkillsLoaded(response.body())
                    else
                        listener?.onSkillsLoaded(null)
                }
            })
    }

    override fun getJobTypes(locale: String) {
        getAPI(locale)
            .getJobTypes(secretKey)
            .enqueue(object : Callback<List<JobType>> {
                override fun onFailure(call: Call<List<JobType>>, t: Throwable) {
                    listener?.onJobTypesLoaded(null)
                }

                override fun onResponse(call: Call<List<JobType>>, response: Response<List<JobType>>) {
                    if (response.isSuccessful)
                        listener?.onJobTypesLoaded(response.body())
                    else
                        listener?.onJobTypesLoaded(null)
                }
            })
    }

    override fun getRates(locale: String) {
        getAPI(locale)
            .getRates(secretKey)
            .enqueue(object : Callback<List<CurrencyRate>> {
                override fun onFailure(call: Call<List<CurrencyRate>>, t: Throwable) {
                    listener?.onRatesLoaded(null)
                }

                override fun onResponse(call: Call<List<CurrencyRate>>, response: Response<List<CurrencyRate>>) {
                    if (response.isSuccessful)
                        listener?.onRatesLoaded(response.body())
                    else
                        listener?.onRatesLoaded(null)
                }
            })
    }

    override fun getMyVocations(locale: String, token: String, login: String) {
        getAPI(locale)
            .getMyVocations(secretKey, token, login)
            .enqueue(object : Callback<List<Vocation>> {
                override fun onFailure(call: Call<List<Vocation>>, t: Throwable) {
                    check { listener?.onMyVocationsLoaded(null) }
                    Log.e("test","throw tt")
                }

                override fun onResponse(call: Call<List<Vocation>>, response: Response<List<Vocation>>) {
                    check {
                        Log.e("test","onResp tt success")
                        if (response.isSuccessful) {
                            val (serviceList, workList) = (response.body() ?: listOf()).partition {
                                it.UF_JOBS_ID.toString() == Setting.DEFAULT_JOB_ID_SERVICE_USED.toString()
                            }
                            listener?.onMyVocationsLoaded(
                                workList,
                                serviceList.isNotEmpty()
                                        && (serviceList[0].DETAIL_TEXT == "wrong token"
                                        || serviceList[0].DETAIL_TEXT == "user not found")
                            )
                        } else {
                            Log.e("test","onResp tt")
                            listener?.onMyVocationsLoaded(null)
                        }
                    }
                }
            })
    }

    override fun deleteVocation(locale: String, token: String, login: String, vocation: Vocation) {
        getAPI(locale)
            .deleteVocation(secretKey, token, login, listOf(vocation))
            .enqueue(object : Callback<Result> {
                override fun onFailure(call: Call<Result>, t: Throwable) {
                    check { listener?.onVocationDeleted(false) }
                    Log.e("check", t.localizedMessage ?: "Error")
                }

                override fun onResponse(call: Call<Result>, response: Response<Result>) {
                    check {
                        if (response.isSuccessful)
                            listener?.onVocationDeleted(response.body()?.status == "success")
                        else
                            listener?.onVocationDeleted(false)
                        Log.e("check", Gson().toJson(response.body()))
                    }
                }
            })
    }

    override fun updateMyVocations(
        locale: String,
        token: String,
        login: String,
        vocations: List<Vocation>
    ) {
        getAPI(locale)
            .updateMyVocations(secretKey, token, login, vocations)
            .enqueue(object : Callback<Result> {
                override fun onFailure(call: Call<Result>, t: Throwable) {
                    check { listener?.onMyVocationUpdated(false) }
                }

                override fun onResponse(call: Call<Result>, response: Response<Result>) {
                    check {
                        if (response.isSuccessful)
                            listener?.onMyVocationUpdated(response.body()?.status == "success")
                        else
                            listener?.onMyVocationUpdated(false)
                    }
                }
            })
    }

    override fun addMyVocation(locale: String, token: String, login: String, vocation: Vocation) {
        getAPI(locale)
            .addVocation(secretKey, token, login, listOf(vocation))
            .enqueue(object : Callback<Result> {
                override fun onFailure(call: Call<Result>, t: Throwable) {
                    check { listener?.onVocationAdded(false) }
                }

                override fun onResponse(call: Call<Result>, response: Response<Result>) {
                    check {
                        if (response.isSuccessful)
                            listener?.onVocationAdded(response.body()?.status == "success")
                        else
                            listener?.onVocationAdded(false)
                    }
                }
            })
    }

    override fun signInWithEmail(locale: String, login: String, password: String) {
        getAPI(locale)
            .signInWithEmail(login, password)
            .enqueue(object : Callback<AuthResult> {
                override fun onFailure(call: Call<AuthResult>, t: Throwable) {
                    check { listener?.onSignIn(null) }
                }

                override fun onResponse(call: Call<AuthResult>, response: Response<AuthResult>) {
                    check {
                        if (response.isSuccessful) listener?.onSignIn(response.body())
                        else listener?.onSignIn(null)
                    }
                }
            })
    }

    override fun signInWithFb(locale: String, login: String) {
        getAPI(locale)
            .signInWithFb(login)
            .enqueue(object : Callback<AuthResult> {
                override fun onFailure(call: Call<AuthResult>, t: Throwable) {
                    check { listener?.onSignIn(null) }
                }

                override fun onResponse(call: Call<AuthResult>, response: Response<AuthResult>) {
                    check {
                        if (response.isSuccessful) listener?.onSignIn(response.body())
                        else listener?.onSignIn(null)
                    }
                }
            })
    }

    override fun signInWithGoogle(locale: String, login: String) {
        getAPI(locale)
            .signInWithGoogle(login)
            .enqueue(object : Callback<AuthResult> {
                override fun onFailure(call: Call<AuthResult>, t: Throwable) {
                    check { listener?.onSignIn(null) }
                }

                override fun onResponse(call: Call<AuthResult>, response: Response<AuthResult>) {
                    check {
                        if (response.isSuccessful) listener?.onSignIn(response.body())
                        else listener?.onSignIn(null)
                    }
                }
            })
    }

    override fun signUpWithEmail(locale: String, login: String, password: String) {
        getAPI(locale)
            .signUpWithEmail(login, password)
            .enqueue(object : Callback<AuthResult> {
                override fun onFailure(call: Call<AuthResult>, t: Throwable) {
                    check { listener?.onSignUp(null) }
                }

                override fun onResponse(call: Call<AuthResult>, response: Response<AuthResult>) {
                    check {
                        if (response.isSuccessful) listener?.onSignUp(response.body())
                        else listener?.onSignUp(null)
                    }
                }
            })    }

    override fun signUpWithFb(locale: String, login: String, password: String) {
        getAPI(locale)
            .signUpWithFb(login, password = password)
            .enqueue(object : Callback<AuthResult> {
                override fun onFailure(call: Call<AuthResult>, t: Throwable) {
                    check { listener?.onSignUp(null) }
                }

                override fun onResponse(call: Call<AuthResult>, response: Response<AuthResult>) {
                    check {
                        if (response.isSuccessful) listener?.onSignUp(response.body())
                        else listener?.onSignUp(null)
                    }
                }
            })
    }

    override fun signUpWithGoogle(locale: String, login: String, password: String) {
        getAPI(locale)
            .signUpWithGoogle(login, password = password)
            .enqueue(object : Callback<AuthResult> {
                override fun onFailure(call: Call<AuthResult>, t: Throwable) {
                    check { listener?.onSignUp(null) }
                }

                override fun onResponse(call: Call<AuthResult>, response: Response<AuthResult>) {
                    check {
                        if (response.isSuccessful) listener?.onSignUp(response.body())
                        else listener?.onSignUp(null)
                    }
                }
            })
    }

    override fun deleteMe(locale: String, login: String, token: String) {
        getAPI(locale)
            .deleteMe(secretKey, token, login)
            .enqueue(object : Callback<Result> {
                override fun onFailure(call: Call<Result>, t: Throwable) {
                    check { listener?.onAccountDeleted(null) }
                }

                override fun onResponse(call: Call<Result>, response: Response<Result>) {
                    check {
                        if (response.isSuccessful)
                            listener?.onAccountDeleted(response.body())
                        else
                            listener?.onAccountDeleted(null)
                    }
                }
            })
    }

    override fun createSkill(locale: String, name: String, token: String, login: String) {
        getAPI(locale)
            .createSkill(secretKey, token, login, listOf(CreateSkillRequest(name)))
            .enqueue(object : Callback<AuthResult> {
                override fun onResponse(call: Call<AuthResult>, response: Response<AuthResult>) {
                    check {
                        if (response.isSuccessful)
                            listener?.onSkillCreated(response.body())
                        else
                            listener?.onSkillCreated(null)
                    }
                }

                override fun onFailure(call: Call<AuthResult>, t: Throwable) {
                    check { listener?.onSkillCreated(null) }
                }
            })
    }

    override fun setSubs(
        locale: String,
        token: String,
        login: String,
        subsId: String?,
        subsEnd: String?,
        storeToken: String?
    ) {
        getAPI(locale)
            .setMySubs(secretKey, token, login, subsId, subsEnd, storeToken)
            .enqueue(object : Callback<AuthResult> {
                override fun onResponse(call: Call<AuthResult>, response: Response<AuthResult>) {
                    check { listener?.onSubsInstalled(response.body()) }
                }

                override fun onFailure(call: Call<AuthResult>, t: Throwable) {
                    check { listener?.onSubsInstalled(null) }
                }

            })
    }

    override fun checkMySub(locale: String, token: String, login: String) {
        getAPI(locale)
            .getMySub(secretKey, token, login)
            .enqueue(object : Callback<SubAnswer> {
                override fun onResponse(call: Call<SubAnswer>, response: Response<SubAnswer>) {
                    check { listener?.onMySubLoaded(response.body()) }
                }

                override fun onFailure(call: Call<SubAnswer>, t: Throwable) {
                    check { listener?.onMySubLoaded(null) }
                }

            })
    }

}