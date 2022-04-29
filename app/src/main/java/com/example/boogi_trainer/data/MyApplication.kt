package com.example.boogi_trainer.data

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class MyApplication : Application() {
    companion object {
        lateinit var prefs: PreferenceUtil
        lateinit var id: PreferenceUtil
        lateinit var uuid: PreferenceUtil
    }
    override fun onCreate() {
        prefs = PreferenceUtil(applicationContext)
        id = PreferenceUtil(applicationContext)
        uuid = PreferenceUtil(applicationContext)
        super.onCreate()
    }
}

class PreferenceUtil(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("token", Context.MODE_PRIVATE)
    private val id: SharedPreferences = context.getSharedPreferences("id",Context.MODE_PRIVATE)
    private val uuid: SharedPreferences = context.getSharedPreferences("uuid",Context.MODE_PRIVATE)
    fun getString(key: String, defValue: String): String {
        return prefs.getString(key, defValue).toString()
    }
    fun setString(key: String, str: String) {
        prefs.edit().putString(key, str).apply()
    }
    fun getId(key: String, defValue: String): String {
        return id.getString(key, defValue).toString()
    }
    fun setId(key: String, str: String) {
        id.edit().putString(key, str).apply()
    }
    fun getUid(key: String, defValue: String): String {
        return uuid.getString(key, defValue).toString()
    }
    fun setUid(key: String, str: String) {
        uuid.edit().putString(key, str).apply()
    }
}
