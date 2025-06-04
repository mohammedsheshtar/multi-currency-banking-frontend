package com.joincoded.bankapi

import android.app.Application
import com.joincoded.bankapi.network.RetrofitHelper

class BankApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitHelper.initialize(this)
    }
} 