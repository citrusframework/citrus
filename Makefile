# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

VERSION := 4.1.0-SNAPSHOT
RELEASE_GIT_REMOTE := origin
GIT_COMMIT := $(shell git rev-list -1 HEAD)

default: help

help:
	./scripts/citrus -h

open-manual:
	./scripts/citrus manual

generate-docs:
	./scripts/citrus docs --release-version $(VERSION) --local --html-only

release-docs:
	./scripts/citrus docs --release-version $(VERSION)

release-docs-major:
	./scripts/citrus docs --release-version $(VERSION) --major-release

release-docs-dry-run:
	./scripts/citrus docs --release-version $(VERSION) --dry-run

release:
	./scripts/citrus release --git-remote $(RELEASE_GIT_REMOTE) --release-version $(VERSION) --skip-tests

release-snapshot:
	./scripts/citrus release --git-remote $(RELEASE_GIT_REMOTE) --snapshot-release --no-git-push

release-local:
	./scripts/citrus release --git-remote $(RELEASE_GIT_REMOTE) --local-release --no-git-push --release-version $(VERSION)

release-major:
	./scripts/citrus release --git-remote $(RELEASE_GIT_REMOTE) --major-release --release-version $(VERSION)

release-dry-run:
	./scripts/citrus release --git-remote $(RELEASE_GIT_REMOTE) --release-version $(VERSION) --skip-tests --dry-run --no-git-push --keep-staging-repo
