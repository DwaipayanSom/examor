FROM mysql:8.0

ENV MYSQL_ALLOW_EMPTY_PASSWORD=true
ENV MYSQL_DATABASE=db

COPY ./database.sql /docker-entrypoint-initdb.d/
