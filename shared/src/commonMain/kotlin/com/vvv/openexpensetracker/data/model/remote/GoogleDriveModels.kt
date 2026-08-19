package com.vvv.openexpensetracker.data.model.remote

import kotlinx.serialization.Serializable

@Serializable
data class DriveFileListResponse(
    val files: List<DriveFile> = emptyList()
)

@Serializable
data class DriveFile(
    val id: String,
    val name: String
)
