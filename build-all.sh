cd ./components/components-react
./gradlew :build

cd ../../daos/firebase-dao
./gradlew :build

cd ../neo4j-dao
./gradlew :build

cd ../rest-dao
./gradlew :build

cd ../../email
./gradlew :build

cd ../enterprise/enterprise-react
./gradlew :build

cd ../../firebase/firebase-auth
./gradlew :build

cd ../firebase-core
./gradlew :build
