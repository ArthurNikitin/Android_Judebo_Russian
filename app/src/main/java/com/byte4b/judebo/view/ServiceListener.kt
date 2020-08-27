package com.byte4b.judebo.view

import com.byte4b.judebo.models.*

interface ServiceListener {

    fun onNearbyMarkersLoaded(list: List<MyMarker>?) {}

    fun onSkillsLoaded(list: List<Skill>?) {}

    fun onJobTypesLoaded(list: List<JobType>?) {}

    fun onRatesLoaded(list: List<CurrencyRate>?) {}

    fun onMyVocationsLoaded(list: List<Vocation>?) {}

    fun onVocationDeleted(success: Boolean) {}

}