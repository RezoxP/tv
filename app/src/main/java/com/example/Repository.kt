package com.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.util.zip.GZIPInputStream

class Repository {
    private val client = OkHttpClient.Builder().build()
    
    // We create a lenient json parser because community files can have weird formatting
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; explicitNulls = false }
    
    private val baseUrl = "https://raw.githubusercontent.com/globetvapp/globetv.app/main/"

    // In a production app, we would cache these files to disk to save bandwidth.
    suspend fun getChannelsAndStreams(): List<ChannelStream> = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch Streams (usually smaller, mapping channel to url)
            val streamsJsonText = fetchAndDecompress("streams.json.gz")
            val streamsList: List<ApiStream> = json.decodeFromString(streamsJsonText)
            
            // Optimization for memory on 2GB devices: keep only one active stream per channel ID
            // Or just keep a map of valid channel IDs that have streams to filter our channel parsing
            val streamMap = streamsList.associateBy { it.channel }
            
            // 2. Fetch Channels
            val channelsJsonText = fetchAndDecompress("channels.json.gz")
            val channelsList: List<ApiChannel> = json.decodeFromString(channelsJsonText)
            
            // 3. Map and Filter
            channelsList.mapNotNull { channel ->
                val stream = streamMap[channel.id]
                if (stream != null) {
                    ChannelStream(
                        id = channel.id,
                        name = channel.name,
                        country = channel.country ?: "Unknown",
                        categories = channel.categories,
                        url = stream.url,
                        logoUrl = channel.logo
                    )
                } else null
            }.sortedBy { it.name }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun fetchAndDecompress(filename: String): String {
        val request = Request.Builder()
            .url(baseUrl + filename)
            // It's raw bytes of .gz file, not http encoding
            .build()
            
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Failed to download $filename: ${response.code}")
            val inputStream: InputStream = response.body?.byteStream() ?: throw Exception("Empty body")
            return GZIPInputStream(inputStream).bufferedReader(Charsets.UTF_8).use { it.readText() }
        }
    }
}
