package com.dergoogler.mmrl.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class RepositorySearchTest {
    @Test
    fun parsesSearchPrefixesAndWhitespace() {
        assertEquals(RepositorySearch("value", "id", "%\"value\"%"), RepositorySearch.parse("ID: value "))
        assertEquals(RepositorySearch("name:thing", "", "%\"name:thing\"%"), RepositorySearch.parse(" name:thing "))
        assertEquals(RepositorySearch("", "author", "%\"\"%"), RepositorySearch.parse("author: "))
    }

    @Test
    fun escapesCategoryPatternsAndJsonCharacters() {
        val search = RepositorySearch.parse("category:100%_\\tools")
        assertEquals("category", search.prefix)
        assertEquals("%\"100\\%\\_\\\\\\\\tools\"%", search.categoryPattern)
    }
}
