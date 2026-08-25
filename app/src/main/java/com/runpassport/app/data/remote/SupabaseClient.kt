package com.runpassport.app.data.remote

import android.util.Log
import com.runpassport.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseClientProvider @Inject constructor() {

    private val supabaseUrl = BuildConfig.SUPABASE_URL
    private val supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY

    init {
        Log.d("SupabaseClient", "Supabase 클라이언트 초기화")
        Log.d("SupabaseClient", "URL: $supabaseUrl")
        Log.d("SupabaseClient", "AnonKey: ${supabaseAnonKey.take(20)}...")
    }

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = supabaseUrl,
        supabaseKey = supabaseAnonKey
    ) {
        install(Auth)
        install(Postgrest)
    }
}