(function() {
    define(["TemplateManager"], function(TemplateManager) {
        var OpenProjectView = Backbone.View.extend({
          
          project: {},
          
          events: {
              "click #button-browse" : "browse",
              "click #button-close" : "close",
              "click #button-select" : "select"
          },

          initialize: function() {
              $.ajax({
                  url: "project/active",
                  type: 'GET',
                  dataType: "json",
                  success: _.bind(function(response) {
                      this.project = response;
                  }, this),
                  async: false
              });
          },
          
          render: function() {
              $(this.el).html(TemplateManager.template('OpenProjectView', this.project));
              
              $('#file-tree').fileTree({ 
                  root: '/',
                  script: 'project',
                  multiFolder: false,
                  expandSpeed: 1, 
                  collapseSpeed: 1
              }, function(file) {
                  $('input[name="projecthome"]').val(file);
                  $('#file-tree').hide();
              });
              
              return this;
          },
          
          browse: function() {
              $('#dialog-file-tree').modal();
          },
          
          close: function() {
              $('#dialog-file-tree').modal('hide');
          },
          
          select: function() {
              var selected = $('ul.jqueryFileTree li.expanded').last().children('a:first').attr('rel');
              $('input[name="projecthome"]').val(selected);
              $('#dialog-file-tree').modal('hide');
          }
        
        });
        
        return OpenProjectView;
    });
}).call(this);