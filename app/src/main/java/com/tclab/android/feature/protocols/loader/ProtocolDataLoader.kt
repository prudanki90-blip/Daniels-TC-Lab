
package com.tclab.android.feature.protocols.loader

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

/**
 * Loads supported local documents for the standalone Protocols feature.
 *
 * This class reads source documents only. It does not extract scientific
 * fields, save protocols, or depend on another TC Lab feature.
 */
class ProtocolDataLoader(context: Context) {

    private val resolver: ContentResolver =
        context.applicationContext.contentResolver

    companion object {
        private const val MAX_TEXT_SIZE_BYTES = 10L * 1024L * 1024L

        private val supportedTextExtensions = setOf(
            "txt",
            "text",
            "md",
            "markdown",
            "csv",
            "tsv",
            "json",
            "xml",
            "html",
            "htm",
            "rtf"
        )

        private val supportedTextMimeTypes = setOf(
            "text/plain",
            "text/markdown",
            "text/csv",
            "text/tab-separated-values",
            "application/json",
            "application/xml",
            "text/xml",
            "text/html",
            "application/rtf",
            "text/rtf"
        )
    }

    /**
     * Reads a document selected through Android's document picker.
     *
     * Unsupported formats return a clear warning instead of being
     * interpreted as plain text.
     */
    fun load(uri: Uri): ProtocolLoadResult {
        val fileName = getDisplayName(uri)
            ?: uri.lastPathSegment
            ?: "unknown_document"

        val mimeType = resolver.getType(uri)
            ?.substringBefore(';')
            ?.trim()
            ?.lowercase()
            .orEmpty()

        val extension = fileName
            .substringAfterLast('.', missingDelimiterValue = "")
            .lowercase()

        if (!isSupportedTextFile(extension, mimeType)) {
            return ProtocolLoadResult(
                success = false,
                fileName = fileName,
                mimeType = mimeType,
                text = "",
                warnings = listOf(
                    "This file format is not supported by the current " +
                        "text loader. No document content was extracted."
                ),
                error = "Unsupported document format."
            )
        }

        return try {
            resolver.openAssetFileDescriptor(uri, "r")?.use { descriptor ->
                val declaredLength = descriptor.length

                if (
                    declaredLength > MAX_TEXT_SIZE_BYTES
                ) {
                    return ProtocolLoadResult(
                        success = false,
                        fileName = fileName,
                        mimeType = mimeType,
                        text = "",
                        warnings = emptyList(),
                        error = "The file exceeds the 10 MB text-import limit."
                    )
                }
            }

            val text = resolver.openInputStream(uri)?.use { input ->
                val output = StringBuilder()
                val buffer = CharArray(8192)
                var totalBytes = 0L

                InputStreamReader(
                    input,
                    StandardCharsets.UTF_8
                ).use { reader ->
                    while (true) {
                        val charsRead = reader.read(buffer)

                        if (charsRead < 0) {
                            break
                        }

                        totalBytes +=
                            charsRead.toLong() * 4L

                        if (totalBytes > MAX_TEXT_SIZE_BYTES) {
                            throw IOException(
                                "The file exceeds the 10 MB text-import limit."
                            )
                        }

                        output.append(buffer, 0, charsRead)
                    }
                }

                output.toString()
            } ?: throw IOException(
                "Android could not open this document."
            )

            if (text.isBlank()) {
                ProtocolLoadResult(
                    success = false,
                    fileName = fileName,
                    mimeType = mimeType,
                    text = "",
                    warnings = listOf(
                        "The selected document contains no readable text."
                    ),
                    error = "The document is empty or unreadable."
                )
            } else {
                ProtocolLoadResult(
                    success = true,
                    fileName = fileName,
                    mimeType = mimeType,
                    text = text,
                    warnings = emptyList(),
                    error = null
                )
            }
        } catch (exception: SecurityException) {
            failure(
                fileName,
                mimeType,
                "Permission to read this document was denied."
            )
        } catch (exception: IOException) {
            failure(
                fileName,
                mimeType,
                exception.message ?: "The document could not be read."
            )
        } catch (exception: Exception) {
            failure(
                fileName,
                mimeType,
                "An unexpected error occurred while reading the document."
            )
        }
    }

    private fun isSupportedTextFile(
        extension: String,
        mimeType: String
    ): Boolean {
        if (extension in supportedTextExtensions) {
            return true
        }

        if (mimeType in supportedTextMimeTypes) {
            return true
        }

        // Some providers do not supply MIME types.
        return mimeType.isBlank() &&
            extension in supportedTextExtensions
    }

    private fun getDisplayName(uri: Uri): String? {
        return try {
            resolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(
                        OpenableColumns.DISPLAY_NAME
                    )

                    if (columnIndex >= 0) {
                        cursor.getString(columnIndex)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun failure(
        fileName: String,
        mimeType: String,
        message: String
    ): ProtocolLoadResult {
        return ProtocolLoadResult(
            success = false,
            fileName = fileName,
            mimeType = mimeType,
            text = "",
            warnings = emptyList(),
            error = message
        )
    }
}

/**
 * Result of loading one local document.
 *
 * The original text is kept separate from extracted protocol fields
 * so that later extraction and review can use the source content.
 */
data class ProtocolLoadResult(
    val success: Boolean,
    val fileName: String,
    val mimeType: String,
    val text: String,
    val warnings: List<String>,
    val error: String?
)
