(function () {
    define(["TemplateManager","views/ConfigXsdSchemaView"], function (TemplateManager, ConfigXsdSchemaView) {
        var ConfigView = Backbone.View.extend({
            tabs:[
                {
                    active: true,
                    idSuffix: "xsd-schemas",
                    displayName: "XSD Schemas",
                    view: ConfigXsdSchemaView
                },
                {
                    idSuffix: "schema-repositories",
                    displayName: "Schema Repositories"
                }
            ],

            events: {
            },

            render: function () {
                $(this.el).html(TemplateManager.template('ConfigView', {}));
                return this;
            },

            afterRender: function () {
                this.createConfigTabs();
            },

            createConfigTabs: function () {
                _.each(this.tabs, this.createConfigTab);
            },

            createConfigTab: function (tab) {
                // generate tab-content-containers
                $('#config-tab-content').append(Handlebars.compile($('#config-tab-content-template').html())({id: tab.idSuffix}));

                // generate tab-header-container
                $('ul#config-tabs').append(Handlebars.compile($('#config-tab-template').html())({id: tab.idSuffix, tabDisplayName: tab.displayName, active: tab.active}));

                if(tab.active) {
                    var view = new tab.view({el: $('#config-xsd-schemas')});
                    view.render();
                    view.afterRender();
                    $('#config-tabs a[href="#config-tab-' + tab.idSuffix + '"]').tab('show');
                }
            }

        });

        return ConfigView;
    });
}).call(this);
