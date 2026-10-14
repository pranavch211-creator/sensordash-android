package com.example.sensordash.data

/** Plain snapshots of the live readings. The UI only renders these; it never reads Android APIs. */
data class BatteryState(val percent: Int? = null, val status: String = "Reading...")

/** [detail] is already worded as an approximation, e.g. "approx. 10.9 / 15.9 GB". */
data class MemoryState(val percent: Float? = null, val detail: String = "Reading...")

/** [percent] is this app's CPU use as a percentage of ONE core (can exceed 100). */
data class CpuState(val percent: Float? = null, val note: String = "measuring...")

data class ClockState(
    val hour: Int = 0, val minute: Int = 0, val second: Int = 0,
    val time: String = "--:--:--", val date: String = "--", val weekday: String = "--",
)

data class DeviceInfoState(
    val platform: String = "--", val model: String = "--", val language: String = "--", val screenDp: String = "--",
)

data class DisplayState(
    val resolution: String = "--", val density: String = "--", val orientation: String = "--",
)

data class NetworkState(
    val connection: String = "Checking...", val status: String = "Checking...", val online: Boolean = false,
    val down: String = "Unavailable", val up: String = "Unavailable",
)

data class DeviceState(
    val battery: BatteryState = BatteryState(),
    val memory: MemoryState = MemoryState(),
    val cpu: CpuState = CpuState(),
    val clock: ClockState = ClockState(),
    val device: DeviceInfoState = DeviceInfoState(),
    val display: DisplayState = DisplayState(),
    val network: NetworkState = NetworkState(),
)
