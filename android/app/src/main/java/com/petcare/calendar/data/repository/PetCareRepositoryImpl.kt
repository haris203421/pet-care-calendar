package com.petcare.calendar.data.repository

import com.petcare.calendar.data.local.LocalPreferencesStore
import com.petcare.calendar.data.sample.SampleData
import com.petcare.calendar.domain.model.*
import com.petcare.calendar.domain.repository.PetCareRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class PetCareRepositoryImpl(private val store: LocalPreferencesStore, private val onChanged: (CalendarData) -> Unit) : PetCareRepository {
    private val mutable = MutableStateFlow(CalendarStore())
    override val state = mutable.asStateFlow()
    private val mutex = Mutex()

    override suspend fun load() = mutex.withLock {
        withContext(Dispatchers.IO) {
            runCatching { store.read() ?: store.replace(SampleData.create()) }
                .onSuccess { data -> mutable.update { it.copy(data = data, loadState = LoadState.READY) }; runCatching { onChanged(data) } }
                .onFailure { mutable.update { it.copy(loadState = LoadState.ERROR) } }
            Unit
        }
    }

    override suspend fun change(transform: (CalendarData) -> CalendarData): Result<Unit> = mutex.withLock {
        withContext(Dispatchers.IO) {
            runCatching {
                check(state.value.loadState == LoadState.READY)
                val next = store.update({ SampleData.create() }, transform)
                mutable.update { it.copy(data = next, loadState = LoadState.READY) }
                runCatching { onChanged(next) }
                Unit
            }
        }
    }

    override suspend fun reset(): Result<Unit> = mutex.withLock {
        withContext(Dispatchers.IO) {
            runCatching {
                val next = store.replace(CalendarData(settings = AppSettings(sampleMode = false)))
                mutable.update { it.copy(data = next, loadState = LoadState.READY) }
                runCatching { onChanged(next) }
                Unit
            }
        }
    }

    override suspend fun loadSamples(): Result<Unit> = change {
        if (it.pets.isEmpty() && it.logs.isEmpty()) SampleData.create() else it
    }
}
