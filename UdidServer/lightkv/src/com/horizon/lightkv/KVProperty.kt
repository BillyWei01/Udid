package com.horizon.lightkv

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


class KVProperty<T>(private val key: Int) : ReadWriteProperty<KVData, T> {

    @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
    override operator fun getValue(thisRef: KVData, property: KProperty<*>): T = with(thisRef.data) {
        return when (key and DataType.MASK) {
            DataType.BOOLEAN -> getBoolean(key)
            DataType.INT -> getInt(key)
            DataType.FLOAT -> getFloat(key)
            DataType.LONG -> getLong(key)
            DataType.DOUBLE -> getDouble(key)
            DataType.STRING -> getString(key)
            DataType.ARRAY -> getArray(key)
            else -> throw IllegalArgumentException("Invalid Key: $key")
        } as T
    }

    override operator fun setValue(thisRef: KVData, property: KProperty<*>, value: T)  = with(thisRef.data) {
        when (key and DataType.MASK) {
            DataType.BOOLEAN -> putBoolean(key, value as Boolean)
            DataType.INT -> putInt(key, value as Int)
            DataType.FLOAT -> putFloat(key, value as Float)
            DataType.LONG -> putLong(key, value as Long)
            DataType.DOUBLE -> putDouble(key, value as Double)
            DataType.STRING -> putString(key, value as String)
            DataType.ARRAY -> putArray(key, value as ByteArray)
            else -> throw IllegalArgumentException("Invalid Key: $key")
        }
        if(mMode == LightKV.SYNC_MODE && thisRef.autoCommit){
            commit()
        }
    }
}