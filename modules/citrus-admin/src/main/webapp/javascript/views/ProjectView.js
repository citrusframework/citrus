(function() {
    define(["TemplateManager"], function(TemplateManager) {
        var ProjectView = Backbone.View.extend({

            status: false,

            render: function() {
                $(this.el).html(TemplateManager.template('ProjectView',{}));
                return this;
            }

        });

        return ProjectView;
    });
}).call(this);