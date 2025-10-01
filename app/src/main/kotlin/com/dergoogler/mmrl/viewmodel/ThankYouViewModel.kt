package com.dergoogler.mmrl.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.dergoogler.mmrl.datastore.UserPreferencesRepository
import com.dergoogler.mmrl.model.online.ExploreRepositoryMember
import com.dergoogler.mmrl.network.runRequest
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import com.dergoogler.mmrl.stub.IMMRLApiManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ThankYouViewModel @Inject constructor(
    application: Application,
    localRepository: LocalRepository,
    modulesRepository: ModulesRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : MMRLViewModel(application, localRepository, modulesRepository, userPreferencesRepository) {
    private val sponsorsFlow = MutableStateFlow(listOf<ExploreRepositoryMember>())
    var totalSponsorAmount by mutableIntStateOf(0)
        private set
    val sponsors get() = sponsorsFlow.asStateFlow()

    private val contributorsFlow = MutableStateFlow(listOf<ExploreRepositoryMember>())
    var totalContributionsCount by mutableIntStateOf(0)
        private set
    val contributors get() = contributorsFlow.asStateFlow()

    init {
        viewModelScope.launch {
            runRequest {
                withContext(Dispatchers.IO) {
                    return@withContext IMMRLApiManager.build().sponsors.execute()
                }
            }.onSuccess { list ->
                sponsorsFlow.value = list.map { it.toMember(context) }
                totalSponsorAmount = list.sumOf { it.amount }
            }.onFailure {
                Timber.e(it, "unable to get sponsors")
            }

            runRequest {
                withContext(Dispatchers.IO) {
                    return@withContext IMMRLApiManager.build().contributors.execute()
                }
            }.onSuccess { list ->
                contributorsFlow.value = list.map { it.toMember(context) }
                totalContributionsCount = list.sumOf { it.contributions }
            }.onFailure {
                Timber.e(it, "unable to get contributors")
            }
        }
    }
}