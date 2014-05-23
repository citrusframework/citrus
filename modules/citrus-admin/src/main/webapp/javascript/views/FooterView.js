(function() {
    define(["TemplateManager"], function(TemplateManager) {
        var FooterView = Backbone.View.extend({

            events: {
                "show .nav-tabs a": "navigateTab",
                "click #footer-maximize": "maximize",
                "click #footer-minimize" : "minimize"
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
                // TODO MM fix bug where selected tab is not preserved when using header navigation

                if (message) {
                    console.log(message);
                    jsMessage = $.parseJSON(message)

                    if ("PING" == jsMessage.event) {
                        return;
                    }

                    var processId = jsMessage.processId;
                    var idHash = processId.toLowerCase();
                    var logId = 'div#footer-tab-content-' + idHash + ' pre';
                    var msg = jsMessage.msg;

                    if ("PROCESS_START" == jsMessage.event) {
                        this.createOrShowTab(processId, idHash);

                        $('div#footer-tab-content-' + idHash).find('div.footer-task-bar').children('i.fa-stop').removeClass('disabled');

                        if ($('#footer').hasClass('minimized')) {
                            this.minimize(); // auto open footer task bar
                        }

                        $(logId).append(msg);
                    } else if ("PROCESS_SUCCESS" == jsMessage.event || "PROCESS_FAILED" == jsMessage.event) {
                        $('div#footer-tab-content-' + idHash).find('div.footer-task-bar').children('i.fa-stop').addClass('disabled');
                        $(logId).append(msg);
                    } else if ("LOG_MESSAGE" == jsMessage.event) {
                        $(logId).append(msg);
                    } else {
                        return;
                    }

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

                    // bind clear function on newly created tab
                    $('#footer-tab-clear-' + idHash).click(function () {
                        $('#footer-tab-content-' + idHash).find('pre.logger').text('');
                    });

                    // bind scroll function on newly created tab
                    $('#footer-tab-scroll-' + idHash).click(function () {
                        var logger = $('#footer-tab-content-' + idHash).find('pre.logger');
                        logger.scrollTop(logger[0].scrollHeight);
                    });
                }

                // show processId details tab
                $('#footer-tabs a[href="#footer-tab-' + idHash + '"]').tab('show');
            },
          
            maximize: function() {
                if ($('#footer').hasClass('maximized')) {
                    $('#footer').removeClass('maximized');
                    $('.footer-tab').removeClass('maximized');
                    $('.footer-task-bar').removeClass('maximized');
                    $('pre.logger').removeClass('maximized');
                } else {
                    $('#footer').addClass('maximized');
                    $('.footer-tab').addClass('maximized');
                    $('.footer-task-bar').addClass('maximized');
                    $('pre.logger').addClass('maximized');

                    if ($('#footer').hasClass('minimized')) {
                        this.minimize();
                    }
                }
            },

            minimize: function() {
                if ($('#footer').hasClass('minimized')) {
                    $('#footer').hide('slide', {direction: 'down'}, 150, function() {
                        $('#footer').removeClass('minimized');
                        $('.footer-tab').removeClass('minimized');
                        $('.footer-task-bar').removeClass('minimized');
                        $('pre.logger').removeClass('minimized');

                        $('i#footer-minimize').addClass('fa-chevron-down');
                        $('i#footer-minimize').removeClass('fa-chevron-up');

                        $('#footer').show('slide', {direction: 'down'}, 300);
                    });
                } else {
                    $('#footer').hide('slide', {direction: 'down'}, 300, function() {
                        $('#footer').removeClass('maximized');
                        $('.footer-tab').removeClass('maximized');
                        $('.footer-task-bar').removeClass('maximized');
                        $('pre.logger').removeClass('maximized');

                        $('i#footer-minimize').removeClass('fa-chevron-down');
                        $('i#footer-minimize').addClass('fa-chevron-up');

                        $('#footer').addClass('minimized');
                        $('.footer-tab').addClass('minimized');
                        $('.footer-task-bar').addClass('minimized');
                        $('pre.logger').addClass('minimized');

                        $('#footer').show('slide', {direction: 'down'}, 150);
                    });
                }
            }

        });
        
        return FooterView;
    });
}).call(this);