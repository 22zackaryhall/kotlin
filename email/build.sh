echo "Building Email"
chmod +x gradlew || exit
echo "Running Wrapper"
./gradlew wrapper || exit
./gradlew :build || exit
echo "Finished Building Email"