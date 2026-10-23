package com.tclab.android.feature.protocols.loader

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

/**
 * Loads local documents for the standalone Protocols feature.
 *
 * Supports text documents and PDFs containing selectable text.
 * Scanned PDFs require OCR and are not processed by this loader.
 */
class ProtocolDataLoader(context: Context) {

    private val appContext = context.applicationContext
    private val resolver: ContentResolver =
        appContext.contentResolver

    companion object {
        private const val MAX_FILE_SIZE_BYTES =
            10L * 1024L * 1024L

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
     * Loads a selected document and returns its readable text.
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

        val isPdf = extension == "pdf" ||
            mimeType == "application/pdf"

        if (!isPdf && !isSupportedTextFile(extension, mimeType)) {
            return ProtocolLoadResult(
                success = false,
                fileName = fileName,
                mimeType = mimeType,
                text = "",
                warnings = listOf(
                    "This file format is not supported yet. " +
                        "No document content was extracted."
                ),
                error = "Unsupported document format."
            )
        }

        return try {
            val declaredLength = getDeclaredLength(uri)

            if (declaredLength > MAX_FILE_SIZE_BYTES) {
                return failure(
                    fileName,
                    mimeType,
                    "The file exceeds the 10 MB import limit."
                )
            }

            val text = if (isPdf) {
                extractPdfText(uri)
            } else {
                readTextDocument(uri)
            }

            if (text.isBlank()) {
                ProtocolLoadResult(
                    success = false,
                    fileName = fileName,
                    mimeType = mimeType,
                    text = "",
                    warnings = if (isPdf) {
                        listOf(
                            "No selectable text was found. " +
                                "This may be a scanned PDF that needs OCR."
                        )
                    } else {
                        listOf(
                            "The selected document contains no readable text."
                        )
                    },
                    error = if (isPdf) {
                        "No readable text was extracted from the PDF."
                    } else {
                        "The document is empty or unreadable."
                    }
                )
            } else {
                ProtocolLoadResult(
                    success = true,
                    fileName = fileName,
                    mimeType = if (isPdf) {
                        "application/pdf"
                    } else {
                        mimeType
                    },
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
                exception.message
                    ?: "The document could not be read."
            )
        } catch (exception: Exception) {
            failure(
                fileName,
                mimeType,
                "The document could not be processed. " +
                    "Check that it is a valid, unencrypted file."
            )
        }
    }

    /**
     * Extracts selectable text from a PDF.
     */
    private fun extractPdfText(uri: Uri): String {
        PDFBoxResourceLoader.init(appContext)

        val input = resolver.openInputStream(uri)
            ?: throw IOException(
                "Android could not open this PDF."
            )

        input.use { stream ->
            PDDocument.load(stream).use { document ->
                if (document.isEncrypted) {
                    throw IOException(
                        "Password-protected PDFs are not supported."
                    )
                }

                return PDFTextStripper().getText(document)
            }
        }
    }

    /**
     * Reads supported text-based documents as UTF-8.
     */
    private fun readTextDocument(uri: Uri): String {
        val input = resolver.openInputStream(uri)
            ?: throw IOException(
                "Android could not open this document."
            )

        input.use { stream ->
            InputStreamReader(
                stream,
                StandardCharsets.UTF_8
            ).use { reader ->
                val output = StringBuilder()
                val buffer = CharArray(8192)
                var estimatedBytes = 0L

                while (true) {
                    val charsRead = reader.read(buffer)

                    if (charsRead < 0) {
                        break
                    }

                    estimatedBytes += charsRead.toLong() * 4L

                    if (estimatedBytes > MAX_FILE_SIZE_BYTES) {
                        throw IOException(
                            "The file exceeds the 10 MB import limit."
                        )
                    }

                    output.append(buffer, 0, charsRead)
                }

                return output.toString()
            }
        }
    }

    private fun getDeclaredLength(uri: Uri): Long {
        return try {
            resolver.openAssetFileDescriptor(uri, "r")
                ?.use { descriptor ->
                    descriptor.length
                } ?: -1L
        } catch (exception: SecurityException) {
            throw exception
        } catch (_: Exception) {
            -1L
        }
    }

    private fun isSupportedTextFile(
        extension: String,
        mimeType: String
    ): Boolean {
        return extension in supportedTextExtensions ||
            mimeType in supportedTextMimeTypes
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
                    val index = cursor.getColumnIndex(
                        OpenableColumns.DISPLAY_NAME
                    )

                    if (index >= 0) cursor.getString(index) else null
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
 */
data class ProtocolLoadResult(
    val success: Boolean,
    val fileName: String,
    val mimeType: String,
    val text: String,
    val warnings: List<String>,
    val error: String?
)


