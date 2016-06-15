# Tetrad: cross-chat-platform integration

Send messages from an XMPP chatroom to a Slack channel and vice versa. 
Connect Telegram groups. Have four-five-six-way chats

# Requirements

- This code
- An MQTT server with anon user enabled running on local server (set 
broker urls in configs)

# Running:

Currently only runnable through IDEA. Add the following to your app args:

```
--config /path/to/your/service/config.yml
```

Main class is `com.dmitriid.Tetrad`

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

### Before the get to `firehose`
See `config/samples/slack-service.yml` for samle transformation config.

### Before the get to the dedicated service
See `config/samples/slack-to-xmpp.yml` for samle transformation config.
