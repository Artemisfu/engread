package com.engread.app.parser

import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.engread.app.data.SourceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(AndroidJUnit4::class)
class BookImporterInstrumentedTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun importsEpubWithSpineAndChapterTitles() {
        val epubFile = File(context.cacheDir, "engread_epub_smoke.epub")
        createSmokeEpub(epubFile)

        val book = BookImporter(context).import(Uri.fromFile(epubFile))

        assertEquals(SourceType.EPUB, book.sourceType)
        assertEquals("EngRead EPUB Smoke", book.title)
        assertTrue(book.paragraphs.contains("Chapter One"))
        assertTrue(book.paragraphs.any { it.contains("The first EPUB paragraph appears here.") })
        assertTrue(book.paragraphs.contains("Chapter Two"))
        assertTrue(book.paragraphs.any { it.contains("Second chapter text arrives through the EPUB spine.") })
        assertEquals(listOf("Chapter One", "Chapter Two"), book.toc.map { it.title })
        assertEquals(book.paragraphs.indexOf("Chapter One"), book.toc[0].paragraphIndex)
        assertEquals(book.paragraphs.indexOf("Chapter Two"), book.toc[1].paragraphIndex)
    }

    @Test
    fun importsEpubByMagicWhenExtensionIsUnknown() {
        val epubFile = File(context.cacheDir, "engread_epub_smoke.bin")
        createSmokeEpub(epubFile)

        val book = BookImporter(context).import(Uri.fromFile(epubFile))

        assertEquals(SourceType.EPUB, book.sourceType)
        assertEquals("EngRead EPUB Smoke", book.title)
        assertTrue(book.paragraphs.any { it.contains("The first EPUB paragraph appears here.") })
        assertEquals(listOf("Chapter One", "Chapter Two"), book.toc.map { it.title })
    }

    @Test
    fun importsEpubWhenZipEntryCrcIsInvalid() {
        val epubFile = File(context.cacheDir, "engread_epub_bad_crc.epub")
        createSmokeEpub(epubFile)
        corruptZipEntryCrc(epubFile, "OEBPS/chapter2.xhtml")

        val book = BookImporter(context).import(Uri.fromFile(epubFile))

        assertEquals(SourceType.EPUB, book.sourceType)
        assertEquals("EngRead EPUB Smoke", book.title)
        assertTrue(book.paragraphs.any { it.contains("Second chapter text arrives through the EPUB spine.") })
        assertEquals(listOf("Chapter One", "Chapter Two"), book.toc.map { it.title })
    }

    @Test
    fun importsEpubWhenImageZipEntryCrcIsInvalid() {
        val epubFile = File(context.cacheDir, "engread_epub_bad_image_crc.epub")
        createSmokeEpub(epubFile)
        corruptZipEntryCrc(epubFile, "OEBPS/1865371151774153442_image25.jpg")

        val book = BookImporter(context).import(Uri.fromFile(epubFile))

        assertEquals(SourceType.EPUB, book.sourceType)
        assertEquals("EngRead EPUB Smoke", book.title)
        assertTrue(book.paragraphs.any { it.contains("Second chapter text arrives through the EPUB spine.") })
        assertEquals(listOf("Chapter One", "Chapter Two"), book.toc.map { it.title })
    }

    @Test
    fun importsMobiWithLibmobiNativePath() {
        val mobiFile = File(context.cacheDir, "engread_mobi_sample.mobi")
        InstrumentationRegistry.getInstrumentation().context.assets.open("engread_mobi_sample.mobi").use { input ->
            mobiFile.outputStream().use { output -> input.copyTo(output) }
        }

        val book = BookImporter(context).import(Uri.fromFile(mobiFile))

        assertEquals(SourceType.MOBI, book.sourceType)
        assertTrue(book.paragraphs.isNotEmpty())
        assertTrue(book.content.contains("good friend", ignoreCase = true))
    }

    @Test
    fun importsAzw3ExtensionWithLibmobiNativePath() {
        val azw3File = File(context.cacheDir, "engread_mobi_sample.azw3")
        InstrumentationRegistry.getInstrumentation().context.assets.open("engread_mobi_sample.mobi").use { input ->
            azw3File.outputStream().use { output -> input.copyTo(output) }
        }

        val book = BookImporter(context).import(Uri.fromFile(azw3File))

        assertEquals(SourceType.MOBI, book.sourceType)
        assertTrue(book.paragraphs.isNotEmpty())
        assertTrue(book.content.contains("good friend", ignoreCase = true))
        assertTrue("AZW3 must not be imported as raw binary text", !book.content.contains("BOOKMOBI"))
    }

    @Test
    fun importsMobiByMagicWhenExtensionIsUnknown() {
        val unknownFile = File(context.cacheDir, "engread_mobi_sample")
        InstrumentationRegistry.getInstrumentation().context.assets.open("engread_mobi_sample.mobi").use { input ->
            unknownFile.outputStream().use { output -> input.copyTo(output) }
        }

        val book = BookImporter(context).import(Uri.fromFile(unknownFile))

        assertEquals(SourceType.MOBI, book.sourceType)
        assertTrue(book.paragraphs.isNotEmpty())
        assertTrue(book.content.contains("good friend", ignoreCase = true))
    }

    private fun createSmokeEpub(file: File) {
        ZipOutputStream(file.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("mimetype"))
            zip.write("application/epub+zip".toByteArray())
            zip.closeEntry()
            zip.textEntry(
                "META-INF/container.xml",
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
                  <rootfiles>
                    <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
                  </rootfiles>
                </container>
                """.trimIndent(),
            )
            zip.textEntry(
                "OEBPS/content.opf",
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <package xmlns="http://www.idpf.org/2007/opf" unique-identifier="bookid" version="2.0">
                  <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
                    <dc:title>EngRead EPUB Smoke</dc:title>
                    <dc:identifier id="bookid">engread-epub-smoke</dc:identifier>
                    <dc:language>en</dc:language>
                  </metadata>
                  <manifest>
                    <item id="ncx" href="toc.ncx" media-type="application/x-dtbncx+xml"/>
                    <item id="c1" href="chapter1.xhtml" media-type="application/xhtml+xml"/>
                    <item id="c2" href="chapter2.xhtml" media-type="application/xhtml+xml"/>
                    <item id="image25" href="1865371151774153442_image25.jpg" media-type="image/jpeg"/>
                  </manifest>
                  <spine toc="ncx">
                    <itemref idref="c1"/>
                    <itemref idref="c2"/>
                  </spine>
                </package>
                """.trimIndent(),
            )
            zip.textEntry(
                "OEBPS/toc.ncx",
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <ncx xmlns="http://www.daisy.org/z3986/2005/ncx/" version="2005-1">
                  <head><meta name="dtb:uid" content="engread-epub-smoke"/></head>
                  <docTitle><text>EngRead EPUB Smoke</text></docTitle>
                  <navMap>
                    <navPoint id="navPoint-1" playOrder="1">
                      <navLabel><text>Chapter One</text></navLabel>
                      <content src="chapter1.xhtml"/>
                    </navPoint>
                    <navPoint id="navPoint-2" playOrder="2">
                      <navLabel><text>Chapter Two</text></navLabel>
                      <content src="chapter2.xhtml"/>
                    </navPoint>
                  </navMap>
                </ncx>
                """.trimIndent(),
            )
            zip.textEntry(
                "OEBPS/chapter1.xhtml",
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <html xmlns="http://www.w3.org/1999/xhtml">
                  <head><title>Chapter One</title></head>
                  <body><p>The first EPUB paragraph appears here.</p></body>
                </html>
                """.trimIndent(),
            )
            zip.textEntry(
                "OEBPS/chapter2.xhtml",
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <html xmlns="http://www.w3.org/1999/xhtml">
                  <head><title>Chapter Two</title></head>
                  <body>
                    <p>Second chapter text arrives through the EPUB spine.</p>
                    <img src="1865371151774153442_image25.jpg" alt="decorative"/>
                  </body>
                </html>
                """.trimIndent(),
            )
            zip.putNextEntry(ZipEntry("OEBPS/1865371151774153442_image25.jpg"))
            zip.write(
                byteArrayOf(
                    0xFF.toByte(),
                    0xD8.toByte(),
                    0xFF.toByte(),
                    0xD9.toByte(),
                ),
            )
            zip.closeEntry()
        }
    }

    private fun ZipOutputStream.textEntry(name: String, content: String) {
        putNextEntry(ZipEntry(name))
        write(content.toByteArray())
        closeEntry()
    }

    private fun corruptZipEntryCrc(file: File, entryName: String) {
        val bytes = file.readBytes()
        for (offset in 0..bytes.size - 4) {
            when {
                bytes.hasZipSignature(offset, 0x04034b50) -> {
                    val nameLength = bytes.u16(offset + 26)
                    val nameOffset = offset + 30
                    if (nameOffset + nameLength <= bytes.size &&
                        bytes.copyOfRange(nameOffset, nameOffset + nameLength).decodeToString() == entryName
                    ) {
                        bytes[offset + 14] = (bytes[offset + 14].toInt() xor 0x5A).toByte()
                    }
                }
                bytes.hasZipSignature(offset, 0x02014b50) -> {
                    val nameLength = bytes.u16(offset + 28)
                    val nameOffset = offset + 46
                    if (nameOffset + nameLength <= bytes.size &&
                        bytes.copyOfRange(nameOffset, nameOffset + nameLength).decodeToString() == entryName
                    ) {
                        bytes[offset + 16] = (bytes[offset + 16].toInt() xor 0x5A).toByte()
                    }
                }
            }
        }
        file.writeBytes(bytes)
    }

    private fun ByteArray.hasZipSignature(offset: Int, signature: Int): Boolean =
        offset >= 0 &&
            offset + 4 <= size &&
            (this[offset].toInt() and 0xFF) == (signature and 0xFF) &&
            (this[offset + 1].toInt() and 0xFF) == ((signature ushr 8) and 0xFF) &&
            (this[offset + 2].toInt() and 0xFF) == ((signature ushr 16) and 0xFF) &&
            (this[offset + 3].toInt() and 0xFF) == ((signature ushr 24) and 0xFF)

    private fun ByteArray.u16(offset: Int): Int {
        if (offset < 0 || offset + 2 > size) return 0
        return (this[offset].toInt() and 0xFF) or ((this[offset + 1].toInt() and 0xFF) shl 8)
    }
}
