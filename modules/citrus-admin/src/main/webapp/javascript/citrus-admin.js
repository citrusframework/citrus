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
            	  
            	  $('#test-cases').prepend('<tr class="test-case"><td style="padding-left: 35px;"><p id="' + test.name + '" test-package="' + test.packageName + '"><strong>' + test.name + '</strong></p></td><td><a id="' + test.name + '" class="btn btn-success run-test">Run test</a></td></tr>');
            	  
            	  $('a#' + test.name).click(function() {
            		  jQuery.ajax({
            	          url: "testcase/execute/" + $(this).attr('id'),
            	          type: 'GET',
            	          dataType: "html",
            	          success: function(testResult) {
            	        	  
            	          }
            	      });
            	  });
            	  
            	  $('p#' + test.name).click(function() {
            		  $('pre.test-case-code').remove();
            		  $(this).parent().append('<pre class="prettyprint linenums test-case-code">Loading test ...</pre>');
            		  
            		  jQuery.ajax({
            	          url: "testcase/" + $(this).attr('test-package') + "/" + $(this).attr('id'),
            	          type: 'GET',
            	          dataType: "html",
            	          success: function(fileContent) {
            	        	  $('pre.test-case-code').text(fileContent);
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
  
});