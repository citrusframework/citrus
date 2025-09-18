Feature: JDBC steps

  Background:
    Given load variables secrets.properties
    Given Database connection
      | url       | ${database.url} |
      | username  | ${database.user} |
      | password  | ${database.password} |

  Scenario: SQL update
    Given variables
      | todoId  | citrus:randomNumber(4) |
      | task    | Test Camel with Citrus! |
    Given SQL update: INSERT INTO todo (id, task, completed) VALUES (${todoId}, '${task}', 0)

  Scenario: SQL update batch
    Given SQL updates
      | INSERT INTO todo (id, task, completed) VALUES (2, 'Get some milk', 0) |
      | INSERT INTO todo (id, task, completed) VALUES (3, 'Do laundry', 0) |
      | INSERT INTO todo (id, task, completed) VALUES (4, 'Wash the dog', 0) |

  Scenario: Verify column
    Given SQL query: SELECT task FROM todo WHERE id=1
    Then verify column TASK=Learn some Camel K!

  Scenario: Verify result set
    Given SQL query: SELECT * FROM todo ORDER BY id
    Then verify columns
      | ID        | 1                  | 2             | 3          | 4            | @ignore@               |
      | TASK      | Learn some Camel K! | Get some milk | Do laundry | Wash the dog | Test Camel with Citrus! |
      | COMPLETED | 0                  | 0             | 0          |            0 | 0                      |

  Scenario: Verify script
    Given SQL query: SELECT * FROM todo WHERE id=1
    Then verify result set
      """
      assert rows.size() == 1
      assert rows[0].TASK == 'Learn some Camel K!'
      """
