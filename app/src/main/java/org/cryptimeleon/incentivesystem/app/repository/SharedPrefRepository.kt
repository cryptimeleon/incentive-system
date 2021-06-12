package org.cryptimeleon.incentivesystem.app.repository

import android.content.Context
import android.content.SharedPreferences

/**
 * Some functions that extend string to make the api a little nices
 * See: https://arkapp.medium.com/how-to-use-shared-preferences-the-easy-and-fastest-way-98ce2013bf51
 */
open class SharedPrefRepository(context: Context, prefName: String) {

    private val pref: SharedPreferences =
        context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    private val editor = pref.edit()


    protected fun String.put(string: String) {
        editor.putString(this, string)
        editor.commit()
    }

    protected fun String.put(boolean: Boolean) {
        editor.putBoolean(this, boolean)
        editor.commit()
    }

    protected fun String.getString() = pref.getString(this, "")!!

    protected fun String.getBoolean() = pref.getBoolean(this, false)
}