echo "Publishing persist"
chmod +x gradlew
./gradlew :publishToMavenLocal || exit
echo "Finished publishing persist"