package com.byte4b.judebo.view

import com.byte4b.judebo.models.CurrencyRate
import com.byte4b.judebo.models.JobType
import com.byte4b.judebo.models.MyMarker
import com.byte4b.judebo.models.Skill

interface ServiceListener {

    fun onNearbyMarkersLoaded(list: List<MyMarker>?) {}

    fun onSkillsLoaded(list: List<Skill>?) {}

    fun onJobTypesLoaded(list: List<JobType>?) {}

    fun onRatesLoaded(list: List<CurrencyRate>?) {}

}