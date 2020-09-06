package com.merpati.durgence.utils.permission

import android.app.Activity
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.MutableMap
import kotlin.collections.indices
import kotlin.collections.isNotEmpty
import kotlin.collections.toTypedArray

class Permissions(activity: Activity, permissionCallback: PermissionResultCallback) {
    private var currentActivity = activity
    private var permissionResultCallback = permissionCallback
    var permissionList = java.util.ArrayList<String>()
    var permissionsNeeded =
        java.util.ArrayList<String>()
    var dialogs = ""
    var code = 0

    // Check the API Level & Permission
    fun checkPermissions(permissions: ArrayList<String>, dialog: String, requestCode: Int) {
        permissionList = permissions
        dialogs = dialog
        code = requestCode

        if (Build.VERSION.SDK_INT >= 23)
            if (checkAndRequestPermissions(permissions, requestCode)) {
                permissionResultCallback.permissionGranted(requestCode)
                Log.i("Permissions", "granted")
            } else {
                permissionResultCallback.permissionGranted(requestCode)
                Log.i("Permissions", "granted")
            }

    }

    // Process result of user permission interaction
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> if (grantResults.isNotEmpty()) {
                val permissionMap: MutableMap<String, Int> = HashMap()
                run {
                    var index = 0
                    while (index < permissions.size) {
                        permissionMap[permissions[index]] = grantResults[index]
                        index++
                    }
                }

                val pendingPermissions = ArrayList<String>()
                var i = 0
                while (i < permissionsNeeded.size) {
                    if (permissionMap[permissionsNeeded[i]] != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                currentActivity,
                                permissionsNeeded[i]
                            )
                        ) pendingPermissions.add(permissionsNeeded[i]) else {
                            Log.i("Permissions", "Failed! Enable permissions!")

                            permissionResultCallback.neverAskAgain(code)
                            Toast.makeText(
                                currentActivity,
                                "Go to settings and enable permissions",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }
                    }
                    i++
                }
                if (pendingPermissions.size > 0) {
                    showMessage(dialogs) { _, type ->
                        when (type) {
                            DialogInterface.BUTTON_POSITIVE -> checkPermissions(
                                permissionList,
                                dialogs,
                                code
                            )
                            DialogInterface.BUTTON_NEGATIVE -> {
                                Log.i("Permissons", "not fully given")
                                if (permissionList.size == pendingPermissions.size)
                                    permissionResultCallback.permissionDenied(code)
                                else permissionResultCallback.partialPermissionGranted(
                                    code,
                                    pendingPermissions
                                )
                            }
                        }
                    }
                } else {
                    Log.i("Permissions", "permissions granted")
                    permissionResultCallback.permissionGranted(code)
                }
            }
        }
    }


    // Check and request the Permissions
    private fun checkAndRequestPermissions(
        permissions: ArrayList<String>,
        requestCode: Int
    ): Boolean {
        if (permissions.size > 0) {
            permissionsNeeded = ArrayList()

            // Get every permission needed
            for (i in permissions.indices) {
                val getPermission =
                    ContextCompat.checkSelfPermission(currentActivity, permissions[i])
                if (getPermission != PackageManager.PERMISSION_GRANTED)
                    permissionsNeeded.add(permissions[i])
            }
            if (permissionsNeeded.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    currentActivity,
                    permissionsNeeded.toTypedArray(),
                    requestCode
                )
                return false
            }
        }
        return true
    }

    // Display message why the app needs permissions
    private fun showMessage(message: String, dialog: DialogInterface.OnClickListener) {
        MaterialAlertDialogBuilder(currentActivity).apply {
            setMessage(message)
            setPositiveButton("Lanjutkan", dialog)
            setNegativeButton("Batalkan", dialog)
            create()
            show()
        }
    }
}