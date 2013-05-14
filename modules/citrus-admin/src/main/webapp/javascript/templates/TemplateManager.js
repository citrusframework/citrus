(function() {
    define([], function() {
        var TemplateManager = {
        
            templates: {},
        
            load: function(names, callback) {
                _.each(names, _.bind(function(name) {
                    $.ajax({
                        url: 'javascript/templates/' + name + '.html',
                        success: _.bind(function(data) {
                                     this.templates[name] = Handlebars.compile(data);
                                 }, this),
                        async: false
                    });
                }, this));
                
                callback();
            },
            
            template: function(name, context) {
                if (!this.templates[name]) {
                    $.ajax({
                        url: 'javascript/templates/' + name + '.html',
                        success: _.bind(function(data) {
                                     this.templates[name] = Handlebars.compile(data);
                                 }, this),
                        async: false
                    });
                }
                
                return this.templates[name](context);
            }
            
        };
        
        return TemplateManager;
    });
}).call(this);
