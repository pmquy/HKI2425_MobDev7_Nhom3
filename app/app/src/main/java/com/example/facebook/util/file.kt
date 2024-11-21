package com.example.facebook.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date

fun uriToFile(context: Context, uri: Uri): Pair<File, String> {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    val fileName = cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.getString(nameIndex)
        } else {
            "temp_file"
        }
    } ?: "temp_file"

    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    val type = context.contentResolver.getType(uri)
    val file = File(context.cacheDir, fileName)
    val outputStream = FileOutputStream(file)
    inputStream?.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }
    Log.d("ChatGroupScreen", "File saved to ${file.absolutePath}")
    Log.d("ChatGroupScreen", "File type: $type")
    return Pair(file, type ?: "application/octet-stream")
}


@SuppressLint("SimpleDateFormat")
fun createImageFile(context: Context): File {
    // Create an image file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        "JPEG_${timeStamp}_", //prefix
        ".jpg", //suffix
        storageDir //directory
    )
}