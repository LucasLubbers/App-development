FROM alpine:latest

WORKDIR /pb

RUN apk add --no-cache curl unzip bash \
  && curl -L -o pb.zip https://github.com/pocketbase/pocketbase/releases/download/v0.27.2/pocketbase_0.27.2_linux_amd64.zip \
  && unzip pb.zip \
  && rm pb.zip \
  && chmod +x pocketbase

# COPY generate_dummy_data.sh /pb/generate_dummy_data.sh
# RUN chmod +x /pb/generate_dummy_data.sh

EXPOSE 8090

CMD ./pocketbase serve --http=0.0.0.0:8090 
#& /pb/generate_dummy_data.sh
