# Priority

Slack -> xxx:

- DONE ~~:smiley: -> actual smiley~~
- DONE  ~~@username is displayed as <@U...>~~
- DONE  ~~#channel is displayed as <#U...>~~
- /giphy etc.
- DONE ~~unescape HTML ? (for XMPP/Telegram at least)~~
- username has to be `user@`

Slack -> XMPP:

- DOM ~~username passed as <strong>username</strong> to XMPP~~
- http://git.emojione.com/demos/ascii-smileys.html or http://mts.io/2015/04/21/unicode-symbol-render-text-emoji/
- markdown to xmpp xhtml

xxx -> Slack

- commands (text beginning with `/`) should be sent as commands 
(careful about `/o\` and `/me` etc.)


xxx -> XMPP

- Per=user login with different resources

xxx, xxx -> xxx

- Log all errors

xxx -> IRC -> xxx:
- implement IRC support (req. by Mendor)

### Extremely low priority

xxx -> Telegram:

- html escape/unescape
- do something about links http://dmitriid.com/i/gc4ukqjsgw4u1qb3.png

Telegram -> xxx

- stickers
- user should be user@telegram or similar
