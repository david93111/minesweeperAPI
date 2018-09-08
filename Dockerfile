# Image based on openjdk-alpine image
FROM openjdk:8-alpine

RUN apk update &&\
    apk add --no-cache bash && \
    apk add --no-cache unzip && \
    addgroup -S minesweepergroup && \
    adduser -S minesweeper -G minesweepergroup

COPY target/universal/minesweeperms.zip /home/minesweeper

RUN unzip /home/minesweeper/minesweeperms.zip -d /home/minesweeper/

RUN rm /home/minesweeper/minesweeperms.zip && \
    chown -R minesweeper /home/minesweeper/* && \
    ls -R /home/minesweeper/ && \
    chmod +x /home/minesweeper/minesweeperms/bin/minesweeperms

WORKDIR /home/minesweeper/minesweeperms/bin/

USER minesweeper

CMD ["./minesweeperms"]