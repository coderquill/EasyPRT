plugins {
    id("java")
}

group = "edu.cmu.cs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
        vendor = JvmVendorSpec.ORACLE
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient
    implementation("org.apache.httpcomponents:httpclient:4.5.13")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.0")

    // https://mvnrepository.com/artifact/org.apache.lucene/lucene-spatial
    implementation("org.apache.lucene:lucene-spatial:8.2.0")

}

tasks.javadoc {
    setDestinationDir(file("javadoc"))
}

tasks.test {
    useJUnitPlatform()
}