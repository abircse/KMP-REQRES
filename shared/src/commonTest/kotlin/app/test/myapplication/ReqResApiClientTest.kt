package app.test.myapplication

import app.test.myapplication.data.model.*
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ReqResApiClientTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Test
    fun testUserDeserialization() {
        val jsonString = """
            {
                "id": 2,
                "email": "janet.weaver@reqres.in",
                "first_name": "Janet",
                "last_name": "Weaver",
                "avatar": "https://reqres.in/img/faces/2-image.jpg"
            }
        """.trimIndent()

        val user = json.decodeFromString<User>(jsonString)

        assertEquals(2, user.id)
        assertEquals("janet.weaver@reqres.in", user.email)
        assertEquals("Janet", user.firstName)
        assertEquals("Weaver", user.lastName)
        assertEquals("Janet Weaver", user.fullName)
        assertEquals("JW", user.initials)
    }

    @Test
    fun testUserListResponseDeserialization() {
        val jsonString = """
            {
                "page": 1,
                "per_page": 6,
                "total": 12,
                "total_pages": 2,
                "data": [
                    {
                        "id": 1,
                        "email": "george.bluth@reqres.in",
                        "first_name": "George",
                        "last_name": "Bluth",
                        "avatar": "https://reqres.in/img/faces/1-image.jpg"
                    }
                ],
                "support": {
                    "url": "https://contentcoda.io",
                    "text": "To keep ReqRes free, contributions are appreciated!"
                }
            }
        """.trimIndent()

        val response = json.decodeFromString<UserListResponse>(jsonString)

        assertEquals(1, response.page)
        assertEquals(12, response.total)
        assertEquals(1, response.data.size)
        assertEquals("George Bluth", response.data[0].fullName)
        assertNotNull(response.support)
    }

    @Test
    fun testColorResourceDeserialization() {
        val jsonString = """
            {
                "id": 1,
                "name": "cerulean",
                "year": 2000,
                "color": "#98B2D1",
                "pantone_value": "15-4020"
            }
        """.trimIndent()

        val resource = json.decodeFromString<ColorResource>(jsonString)

        assertEquals(1, resource.id)
        assertEquals("cerulean", resource.name)
        assertEquals(2000, resource.year)
        assertEquals("#98B2D1", resource.color)
        assertEquals("15-4020", resource.pantoneValue)
    }
}
