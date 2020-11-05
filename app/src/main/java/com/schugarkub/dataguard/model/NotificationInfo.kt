package com.schugarkub.dataguard.model

import androidx.room.*
import com.schugarkub.dataguard.database.NOTIFICATIONS_DATABASE_NAME
import java.text.DateFormat
import java.util.*

private val DATE_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM)

@Entity(tableName = NOTIFICATIONS_DATABASE_NAME)
@TypeConverters(Converter::class)
data class NotificationInfo(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "package_name")
    val packageName: String,

    @ColumnInfo(name = "network_type")
    val networkType: NetworkType
) {

    val formattedTimestamp: String
        get() = DATE_FORMAT.format(Date(timestamp))

    enum class NetworkType(val value: String) {
        MOBILE("Mobile"),
        WIFI("Wi-Fi"),
        UNKNOWN("Unknown")
    }
}

class Converter {

    @TypeConverter
    fun convertNetworkTypeToString(networkType: NotificationInfo.NetworkType?): String {
        return when(networkType) {
            NotificationInfo.NetworkType.MOBILE -> NotificationInfo.NetworkType.MOBILE.value
            NotificationInfo.NetworkType.WIFI -> NotificationInfo.NetworkType.WIFI.value
            else -> NotificationInfo.NetworkType.UNKNOWN.value
        }
    }

    @TypeConverter
    fun convertStringToNetworkType(string: String?): NotificationInfo.NetworkType {
        return when (string) {
            NotificationInfo.NetworkType.MOBILE.value -> NotificationInfo.NetworkType.MOBILE
            NotificationInfo.NetworkType.WIFI.value -> NotificationInfo.NetworkType.WIFI
            else -> NotificationInfo.NetworkType.UNKNOWN
        }
    }
}