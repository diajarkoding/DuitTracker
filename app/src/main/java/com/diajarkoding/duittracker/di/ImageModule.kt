 package com.diajarkoding.duittracker.di
 
 import android.content.Context
 import coil.ImageLoader
 import coil.disk.DiskCache
 import coil.memory.MemoryCache
 import coil.request.CachePolicy
 import coil.util.DebugLogger
 import com.diajarkoding.duittracker.BuildConfig
 import dagger.Module
 import dagger.Provides
 import dagger.hilt.InstallIn
 import dagger.hilt.android.qualifiers.ApplicationContext
 import dagger.hilt.components.SingletonComponent
 import javax.inject.Singleton
 
 /**
  * Hilt module for providing optimized image loading configuration.
  * 
  * Configures Coil ImageLoader with:
  * - Memory caching for fast access to recently loaded images
  * - Disk caching for persistent storage
  * - Optimized cache policies for performance
  */
 @Module
 @InstallIn(SingletonComponent::class)
 object ImageModule {
     
     /**
      * Provides a singleton ImageLoader with optimized caching configuration.
      * 
      * Memory cache: 25% of available memory (max 256MB)
      * Disk cache: 100MB in app's cache directory
      * 
      * @param context Application context
      * @return Configured ImageLoader instance
      */
     @Provides
     @Singleton
     fun provideImageLoader(
         @ApplicationContext context: Context
     ): ImageLoader {
         return ImageLoader.Builder(context)
             .memoryCache {
                 MemoryCache.Builder(context)
                     .maxSizePercent(0.25) // Use 25% of available memory
                     .build()
             }
             .diskCache {
                 DiskCache.Builder()
                     .directory(context.cacheDir.resolve("image_cache"))
                     .maxSizeBytes(100L * 1024 * 1024) // 100MB disk cache
                     .build()
             }
             .memoryCachePolicy(CachePolicy.ENABLED)
             .diskCachePolicy(CachePolicy.ENABLED)
             .networkCachePolicy(CachePolicy.ENABLED)
             .crossfade(true)
             .crossfade(200)
             .respectCacheHeaders(false) // Cache all images regardless of headers
             .apply {
                 if (BuildConfig.DEBUG) {
                     logger(DebugLogger())
                 }
             }
             .build()
     }
 }
