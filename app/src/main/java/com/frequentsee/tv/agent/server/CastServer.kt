package com.frequentsee.tv.agent.server

import android.content.Context
import android.content.Intent
import android.util.Log
import com.frequentsee.tv.agent.PlayerActivity
import com.frequentsee.tv.agent.models.ApiResponse
import com.frequentsee.tv.agent.models.CastRequest
import com.frequentsee.tv.agent.models.PlaybackState
import com.google.gson.Gson
import fi.iki.elonen.NanoHTTPD

class CastServer(
    private val context: Context,
    port: Int = 8080
) : NanoHTTPD(port) {

    private val gson = Gson()
    private var currentState = PlaybackState(status = "idle")

    companion object {
        private const val TAG = "CastServer"
        const val ACTION_STOP_PLAYBACK = "com.frequentsee.tv.agent.STOP_PLAYBACK"
    }

    override fun serve(session: IHTTPSession): Response {
        Log.d(TAG, "Request: ${session.method} ${session.uri}")

        // Handle CORS preflight
        if (session.method == Method.OPTIONS) {
            return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "").apply {
                addCORSHeaders(this)
            }
        }

        return when (session.uri) {
            "/cast" -> handleCast(session)
            "/stop" -> handleStop(session)
            "/status" -> handleStatus(session)
            else -> newFixedLengthResponse(
                Response.Status.NOT_FOUND,
                "application/json",
                gson.toJson(ApiResponse(success = false, message = "Endpoint not found"))
            ).apply { addCORSHeaders(this) }
        }
    }

    private fun handleCast(session: IHTTPSession): Response {
        if (session.method != Method.POST) {
            return newFixedLengthResponse(
                Response.Status.METHOD_NOT_ALLOWED,
                "application/json",
                gson.toJson(ApiResponse(success = false, message = "Method not allowed"))
            ).apply { addCORSHeaders(this) }
        }

        return try {
            // Read request body
            val files = mutableMapOf<String, String>()
            session.parseBody(files)
            val body = files["postData"] ?: ""

            val castRequest = gson.fromJson(body, CastRequest::class.java)

            // Update state
            currentState = PlaybackState(
                status = "playing",
                streamUrl = castRequest.streamUrl,
                title = castRequest.title,
                subtitle = castRequest.subtitle
            )

            // Start PlayerActivity
            val intent = Intent(context, PlayerActivity::class.java).apply {
                putExtra("streamUrl", castRequest.streamUrl)
                putExtra("title", castRequest.title)
                putExtra("subtitle", castRequest.subtitle)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)

            Log.d(TAG, "Starting playback: ${castRequest.streamUrl}")

            newFixedLengthResponse(
                Response.Status.OK,
                "application/json",
                gson.toJson(ApiResponse(success = true, message = "Playback started"))
            ).apply { addCORSHeaders(this) }

        } catch (e: Exception) {
            Log.e(TAG, "Error handling cast request", e)
            newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                "application/json",
                gson.toJson(ApiResponse(success = false, message = e.message))
            ).apply { addCORSHeaders(this) }
        }
    }

    private fun handleStop(session: IHTTPSession): Response {
        if (session.method != Method.POST) {
            return newFixedLengthResponse(
                Response.Status.METHOD_NOT_ALLOWED,
                "application/json",
                gson.toJson(ApiResponse(success = false, message = "Method not allowed"))
            ).apply { addCORSHeaders(this) }
        }

        // Broadcast stop intent
        val stopIntent = Intent(ACTION_STOP_PLAYBACK).apply {
            setPackage(context.packageName)
        }
        context.sendBroadcast(stopIntent)

        currentState = PlaybackState(status = "stopped")
        Log.d(TAG, "Playback stopped")

        return newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            gson.toJson(ApiResponse(success = true, message = "Playback stopped"))
        ).apply { addCORSHeaders(this) }
    }

    private fun handleStatus(session: IHTTPSession): Response {
        return newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            gson.toJson(currentState)
        ).apply { addCORSHeaders(this) }
    }

    private fun addCORSHeaders(response: Response) {
        response.addHeader("Access-Control-Allow-Origin", "*")
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        response.addHeader("Access-Control-Allow-Headers", "Content-Type")
    }

    fun updateState(state: PlaybackState) {
        currentState = state
    }
}
