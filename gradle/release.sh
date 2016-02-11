#!/bin/bash

REL_DIR=$HOME/dev/src/java/bbct/release
BIN_REL=$REL_DIR/bin/android
SRC_REL=$REL_DIR/src/android

PREFIX=bbct-android

build_apk() {
    local edition=$1
    local version=$2
    local apk_prefix=${PREFIX}-${edition}

    cd ${edition}/main &&
    ant clean release &&
    cp bin/${apk_prefix}-release.apk ${BIN_REL}/${edition}/${apk_prefix}-$version.apk &&
    cd -
}

hash() {
    local edition=$1
    local prefix=${PREFIX}-${edition}

    cd ${REL_DIR}/bin/android/${edition} &&
    md5sum *.apk > ${prefix}.md5 &&
    echo Checking MD5... &&
    md5sum -c ${prefix}.md5 &&
    sha1sum *.apk > ${prefix}.sha1 &&
    echo Checking SHA1... &&
    sha1sum -c ${prefix}.sha1 &&
    cd -
}

pull_devel() {
    echo Pull devel branch... &&
    git stash save 'Stash before building release' &&
    git checkout devel/android &&
    git pull upstream devel/android
}

if [ $# == 1 ]
then {
    version=$1

    echo Building APKs... &&
    build_apk lite ${version} &&
    build_apk premium ${version} &&

    echo Archiving source files... &&
    git archive --format=zip --prefix=bbct/ -o ${SRC_REL}/bbct-lite-src.${version}.zip master lite common &&
    git archive --format=tar --prefix=bbct/ master lite common | gzip > ${SRC_REL}/bbct-lite-src.${version}.tar.gz &&

    echo Generating hashes... &&
    hash lite &&
    hash premium &&

    cd ${REL_DIR}/src/android &&
    md5sum *.tar.gz *.zip > $PREFIX-src.md5 &&
    echo Checking MD5... &&
    md5sum -c $PREFIX-src.md5 &&
    sha1sum *.tar.gz *.zip > $PREFIX-src.sha1 &&
    echo Checking SHA1... &&
    sha1sum -c $PREFIX-src.sha1 &&
    cd - &&

    echo Merge master... &&
    git checkout master &&
    git merge devel &&

    echo Tag... &&
    git tag l$version &&
    git tag p$version
}
else {
    echo Usage: './release <version>'
}
fi
