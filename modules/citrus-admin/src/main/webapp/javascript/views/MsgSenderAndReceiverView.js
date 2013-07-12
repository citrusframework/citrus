(function () {
    define(["TemplateManager"], function (TemplateManager) {
        var MsgSenderAndReceiverView = Backbone.View.extend({
            msgSender: undefined,
            msgReceiver: undefined,

            templateView: 'TableView',

            events: {
                "click #btn-new-config-sender": "showNewMsgSenderForm",
                "click #btn-save-config-sender": "createMsgSender",
                "click #btn-update-config-sender": "updateMsgSender",
                "click #btn-cancel-config-sender": "closeMsgSenderForm",
                "click #btn-new-config-receiver": "showNewMsgReceiverForm",
                "click #btn-save-config-receiver": "createMsgReceiver",
                "click #btn-update-config-receiver": "updateMsgReceiver",
                "click #btn-cancel-config-receiver": "closeMsgReceiverForm",
                "click .edit-config-sender": "showEditMsgSenderForm",
                "click .del-config-sender": "removeMsgSender",
                "click .edit-config-receiver": "showEditMsgReceiverForm",
                "click .del-config-receiver": "removeMsgReceiver",
                "click #btn-reload-config-sender": "reload",
                "click #btn-reload-config-receiver": "reload",
                "click a.change-view": "changeView"
            },

            initialize: function () {
            },

            render: function () {
                $(this.el).html(TemplateManager.template('MsgSenderAndReceiverView'));
                return this;
            },

            afterRender: function () {
                this.reload();
            },

            reload: function() {
                this.getMsgSender();
                this.getMsgReceiver();
            },

            getMsgSender: function () {
                $.ajax({
                    url: "config/msg-sender",
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        this.msgSender = response;
                        $("#config-senders").html(TemplateManager.template('MsgSender' + this.templateView, {matches: this.msgSender}));
                    }, this),
                    async: true
                });
            },

            getMsgReceiver: function () {
                $.ajax({
                    url: "config/msg-receiver",
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        this.msgReceiver = response;
                        $("#config-receivers").html(TemplateManager.template('MsgReceiver' + this.templateView, {matches: this.msgReceiver}));
                    }, this),
                    async: true
                });
            },

            removeMsgSender: function (event) {
                var encodedSchemaId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedSchemaId);
                var url = "config/msg-sender/" + id;

                $.ajax({
                    url: url,
                    type: 'DELETE',
                    success: _.bind(function (response) {
                        this.reload();
                    }, this),
                    async: true
                });

            },

            removeMsgReceiver: function (event) {
                var encodedId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedId);
                var url = "config/msg-receiver/" + id;

                $.ajax({
                    url: url,
                    type: 'DELETE',
                    success: _.bind(function (response) {
                        this.reload();
                    }, this),
                    async: true
                });
            },

            showNewMsgSenderForm: function() {
                $('#dialog-edit-config-sender').html(TemplateManager.template('MsgSenderEditView', {schema: undefined}));
                $('#dialog-edit-config-sender .modal').modal();

            },

            showNewMsgReceiverForm: function() {
                $('#dialog-edit-config-receiver').html(TemplateManager.template('MsgReceiverEditView', {schemaRepository: undefined, schemas: this.schemas}));
                $('#dialog-edit-config-receiver .modal').modal();

            },

            showEditMsgSenderForm: function() {
                var encodedSchemaId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedSchemaId);
                var url = "config/msg-sender/" + id;

                $.ajax({
                    url: url,
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        $('#dialog-edit-config-sender').html(TemplateManager.template('MsgSenderEditView', {schema: response}));
                        $('#dialog-edit-config-sender .modal').modal();
                    }, this),
                    async: true
                });
            },

            showEditMsgReceiverForm: function() {
                var encodedSchemaId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedSchemaId);
                var url = "config/msg-receiver/" + id;

                $.ajax({
                    url: url,
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        $('#dialog-edit-config-receiver').html(TemplateManager.template('MsgReceiverEditView', {schemaRepository: response, schemas: this.schemas}));
                        $('#dialog-edit-config-receiver .modal').modal();
                    }, this),
                    async: true
                });
            },

            createMsgSender: function() {
                var form = $('#form-edit-config-sender form');
                this.closeMsgSenderForm();

                var serializedForm = form.serializeObject();
                var jsonForm = JSON.stringify(serializedForm);
                var url = "config/msg-sender";
                $.ajax({
                    url: url,
                    type: 'POST',
                    dataType: "json",
                    contentType: "application/json",
                    data: jsonForm,
                    success: _.bind(function (response) {
                        this.reload();
                    }, this),
                    async: true
                });
                return false;
            },

            createMsgReceiver: function() {
                var form = $('#form-edit-config-receiver form');
                this.closeMsgReceiverForm();

                var serializedForm = form.serializeObject();
                var url = "config/msg-receiver";
                $.ajax({
                    url: url,
                    type: 'POST',
                    dataType: "json",
                    contentType: "application/json",
                    data: JSON.stringify(serializedForm),
                    success: _.bind(function (response) {
                        this.reload();
                    }, this),
                    async: true
                });
                return false;
            },

            updateMsgSender: function() {
                var form = $('#form-edit-config-sender form');
                this.closeMsgSenderForm();

                var serializedForm = form.serializeObject();
                var schemaId = serializedForm.id;

                if (serializedForm.id != serializedForm.newId) {
                    serializedForm.id = serializedForm.newId;
                }

                serializedForm = _.omit(serializedForm, "newId");

                var jsonForm = JSON.stringify(serializedForm);
                var url = "config/msg-sender/" + schemaId;
                $.ajax({
                    url: url,
                    type: 'PUT',
                    dataType: "json",
                    contentType: "application/json",
                    data: jsonForm,
                    success: _.bind(function (response) {
                        this.reload();
                    }, this),
                    async: true
                });
                return false;
            },

            updateMsgReceiver: function() {
                var form = $('#form-edit-config-receiver form');
                this.closeMsgReceiverForm();

                var serializedForm = form.serializeObject();
                var schemaRepositoryId = serializedForm.id;

                if (serializedForm.id != serializedForm.newId) {
                    serializedForm.id = serializedForm.newId;
                }

                serializedForm = _.omit(serializedForm, "newId");

                var url = "config/msg-receiver/" + schemaRepositoryId;
                $.ajax({
                    url: url,
                    type: 'PUT',
                    dataType: "json",
                    contentType: "application/json",
                    data: JSON.stringify(serializedForm),
                    success: _.bind(function (response) {
                        this.reload();
                    }, this),
                    async: true
                });
                return false;
            },

            closeMsgSenderForm: function() {
                $('#dialog-edit-config-sender .modal').modal('hide');
            },

            closeMsgReceiverForm: function() {
                $('#dialog-edit-config-receiver .modal').modal('hide');
            },

            changeView: function(event) {
                this.templateView = $(event.currentTarget).attr('alt') + 'View';

                this.reload();
                event.stopPropagation();
                return false;
            },

            extractId: function(encodedId) {
                var splitString = encodedId.split('-');
                return splitString[splitString.length-1];
            }

        });

        return MsgSenderAndReceiverView;
    });
}).call(this);
