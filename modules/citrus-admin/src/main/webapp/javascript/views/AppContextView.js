(function() {
    define(["jquery", "backbone", "TemplateManager"], function($, Backbone, TemplateManager) {
        var AppContextView = Backbone.View.extend({
          
          events: {
              "click #load-context" : "loadContext",
              "click #stop-context" : "stopContext",
          },
          
          render: function() {
              $(this.el).html(TemplateManager.template('AppContextView',{}));
              return this;
          },
          
          loadContext: function() {
              $('img#load-context-progress').show('fast');
              
              $.ajax({
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
          },
          
          stopContext: function() {
              $('img#load-context-progress').show('fast');
              
              $.ajax({
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
          }
          
        });
        
        return AppContextView;
    });
}).call(this);