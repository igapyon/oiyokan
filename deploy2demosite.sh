mvn install

OIYOKAN_VERSION=1.6.20210425
export OIYOKAN_VERSION
mvn deploy:deploy-file -Durl=file:./../oiyokan-demosite/repo -Dfile=target/oiyokan-${OIYOKAN_VERSION}.jar -DgroupId=jp.oiyokan -DartifactId=oiyokan -Dpackaging=jar -Dversion=${OIYOKAN_VERSION}
