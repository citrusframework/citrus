(function() {
    define(["TemplateManager"], function(TemplateManager) {
        var WebSocketView = Backbone.View.extend({
        
            initialize: function() {
                this.model.on('change:status', this.render, this);
                this.model.on('change:onmessage', this.onSocketMessage, this);
                this.model.on('change:onerror', this.onSocketErrorMessage, this);
            },
            
            render: function() {
                $(this.el).html(TemplateManager.template('WebSocketView', { socket: this.model }));
                return this;
            },
            
            onSocketMessage: function(message) {
              // TODO MM escape HTML
              console.log(message);
              if(message != undefined) {
                jsMessage = $.parseJSON(message)
                if("START" == jsMessage.event) {
                  $('.log').html(jsMessage.msg);
                }
                else if("MESSAGE" == jsMessage.event) {
                  $('.log').append(jsMessage.msg)
                }
                else if("SUCCESS" == jsMessage.event) {
                  $('.log').append(jsMessage.msg)
                }
                else if("FAILED" == jsMessage.event) {
                  $('.log').append(jsMessage.msg)
                }
                else {
                  return;
                }
                $('.log').append("\n")
                $('.log').scrollTop($('.log')[0].scrollHeight);
              }
            },
            
            onSocketErrorMessage: function(message) {
              console.log(message.msg);
            }
        
        });
        
        return WebSocketView;
    });
}).call(this);