FROM anapsix/alpine-java:latest
RUN touch /var/log/access.log
COPY stats.sh target/stats.jar /
ENTRYPOINT ["sh", "stats.sh", "-f", "/var/log/access.log"]