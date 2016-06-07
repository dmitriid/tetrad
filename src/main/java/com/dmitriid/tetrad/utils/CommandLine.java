package com.dmitriid.tetrad.utils;


import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandLine {
  private HashMap<String, String> _options = new HashMap<String, String>();

  // handle -o and --opt and -opt

  public CommandLine(String[] args) {
    String optionName = null;
    Matcher matcher;

    for (String o : args) {
      Pattern _optionMatch = Pattern.compile("^[-]{1,2}(\\w+)");
      matcher = _optionMatch.matcher(o);
      if (matcher.find()) {
        if (optionName != null) { // in case there was an option without a parameter before this one
          _options.put(matcher.group(1), null);
          optionName = null;
        }
        optionName = matcher.group(1);
      } else {
        if (optionName != null) {
          _options.put(optionName, o);
        }

        optionName = null;
      }
    }
  }

  public boolean hasOption(String... keys) {
    for (String key : keys) {
      if (_options.containsKey(key)) {
        return true;
      }
    }
    return false;
  }

  public String optionValue(String... options) {
    for (String option : options) {
      if (_options.containsKey(option)) {
        return _options.get(option);
      }
    }

    return null;
  }
}
