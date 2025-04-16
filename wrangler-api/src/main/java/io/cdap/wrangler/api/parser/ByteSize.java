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
 * Token implementation for representing byte sizes, such as "10KB" or "5MB".
 * Provides methods to retrieve the size in a canonical unit (bytes).
 */
@PublicEvolving
public class ByteSize implements Token {
  private static final Pattern BYTE_SIZE_PATTERN = Pattern.compile("(\\d+)\\s*([KMGTPE]?B)?", Pattern.CASE_INSENSITIVE);
  private final String original;
  private final long bytes;

  /**
   * Creates a new ByteSize token by parsing the provided string.
   *
   * @param value String representation of a byte size (e.g. "10KB", "5MB", "1024")
   * @throws IllegalArgumentException if the string cannot be parsed as a valid byte size
   */
  public ByteSize(String value) {
    this.original = value;
    this.bytes = parseByteSize(value);
  }

  /**
   * Parses a string representing a byte size into its equivalent value in bytes.
   *
   * @param input String to parse
   * @return size in bytes
   * @throws IllegalArgumentException if the input cannot be parsed
   */
  private long parseByteSize(String input) {
    if (input == null || input.trim().isEmpty()) {
      throw new IllegalArgumentException("Byte size cannot be null or empty");
    }

    Matcher matcher = BYTE_SIZE_PATTERN.matcher(input.trim());
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid byte size format: " + input);
    }

    long value = Long.parseLong(matcher.group(1));
    String unit = matcher.group(2);

    if (unit == null || unit.equalsIgnoreCase("B")) {
      return value; // bytes
    } else {
      char prefix = Character.toUpperCase(unit.charAt(0));
      switch (prefix) {
        case 'K':
          return value * 1024L;
        case 'M':
          return value * 1024L * 1024L;
        case 'G':
          return value * 1024L * 1024L * 1024L;
        case 'T':
          return value * 1024L * 1024L * 1024L * 1024L;
        case 'P':
          return value * 1024L * 1024L * 1024L * 1024L * 1024L;
        case 'E':
          return value * 1024L * 1024L * 1024L * 1024L * 1024L * 1024L;
        default:
          throw new IllegalArgumentException("Unknown byte size unit: " + unit);
      }
    }
  }

  /**
   * Returns the size in bytes.
   *
   * @return size in bytes
   */
  public long getBytes() {
    return bytes;
  }

  @Override
  public Object value() {
    return original;
  }

  @Override
  public TokenType type() {
    return TokenType.BYTE_SIZE;
  }

  @Override
  public JsonElement toJson() {
    JsonObject object = new JsonObject();
    object.addProperty("type", type().name());
    object.addProperty("value", original);
    object.addProperty("bytes", bytes);
    return object;
  }

  @Override
  public String toString() {
    return original;
  }
}
