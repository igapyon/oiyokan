OIYOKAN_VERSION=1.14.20210510a-SNAPSHOT
export OIYOKAN_VERSION

mvn clean install

mvn deploy:deploy-file -Durl=file:./../oiyokan-demosite/repo -Dfile=target/oiyokan-${OIYOKAN_VERSION}.jar -DgroupId=jp.igapyon.oiyokan -DartifactId=oiyokan -Dpackaging=jar -Dversion=${OIYOKAN_VERSION}
