/*
 * Copyright 2006-2012 the original author or authors.
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

var logOutput;
var websocket;

var webSocketInit = function() {
    websocket = new WebSocket("ws://localhost:9080/citrus-admin/log/");
    websocket.onopen = function(evt) { logOutput.append('<p>Opened web socket connection</p>') };
    websocket.onclose = function(evt) { logOutput.append('<p>Closed web socket connection</p>') };
    websocket.onmessage = function(evt) { logOutput.append('<p>' + evt.data + '</p>') };
    websocket.onerror = function(evt) { logOutput.append('<p>Error: ' + evt.data + '</p>') };
}

var app;

curl({
  baseUrl: 'javascript',
  paths: {
      "jquery" : "support/jquery",
      "underscore": "support/underscore",
      "backbone": "support/backbone",
      "handlebars" : "support/handlebars",
      "bootstrap-alert" : "support/bootstrap-alert",
      "prettify" : "support/prettify",
      "TemplateManager" : "views/TemplateManager",
      "AppRouter" : "router/AppRouter"
  }},
  ["jquery", "underscore", "backbone", "TemplateManager", "AppRouter", "domReady!"], function($, _, Backbone, TemplateManager, AppRouter) {
    
  TemplateManager.load(['HeaderView', 'AppContextView', 'TestCasesView'], function() {
      app = new AppRouter();
      Backbone.history.start();
  });
  
  logOutput = $('body');
  webSocketInit();
  
});