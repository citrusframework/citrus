(function () {
    define(["TemplateManager"], function (TemplateManager) {
        var EndpointListView = Backbone.View.extend({
            endpoints: undefined,

            events: {
                "click tr.endpoint": "showEditForm",
                "click .btn-remove": "remove",
                "click .btn-new": "showNewForm",
                "click #btn-add": "create",
                "click #btn-save": "save",
                "click #btn-cancel": "closeForm"
            },

            initialize: function () {
                this.getEndpoints();
            },

            render: function () {
                var groupedEndpoints = _.groupBy(this.endpoints, function(endpoint) { return endpoint.type; });
                groupedEndpoints = _.map(groupedEndpoints, function(value, key) { return { type: key, endpoints: value }; });

                $(this.el).html(TemplateManager.template('EndpointListView', {endpointTypes: groupedEndpoints}));
                return this;
            },

            afterRender: function () {
            },

            getEndpoints: function () {
                $.ajax({
                    url: "endpoint",
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        this.endpoints = response;
                    }, this),
                    async: false
                });
            },

            remove: function (event) {
                var encodedId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedId);
                var url = "endpoint/" + id;

                $.ajax({
                    url: url,
                    type: 'DELETE',
                    success: _.bind(function (response) {
                        this.render();
                    }, this),
                    async: true
                });
            },

            showNewForm: function() {
                $('#endpoint-edit').html(TemplateManager.template('EndpointEditView', {}));
                $('#endpoint-list').hide('slide', function() {
                    $('#endpoint-edit').show('slide');
                });

            },

            showEditForm: function() {
                var encodedId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedId);
                var url = "endpoint/" + id;

                $.ajax({
                    url: url,
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        $('#endpoint-edit').html(TemplateManager.template('EndpointEditView', response));
                        $('#endpoint-list').hide('slide', function() {
                            $('#endpoint-edit').show('slide');
                        });
                    }, this),
                    async: true
                });
            },

            create: function() {
                var form = $('#endpoint-edit-form');
                this.closeForm();

                var serializedForm = form.serializeObject();
                var url = "endpoint";
                $.ajax({
                    url: url,
                    type: 'POST',
                    dataType: "json",
                    contentType: "application/json",
                    data: JSON.stringify(serializedForm),
                    success: _.bind(function (response) {
                        this.render();
                    }, this),
                    async: true
                });
                return false;
            },

            save: function() {
                var form = $('#endpoint-edit-form');
                this.closeForm();

                var serializedForm = form.serializeObject();
                var elementId = serializedForm.id;

                if (serializedForm.id != serializedForm.newId) {
                    serializedForm.id = serializedForm.newId;
                }

                serializedForm = _.omit(serializedForm, "newId");

                var url = "endpoint/" + elementId;
                $.ajax({
                    url: url,
                    type: 'PUT',
                    dataType: "json",
                    contentType: "application/json",
                    data: JSON.stringify(serializedForm),
                    success: _.bind(function (response) {
                        this.render();
                    }, this),
                    async: true
                });
                return false;
            },

            closeForm: function() {
                $('#endpoint-edit').hide('slide', function() {
                    $('#endpoint-list').show('slide');
                });
            },

            extractId: function(encodedId) {
                var splitString = encodedId.split('-');
                return splitString[splitString.length-1];
            }

        });

        return EndpointListView;
    });
}).call(this);
