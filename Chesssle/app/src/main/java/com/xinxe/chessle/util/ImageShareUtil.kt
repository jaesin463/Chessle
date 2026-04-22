package com.xinxe.chessle.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ImageShareUtil {

    /**
     * 비트맵을 임시 파일로 저장하고 시스템 공유 시트를 호출합니다.
     */
    fun shareBitmap(context: Context, bitmap: Bitmap) {
        val imagesDir = File(context.cacheDir, "images")
        if (!imagesDir.exists()) imagesDir.mkdirs()

        val file = File(imagesDir, "chessle_result.png")

        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "결과 공유하기"))
        } catch (e: Exception) {
            Log.e("ImageShareUtil", "공유 중 오류 발생", e)
            Toast.makeText(context, "공유 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}