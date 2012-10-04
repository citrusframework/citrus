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

var changeNavigation = function(clicked) {
	$('ul.nav li').removeClass('active');
	clicked.parent().addClass('active');
}

var output = $('body');
var websocket;

var webSocketInit = function() {
	websocket = new WebSocket("ws://localhost:9080/citrus-admin/log/");
    websocket.onopen = function(evt) { output.append('<p>Opened web socket connection</p>') };
    websocket.onclose = function(evt) { output.append('<p>Closed web socket connection</p>') };
    websocket.onmessage = function(evt) { output.append('<p>' + evt.data + '</p>') };
    websocket.onerror = function(evt) { output.append('<p>Error: ' + evt.data + '</p>') };
}

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
	  changeNavigation($(this));
	  $('#testcases-view').hide();
	  $('#context-view').show();
  });
  
  $('#nav_testcases').click(function() {
	  changeNavigation($(this));
	  $('#context-view').hide();
	  $('#testcases-view').show();
	  
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
            			  $('#test-cases').prepend('<tr class="test-package"><td colspan="2"><b>' + packageName + '</b></td></tr>');
            		  }
            		  
            		  packageName = test.packageName;
            	  }
            	  
            	  $('#test-cases').prepend('<tr class="test-case"><td style="padding-left: 35px;"><p id="' + test.name + '"><i id="' + test.name + '" test-package="' + test.packageName + '" class="icon-list-alt"></i>&nbsp;&nbsp;<strong>' + test.name + '</strong></p></td><td><a id="' + test.name + '" class="btn btn-success run-test"><i class="icon-play icon-white"></i></a></td></tr>');
            	  
            	  $('a#' + test.name).click(function() {
            		  $('div.alert').hide('fast');
            		  $('div.alert').remove();
            		  var id = $(this).attr('id');
            		  $('p#' + id).append('<div class="alert"><strong>Running test!</strong> Run Citrus test case ... <img src="images/ajax-loader.gif" alt="ajax-loader.gif" class="ajax-loader"/></div>');
            		  
            		  output = $('p#' + id);
            		  
            		  jQuery.ajax({
            	          url: "testcase/execute/" + $(this).attr('id'),
            	          type: 'GET',
            	          dataType: "json",
            	          success: function(testResult) {
            	        	  $('div.alert').hide('fast');
            	        	  $('div.alert').remove();
            	        	  
            	        	  var id = testResult.testCase.name;
            	        	  
            	        	  if (testResult.success) {
            	        		  $('p#' + id).append('<div class="alert alert-success" style="display:none;"><a class="close" data-dismiss="alert">×</a><strong>SUCCESS!</strong> ' + id + ' was executed successfully!</div>');
            	        	  } else {
            	        		  $('p#' + id).append('<div class="alert alert-error" style="display:none;"><a class="close" data-dismiss="alert">×</a><strong>FAILED!</strong> ' + id + ' failed!<p>' + testResult.failureStack + '</p><p>' + testResult.stackTrace + '</p></div>');
            	        	  }
            	        	  
            	        	  $('div.alert').alert(); // enable alert dismissal
            	        	  $('div.alert').show('fast');
            	          }
            	      });
            	  });
            	  
            	  $('i#' + test.name).click(function() {
            		  $('pre.test-case-code').remove();
            		  $(this).parent().append('<pre class="prettyprint linenums test-case-code">Loading test ...</pre>');
            		  
            		  jQuery.ajax({
            	          url: "testcase/" + $(this).attr('test-package') + "/" + $(this).attr('id'),
            	          type: 'GET',
            	          dataType: "html",
            	          success: function(fileContent) {
            	        	  $('pre.test-case-code').text(fileContent);
            	        	  $('pre.test-case-code').prepend('<a class="close close-code">×</a>');
            	        	  
            	        	  $('a.close-code').click(function() {
                    			  $('pre.test-case-code').remove();
                    		  });
            	        	  
            	        	  prettyPrint();
            	          }
            	      });
            	  });
              });
              
              if (packageName) {
    			  $('#test-cases').prepend('<tr class="test-package"><td colspan="2"><b>' + packageName + '</b></td></tr>');
    		  }
              
              $('#cnt-tests').text(response.testCaseInfos.length);
          }
	  });
  });
  
  webSocketInit();
  
});