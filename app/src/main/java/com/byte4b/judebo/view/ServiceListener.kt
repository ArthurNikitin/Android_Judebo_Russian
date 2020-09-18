package com.byte4b.judebo.view

import com.byte4b.judebo.models.*

interface ServiceListener {

    fun onNearbyMarkersLoaded(list: List<MyMarker>?) {}

    fun onSkillsLoaded(list: List<Skill>?) {}

    fun onJobTypesLoaded(list: List<JobType>?) {}

    fun onRatesLoaded(list: List<CurrencyRate>?) {}

    fun onMyVocationsLoaded(list: List<Vocation>?, isNeedLogout: Boolean = false) {}

    fun onVocationDeleted(success: Boolean) {}

    fun onMyVocationUpdated(success: Boolean) {}

    fun onVocationAdded(success: Boolean) {}

    fun onSignIn(result: AuthResult?) {}

    fun onSignUp(result: AuthResult?) {}

    fun onAccountDeleted(result: Result?) {}

    fun onSkillCreated(result: AuthResult?) {}

    fun onSubsInstalled(result: AuthResult?) {}

}