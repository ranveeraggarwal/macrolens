package com.ranveeraggarwal.macrolens.photo

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Where meal photos live: the app's private files directory, never the camera roll.
 *
 * There is only one photo file for now. Each new snap overwrites it.
 */
class PhotoStorage(private val context: Context) {

    val mealPhotoFile: File = File(File(context.filesDir, "photos"), "meal.jpg")

    /** A content:// Uri that the camera app is allowed to write the photo to. */
    fun mealPhotoUriForCamera(): Uri {
        mealPhotoFile.parentFile?.mkdirs()
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", mealPhotoFile)
    }
}
