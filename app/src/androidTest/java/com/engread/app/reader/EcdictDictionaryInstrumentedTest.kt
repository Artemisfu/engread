package com.engread.app.reader

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EcdictDictionaryInstrumentedTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun normalizesCommonInflectionsBeforeLlmFallback() {
        val dictionary = EcdictDictionary(context, autoDownloadExtended = false)

        assertLookup(dictionary, "looked", "look")
        assertLookup(dictionary, "running", "run")
        assertLookup(dictionary, "studies", "study")
        assertLookup(dictionary, "children", "child")
        assertLookup(dictionary, "written", "write")
        assertLookup(dictionary, "given", "give")
        assertLookup(dictionary, "wasn't", "be")
        assertLookup(dictionary, "leaves", "leaf")
    }

    private fun assertLookup(dictionary: EcdictDictionary, word: String, expectedBase: String) {
        val entry = dictionary.lookup(word)
        assertNotNull("Expected local lookup for $word", entry)
        assertEquals(expectedBase, entry?.word)
    }
}
