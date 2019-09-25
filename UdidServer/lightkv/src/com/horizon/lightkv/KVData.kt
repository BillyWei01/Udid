package com.horizon.lightkv

/**
 * Kotlin support to LightKV, make it easier to use.
 *
 */
abstract class KVData {
    /**
     * auto commit flag for SYNC_MODE,
     * ASYNC_MODE will flush data by kernel
     */
    internal var autoCommit = true

    abstract val data: LightKV

    protected fun boolean(key: Int) = KVProperty<Boolean>(key or DataType.BOOLEAN)
    protected fun int(key: Int) = KVProperty<Int>(key or DataType.INT)
    protected fun float(key: Int) = KVProperty<Float>(key or DataType.FLOAT)
    protected fun double(key: Int) = KVProperty<Double>(key or DataType.DOUBLE)
    protected fun long(key: Int) = KVProperty<Long>(key or DataType.LONG)
    protected fun string(key: Int) = KVProperty<String>(key or DataType.STRING)
    protected fun array(key: Int) = KVProperty<ByteArray>(key or DataType.ARRAY)

    /**
     * In SYNC_MODE, if you need to commit key-values in one time,
     * it's recommend to disable auto commit,
     * and enable it after updating data..
     *
     * In ASYNC_MODE, it's unnecessary to commit in manually.
     */
    fun disableAutoCommit(){
        autoCommit = false
    }

    fun enableAutoCommit(){
        autoCommit = true
        data.commit()
    }
}