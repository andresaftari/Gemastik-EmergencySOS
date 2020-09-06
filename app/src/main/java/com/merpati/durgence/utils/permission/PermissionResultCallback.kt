package com.merpati.durgence.utils.permission

interface PermissionResultCallback {
    fun permissionGranted(requestCode: Int)
    fun partialPermissionGranted(requestCode: Int, grantedPermissions: ArrayList<String>)
    fun permissionDenied(requestCode: Int)
    fun neverAskAgain(requestCode: Int)
}