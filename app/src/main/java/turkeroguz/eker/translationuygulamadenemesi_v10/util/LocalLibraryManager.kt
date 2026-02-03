package turkeroguz.eker.translationuygulamadenemesi_v10.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Book
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.URL

object LocalLibraryManager {

    private const val DIR_BOOKS = "downloaded_books"
    private const val DIR_PDFS = "book_pdfs"

    // 1. Kitabı ve PDF'lerini İndirip Kaydetme
    suspend fun downloadAndSaveBook(context: Context, book: Book): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // PDF'leri saklayacağımız klasör
                val pdfDir = File(context.filesDir, DIR_PDFS)
                if (!pdfDir.exists()) pdfDir.mkdirs()

                // Yeni yerel yolları tutacak liste
                val localStoryPaths = ArrayList<String>()

                // Her bir hikaye parçasını indir
                book.storyUrls.forEachIndexed { index, url ->
                    val fileName = "${book.bookId}_part_$index.pdf"
                    val file = File(pdfDir, fileName)

                    // İnternetten çek ve dosyaya yaz
                    URL(url).openStream().use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }
                    localStoryPaths.add(file.absolutePath)
                }

                // Kitap objesinin kopyasını oluştur ve linkleri yerel dosya yollarıyla değiştir
                val localBook = book.copy(storyUrls = localStoryPaths)

                // Kitap Bilgisini (Metadata) Kaydet
                saveBookMetadata(context, localBook)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    // Kitap objesini dosyaya yazma (Serileştirme)
    private fun saveBookMetadata(context: Context, book: Book) {
        val booksDir = File(context.filesDir, DIR_BOOKS)
        if (!booksDir.exists()) booksDir.mkdirs()

        val bookFile = File(booksDir, "book_${book.bookId}.dat")
        ObjectOutputStream(FileOutputStream(bookFile)).use { it.writeObject(book) }
    }

    // 2. İndirilen Kitapları Listeleme
    fun getDownloadedBooks(context: Context): List<Book> {
        val books = ArrayList<Book>()
        val booksDir = File(context.filesDir, DIR_BOOKS)

        if (booksDir.exists()) {
            booksDir.listFiles()?.forEach { file ->
                try {
                    ObjectInputStream(file.inputStream()).use {
                        val book = it.readObject() as Book
                        books.add(book)
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
        return books
    }

    // 3. Kitabı Silme (Hem PDF hem Bilgi)
    fun deleteBook(context: Context, book: Book): Boolean {
        try {
            // PDF Dosyalarını Sil
            book.storyUrls.forEach { path ->
                val file = File(path)
                if (file.exists()) file.delete()
            }

            // Kitap Bilgi Dosyasını Sil
            val metaFile = File(context.filesDir, "$DIR_BOOKS/book_${book.bookId}.dat")
            if (metaFile.exists()) metaFile.delete()

            return true
        } catch (e: Exception) {
            return false
        }
    }

    // Kitap zaten indirilmiş mi kontrolü
    fun isBookDownloaded(context: Context, bookId: String): Boolean {
        val file = File(context.filesDir, "$DIR_BOOKS/book_$bookId.dat")
        return file.exists()
    }
}