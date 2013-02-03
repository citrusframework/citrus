(function() {
    define(["TemplateManager"], function(TemplateManager) {
        var ProjectView = Backbone.View.extend({
        
          events: {
              "click #button-browse" : "showFileTree",
              "click #button-close" : "hideFileTree",
              "click #button-select" : "selectProject"
          },
          
          render: function() {
              $(this.el).html(TemplateManager.template('ProjectView',{}));
              
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
          
          showFileTree: function() {
              $('#dialog-file-tree').modal();
          },
          
          hideFileTree: function() {
              $('#dialog-file-tree').modal('hide');
          },
          
          selectProject: function() {
              var selected = $('ul.jqueryFileTree li.expanded').last().children('a:first').attr('rel');
              $('input[name="projecthome"]').val(selected);
              $('#dialog-file-tree').modal('hide');
          }
        
        });
        
        return ProjectView;
    });
}).call(this);