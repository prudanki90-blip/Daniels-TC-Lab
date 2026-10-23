
package com.tclab.android.feature.protocols.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

/**
 * Local JSON repository for the Protocols feature.
 *
 * Stores protocols independently of all other TC Lab features.
 * This first version uses SharedPreferences and is suitable for
 * a modest offline protocol catalogue.
 */
class ProtocolRepository(context: Context) {

    private val preferences = context.applicationContext
        .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val gson = Gson()

    @Synchronized
    fun getAll(): List<Protocol> {
        val json = preferences.getString(KEY_PROTOCOLS, null)
            ?: return emptyList()

        return try {
            val type = object : TypeToken<List<Protocol>>() {}.type
            gson.fromJson<List<Protocol>>(json, type) ?: emptyList()
        } catch (_: JsonSyntaxException) {
            emptyList()
        } catch (_: IllegalStateException) {
            emptyList()
        }
    }

    fun getById(id: String): Protocol? =
        getAll().firstOrNull { it.id == id }

    @Synchronized
    fun save(protocol: Protocol): Protocol {
        val now = System.currentTimeMillis()
        val existing = getById(protocol.id)

        val saved = protocol.copy(
            createdAtEpochMillis =
                existing?.createdAtEpochMillis
                    ?: protocol.createdAtEpochMillis,
            updatedAtEpochMillis = now
        )

        val updatedList = getAll()
            .filterNot { it.id == saved.id }
            .plus(saved)

        writeAll(updatedList)
        return saved
    }

    @Synchronized
    fun delete(id: String): Boolean {
        val current = getAll()
        val updated = current.filterNot { it.id == id }

        if (updated.size == current.size) {
            return false
        }

        writeAll(updated)
        return true
    }

    @Synchronized
    fun replaceAll(protocols: List<Protocol>) {
        require(protocols.map { it.id }.distinct().size == protocols.size) {
            "Protocol IDs must be unique."
        }
        writeAll(protocols)
    }

    fun count(): Int = getAll().size

    @Synchronized
    private fun writeAll(protocols: List<Protocol>) {
        val json = gson.toJson(protocols)
        val success = preferences.edit()
            .putString(KEY_PROTOCOLS, json)
            .commit()

        check(success) {
            "Could not save the protocol catalogue to local storage."
        }
    }

    companion object {
        private const val PREFERENCES_NAME =
            "tc_lab_protocols_storage"

        private const val KEY_PROTOCOLS =
            "saved_protocols_json"
    }
}
