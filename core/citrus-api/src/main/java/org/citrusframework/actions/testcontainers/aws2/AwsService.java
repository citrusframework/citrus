/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.actions.testcontainers.aws2;

import java.util.Arrays;

import org.citrusframework.exceptions.CitrusRuntimeException;

public enum AwsService {
    CLOUD_WATCH("cloudwatch"),
    DYNAMODB("dynamodb"),
    EC2("ec2"),
    EVENT_BRIDGE("eventbridge"),
    IAM("iam"),
    KINESIS("kinesis"),
    KMS("kms"),
    LAMBDA("lambda"),
    S3("s3"),
    SECRETS_MANAGER("secretsmanager"),
    SNS("sns"),
    SQS("sqs"),
    STS("sts");

    private final String serviceName;

    AwsService(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public static String serviceName(AwsService service) {
        return service.serviceName;
    }

    public static AwsService fromServiceName(String serviceName) {
        return Arrays.stream(AwsService.values())
                .filter(service -> service.serviceName.equals(serviceName))
                .findFirst()
                .orElseThrow(() -> new CitrusRuntimeException("Unknown AWS LocalStack service name: %s".formatted(serviceName)));
    }
}
