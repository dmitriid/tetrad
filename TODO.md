# Priority

Slack -> xxx:

- ✔︎ ~~:smiley: -> actual smiley~~
- ✔︎ ~~@username is displayed as <@U...>~~
- ✔︎ ~~#channel is displayed as <#U...>~~
- ✔︎ ~~unescape HTML ? (for XMPP/Telegram at least)~~
- ✔︎ ~~/giphy, /gif, link expansion~~
- username has to be `user@slack-service`?

Slack -> XMPP:

- ✔︎ ~~username passed as <strong>username</strong> to XMPP~~
- http://git.emojione.com/demos/ascii-smileys.html
- markdown to xmpp xhtml
- ✔︎ convert `\n` to `<br>` for XMPP XHTML messages
- "Set channel purpose" -> Set topic

xxx -> Slack

- commands (text beginning with `/`) should be sent as commands 
(careful about `/o\` and `/me` etc.)
- set topic -> set channel purpose

xxx -> XMPP

- Per=user login with different resources

xxx, xxx -> xxx

- Log all errors

### Extremely low priority

xxx -> IRC -> xxx:
- implement IRC support (req. by Mendor)

xxx -> Telegram:

- html escape/unescape
- do something about links http://dmitriid.com/i/gc4ukqjsgw4u1qb3.png

Telegram -> xxx

- stickers
- user should be user@telegram or similar
