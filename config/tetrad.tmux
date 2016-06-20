neww -n "xmpp-service"   java -jar /tetrad/java/tetrad.jar --config /tetrad/config/xmpp-service.yml
neww -n "slack-service"  java -jar /tetrad/java/tetrad.jar --config /tetrad/config/slack-service.yml
neww -n "slack-to-xmpp"  java -jar /tetrad/java/tetrad.jar --config /tetrad/config/slack-to-xmpp.yml
neww -n "xmpp-to-slack"  java -jar /tetrad/java/tetrad.jar --config /tetrad/config/xmpp-to-slack.yml
neww -n "slack-to-slack" java -jar /tetrad/java/tetrad.jar --config /tetrad/config/slack-to-slack.yml
