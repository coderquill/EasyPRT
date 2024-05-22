# Build and test instructions
This project is set up with the Gradle wrapper so you do not need a local installation of Gradle.

For compilation, run `./gradlew build`
For running unit tests, run `./gradlew test`
For creating documentation files, run `./gradlew javadoc`

The project must be set up in an IDE with the f23-project-team4 directory as the working directory since some file paths
have been hardcoded in the code that are relative to this directory (like the file path for the API key). If using 
IntelliJ, open the f23-project-team4 directory when opening a project.

# Adding API key
The API key should be added to a key.secret file in the pgh-prt-truetime folder.

# Sample client code
- `PrtGeneralClient.java` contains sample client code for `PrtRealTime` and `PrtInfo`.
- `PrtHistoryClient.java` contains sample client code for `PrtHistoryTable`.
- `PrtScheduleClient.java` contains sample client code for `PrtScheduleTable`.
