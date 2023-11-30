/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.selenium.groovy

import org.openqa.selenium.By

import static org.citrusframework.selenium.actions.SeleniumActionBuilder.selenium

name "SeleniumTest"
author "Christoph"
status "FINAL"
description "Sample test in Groovy"

actions {
    $(selenium()
        .browser(seleniumBrowser)
        .start()
    )

    $(selenium()
        .alert()
        .accept()
    )

    $(selenium()
        .alert()
        .dismiss()
        .text("This is a warning message!")
    )

    $(selenium()
        .find()
          .element(By.className("clickable"))
          .tagName("button")
          .text("Ok")
          .displayed(true)
          .enabled(true)
          .attribute("type", "submit")
          .style("color", "#000000")
    )

    $(selenium()
        .page(userForm)
        .action("setUserName")
        .argument('${username}')
    )

    $(selenium()
        .page(org.citrusframework.selenium.pages.UserFormPage.class)
        .action("validate")
        .validator(pageValidator)
    )

    $(selenium()
        .click()
        .element(By.id("edit-link"))
    )

    $(selenium()
        .hover()
        .element(By.id("edit-link"))
    )

    $(selenium()
        .setInput()
        .value("new-value")
        .element(By.tagName("input"))
    )

    $(selenium()
        .checkInput()
        .checked(true)
        .element(By.xpath("//input[@type: 'checkbox']"))
    )

    $(selenium()
        .select()
        .option("male")
        .element(By.name("gender"))
    )

    $(selenium()
        .select()
        .element(By.id("title"))
        .options("Mr.", "Dr.")
    )

    $(selenium()
        .fillForm()
        .field("username", "foo_user")
        .field("password", "secret")
    )

    $(selenium()
        .fillForm()
        .fromJson("""
            {
              "username": "foo_user",
              "password": "secret"
            }
        """)
        .submit(By.id("save"))
    )

    $(selenium()
        .waitUntil()
        .hidden()
        .element(By.id("dialog"))
    )

    $(selenium()
        .javascript()
        .script("alert('This is awesome!')")
        .error("Something went wrong")
    )

    $(selenium()
        .browser(seleniumBrowser)
        .screenshot()
        .outputDir("/tmp/storage")
    )

    $(selenium()
        .navigate()
        .page("back")
    )

    $(selenium()
        .open()
        .window("newWindow")
    )

    $(selenium()
        .focus()
        .window("switchWindow")
    )

    $(selenium()
        .close()
        .window( "closeWindow")
    )

    $(selenium()
        .store()
        .filePath("classpath:download/file.txt")
    )

    $(selenium()
        .getStored()
        .fileName("file.txt")
    )

    $(selenium()
        .clearCache()
    )

    $(selenium()
        .stop()
        .browser(seleniumBrowser)
    )
}
