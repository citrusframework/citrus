(function() {
    define([], function() {
        var LoggingWebSocketModel = Backbone.Model.extend({
        
          socket: {},
        
          logOutput: {},
          
          initialize: function() {
              this.logOutput = $('body');
              this.socket = new WebSocket("ws://localhost:9080/citrus-admin/log/");
              this.socket.onopen = _.bind(function(evt) { 
                  this.logOutput.append('<p>Opened web socket connection</p>'); 
              }, this);
              this.socket.onclose = _.bind(function(evt) { this.logOutput.append('<p>Closed web socket connection</p>'); }, this);
              this.socket.onmessage = _.bind(function(evt) { this.logOutput.append('<p>' + evt.data + '</p>'); }, this);
              this.socket.onerror = _.bind(function(evt) { this.logOutput.append('<p>Error: ' + evt.data + '</p>'); }, this);
              
              return this;
          },
          
          bindOutput: function(id) {
              this.logOutput = $(id);
          }
        
        });
        
        return LoggingWebSocketModel;
    });
}).call(this);

    