FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /build
COPY lib/ lib/
ADD https://repo.maven.apache.org/maven2/com/h2database/h2/2.5.252/h2-2.5.252.jar lib/h2-2.5.252.jar
ADD https://repo.maven.apache.org/maven2/org/postgresql/postgresql/42.7.14/postgresql-42.7.14.jar lib/postgresql-42.7.14.jar
RUN echo "90b11dc413da82070ef15fc943282c1408e6ffd34fa68998335fea8603b5ff20  lib/h2-2.5.252.jar" | sha256sum -c - \
 && echo "73914527305a40cce504b0d3d90b23caf565136912d607ae8ae7c5895512332c  lib/postgresql-42.7.14.jar" | sha256sum -c -
COPY src/ src/
RUN mkdir classes && javac -encoding UTF-8 --release 21 -cp "lib/*" -d classes src/Bookstore.java

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /build/classes/ classes/
COPY --from=build /build/lib/ lib/
COPY web/ web/
COPY sql/ sql/
RUN mkdir data && chown -R 10001:10001 /app
USER 10001:10001
ENV BOOKSTORE_BIND=0.0.0.0
ENV BOOKSTORE_PUBLIC=true
ENV PORT=10000
ENV TZ=Asia/Ho_Chi_Minh
EXPOSE 10000
CMD ["java", "-Xms32m", "-Xmx256m", "-cp", "classes:lib/*", "Bookstore"]
