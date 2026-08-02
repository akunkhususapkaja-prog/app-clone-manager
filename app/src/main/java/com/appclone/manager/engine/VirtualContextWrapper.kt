package com.appclone.manager.engine

import android.content.Context
import android.content.ContextWrapper
import java.io.File

/**
 * VirtualContextWrapper - Wraps the host context and redirects
 * file operations to the virtual instance's data directory.
 * 
 * This is the core of data isolation - each virtual instance
 * gets its own isolated file system view while sharing the
 * same process space.
 */
class VirtualContextWrapper(
    base: Context,
    private val instance: VirtualInstance
) : ContextWrapper(base) {

    /**
     * Override getFilesDir to return virtual files directory
     */
    override fun getFilesDir(): File = instance.virtualFilesDir

    /**
     * Override getCacheDir to return virtual cache directory
     */
    override fun getCacheDir(): File = instance.virtualCacheDir

    /**
     * Override getDatabasePath to return virtual database path
     */
    override fun getDatabasePath(name: String?): File =
        File(instance.virtualDatabaseDir, name ?: "default.db")

    /**
     * Override getSharedPreferences to use virtual preferences directory
     */
    override fun getSharedPreferences(name: String?, mode: Int) =
        super.getSharedPreferences("${instance.virtualPackageId}_$name", mode)

    /**
     * Override getDir to create directories in virtual space
     */
    override fun getDir(name: String?, mode: Int): File {
        val dir = File(instance.virtualDataDir, name ?: "default")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Create a virtual context for a specific instance
     */
    companion object {
        fun create(base: Context, instance: VirtualInstance): VirtualContextWrapper {
            return VirtualContextWrapper(base, instance)
        }
    }
}
