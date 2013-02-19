(function () {
    define(["TemplateManager"], function (TemplateManager) {
        var LoggerView = Backbone.View.extend({

            initialize: function () {
                this.model.on('change:onmessage', this.onSocketMessage, this);
            },

            events: {
                "show .nav-tabs a": "navigateTab",
                "click #toggle-log-content" : "toggleLogContent"

            },

            navigateTab: function(e) {
                var hash = $(e.currentTarget).attr('href');
                localStorage.setItem('lastLogTab', hash);
                window.location.hash = hash;
            },

            render: function () {
                $(this.el).html(TemplateManager.template('LoggerView', { socket: this.model }));
                return this;
            },

            toggleLogContent: function() {
                if (!$('#toggle-log-content').hasClass('log-expanded')) {
                    $('div#log-tab-content').show(200);
                    $('#toggle-log-content').addClass('log-expanded');
                    $('#toggle-log-content').html('Collapse <i class="icon-minus icon-white"></i>');
                } else {
                    $('div#log-tab-content').hide(200);
                    $('#toggle-log-content').removeClass('log-expanded');
                    $('#toggle-log-content').html('Expand <i class="icon-plus icon-white"></i>');
                }
            },

            onSocketMessage: function (message) {
                // TODO MM escape HTML
                // TODO MM re-connect websocket if closed
                // TODO MM change focus to tab on test start
                // TODO MM add close button to process-log output
                // TODO MM add clear button to log output
                // TODO MM rename GENERAL tab to MESSAGES
                // TODO MM fix bug where selected tab is not preserved when using header navigation

                console.log(message);
                if (message != undefined) {
                    jsMessage = $.parseJSON(message)
                    var processId = jsMessage.processId;
                    var idHash = processId.toLowerCase();
                    var logId = 'div#log-details-' + idHash + ' pre';
                    var msg = jsMessage.msg;

                    if ("START" == jsMessage.event) {
                        this.createOrShowProcessTab(processId, idHash);
                        this.displayCancelButton(idHash);
                        $(logId).append(msg);
                    }
                    else if ("MESSAGE" == jsMessage.event) {
                        $(logId).append(msg)
                    }
                    else if ("SUCCESS" == jsMessage.event) {
                        $(logId).append(msg)
                        this.hideCancelButton(idHash);
                    }
                    else if ("FAILED" == jsMessage.event) {
                        $(logId).append(msg)
                        this.hideCancelButton(idHash);
                    }
                    else {
                        return;
                    }
                    $(logId).append("\n")
                    $(logId).scrollTop($(logId)[0].scrollHeight);
                }
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

                    // bind cancel function on newly created tab
                    $('#log-tab-cancel-' + idHash).click(function () {
                        $.ajax({
                            url: "testcase/stop/" + processId,
                            type: 'GET',
                            dataType: "json",
                            success: function() {
                            }
                        });
                    });
                }

                // show processId details tab
                $('#log-tabs a[href="#log-tab-' + idHash + '"]').tab('show');
            },

            displayCancelButton: function (idHash) {
                $('#log-tab-cancel-' + idHash).show('fast');
            },

            hideCancelButton: function (idHash) {
                $('#log-tab-cancel-' + idHash).hide('fast');
            }
        });

        return LoggerView;
    });
}).call(this);