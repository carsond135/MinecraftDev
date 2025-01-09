plugins {
    id("org.jetbrains.intellij.platform")
}

tasks.publishPlugin {
    properties["mcdev.deploy.token"]?.let { deployToken ->
        token.set(deployToken.toString())
    }
    channels.add(properties["mcdev.deploy.channel"]?.toString() ?: "Stable")
}
