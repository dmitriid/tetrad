# ![](./icon.png) Tetrad: cross-chat-platform integration

Send messages from an XMPP chatroom to a Slack channel and vice versa.
Connect Telegram groups. Have four-five-six-way chats.

The `tetrad` name comes from [`tetra`](https://en.wiktionary.org/wiki/tetra-#Prefix)
meaning "four" and `d` for "daemon", as originally it was going to connect
four platforms.

The word "тетрадь" ([`tetrádʹ [tʲɪˈtratʲ]`](https://en.wiktionary.org/wiki/тетрадь#Pronunciation))
means "exercise book, notebook" in Russian, hence the icon (I know it's a
clipboard, not a notebook, but I like it :) ).

# Requirements

- This code
- An MQTT server with anon user enabled running on local server (set
broker urls in configs)

# Running:

## Fat jar

```
mvn package
```
and then
```
java -jar tetrad-0.1-with-dependencies.jar --config /path/to/config.yml
```

## Docker

See `Dockerfile.build` and `Dockerfile.run`. The `Dockerfile.run` currently in repo
will start several different services. Use it to create your own configurations

### Makefile

Run `make all` to build source code, create and run docker images. Rum `make run`
to just run the created docker image

# What

Config files specify what exactly `tetrad` should be running. These could be
services or mappers.

## Services

These will listen to all incoming messages from a service (Slack,
Telegram etc.) and post them to a `firehose` queue in MQTT.

They will listen to a specified mqtt topic and post messages on that
topic back to the service.

See `X-service.yml` for sample service configs.

## Mappers

A mapping will listen to all messages on the `firehose` mqtt topic, will
convert the message according to a configuration and post the new message
back to a specified mqtt topic.

See `X-to-Y.yml` for samle mapping configs.

## Transformations

Additionally apply a random transformation on a `FirehoseMessage`.

### Before the messages get to `firehose`
See `config/samples/slack-service.yml` for samle transformation config.

### Before the messages get to the dedicated service
See `config/samples/slack-to-xmpp.yml` for samle transformation config.

# Credits

Icon made by [Dario Ferrando](http://www.flaticon.com/authors/dario-ferrando)
from [www.flaticon.com](http://www.flaticon.com) is licensed by
[CC 3.0 BY](http://creativecommons.org/licenses/by/3.0/)
