(function () {
    define(["TemplateManager"], function (TemplateManager) {
        var ConfigXsdSchemaView = Backbone.View.extend({
            events: {
                "click #btn-new-config-xsd-schema": "showNewXsdSchemaForm",
                "click #btn-save-config-xsd-schema": "createXsdSchema",
                "click #btn-update-config-xsd-schema": "updateXsdSchema",
                "click #btn-cancel-config-xsd-schema": "closeXsdSchemaForm",
                "click .edit-config-xsd-schema": "showEditXsdSchemaForm",
                "click .del-config-xsd-schema": "removeXsdSchema",
                "submit #form-filter-config-xsd-schemas": "filterXsdSchemas",
                "click #btn-reload-config-xsd-schemas": "getXsdSchemas"
            },

            initialize: function () {
            },

            render: function () {
                $(this.el).html(TemplateManager.template('ConfigXsdSchemaView'));
                return this;
            },

            afterRender: function () {
                this.getXsdSchemas();
            },

            getXsdSchemas: function () {
                $.ajax({
                    url: "config/xsd-schema",
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        this.schemas = response;
                        $('#input-filter-config-xsd-schemas').keyup(_.bind(function () {
                            this.filterXsdSchemas();
                        }, this));

                        $("#config-xsd-schemas-table").html(TemplateManager.template('ConfigXsdSchemaTableView', {matches: this.schemas}));
                        $('#input-filter-config-xsd-schemas').val('');
                    }, this),
                    async: true
                });
            },

            filterXsdSchemas: function () {
                var searchKey = $('#input-filter-config-xsd-schemas').val();

                if (searchKey.length) {
                    $("#config-xsd-schemas-table").html(TemplateManager.template('ConfigXsdSchemaTableView', {matches: _.filter(this.schemas, function (schema) {
                        return JSON.stringify(schema).indexOf('":"' + searchKey) >= 0;
                    })}));
                } else {
                    $("#config-xsd-schemas-table").html(TemplateManager.template('ConfigXsdSchemaTableView', {matches: this.schemas}));
                }

                return false;
            },

            removeXsdSchema: function (event) {
                var encodedSchemaId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedSchemaId);
                var url = "config/xsd-schema/" + id;

                $.ajax({
                    url: url,
                    type: 'DELETE',
                    success: _.bind(function (response) {
                        this.getXsdSchemas();
                    }, this),
                    async: true
                });

            },

            showNewXsdSchemaForm: function() {
                $('#dialog-edit-config-xsd-schema').html(TemplateManager.template('ConfigXsdSchemaEditView', {schema: undefined}));
                $('#dialog-edit-config-xsd-schema .modal').modal();

            },

            showEditXsdSchemaForm: function() {
                var encodedSchemaId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedSchemaId);
                var url = "config/xsd-schema/" + id;

                $.ajax({
                    url: url,
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        $('#dialog-edit-config-xsd-schema').html(TemplateManager.template('ConfigXsdSchemaEditView', {schema: response}));
                        $('#dialog-edit-config-xsd-schema .modal').modal();
                    }, this),
                    async: true
                });
            },

            createXsdSchema: function() {
                var form = $('#form-edit-config-xsd-schema form');
                this.closeXsdSchemaForm();

                var serializedForm = form.serializeObject();
                var jsonForm = JSON.stringify(serializedForm);
                var url = "config/xsd-schema";
                $.ajax({
                    url: url,
                    type: 'POST',
                    dataType: "json",
                    contentType: "application/json",
                    data: jsonForm,
                    success: _.bind(function (response) {
                        this.getXsdSchemas();
                    }, this),
                    async: true
                });
                return false;
            },

            updateXsdSchema: function() {
                var form = $('#form-edit-config-xsd-schema form');
                this.closeXsdSchemaForm();

                var serializedForm = form.serializeObject();
                var jsonForm = JSON.stringify(serializedForm);
                var url = "config/xsd-schema/" + serializedForm.id;
                $.ajax({
                    url: url,
                    type: 'PUT',
                    dataType: "json",
                    contentType: "application/json",
                    data: jsonForm,
                    success: _.bind(function (response) {
                        this.getXsdSchemas();
                    }, this),
                    async: true
                });
                return false;
            },

            closeXsdSchemaForm: function() {
                $('#dialog-edit-config-xsd-schema .modal').modal('hide');
            },

            extractId: function(encodedId) {
                var splitString = encodedId.split('-');
                return splitString[splitString.length-1];
            }

        });

        return ConfigXsdSchemaView;
    });
}).call(this);
