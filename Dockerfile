FROM openjdk:8-jre-slim as base
RUN apt-get update && apt-get install -y \
    build-essential \
    wget \
    liblzma-dev \
    libbz2-dev \
    zlib1g-dev \
    python \
    git && \
    wget https://github.com/biod/sambamba/releases/download/v0.6.8/sambamba-0.6.8-linux-static.gz && \
    wget https://github.com/ENCODE-DCC/chip-seq-pipeline2/raw/master/src/assign_multimappers.py && mv assign_multimappers.py /bin && chmod 755 /bin/assign_multimappers.py && \
    gunzip sambamba-0.6.8-linux-static.gz && mv sambamba-0.6.8-linux-static /bin/sambamba && chmod 755 /bin/sambamba && \
    wget https://github.com/broadinstitute/picard/releases/download/2.18.14/picard.jar -O /bin/picard.jar && chmod +x /bin/picard.jar && \
    wget https://github.com/samtools/samtools/releases/download/1.9/samtools-1.9.tar.bz2 && \
    tar xvjf samtools-1.9.tar.bz2 && cd samtools-1.9 && \
    ./configure --without-curses --disable-lzma --disable-bz2 && \
    make && make install && cd .. && \
    rm -r samtools-1.9 && rm samtools-1.9.tar.bz2 && \
    git clone https://github.com/lh3/bwa.git && cd bwa && make && mv bwa /bin && cd .. && rm -rf bwa && \
    git clone https://github.com/arq5x/bedtools2 && cd bedtools2 && make && mv bin/bedtools /bin/bedtools && \
    cd .. && rm -r bedtools2 && \
    apt-get purge --auto-remove -y  git build-essential wget

FROM openjdk:8-jdk-alpine as build
COPY . /src
WORKDIR /src

RUN ./gradlew clean shadowJar

FROM base
RUN mkdir /app
COPY --from=build /src/build/chipseq-filter-*.jar /app/chipseq.jar