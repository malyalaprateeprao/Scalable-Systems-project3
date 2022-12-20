FROM bellsoft/liberica-openjdk-alpine-musl:11 AS server-build
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
RUN javac *.java
#CMD java Coordinator localhost 1111 & java Coordinator localhost 1112 & java Coordinator localhost 1113 & java Coordinator localhost 1114

FROM bellsoft/liberica-openjdk-alpine-musl:11 AS client-build
COPY . /usr/src/myapp1
WORKDIR /usr/src/myapp1
RUN javac *.java
# CMD java Client localhost