package com.example.qcinline

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

data class DefectData(
    var line: String = "",
    var defectType: String = "",
    var defectArea: String = "",
    var qtyInspect: String = "",
    var qtyDefect: String = "",
    val photoUris: MutableList<Uri> = mutableListOf()
)

class QCViewModel : ViewModel() {
    val selectedRound = mutableStateOf(1)
    val nik = mutableStateOf("")
    val operatorName = mutableStateOf("")
    val latitude = mutableStateOf<Double?>(null)
    val longitude = mutableStateOf<Double?>(null)
    
    val defects = mutableStateListOf(DefectData())

    val isSubmitting = mutableStateOf(false)
    val submissionResult = mutableStateOf<String?>(null)

    fun addDefect() {
        defects.add(DefectData())
    }

    fun updateDefect(index: Int, updatedDefect: DefectData) {
        defects[index] = updatedDefect
    }

    fun addPhotoToDefect(defectIndex: Int, uri: Uri) {
        val updatedDefect = defects[defectIndex].copy()
        updatedDefect.photoUris.add(uri)
        defects[defectIndex] = updatedDefect
    }

    fun submitData(context: Context) {
        viewModelScope.launch {
            isSubmitting.value = true
            try {
                val sessionDataMap = mapOf(
                    "round" to selectedRound.value,
                    "nik" to nik.value,
                    "operator_name" to operatorName.value,
                    "latitude" to latitude.value,
                    "longitude" to longitude.value
                )

                val multipartPhotos = mutableListOf<MultipartBody.Part>()
                val defectsDataList = mutableListOf<Map<String, Any>>()
                
                var currentPhotoIndex = 0

                defects.forEach { defect ->
                    val photoIndexesForThisDefect = mutableListOf<Int>()
                    
                    defect.photoUris.forEach { uri ->
                        val file = getFileFromUri(context, uri)
                        if (file != null) {
                            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                            val body = MultipartBody.Part.createFormData("photos", file.name, requestFile)
                            multipartPhotos.add(body)
                            photoIndexesForThisDefect.add(currentPhotoIndex)
                            currentPhotoIndex++
                        }
                    }

                    defectsDataList.add(mapOf(
                        "line" to defect.line,
                        "defect_type" to defect.defectType,
                        "defect_area" to defect.defectArea,
                        "qty_inspect" to (defect.qtyInspect.toIntOrNull() ?: 0),
                        "qty_defect" to (defect.qtyDefect.toIntOrNull() ?: 0),
                        "photo_indexes" to photoIndexesForThisDefect
                    ))
                }

                val gson = Gson()
                val sessionDataJson = gson.toJson(sessionDataMap)
                val defectsDataJson = gson.toJson(defectsDataList)

                val sessionDataBody = sessionDataJson.toRequestBody("application/json".toMediaTypeOrNull())
                val defectsDataBody = defectsDataJson.toRequestBody("application/json".toMediaTypeOrNull())

                val response = NetworkClient.apiService.submitQC(sessionDataBody, defectsDataBody, multipartPhotos)
                submissionResult.value = "Success: ${response.message}"
            } catch (e: Exception) {
                e.printStackTrace()
                submissionResult.value = "Error: ${e.message}"
            } finally {
                isSubmitting.value = false
            }
        }
    }

    // Helper to convert content URI to physical file for Retrofit
    private fun getFileFromUri(context: Context, uri: Uri): File? {
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        var fileName = "temp_image_${System.currentTimeMillis()}.jpg"
        if (cursor != null && cursor.moveToFirst() && nameIndex != null) {
            fileName = cursor.getString(nameIndex)
        }
        cursor?.close()

        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
