Feature: AWS S3

  Background:
    Given Disable auto removal of Testcontainers resources
    Given variable bucketName="mybucket"
    Given New global Camel context

  Scenario: Start container
    Given Enable service S3
    Given LocalStack container options
      | buckets | ${bucketName} |
    Given start LocalStack container
    Given HTTP request timeout is 20000 ms
    And wait for URL ${CITRUS_TESTCONTAINERS_LOCALSTACK_SERVICE_URL}

  Scenario: Verify upload file
    # Publish event
    Given Camel exchange message header CamelAwsS3Key="citrus.txt"
    Given send Camel exchange to("aws2-s3://${bucketName}?amazonS3Client=#s3Client") with body: Citrus rocks!

    # Verify uploaded file
    Given Camel route getS3Object.groovy
    """
    from("direct:getObject")
       .to("aws2-s3://${bucketName}?amazonS3Client=#s3Client&operation=getObject")
       .convertBodyTo(String.class)
       .to("seda:result")
    """
    Given Camel exchange message header CamelAwsS3Key="citrus.txt"
    When send Camel exchange to("direct:getObject")
    Then receive Camel exchange from("seda:result") with body: Citrus rocks!

  Scenario: Stop container
    Given stop LocalStack container
