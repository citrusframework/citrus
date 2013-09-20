(function() {
    define([], function() {
        var WebSocketHolder = Backbone.Model.extend({
        
          socket: {},
          online: false,
        
          statusMessages: [],
          
          initialize: function() {
              this.socket = new WebSocket("ws://" + window.location.hostname + ":" + window.location.port + "/citrus-admin/log/");
              this.socket.onopen = _.bind(function(evt) { 
                  this.statusMessages.push('Opened web socket connection');
                  this.online = true;
                  this.trigger('change:status');
              }, this);
              
              this.socket.onclose = _.bind(function(evt) { 
                  this.statusMessages.push('Closed web socket connection'); 
                  this.online = false;
                  this.trigger('change:status');
              }, this);
              
              this.socket.onmessage = _.bind(function(evt) { 
                  this.statusMessages.push(evt.data);
                  this.trigger('change:onmessage', evt.data);
              }, this);
              
              this.socket.onerror = _.bind(function(evt) { 
                  this.statusMessages.push(evt.data); 
                  this.trigger('change:onerror', evt.data);
              }, this);
              
              return this;
          }
        });
        
        return WebSocketHolder;
    });
}).call(this);

    