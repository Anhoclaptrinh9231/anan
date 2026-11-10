FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /build
ADD https://repo.maven.apache.org/maven2/com/mysql/mysql-connector-j/26.10.0/mysql-connector-j-26.10.0.jar lib/mysql-connector-j-26.10.0.jar
ADD https://repo.maven.apache.org/maven2/com/google/code/gson/gson/2.13.2/gson-2.13.2.jar lib/gson-2.13.2.jar
ADD https://repo.maven.apache.org/maven2/org/postgresql/postgresql/42.7.14/postgresql-42.7.14.jar lib/postgresql-42.7.14.jar
RUN echo "cef70caf532833c9ab8f41f74773ce22c08bf503fad215c9c2ee1e9db87e759e  lib/mysql-connector-j-26.10.0.jar" | sha256sum -c - \
 && echo "dd0ce1b55a3ed2080cb70f9c655850cda86c206862310009dcb5e5c95265a5e0  lib/gson-2.13.2.jar" | sha256sum -c - \
 && echo "73914527305a40cce504b0d3d90b23caf565136912d607ae8ae7c5895512332c  lib/postgresql-42.7.14.jar" | sha256sum -c - \
 && chmod 0644 lib/*.jar
COPY src/ src/
RUN mkdir classes && javac -encoding UTF-8 --release 21 -cp "lib/*" -d classes src/*.java

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
ARG BOOKSTORE_DB_CA_PEM_BASE64
RUN if [ -n "$BOOKSTORE_DB_CA_PEM_BASE64" ]; then \
      printf '%s' "$BOOKSTORE_DB_CA_PEM_BASE64" | base64 -d \
      | keytool -importcert -noprompt -alias bookstore-db -storetype PKCS12 -keystore /app/mysql-ca.p12 -storepass changeit \
      && chmod 0444 /app/mysql-ca.p12; \
    fi
COPY --from=build /build/classes/ classes/
COPY --from=build /build/lib/ lib/
COPY web/ web/
COPY sql/ sql/
USER 10001:10001
ENV BOOKSTORE_BIND=0.0.0.0
ENV BOOKSTORE_PUBLIC=true
ENV PORT=10000
ENV TZ=Asia/Ho_Chi_Minh
EXPOSE 10000
CMD ["java", "-Xms32m", "-Xmx256m", "-cp", "classes:lib/*", "Bookstore"]
