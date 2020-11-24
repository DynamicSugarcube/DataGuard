package com.schugarkub.dataguard.model

import android.content.Context
import androidx.room.*
import com.schugarkub.dataguard.database.notifications.NOTIFICATIONS_DATABASE_NAME
import com.schugarkub.dataguard.R
import com.schugarkub.dataguard.constants.NetworkTypeConstants.NETWORK_TYPE_MOBILE
import com.schugarkub.dataguard.constants.NetworkTypeConstants.NETWORK_TYPE_WIFI
import java.text.DateFormat
import java.util.*

private val DATE_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM)
private val TIME_FORMAT = DateFormat.getTimeInstance(DateFormat.SHORT)

@Entity(tableName = NOTIFICATIONS_DATABASE_NAME)
data class NotificationInfo(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "description_res_id")
    val descriptionResId: Int,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "package_name")
    val packageName: String,

    @ColumnInfo(name = "network_type")
    val networkType: Int
) {

    val formattedTimestamp: String
        get() {
            val date = Date(timestamp)
            return "${TIME_FORMAT.format(date)}, ${DATE_FORMAT.format(Date(timestamp))}"
        }

    fun getNetworkTypeString(context: Context): String {
        return when (networkType) {
            NETWORK_TYPE_MOBILE -> context.getString(R.string.network_type_mobile)
            NETWORK_TYPE_WIFI -> context.getString(R.string.network_type_wifi)
            else -> context.getString(R.string.network_type_unknown)
        }
    }
}