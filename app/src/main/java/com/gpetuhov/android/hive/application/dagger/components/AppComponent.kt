package com.gpetuhov.android.hive.application.dagger.components

import com.gpetuhov.android.hive.application.dagger.modules.AppModule
import com.gpetuhov.android.hive.ui.activity.MainActivity
import com.gpetuhov.android.hive.ui.fragment.MapFragment
import dagger.Component
import javax.inject.Singleton

@Component(modules = [AppModule::class])
@Singleton
interface AppComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(mapFragment: MapFragment)
}