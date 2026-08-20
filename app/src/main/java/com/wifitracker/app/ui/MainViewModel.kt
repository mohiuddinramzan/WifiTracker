package com.wifitracker.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wifitracker.app.data.PeriodStat
import com.wifitracker.app.data.StatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

enum class Period(val label: String) {
    DAILY("দৈনিক"),
    WEEKLY("সাপ্তাহিক"),
    MONTHLY("মাসিক"),
    YEARLY("বাৎসরিক"),
    ALL_TIME("সবগুলো")
}

data class UiState(
    val period: Period = Period.DAILY,
    val totalDurationMillis: Long = 0L,
    val totalRangeMillis: Long = 24L * 60 * 60 * 1000,
    val bars: List<PeriodStat> = emptyList(),
    val barLabels: List<String> = emptyList(),
    val loading: Boolean = true
)

class MainViewModel(private val repository: StatsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadPeriod(Period.DAILY)
    }

    fun selectPeriod(period: Period) {
        loadPeriod(period)
    }

    fun refresh() {
        loadPeriod(_uiState.value.period)
    }

    private fun loadPeriod(period: Period) {
        _uiState.value = _uiState.value.copy(period = period, loading = true)
        viewModelScope.launch {
            val now = Calendar.getInstance()
            when (period) {
                Period.DAILY -> {
                    val (start, end) = repository.dayBounds(now)
                    val total = repository.getConnectedDuration(start, end)
                    val bars = repository.lastNDays(7)
                    val labels = last7DayLabels()
                    emit(period, total, end - start, bars, labels)
                }
                Period.WEEKLY -> {
                    val (start, end) = repository.weekBounds(now)
                    val total = repository.getConnectedDuration(start, end)
                    val bars = repository.lastNWeeks(8)
                    val labels = bars.mapIndexed { i, _ -> "সপ্তাহ ${i + 1}" }
                    emit(period, total, end - start, bars, labels)
                }
                Period.MONTHLY -> {
                    val (start, end) = repository.monthBounds(now)
                    val total = repository.getConnectedDuration(start, end)
                    val bars = repository.lastNMonths(12)
                    val labels = last12MonthLabels()
                    emit(period, total, end - start, bars, labels)
                }
                Period.YEARLY -> {
                    val (start, end) = repository.yearBounds(now)
                    val total = repository.getConnectedDuration(start, end)
                    val bars = repository.lastNYears(5)
                    val labels = bars.map { yearLabelOf(it.rangeStart) }
                    emit(period, total, end - start, bars, labels)
                }
                Period.ALL_TIME -> {
                    val total = repository.getAllTimeDuration()
                    val firstStart = repository.getFirstSessionStart() ?: System.currentTimeMillis()
                    val range = (System.currentTimeMillis() - firstStart).coerceAtLeast(1)
                    val bars = repository.lastNYears(5)
                    val labels = bars.map { yearLabelOf(it.rangeStart) }
                    emit(period, total, range, bars, labels)
                }
            }
        }
    }

    private fun emit(
        period: Period,
        total: Long,
        range: Long,
        bars: List<PeriodStat>,
        labels: List<String>
    ) {
        _uiState.value = UiState(
            period = period,
            totalDurationMillis = total,
            totalRangeMillis = range,
            bars = bars,
            barLabels = labels,
            loading = false
        )
    }

    private fun last7DayLabels(): List<String> {
        val names = listOf("রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র", "শনি")
        val cal = Calendar.getInstance()
        val result = mutableListOf<String>()
        for (i in 6 downTo 0) {
            val c = cal.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, -i)
            result.add(names[c.get(Calendar.DAY_OF_WEEK) - 1])
        }
        return result
    }

    private fun last12MonthLabels(): List<String> {
        val names = listOf(
            "জানু", "ফেব্রু", "মার্চ", "এপ্রি", "মে", "জুন",
            "জুলা", "আগ", "সেপ্টে", "অক্টো", "নভে", "ডিসে"
        )
        val cal = Calendar.getInstance()
        val result = mutableListOf<String>()
        for (i in 11 downTo 0) {
            val c = cal.clone() as Calendar
            c.add(Calendar.MONTH, -i)
            result.add(names[c.get(Calendar.MONTH)])
        }
        return result
    }

    private fun yearLabelOf(millis: Long): String {
        val c = Calendar.getInstance().apply { timeInMillis = millis }
        return c.get(Calendar.YEAR).toString()
    }
}

class MainViewModelFactory(private val repository: StatsRepository) :
    androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository) as T
    }
}
