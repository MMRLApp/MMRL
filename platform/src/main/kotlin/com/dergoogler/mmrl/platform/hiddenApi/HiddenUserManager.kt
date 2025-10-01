@file:Suppress("UNCHECKED_CAST", "UnusedReceiverParameter", "MemberVisibilityCanBePrivate")

package com.dergoogler.mmrl.platform.hiddenApi

import android.content.Context
import android.content.pm.UserInfo
import android.os.IUserManager
import android.os.Parcel
import android.os.Process
import android.os.RemoteException
import android.os.UserHandle
import android.util.Log
import android.util.SparseArray
import androidx.core.util.size
import com.dergoogler.mmrl.platform.PlatformManager.getSystemService
import com.dergoogler.mmrl.platform.stub.IServiceManager
import java.util.Random


class HiddenUserManager(
    private val service: IServiceManager,
) {
    private val userManager by lazy {
        IUserManager.Stub.asInterface(
            service.getSystemService(Context.USER_SERVICE)
        )
    }

    @Throws(RemoteException::class)
    fun getUsers(
        excludePartial: Boolean = true,
        excludeDying: Boolean = true,
        excludePreCreated: Boolean = true,
    ): List<UserInfo> {
        try {
            return userManager::class.java
                .getMethod(
                    "getUsers",
                    Boolean::class.javaPrimitiveType,
                    Boolean::class.javaPrimitiveType,
                    Boolean::class.javaPrimitiveType
                )
                .invoke(
                    userManager,
                    excludePartial,
                    excludeDying,
                    excludePreCreated
                ) as List<UserInfo>
        } catch (e: NoSuchMethodException) {
            Log.w("HiddenUserManager", "Error getting users, falling back to old method.", e)

            return try {
                userManager::class.java
                    .getMethod(
                        "getUsers",
                        Boolean::class.javaPrimitiveType,
                        Boolean::class.javaPrimitiveType
                    )
                    .invoke(userManager, excludePartial, excludeDying) as List<UserInfo>
            } catch (e2: NoSuchMethodException) {
                Log.w("HiddenUserManager", "Error getting users, falling back to old method.", e2)

                userManager::class.java
                    .getMethod("getUsers", Boolean::class.javaPrimitiveType)
                    .invoke(userManager, excludePartial) as List<UserInfo>
            }
        }
    }

    @Throws(RemoteException::class)
    fun getUsers(): List<UserInfo> = getUsers(
        excludePartial = true,
        excludeDying = true,
        excludePreCreated = true
    )

    fun getProfileIds(userId: Int, enabledOnly: Boolean): IntArray {
        return try {
            userManager.getProfileIds(userId, enabledOnly)
        } catch (e: RemoteException) {
            Log.w("HiddenUserManager", "Error getting profile ids.", e)
            IntArray(0)
        }
    }

    fun getUserProfiles(): List<UserHandle> {
        val userIds: IntArray = getProfileIds(myUserId, true)
        return convertUserIdsToUserHandles(userIds)
    }

    fun getUserProfiles(userId: Int): List<UserHandle> = getUserProfiles(userId, true)

    fun getUserProfiles(enabledOnly: Boolean): List<UserHandle> = getUserProfiles(myUserId, enabledOnly)

    fun getUserProfiles(userId: Int, enabledOnly: Boolean): List<UserHandle> {
        val userIds: IntArray = userManager.getProfileIds(userId, enabledOnly)
        return convertUserIdsToUserHandles(userIds)
    }

    fun getUserInfo(userId: Int): UserInfo = userManager.getUserInfo(userId)

    fun getUserId(uid: Int): Int = uid / PER_USER_RANGE

    val myUserId get(): Int = getUserId(Process.myUid())

    fun isSameUser(uid1: Int, uid2: Int): Boolean = getUserId(uid1) == getUserId(uid2)

    private fun convertUserIdsToUserHandles(userIds: IntArray): List<UserHandle> {
        val result: MutableList<UserHandle> = ArrayList(userIds.size)

        for (userId in userIds) {
            result.add(of(userId))
        }

        return result
    }

    companion object {
        const val PER_USER_RANGE: Int = 100000

        const val MU_ENABLED: Boolean = true
        val NUM_CACHED_USERS = if (MU_ENABLED) 8 else 0;

        val CACHED_USER_HANDLES: Array<UserHandle?> =
            arrayOfNulls(NUM_CACHED_USERS)

        val extraUserHandleCache: SparseArray<UserHandle> = SparseArray(0)

        const val USER_OWNER: Int = 0
        const val USER_SYSTEM: Int = 0
        const val USER_NULL: Int = -10000
        const val USER_ALL: Int = -1
        const val USER_CURRENT: Int = -2
        const val USER_CURRENT_OR_SELF: Int = -3
        const val MIN_SECONDARY_USER_ID: Int = 10
        const val MAX_EXTRA_USER_HANDLE_CACHE_SIZE: Int = 32

        init {
            for (i in CACHED_USER_HANDLES.indices) {
                CACHED_USER_HANDLES[i] = userHandle(MIN_SECONDARY_USER_ID + i)
            }
        }

        val OWNER: UserHandle = userHandle(USER_OWNER)
        val SYSTEM: UserHandle = userHandle(USER_SYSTEM)
        val ALL: UserHandle = userHandle(USER_ALL)
        val NULL: UserHandle = userHandle(USER_NULL)
        val CURRENT: UserHandle = userHandle(USER_CURRENT)
        val CURRENT_OR_SELF: UserHandle = userHandle(USER_CURRENT_OR_SELF)

        fun of(userId: Int): UserHandle {
            if (userId == USER_SYSTEM) {
                return SYSTEM // Most common.
            }

            // These are sequential; so use a switch. Maybe they'll be optimized to a table lookup.
            when (userId) {
                USER_ALL -> return ALL
                USER_CURRENT -> return CURRENT
                USER_CURRENT_OR_SELF -> return CURRENT_OR_SELF
            }

            if (userId >= MIN_SECONDARY_USER_ID
                && userId < (MIN_SECONDARY_USER_ID + CACHED_USER_HANDLES.size)
            ) {
                return CACHED_USER_HANDLES[userId - MIN_SECONDARY_USER_ID] ?: NULL
            }

            if (userId == USER_NULL) { // Not common.
                return NULL
            }

            return getUserHandleFromExtraCache(userId)
        }

        fun getUserHandleFromExtraCache(userId: Int): UserHandle {
            synchronized(extraUserHandleCache) {
                val extraCached: UserHandle? = extraUserHandleCache.get(userId)
                if (extraCached != null) {
                    return extraCached
                }
                if (extraUserHandleCache.size >= MAX_EXTRA_USER_HANDLE_CACHE_SIZE) {
                    extraUserHandleCache.removeAt(
                        (Random()).nextInt(MAX_EXTRA_USER_HANDLE_CACHE_SIZE)
                    )
                }
                val newHandle = userHandle(userId)
                extraUserHandleCache.put(userId, newHandle)
                return newHandle
            }
        }

        fun userHandle(userId: Int): UserHandle {
            val parcel = Parcel.obtain()
            parcel.writeInt(userId)
            parcel.setDataPosition(0)


            val handle: UserHandle = UserHandle.CREATOR.createFromParcel(parcel)
            parcel.recycle()
            return handle
        }
    }
}

