/*
 * Copyright 2024 Yevgeny Nyden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.curre.jjeopardy.service;

/**
 * Object of this class represents a service exception.
 *
 * @author Yevgeny Nyden
 */
public class ServiceException extends Exception {

  /**
   * Constructor that sets exception message.
   *
   * @param message Exception message to set.
   */
  public ServiceException(String message) {
    super(message);
  }

  /**
   * Constructor that sets exception message
   * and another exception to wrap.
   *
   * @param message Exception message to set.
   * @param e       Exception to wrap.
   */
  public ServiceException(String message, Exception e) {
    super(message, e);
  }

}
