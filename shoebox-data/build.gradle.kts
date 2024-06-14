plugins {
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.android.library")
    id("maven-publish")
    signing
}

val GROUP_ID = "io.github.mattshoe"
val ARTIFACT_ID = "shoebox-data"
val VERSION = "0.0.3"

group = GROUP_ID
version = VERSION

publishing {
    repositories {
        maven {
            name = "localPublication"
            url = uri("${project.layout.buildDirectory}/repos/releases")
        }
    }
    publications {
        create<MavenPublication>("shoeBoxData") {
            groupId = GROUP_ID
            artifactId = ARTIFACT_ID
            version = VERSION

            pom {
                name = "ShoeBox-Data"
                description = "A box of tools for data management."
                url = "https://github.com/mattshoe/shoebox"
                properties = mapOf(
                    "myProp" to "value"
                )
                packaging = "aar"
                inceptionYear = "2024"
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "mattshoe"
                        name = "Matthew Shoemaker"
                        email = "mattshoe81@gmail.com"
                    }
                }
                scm {
                    connection = "scm:git:git@github.com:mattshoe/shoebox.git"
                    developerConnection = "scm:git:git@github.com:mattshoe/shoebox.git"
                    url = "https://github.com/mattshoe/shoebox"
                }
            }

            afterEvaluate {
                from(components["release"])
            }
        }
    }
    signing {
        val signingKey = providers
            .environmentVariable("GPG_SIGNING_KEY")
            .forUseAtConfigurationTime()
        val signingPassphrase = providers
            .environmentVariable("GPG_SIGNING_PASSPHRASE")
            .forUseAtConfigurationTime()
        if (signingKey.isPresent && signingPassphrase.isPresent) {
            useInMemoryPgpKeys(signingKey.get(), signingPassphrase.get())
            sign(publishing.publications["shoeBoxData"])
        }
    }
}


android {
    namespace = GROUP_ID
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        targetSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.gson)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

tasks.register<Zip>("generateShoeBoxData") {
    val publishTask = tasks.named(
        "publishShoeBoxDataPublicationToLocalPublicationRepository",
        PublishToMavenRepository::class.java
    )
    from(publishTask.map { it.repository.url })
    into("")
    archiveFileName.set("release.zip")
}