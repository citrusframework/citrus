/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// modules: citrus-camel,citrus-testcontainers
// deps: org.apache.camel:camel-aws2-s3:4.20.0

name "my-test"
author "Christoph"
status "FINAL"
description "Sample test in Groovy"

variables {
    foo="bar"
    id="citrus:randomNumber(4)"
}

configuration {
    endpoints {
        jms()
            .asynchronous()
            .name("foo")
            .destination("my-queue")
    }
}

actions {
    $(print().message("Citrus rocks!"))

    $(send().endpoint("http://localhost:8080/test")
      .message()
          .body("Howdy"))

    $(send()
      .endpoint('camel:paho-mqtt5:${mqtt.topic}?brokerUrl=tcp://localhost:12883&amp;clientId=${mqtt.client.id}')
      .message()
          .body("Hi"))

    $(send()
      .endpoint("kafka:my-topic")
      .message()
        .body('Hello ${id}'))

    $(receive()
      .endpoint("kafka:my-topic")
      .message()
        .body("@ignore@ @isNumber()@"))

    $(iterate()
      .condition("i < 10")
      .actions(
        print().message('${i}')
      )
  )
}
