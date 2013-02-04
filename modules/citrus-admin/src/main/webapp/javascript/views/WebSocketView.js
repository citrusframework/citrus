(function () {
    define(["TemplateManager"], function (TemplateManager) {
        var WebSocketView = Backbone.View.extend({

            initialize: function () {
                this.model.on('change:status', this.render, this);
                this.model.on('change:onmessage', this.onSocketMessage, this);
                this.model.on('change:onerror', this.onSocketErrorMessage, this);
            },

            render: function () {
                $(this.el).html(TemplateManager.template('WebSocketView', { socket: this.model }));
                return this;
            },

            onSocketMessage: function (message) {
                // TODO MM escape HTML
                // TODO MM re-connect websocket if closed
                // TODO MM change focus to tab on test start
                // TODO MM add close button to process-log output
                // TODO MM add clear button to log output
                // TODO MM rename GENERAL tab to MESSAGES

                console.log(message);
                if (message != undefined) {
                    jsMessage = $.parseJSON(message)
                    var processId = jsMessage.processId;
                    var idHash = processId.toLowerCase();
                    var logId = 'div#log-details-' + idHash + ' pre';
                    var msg = jsMessage.msg;

                    if ("START" == jsMessage.event) {
                        this.createOrShowProcessTab(processId, idHash);
                        $(logId).html(jsMessage.msg);
                    }
                    else if ("MESSAGE" == jsMessage.event) {
                        $(logId).append(msg)
                    }
                    else if ("SUCCESS" == jsMessage.event) {
                        $(logId).append(msg)
                    }
                    else if ("FAILED" == jsMessage.event) {
                        $(logId).append(msg)
                    }
                    else {
                        return;
                    }
                    $(logId).append("\n")
                    $(logId).scrollTop($(logId)[0].scrollHeight);
                }
            },

            onSocketErrorMessage: function (message) {
                console.log(message.msg);
            },

            createOrShowProcessTab: function (processId, idHash) {
                if ($('ul#log-tabs li#tab-' + idHash).size() === 0) {

                    $('ul#log-tabs').append(Handlebars.compile($('#log-tab').html())({hash: idHash, name: processId}));
                    $('div#log-tab-content').append(Handlebars.compile($('#log-tab-pane').html())({hash: idHash}));

                    // bind close function on newly created tab
                    $('#log-tab-close-' + idHash).click(function () {
                        if ($(this).parent('li').hasClass('active')) {
                            // removed tab was active so display first tab (search tab)
                            $('#log-tabs a:first').tab('show');
                        }

                        // remove tab item
                        $(this).parent('li').remove();
                    });
                }

                // show processId details tab
                $('#log-tabs a[href="#log-tab-' + idHash + '"]').tab('show');
            }

        });

        return WebSocketView;
    });
}).call(this);