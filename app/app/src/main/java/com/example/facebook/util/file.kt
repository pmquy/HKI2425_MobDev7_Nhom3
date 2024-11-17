package com.example.facebook.util

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@RequiresApi(Build.VERSION_CODES.Q)
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