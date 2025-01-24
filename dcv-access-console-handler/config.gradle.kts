mapOf(
        Pair("jettyVersion", "9.4.48")
).entries.forEach {
    project.extra.set(it.key, it.value)
}