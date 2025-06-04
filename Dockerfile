# Dockerfile (BE)
FROM eclipse-temurin:21-jdk

WORKDIR /backend

# 빌드된 JAR 파일 복사
COPY build/libs/careerbee-api.jar /backend/careerbee-api.jar

# Scouter는 volumes로 붙일 예정이므로 여기선 제외

# ENTRYPOINT를 한 줄짜리 JSON 배열로 작성
ENTRYPOINT ["java","--add-opens=java.base/java.lang=ALL-UNNAMED","--add-exports=java.base/sun.net=ALL-UNNAMED","-Djdk.attach.allowAttachSelf=true","-Duser.timezone=Asia/Seoul","-Dspring.profiles.active=prod","-jar","/backend/careerbee-api.jar"]