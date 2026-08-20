package com.wifitracker.app.data

import java.util.Calendar

/**
 * দিনের সীমানা ব্যবহারকারীর চাহিদা অনুযায়ী:
 * রাত ১২:০০:০১ সেকেন্ড থেকে পরের রাত ১২:০০:০০ পর্যন্ত একটি দিন।
 * অর্থাৎ প্রতিটি দিন শুরু হয় 00:00:01.000 এ এবং শেষ হয় পরের দিনের 00:00:01.000 এর ঠিক আগে (exclusive)।
 */
class StatsRepository(private val dao: WifiSessionDao) {

    private fun startOfCalendarDay(cal: Calendar): Calendar {
        val c = cal.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 1)
        c.set(Calendar.MILLISECOND, 0)
        return c
    }

    /** যে দিনটির মধ্যে [cal] পড়ে, তার [start, end) রেঞ্জ ফেরত দেয়। */
    fun dayBounds(cal: Calendar): Pair<Long, Long> {
        var start = startOfCalendarDay(cal)
        // যদি সময়টি 00:00:00.000 থেকে 00:00:01.000 এর মধ্যে পড়ে, তাহলে এটি আগের দিনের অংশ
        if (cal.timeInMillis < start.timeInMillis) {
            start = start.clone() as Calendar
            start.add(Calendar.DAY_OF_YEAR, -1)
        }
        val end = start.clone() as Calendar
        end.add(Calendar.DAY_OF_YEAR, 1)
        return start.timeInMillis to end.timeInMillis
    }

    fun weekBounds(cal: Calendar): Pair<Long, Long> {
        val c = cal.clone() as Calendar
        c.set(Calendar.DAY_OF_WEEK, c.firstDayOfWeek)
        val (start, _) = dayBounds(c)
        val startCal = Calendar.getInstance().apply { timeInMillis = start }
        val end = startCal.clone() as Calendar
        end.add(Calendar.DAY_OF_YEAR, 7)
        return start to end.timeInMillis
    }

    fun monthBounds(cal: Calendar): Pair<Long, Long> {
        val c = cal.clone() as Calendar
        c.set(Calendar.DAY_OF_MONTH, 1)
        val (start, _) = dayBounds(c)
        val startCal = Calendar.getInstance().apply { timeInMillis = start }
        val end = startCal.clone() as Calendar
        end.add(Calendar.MONTH, 1)
        return start to end.timeInMillis
    }

    fun yearBounds(cal: Calendar): Pair<Long, Long> {
        val c = cal.clone() as Calendar
        c.set(Calendar.DAY_OF_YEAR, 1)
        val (start, _) = dayBounds(c)
        val startCal = Calendar.getInstance().apply { timeInMillis = start }
        val end = startCal.clone() as Calendar
        end.add(Calendar.YEAR, 1)
        return start to end.timeInMillis
    }

    /** [rangeStart, rangeEnd) সময়ে মোট কতক্ষণ WiFi সংযুক্ত ছিল (মিলিসেকেন্ডে)। */
    suspend fun getConnectedDuration(rangeStart: Long, rangeEnd: Long): Long {
        val sessions = dao.getSessionsInRange(rangeStart, rangeEnd)
        var total = 0L
        for (s in sessions) {
            val overlapStart = maxOf(s.startTime, rangeStart)
            val overlapEnd = minOf(s.endTime, rangeEnd)
            if (overlapEnd > overlapStart) total += (overlapEnd - overlapStart)
        }
        return total
    }

    /** সর্বমোট (all-time) হিসাবের জন্য প্রথম সেশনের শুরু থেকে এখন পর্যন্ত। */
    suspend fun getAllTimeDuration(): Long {
        val firstStart = dao.getFirstSessionStart() ?: return 0L
        return getConnectedDuration(firstStart, System.currentTimeMillis() + 1)
    }

    suspend fun getFirstSessionStart(): Long? = dao.getFirstSessionStart()

    /** শেষ [count]টি দিনের প্রতিটির জন্য (label, durationMillis) তালিকা, পুরনো থেকে নতুন ক্রমে। */
    suspend fun lastNDays(count: Int, referenceTime: Long = System.currentTimeMillis()): List<PeriodStat> {
        val result = mutableListOf<PeriodStat>()
        val ref = Calendar.getInstance().apply { timeInMillis = referenceTime }
        for (i in (count - 1) downTo 0) {
            val c = ref.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, -i)
            val (start, end) = dayBounds(c)
            val duration = getConnectedDuration(start, end)
            result.add(PeriodStat(start, end, duration))
        }
        return result
    }

    suspend fun lastNWeeks(count: Int, referenceTime: Long = System.currentTimeMillis()): List<PeriodStat> {
        val result = mutableListOf<PeriodStat>()
        val ref = Calendar.getInstance().apply { timeInMillis = referenceTime }
        for (i in (count - 1) downTo 0) {
            val c = ref.clone() as Calendar
            c.add(Calendar.WEEK_OF_YEAR, -i)
            val (start, end) = weekBounds(c)
            val duration = getConnectedDuration(start, end)
            result.add(PeriodStat(start, end, duration))
        }
        return result
    }

    suspend fun lastNMonths(count: Int, referenceTime: Long = System.currentTimeMillis()): List<PeriodStat> {
        val result = mutableListOf<PeriodStat>()
        val ref = Calendar.getInstance().apply { timeInMillis = referenceTime }
        for (i in (count - 1) downTo 0) {
            val c = ref.clone() as Calendar
            c.add(Calendar.MONTH, -i)
            val (start, end) = monthBounds(c)
            val duration = getConnectedDuration(start, end)
            result.add(PeriodStat(start, end, duration))
        }
        return result
    }

    suspend fun lastNYears(count: Int, referenceTime: Long = System.currentTimeMillis()): List<PeriodStat> {
        val result = mutableListOf<PeriodStat>()
        val ref = Calendar.getInstance().apply { timeInMillis = referenceTime }
        for (i in (count - 1) downTo 0) {
            val c = ref.clone() as Calendar
            c.add(Calendar.YEAR, -i)
            val (start, end) = yearBounds(c)
            val duration = getConnectedDuration(start, end)
            result.add(PeriodStat(start, end, duration))
        }
        return result
    }
}

data class PeriodStat(
    val rangeStart: Long,
    val rangeEnd: Long,
    val durationMillis: Long
)
