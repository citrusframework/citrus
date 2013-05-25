(function() {
    define(["TemplateManager"], function(TemplateManager) {
        var FooterView = Backbone.View.extend({

            events: {
                "show .nav-tabs a": "navigateTab",
                "dblclick .footer-content": "resize"
            },

            initialize: function () {
                CitrusWebSocket.on('change:onmessage', this.onSocketMessage, this);
            },
          
            render: function() {
                $(this.el).html(TemplateManager.template('FooterView', {}));
                return this;
            },

            navigateTab: function(e) {
                var hash = $(e.currentTarget).attr('href');
                localStorage.setItem('lastLogTab', hash);
                window.location.hash = hash;
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
                    var logId = 'div#footer-details-' + idHash + ' pre';
                    var msg = jsMessage.msg;

                    if ("START" == jsMessage.event) {
                        this.createOrShowTab(processId, idHash);
                        $(logId).append(msg);
                    } else if ("MESSAGE" == jsMessage.event) {
                        $(logId).append(msg)
                    } else if ("SUCCESS" == jsMessage.event) {
                        $(logId).append(msg)
                    } else if ("FAILED" == jsMessage.event) {
                        $(logId).append(msg)
                    } else {
                        return;
                    }
                    $(logId).append("\n")
                    $(logId).scrollTop($(logId)[0].scrollHeight);
                }
            },

            createOrShowTab: function (processId, idHash) {
                if ($('ul#footer-tabs li#tab-' + idHash).size() === 0) {

                    $('ul#footer-tabs').append(Handlebars.compile($('#footer-tab').html())({hash: idHash, name: processId}));
                    $('div#footer-tab-content').append(Handlebars.compile($('#footer-tab-pane').html())({hash: idHash}));

                    // bind close function on newly created tab
                    $('#footer-tab-close-' + idHash).click(function () {
                        if ($(this).parent('li').hasClass('active')) {
                            // removed tab was active so display first tab (search tab)
                            $('#footer-tabs a:first').tab('show');
                        }

                        // remove tab item
                        $(this).parent('li').remove();
                    });

                    // bind cancel function on newly created tab
                    $('#footer-tab-cancel-' + idHash).click(function () {
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
                $('#footer-tabs a[href="#footer-tab-' + idHash + '"]').tab('show');
            },
          
            resize: function() {
                $('#footer').toggleClass('resized');
                $('pre.logevent').toggleClass('resized');
            }

        });
        
        return FooterView;
    });
}).call(this);