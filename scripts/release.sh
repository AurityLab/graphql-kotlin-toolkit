# This simplifies the release process of this project.
# This basically just calls the "allPublish" task but disables parallel executions, because it may cause some strange errors.

./gradlew allPublish --no-parallel
