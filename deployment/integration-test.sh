./deployment/build-docker-images.sh
docker-compose -f ./deployment/docker-compose-localhost.yaml up -d
pushd services 
sleep 20
./gradlew integrationtest

