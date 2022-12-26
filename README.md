# Stefans Mediathek Downloader Server

[![CI](https://github.com/StefanOltmann/smart-home-server/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/StefanOltmann/mediathek-downloader-server/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=smart-home-server&metric=alert_status)](https://sonarcloud.io/dashboard?id=mediathek-downloader-server)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

This project uses information of [MediathekView](https://github.com/mediathekview/MediathekView) to download movies by
subscriptions.

## Configuration

The [keystore.jks](src/main/resources/keystore.jks) for HTTPS connections. You can use mine or generate your own.
This file needs to be put aside the executable.

The [subscriptions.json](docs/subscriptions.json) for configuration of your subscriptions must be placed in a
subdirectory named `data`.

During the first start the server will create an _auth_code.txt_ (also in `data`) that contains a security token for
requests to the service.

The service will then be reachable on [https://localhost:60000/](https://localhost:60000/).

Now you can send a `GET` request to `https://localhost:60000/subscriptions/` with the header key `AUTH_CODE` set to the
security token (see `auth_code.txt`). If this returns you something that looks like the provided _subscriptions.json_,
the service is running.

If you have installed `curl` run the following command to test the service:\
`curl -k -H "AUTH_CODE: $(cat data/auth_code.txt)" https://localhost:60000/subscriptions`
