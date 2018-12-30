FROM openjdk:8
LABEL maintainer="contact@partipirate.org"
COPY java-project /usr/src/congressus
WORKDIR /usr/src/congressus
ENV configuration=configuration_welcome.json
CMD ["/bin/bash","entrypoint.sh"]
