(function () {
    define(["TemplateManager"], function (TemplateManager) {
        var EndpointListView = Backbone.View.extend({
            endpoints: undefined,

            events: {
                "click tr.endpoint": "showEditDialog",
                "click .btn-endpoint-remove": "removeEndpoint",
                "click #btn-endpoint-new": "showNewDialog",
                "click #btn-endpoint-save": "createEndpoint",
                "click #btn-endpoint-update": "updateEndpoint",
                "click #btn-endpoint-cancel": "closeDialog"
            },

            initialize: function () {
                this.getEndpoints();
            },

            render: function () {
                $(this.el).html(TemplateManager.template('EndpointListView', {endpoints: this.endpoints}));
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

            removeEndpoint: function (event) {
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

            showNewDialog: function() {
                $('#endpoint-edit-dialog').html(TemplateManager.template('EndpointEditView', {}));
                $('#endpoint-edit-dialog .modal').modal();

            },

            showEditDialog: function() {
                var encodedSchemaId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedSchemaId);
                var url = "endpoint/" + id;

                $.ajax({
                    url: url,
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        $('#endpoint-edit-dialog').html(TemplateManager.template('EndpointEditView', {}));
                        $('#endpoint-edit-dialog .modal').modal();
                    }, this),
                    async: true
                });
            },

            createEndpoint: function() {
                var form = $('#form-endpoint-edit form');
                this.closeDialog();

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

            updateEndpoint: function() {
                var form = $('#form-endpoint-edit form');
                this.closeDialog();

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

            closeDialog: function() {
                $('#endpoint-edit-dialog .modal').modal('hide');
            },

            extractId: function(encodedId) {
                var splitString = encodedId.split('-');
                return splitString[splitString.length-1];
            }

        });

        return EndpointListView;
    });
}).call(this);
