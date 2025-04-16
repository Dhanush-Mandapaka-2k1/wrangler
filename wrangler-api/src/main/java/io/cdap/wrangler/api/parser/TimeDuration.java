/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.cdap.wrangler.api.annotations.PublicEvolving;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Token implementation for representing time durations, such as "150ms" or "5s".
 * Provides methods to retrieve the duration in a canonical unit (milliseconds).
 */
@PublicEvolving
public class TimeDuration implements Token {
  private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)\\s*([smhdwy])?", Pattern.CASE_INSENSITIVE);
  private final String original;
  private final long milliseconds;

  /**
   * Creates a new TimeDuration token by parsing the provided string.
   *
   * @param value String representation of a time duration (e.g. "150ms", "5s", "1h")
   * @throws IllegalArgumentException if the string cannot be parsed as a valid time duration
   */
  public TimeDuration(String value) {
    this.original = value;
    this.milliseconds = parseTimeDuration(value);
  }

  /**
   * Parses a string representing a time duration into its equivalent value in milliseconds.
   *
   * @param input String to parse
   * @return duration in milliseconds
   * @throws IllegalArgumentException if the input cannot be parsed
   */
  private long parseTimeDuration(String input) {
    if (input == null || input.trim().isEmpty()) {
      throw new IllegalArgumentException("Time duration cannot be null or empty");
    }

    Matcher matcher = TIME_PATTERN.matcher(input.trim());
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid time duration format: " + input);
    }

    long value = Long.parseLong(matcher.group(1));
    String unit = matcher.group(2);

    if (unit == null || unit.equalsIgnoreCase("ms")) {
      return value; // already in milliseconds
    } else {
      char prefix = Character.toLowerCase(unit.charAt(0));
      switch (prefix) {
        case 's':
          return value * 1000L;
        case 'm':
          return value * 60L * 1000L;
        case 'h':
          return value * 60L * 60L * 1000L;
        case 'd':
          return value * 24L * 60L * 60L * 1000L;
        case 'w':
          return value * 7L * 24L * 60L * 60L * 1000L;
        case 'y':
          return value * 365L * 24L * 60L * 60L * 1000L; // approximate
        default:
          throw new IllegalArgumentException("Unknown time unit: " + unit);
      }
    }
  }

  /**
   * Returns the duration in milliseconds.
   *
   * @return duration in milliseconds
   */
  public long getMilliseconds() {
    return milliseconds;
  }

  @Override
  public Object value() {
    return original;
  }

  @Override
  public TokenType type() {
    return TokenType.TIME_DURATION;
  }

  @Override
  public JsonElement toJson() {
    JsonObject object = new JsonObject();
    object.addProperty("type", type().name());
    object.addProperty("value", original);
    object.addProperty("milliseconds", milliseconds);
    return object;
  }

  @Override
  public String toString() {
    return original;
  }
}
