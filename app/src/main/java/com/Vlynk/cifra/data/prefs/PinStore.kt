package com.Vlynk.cifra.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.pinDataStore: DataStore<Preferences> by preferencesDataStore(name = "pin_store")

/**
 * Persists the PIN hash and salt using DataStore Preferences.
 * The raw PIN is **never** written to disk — only the hex-encoded hash + salt.
 */
class PinStore(private val context: Context) {

    companion object {
        private val KEY_PIN_HASH = stringPreferencesKey("pin_hash")
        private val KEY_PIN_SALT = stringPreferencesKey("pin_salt")
    }

    /** Emits `true` when a PIN hash is already stored. */
    val hasPin: Flow<Boolean> = context.pinDataStore.data.map { prefs ->
        prefs[KEY_PIN_HASH] != null && prefs[KEY_PIN_SALT] != null
    }

    /** Returns the stored hex-encoded hash, or `null` if not set. */
    suspend fun getHash(): String? =
        context.pinDataStore.data.map { it[KEY_PIN_HASH] }.first()

    /** Returns the stored hex-encoded salt, or `null` if not set. */
    suspend fun getSalt(): String? =
        context.pinDataStore.data.map { it[KEY_PIN_SALT] }.first()

    /** Persists [hashHex] and [saltHex]. Overwrites any previous values. */
    suspend fun save(hashHex: String, saltHex: String) {
        context.pinDataStore.edit { prefs ->
            prefs[KEY_PIN_HASH] = hashHex
            prefs[KEY_PIN_SALT] = saltHex
        }
    }

    /** Clears the stored PIN (used when resetting the vault). */
    suspend fun clear() {
        context.pinDataStore.edit { prefs ->
            prefs.remove(KEY_PIN_HASH)
            prefs.remove(KEY_PIN_SALT)
        }
    }
}
