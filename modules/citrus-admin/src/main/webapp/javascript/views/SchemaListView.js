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
                "click tr.schema": "showSchemaEditForm",
                "click tr.repository": "showRepositoryEditForm",
                "click a.schema-select": "selectSchema",
                "click #btn-schema-reference-add": "addSchemaReference",
                "click input[name='schema-ref']": "showSchemaReferenceSelect"
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
                    url: "configuration/schema",
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
                    url: "configuration/schema-repository",
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
                var url = "configuration/schema/" + id;

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
                var url = "configuration/schema-repository/" + id;

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
                var url = "configuration/schema/" + id;

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
                var url = "configuration/schema-repository/" + id;

                $.ajax({
                    url: url,
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        $('#repository-edit').html(TemplateManager.template('SchemaRepositoryEditView', {repository: response, schemas: this.filterSchemas(response.schemas.revesAndSchemas)}));

                        $( "#schemas-included, #schemas-excluded" ).sortable({
                          connectWith: ".schema-list"
                        }).disableSelection();

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
                this.closeSchemaForm();

                var serializedForm = form.serializeObject();
                var jsonForm = JSON.stringify(serializedForm);
                var url = "configuration/schema";
                $.ajax({
                    url: url,
                    type: 'POST',
                    dataType: "json",
                    contentType: "application/json",
                    data: jsonForm,
                    success: _.bind(function (response) {
                        this.getSchemas();
                        this.render();
                    }, this),
                    async: true
                });
                return false;
            },

            createRepository: function() {
                var form = $('#repository-edit-form');
                this.closeRepositoryForm();

                var serializedForm = form.serializeObject();
                var url = "configuration/schema-repository";
                $.ajax({
                    url: url,
                    type: 'POST',
                    dataType: "json",
                    contentType: "application/json",
                    data: this.getSchemaRepositoryJSON(serializedForm),
                    success: _.bind(function (response) {
                        this.getSchemaRepositories();
                        this.render();
                    }, this),
                    async: true
                });
                return false;
            },

            updateSchema: function() {
                var form = $('#schema-edit-form form');
                this.closeSchemaForm();

                var serializedForm = form.serializeObject();
                var schemaId = serializedForm.id;

                if (serializedForm.id != serializedForm.newId) {
                    serializedForm.id = serializedForm.newId;
                }

                serializedForm = _.omit(serializedForm, "newId");

                var jsonForm = JSON.stringify(serializedForm);
                var url = "configuration/schema/" + schemaId;
                $.ajax({
                    url: url,
                    type: 'POST',
                    dataType: "json",
                    contentType: "application/json",
                    data: jsonForm,
                    success: _.bind(function (response) {
                        this.getSchemas();
                        this.render();
                    }, this),
                    async: true
                });
                return false;
            },

            updateRepository: function() {
                var form = $('#repository-edit-form form');
                this.closeRepositoryForm();

                var serializedForm = form.serializeObject();
                var schemaRepositoryId = serializedForm.id;

                if (serializedForm.id != serializedForm.newId) {
                    serializedForm.id = serializedForm.newId;
                }

                serializedForm = _.omit(serializedForm, "newId");

                var url = "configuration/schema-repository/" + schemaRepositoryId;
                $.ajax({
                    url: url,
                    type: 'POST',
                    dataType: "json",
                    contentType: "application/json",
                    data: this.getSchemaRepositoryJSON(serializedForm),
                    success: _.bind(function (response) {
                        this.getSchemaRepositories();
                        this.render();
                    }, this),
                    async: true
                });
                return false;
            },

            getSchemaRepositoryJSON: function(serializedForm) {
                var schemas = [];

                $('ul#schema-refs').children('li').each(function(index) {
                    schemas.push({ ref: $(this).attr('id') });
                });

                var schemaRepository = { id: serializedForm.id, schemas: { schemas: schemas } };

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
            },

            selectSchema: function(event) {
                $('input[name="schema-ref"]').val(event.currentTarget.innerText);
                $('#schema-ref-dropdown').removeClass('open');
                return false;
            },

            addSchemaReference: function() {
                var schemaId = $('input[name="schema-ref"]').val();
                if (schemaId.length) {
                    if ($('ul#schema-refs').children('li#' + schemaId).size() == 0) {
                        $('ul#schema-refs').append('<li id="' + schemaId + '"><i class="icon-file-text-alt"></i>&nbsp;' + schemaId + '</li>');
                    }

                    $('input[name="schema-ref"]').val('');
                }

                return false;
            },

            showSchemaReferenceSelect: function(event) {
                $('#schema-ref-dropdown').addClass('open');
                event.stopPropagation();
            }

        });

        return SchemaListView;
    });
}).call(this);
