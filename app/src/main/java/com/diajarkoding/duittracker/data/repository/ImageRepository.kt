package com.diajarkoding.duittracker.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.hours

private const val TAG = "ImageRepository"

@Singleton
class ImageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val supabaseClient: SupabaseClient
) {
    companion object {
        private const val BUCKET_NAME = "receipts"
    }

    private fun getCurrentUserId(): String? {
        return supabaseClient.auth.currentUserOrNull()?.id
    }

    suspend fun uploadImage(imageUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        Log.d(TAG, "uploadImage: Starting upload for URI: $imageUri")
        try {
            val userId = getCurrentUserId()
            Log.d(TAG, "uploadImage: Current user ID: $userId")
            
            if (userId == null) {
                Log.e(TAG, "uploadImage: User not authenticated")
                return@withContext Result.failure(Exception("User not authenticated"))
            }

            Log.d(TAG, "uploadImage: Opening input stream...")
            val inputStream = context.contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                Log.e(TAG, "uploadImage: Cannot open input stream for URI: $imageUri")
                return@withContext Result.failure(Exception("Cannot read image"))
            }

            val bytes = inputStream.readBytes()
            inputStream.close()
            Log.d(TAG, "uploadImage: Read ${bytes.size} bytes from image")

            val fileName = "${userId}/${UUID.randomUUID()}.jpg"
            Log.d(TAG, "uploadImage: Uploading to bucket '$BUCKET_NAME' with path: $fileName")

            supabaseClient.storage.from(BUCKET_NAME).upload(fileName, bytes) {
                upsert = false
            }

            Log.d(TAG, "uploadImage: Upload successful! Path: $fileName")
            Result.success(fileName)
        } catch (e: Exception) {
            Log.e(TAG, "uploadImage: Upload failed with exception", e)
            Log.e(TAG, "uploadImage: Error message: ${e.message}")
            Log.e(TAG, "uploadImage: Error cause: ${e.cause}")
            Result.failure(e)
        }
    }

    suspend fun getImageUrl(imagePath: String): String? = withContext(Dispatchers.IO) {
        Log.d(TAG, "getImageUrl: Getting signed URL for path: $imagePath")
        try {
            if (imagePath.isBlank()) {
                Log.d(TAG, "getImageUrl: Path is blank, returning null")
                return@withContext null
            }
            
            // Use signed URL with 1 hour expiration
            val url = supabaseClient.storage.from(BUCKET_NAME)
                .createSignedUrl(imagePath, 1.hours)
            Log.d(TAG, "getImageUrl: Got signed URL: $url")
            url
        } catch (e: Exception) {
            Log.e(TAG, "getImageUrl: Failed to get signed URL", e)
            Log.e(TAG, "getImageUrl: Error: ${e.message}")
            null
        }
    }

    suspend fun deleteImage(imagePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        Log.d(TAG, "deleteImage: Deleting image at path: $imagePath")
        try {
            if (imagePath.isBlank()) {
                Log.d(TAG, "deleteImage: Path is blank, skipping delete")
                return@withContext Result.success(Unit)
            }
            
            supabaseClient.storage.from(BUCKET_NAME).delete(imagePath)
            Log.d(TAG, "deleteImage: Successfully deleted image")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "deleteImage: Failed to delete image", e)
            Result.failure(e)
        }
    }

    suspend fun saveImageLocally(imageUri: Uri, transactionId: String): Result<String> = withContext(Dispatchers.IO) {
        Log.d(TAG, "saveImageLocally: Saving image locally for transaction: $transactionId")
        Log.d(TAG, "saveImageLocally: Source URI: $imageUri")
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                Log.e(TAG, "saveImageLocally: Cannot open input stream")
                return@withContext Result.failure(Exception("Cannot read image"))
            }

            val imagesDir = File(context.filesDir, "transaction_images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
                Log.d(TAG, "saveImageLocally: Created images directory: ${imagesDir.absolutePath}")
            }

            val localFile = File(imagesDir, "${transactionId}.jpg")
            Log.d(TAG, "saveImageLocally: Saving to: ${localFile.absolutePath}")
            
            localFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()

            Log.d(TAG, "saveImageLocally: Successfully saved image locally")
            Result.success(localFile.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "saveImageLocally: Failed to save image locally", e)
            Result.failure(e)
        }
    }

    fun getLocalImagePath(transactionId: String): String? {
        val imagesDir = File(context.filesDir, "transaction_images")
        val localFile = File(imagesDir, "${transactionId}.jpg")
        return if (localFile.exists()) localFile.absolutePath else null
    }

    fun deleteLocalImage(transactionId: String): Boolean {
        Log.d(TAG, "deleteLocalImage: Deleting local image for transaction: $transactionId")
        val imagesDir = File(context.filesDir, "transaction_images")
        val localFile = File(imagesDir, "${transactionId}.jpg")
        return if (localFile.exists()) {
            val deleted = localFile.delete()
            Log.d(TAG, "deleteLocalImage: Deleted: $deleted")
            deleted
        } else {
            Log.d(TAG, "deleteLocalImage: File does not exist")
            true
        }
    }
}
