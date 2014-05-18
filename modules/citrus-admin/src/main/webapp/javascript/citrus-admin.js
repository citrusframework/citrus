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

var CitrusWebSocket;
var CitrusAdmin;

curl({
  baseUrl: 'javascript',
  paths: {
      "TemplateManager" : "templates/TemplateManager",
      "WebSocketHolder" : "socket/WebSocketHolder",
      "AppRouter" : "router/AppRouter"
  }},
  ["TemplateManager", "AppRouter", "WebSocketHolder", "domReady!"], function(TemplateManager, AppRouter, WebSocketHolder) {
    
  TemplateManager.load(['HeaderView', 'ProjectView'], function() {
      CitrusWebSocket = new WebSocketHolder();
      CitrusAdmin = new AppRouter();
      
      $('body').ajaxStart(function() {
          $('.ajax-loader').show();
      });
      
      $('body').ajaxComplete(function() {
          $('.ajax-loader').hide();
      });
      
      Backbone.history.start();
  });
});