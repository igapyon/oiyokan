OIYOKAN_VERSION=1.7.20210425d
export OIYOKAN_VERSION

mvn clean install

mvn deploy:deploy-file -Durl=file:./../oiyokan-demosite/repo -Dfile=target/oiyokan-${OIYOKAN_VERSION}.jar -DgroupId=jp.oiyokan -DartifactId=oiyokan -Dpackaging=jar -Dversion=${OIYOKAN_VERSION}
