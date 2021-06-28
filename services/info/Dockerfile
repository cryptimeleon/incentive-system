FROM openjdk:11-jdk-slim
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
COPY ${DEPENDENCY}/../../../jni/libmcljava.so /usr/lib/libmcljava.so
ENTRYPOINT ["java","-cp","app:app/lib/*","org.cryptimeleon.incentive.services.info.InfoApplication"]