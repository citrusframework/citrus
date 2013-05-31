(function() {
    define(["TemplateManager", "views/WebSocketView"], function(TemplateManager, WebSocketView) {
        var HeaderView = Backbone.View.extend({
        
            render: function() {
                $(this.el).html(TemplateManager.template('HeaderView',{}));

                Backbone.history.bind('all', _.bind(function() {
                    this.changeNavigation();
                }, this));
              
                $('#web-socket').html(new WebSocketView({ model: CitrusWebSocket }).render().el);
              
                return this;
            },

            changeNavigation: function() {
                if (Backbone.history.fragment == "") { // matches home #
                    $('ul#header-nav > li').removeClass('active');
                    $('ul#header-nav > li').first().addClass('active');
                } else { // try to find matching link target and add active class
                    // save current active nav item
                    var current = $('ul#header-nav > li.active');

                    $('ul#header-nav > li > a').each(function(index) {
                        var link = $(this).attr('href');
                        if (link != "#" && ('#' + Backbone.history.fragment).indexOf(link) != -1) {
                            $(this).parent().addClass('active');
                        } else {
                            $(this).parent().removeClass('active');
                        }
                    });

                    $('ul#header-right-nav > li > a').each(function(index) {
                        var link = $(this).attr('href');
                        if (link != "#" && ('#' + Backbone.history.fragment).indexOf(link) != -1) {
                            $(this).parent().addClass('active');
                        } else {
                            $(this).parent().removeClass('active');
                        }
                    });

                    if ($('ul#header-nav > li.active').size() === 0 && $('ul#header-right-nav > li.active').size() === 0) {
                        // no matching nav item was found for new destination so we activate the old one
                        $(current).addClass('active');
                    }
                }
            }
        
        });
        
        return HeaderView;
    });
}).call(this);