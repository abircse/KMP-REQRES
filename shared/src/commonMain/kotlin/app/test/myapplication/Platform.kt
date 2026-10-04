package app.test.myapplication

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform