FROM openjdk:11
LABEL maintainer="contact@partipirate.org"
RUN apt-get update && apt-get install -y \
  ffmpeg
COPY java-project /usr/src/congressus
# COPY java-project/src/natives /natives
WORKDIR /usr/src/congressus
ENV configuration=configuration_welcome.json
CMD ["/bin/bash","entrypoint.sh"]
