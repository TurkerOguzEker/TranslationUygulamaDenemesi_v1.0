package turkeroguz.eker.translationuygulamadenemesi_v10.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Book
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

object LocalLibraryManager {

    private const val DIR_BOOKS = "downloaded_books"
    // Artık DIR_PDFS klasörüne ihtiyacımız kalmadı, uçurduk!

    // 1. Kitabı İndirip Kaydetme (Işık Hızında!)
    suspend fun downloadAndSaveBook(context: Context, book: Book): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Eskiden burada PDF linklerini arayıp internetten indirmeye çalışıyorduk.
                // Artık kitap objesi (Book) zaten tüm metinleri (Chapters) kendi içinde taşıyor.
                // Tek yapmamız gereken bu objeyi direkt telefonun hafızasına yazmak!
                saveBookMetadata(context, book)
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

    // 2. İndirilen Kitapları Listeleme (Aynen Kalıyor)
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

    // 3. Kitabı Silme (Artık silinecek PDF dosyaları yok!)
    fun deleteBook(context: Context, book: Book): Boolean {
        return try {
            // Sadece kaydettiğimiz tek bir veri dosyasını (.dat) siliyoruz, bitti gitti!
            val metaFile = File(context.filesDir, "$DIR_BOOKS/book_${book.bookId}.dat")
            if (metaFile.exists()) metaFile.delete()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Kitap zaten indirilmiş mi kontrolü (Aynen Kalıyor)
    fun isBookDownloaded(context: Context, bookId: String): Boolean {
        val file = File(context.filesDir, "$DIR_BOOKS/book_$bookId.dat")
        return file.exists()
    }
}