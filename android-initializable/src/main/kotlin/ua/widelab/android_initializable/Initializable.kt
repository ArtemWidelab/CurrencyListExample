package ua.widelab.android_initializable

import android.app.Application

interface Initializable {
    fun init(application: Application)
}