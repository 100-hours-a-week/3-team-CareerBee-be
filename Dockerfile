# Dockerfile (BE)
FROM eclipse-temurin:21-jdk

WORKDIR /backend

# JAR 복사
COPY build/libs/careerbee-api.jar /app/app.jar

# Scouter는 우선 제외 (volumes로 붙일 예정)

ENTRYPOINT ["java",
    "--add-opens", "java.base/java.lang=ALL-UNNAMED",
    "--add-exports", "java.base/sun.net=ALL-UNNAMED",
    "-Djdk.attach.allowAttachSelf=true",
    "-Duser.timezone=Asia/Seoul",
    "-Dspring.profiles.active=prod",
    "-jar", "/app/app.jar"
]