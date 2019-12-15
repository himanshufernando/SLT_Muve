package com.project.himanshu.sltmuve.utilities

import android.content.Context
import android.preference.PreferenceManager

object Utils {

    const val KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates"


    @JvmStatic
    fun requestingLocationUpdates(context: Context?): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false)
    }



}