plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.1.0"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.maxhenkel.de/repository/public") //СВЧ-печь XD XD XD
    maven("https://repo.plasmoverse.com/releases") //PlasmoVoice
    maven("https://repo.essentialsx.net/releases/") {
        name = "essentialsx-releases"
    } //EssentialsX-API
    maven ("https://repo.extendedclip.com/releases/") // PAPI
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")
    implementation("de.maxhenkel.voicechat:voicechat-api:2.6.25")
    compileOnly("su.plo.voice.api:server:2.2.0-beta.2")
    compileOnly("net.essentialsx:EssentialsX:2.21.2")
    compileOnly("me.clip:placeholderapi:2.12.3")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("26.3")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
