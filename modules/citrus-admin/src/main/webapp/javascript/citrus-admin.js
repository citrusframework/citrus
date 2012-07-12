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

/**
 * Main Loader setting up citrus-admin
 */

$(document).ready(function() {
  $('#load-context').click(function() {
	  $('img#load-context-progress').show('fast');
	  
	  jQuery.ajax({
          url: "context",
          type: 'GET',
          dataType: "json",
          success: function(appcontext) {
              $.each(appcontext.messageSenders, function(index, sender) {
            	  $('#message-senders').append('<p class="message-sender">' + sender.name + '</p>');
              });
              
              $.each(appcontext.messageReceivers, function(index, receiver) {
            	  $('#message-receivers').append('<p class="message-receiver">' + receiver.name + '</p>');
              });

              $.each(appcontext.serverInstances, function(index, server) {
            	  $('#server-instances').append('<p class="server-instance">' + server.name + '</p>');
              });
              
              $('img#load-context-progress').hide('fast');
              
              $('#load-context').hide('fast');
              $('#stop-context').show('fast');
          }
	  });
  });
  
  $('#stop-context').click(function() {
	  $('img#load-context-progress').show('fast');
	  
	  jQuery.ajax({
          url: "context",
          type: 'DELETE',
          dataType: "html",
          success: function(response) {
        	  $('.message-sender').remove();
        	  $('.message-receiver').remove();
        	  $('.server-instance').remove();
        	  
              $('img#load-context-progress').hide('fast');
              
              $('#stop-context').hide('fast');
              $('#load-context').show('fast');
          }
	  });
  });
  
  $('#nav_context').click(function() {
	  $('#test-cases-view').hide();
	  $('#app-context-view').show();
  });
  
  $('#nav_testcases').click(function() {
	  $('#app-context-view').hide();
	  $('#test-cases-view').show();
	  
	  jQuery.ajax({
          url: "testcase",
          type: 'GET',
          dataType: "json",
          success: function(response) {
        	  $('.test-case').remove();
        	  $('.test-package').remove();
        	  
        	  var packageName = '';
              $.each(response.testCaseInfos, function(index, test) {
            	  if (test.packageName != packageName) {
            		  if (packageName) {
            			  $('#test-cases').prepend('<h2 class="test-package">' + packageName + '</h2>');
            		  }
            		  
            		  packageName = test.packageName;
            	  }
            	  
            	  $('#test-cases').prepend('<p class="test-case" style="margin-left: 20px;">' + test.name + ' <a id="' + test.name + '" class="btn btn-success run-test">Run test »</a></p>');
            	  
            	  $('#' + test.name).click(function() {
            		  jQuery.ajax({
            	          url: "testcase/execute/" + $(this).attr('id'),
            	          type: 'GET',
            	          dataType: "html"
            	      });
            	  });
              });
              
              if (packageName) {
    			  $('#test-cases').prepend('<h2 class="test-package">' + packageName + '</h2>');
    		  }
          }
	  });
  });
  
});