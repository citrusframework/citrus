(function () {
    define(["TemplateManager"], function (TemplateManager) {
        var GlobalVariablesView = Backbone.View.extend({
            globalVariables: {variables: {}},

            events: {
                "click .btn-remove": "remove",
                "click #btn-add": "add"
            },

            initialize: function () {
                this.getNamespaces();
            },

            render: function () {
                $(this.el).html(TemplateManager.template('GlobalVariablesView', {namespaceCount: this.globalVariables.variables.length, variables: this.globalVariables.variables}));
                return this;
            },

            afterRender: function () {
            },

            getNamespaces: function () {
                $.ajax({
                    url: "global-variables",
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        this.globalVariables = response;
                    }, this),
                    async: false
                });
            },

            remove: function (event) {
                var name = $(event.target).closest($("[id]")).attr('id');

                this.globalVariables.variables = _.reject(this.globalVariables.variables, function(variable) {
                    return variable.name === name;
                });

                $.ajax({
                    url: "global-variables/",
                    type: 'PUT',
                    dataType: "json",
                    contentType: "application/json",
                    data: JSON.stringify(this.globalVariables),
                    success: _.bind(function (response) {
                    }, this),
                    async: true
                });

                this.render();
                return false;
            },

            add: function() {
                this.globalVariables.variables.push({ name: $('input[name = "name"]').val(), value: $('input[name = "value"]').val() });

                $.ajax({
                    url: "global-variables/",
                    type: 'PUT',
                    dataType: "json",
                    contentType: "application/json",
                    data: JSON.stringify(this.globalVariables),
                    success: _.bind(function (response) {
                    }, this),
                    async: true
                });

                this.render();
                return false;
            },

            searchOptions: function(event) {
                $.ajax({
                    url: "configuration/search",
                    type: 'POST',
                    dataType: "json",
                    contentType: "text/plain",
                    data: event.currentTarget.name,
                    success: _.bind(function (response) {
                        $('#dropdown-menu-' + event.currentTarget.id).children().remove();

                        if (response.length) {
                            _.each(response, function(item) {
                                $('#dropdown-menu-' + event.currentTarget.id)
                                    .append('<li><a name="' + item + '" class="clickable option-select"><i class="fa fa-cube"></i>&nbsp;' + item + '</a></li>');
                            });
                        } else {
                            $('#dropdown-menu-' + event.currentTarget.id).append('<li><a name="none">no suggestions</a></li>');
                        }

                        $('#dropdown-menu-' + event.currentTarget.id).find('a.option-select').click(function(e) {
                            $('input[name="' + event.currentTarget.id + '"]').val(e.currentTarget.name);
                        })
                    }, this),
                    async: true
                });

                // prevent default form submission
                return false;
            }

        });

        return GlobalVariablesView;
    });
}).call(this);
