(function() {
    define(["TemplateManager"], function(TemplateManager) {
        var WelcomeView = Backbone.View.extend({

            status: false,

            render: function() {
                $(this.el).html(TemplateManager.template('WelcomeView',{}));
                return this;
            }

        });

        return WelcomeView;
    });
}).call(this);