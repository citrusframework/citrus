/**
 * Main Loader setting up citrus-admin
 *
 */

$(document).ready(function() {
  $('#load-context').click(function() {
	  $('img#load-context-progress').show('fast');
	  
	  jQuery.ajax({
          url: "context",
          type: 'GET',
          dataType: "json",
          success: function(response) {
              var appcontext = eval(response);
              
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
  
});