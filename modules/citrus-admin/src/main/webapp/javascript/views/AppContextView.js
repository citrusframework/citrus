(function() {
    define(["TemplateManager"], function(TemplateManager) {
        var AppContextView = Backbone.View.extend({

            status: false,

            events: {
                "click #load-context" : "loadContext",
                "click #stop-context" : "stopContext"
            },

            initialize: function() {
                $.ajax({
                    url: "context/status",
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function(response) {
                        this.status = response;
                    }, this),
                    async: false
                });
            },

            render: function() {
                $(this.el).html(TemplateManager.template('AppContextView',{}));
                if(this.status) {
                    $(this.loadContext());
                }
                else {
                    $(document).ready(function() {
                        $('#stop-context').hide('fast');
                        $('#load-context').show('fast');
                    });
                }
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