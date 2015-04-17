(function () {
    define(["TemplateManager"], function (TemplateManager) {
        var SchemaListView = Backbone.View.extend({
            schemas: undefined,
            schemaRepositories: undefined,

            events: {
                "click .btn-schema-new": "newSchema",
                "click .btn-schema-remove": "removeSchema",
                "click #btn-schema-add": "createSchema",
                "click #btn-schema-save": "updateSchema",
                "click #btn-schema-cancel": "closeSchemaForm",
                "click .btn-repository-new": "newRepository",
                "click .btn-repository-remove": "removeSchemaRepository",
                "click #btn-repository-add": "createRepository",
                "click #btn-repository-save": "updateRepository",
                "click #btn-repository-cancel": "closeRepositoryForm",
                "click div.schema": "showSchemaEditForm",
                "click div.repository": "showRepositoryEditForm"
            },

            initialize: function () {
                this.getSchemas();
                this.getSchemaRepositories();
            },

            render: function () {
                $(this.el).html(TemplateManager.template('SchemaListView', {repositories: this.schemaRepositories, schemas: this.schemas}));
                return this;
            },

            afterRender: function () {
            },

            getSchemas: function () {
                $.ajax({
                    url: "schema",
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        this.schemas = response;
                    }, this),
                    async: false
                });
            },

            getSchemaRepositories: function () {
                $.ajax({
                    url: "schema-repository",
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        this.schemaRepositories = response;
                    }, this),
                    async: false
                });
            },

            removeSchema: function (event) {
                var encodedSchemaId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedSchemaId);
                var url = "schema/" + id;

                $.ajax({
                    url: url,
                    type: 'DELETE',
                    success: _.bind(function (response) {
                        this.getSchemas();
                        this.render();
                    }, this),
                    async: true
                });

            },

            removeSchemaRepository: function (event) {
                var encodedId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedId);
                var url = "schema-repository/" + id;

                $.ajax({
                    url: url,
                    type: 'DELETE',
                    success: _.bind(function (response) {
                        this.getSchemaRepositories();
                        this.render();
                    }, this),
                    async: true
                });

            },

            newSchema: function() {
                $('#schema-edit').html(TemplateManager.template('SchemaEditView', {}));
                $('#schema-list').hide('slide', function() {
                    $('#schema-edit').show('slide');
                });
            },

            newRepository: function() {
                $('#repository-edit').html(TemplateManager.template('SchemaRepositoryEditView', {schemas: this.schemas}));
                $('#schema-list').hide('slide', function() {
                    $('#repository-edit').show('slide');
                });
            },

            showSchemaEditForm: function() {
                var encodedSchemaId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedSchemaId);
                var url = "schema/" + id;

                $.ajax({
                    url: url,
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        $('#schema-edit').html(TemplateManager.template('SchemaEditView', response));
                        $('#schema-list').hide('slide', function() {
                            $('#schema-edit').show('slide');
                        });
                    }, this),
                    async: true
                });
            },

            showRepositoryEditForm: function() {
                var encodedSchemaId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedSchemaId);
                var url = "schema-repository/" + id;

                $.ajax({
                    url: url,
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        $('#repository-edit').html(TemplateManager.template('SchemaRepositoryEditView', {repository: response, schemas: this.filterSchemas(response.schemas.revesAndSchemas)}));

                        $('#schema-list').hide('slide', function() {
                            $('#repository-edit').show('slide');
                        });
                    }, this),
                    async: true
                });
            },

            filterSchemas: function(schemaRefs) {
                return _.reject(this.schemas, function(schema) {
                    var found = _.find(schemaRefs, function(candidate) {
                        if (candidate.schema) {
                            return candidate.schema === schema.id;
                        } else {
                            return candidate.id === schema.id;
                        }
                    })

                    return found;
                });
            },

            createSchema: function() {
                var form = $('#schema-edit-form');

                var serializedForm = form.serializeObject();
                var jsonForm = JSON.stringify(serializedForm);
                var url = "schema";
                $.ajax({
                    url: url,
                    type: 'POST',
                    dataType: "json",
                    contentType: "application/json",
                    data: jsonForm,
                    success: _.bind(function (response) {
                    }, this),
                    async: false
                });

                this.closeSchemaForm(undefined, _.bind(function() {
                    this.getSchemas();
                    this.render();
                }, this));

                return false;
            },

            createRepository: function() {
                var form = $('#repository-edit-form');

                var serializedForm = form.serializeObject();
                var url = "schema-repository";
                $.ajax({
                    url: url,
                    type: 'POST',
                    dataType: "json",
                    contentType: "application/json",
                    data: this.getSchemaRepositoryJSON(serializedForm),
                    success: _.bind(function (response) {
                    }, this),
                    async: false
                });

                this.closeRepositoryForm(undefined, _.bind(function() {
                    this.getSchemaRepositories();
                    this.render();
                }, this));

                return false;
            },

            updateSchema: function() {
                var form = $('#schema-edit-form');

                var serializedForm = form.serializeObject();
                var schemaId = serializedForm.id;

                if (serializedForm.id != serializedForm.newId) {
                    serializedForm.id = serializedForm.newId;
                }

                serializedForm = _.omit(serializedForm, "newId");

                var jsonForm = JSON.stringify(serializedForm);
                var url = "schema/" + schemaId;
                $.ajax({
                    url: url,
                    type: 'PUT',
                    dataType: "json",
                    contentType: "application/json",
                    data: jsonForm,
                    success: _.bind(function (response) {
                    }, this),
                    async: false
                });

                this.closeSchemaForm(undefined, _.bind(function() {
                    this.getSchemas();
                    this.render();
                }, this));

                return false;
            },

            updateRepository: function() {
                var form = $('#repository-edit-form');

                var serializedForm = form.serializeObject();
                var schemaRepositoryId = serializedForm.id;

                if (serializedForm.id != serializedForm.newId) {
                    serializedForm.id = serializedForm.newId;
                }

                serializedForm = _.omit(serializedForm, "newId");

                var url = "schema-repository/" + schemaRepositoryId;
                $.ajax({
                    url: url,
                    type: 'PUT',
                    dataType: "json",
                    contentType: "application/json",
                    data: this.getSchemaRepositoryJSON(serializedForm),
                    success: _.bind(function (response) {
                    }, this),
                    async: false
                });

                this.closeRepositoryForm(undefined, _.bind(function() {
                    this.getSchemaRepositories();
                    this.render();
                }, this));

                return false;
            },

            getSchemaRepositoryJSON: function(serializedForm) {
                var schemas = [];

                $('input:checked').each(function(index) {
                    schemas.push( $(this).attr('id') );
                });

                var schemaRepository = { id: serializedForm.id, schemas: { revesAndSchemas: schemas } };

                return JSON.stringify(schemaRepository);
            },

            closeSchemaForm: function(event, callback) {
                $('#schema-edit').hide('slide', function() {
                    $('#schema-list').show('slide', function() {
                        if (callback) {
                            callback();
                        }
                    });
                });
            },

            closeRepositoryForm: function(event, callback) {
                $('#repository-edit').hide('slide', function() {
                    $('#schema-list').show('slide', function() {
                        if (callback) {
                            callback();
                        }
                    });
                });
            },

            extractId: function(encodedId) {
                var splitString = encodedId.split('-');
                return splitString[splitString.length-1];
            }

        });

        return SchemaListView;
    });
}).call(this);
