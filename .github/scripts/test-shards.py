#!/usr/bin/env python3
# ---------------------------------------------------------------------------
# Copyright the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ---------------------------------------------------------------------------

"""
Splits the Maven reactor into test shards for the parallel CI build.

Shards follow the repository layout: a module is tested in the shard named
after its top-level directory (endpoints/citrus-http -> endpoints). Two
adjustments keep the shards comparable in size:

- MERGED folds the small foundation directories into `core`.
- SPLIT gives a module its own shard when it alone takes as long as the rest
  of its directory.

A new module is placed by the same rule, and a new top-level directory becomes
a shard of its own. Aggregator (pom packaging) modules carry no tests and are
left out.

Usage: test-shards.py [IMPACTED_MODULES_FILE]

Without a file (push to main) every module is selected. With a file (pull
request) only the modules listed there are selected: one module path per line,
as written by gitflow-incremental-builder's `gib.logImpactedTo`. A file that
does not exist means gib did not restrict the reactor, so every module is
selected; an empty file means nothing needs testing.

Prints a GitHub Actions matrix as JSON: {"include": [{"shard", "modules"}]}.
"""

import json
import os
import sys
import xml.etree.ElementTree as ET

POM_NS = {"m": "http://maven.apache.org/POM/4.0.0"}

# The framework itself: the API and base implementation plus the validators,
# test utilities and catalogs built directly on top of them.
MERGED = {
    "validation": "core",
    "utils": "core",
    "catalog": "core",
}

# Camel's tests drive the Camel JBang CLI and take about as long as all other
# endpoints together, so they get a runner of their own.
SPLIT = {
    "endpoints/citrus-camel": "camel",
}

# Matrix order, from the foundation outwards; any other shard follows.
LAYERS = ["core", "runtime", "endpoints", "camel", "connectors", "tools"]


def read_pom(module_dir):
    return ET.parse(os.path.join(module_dir or ".", "pom.xml")).getroot()


def reactor_modules(module_dir=""):
    """Walks <modules> from the root pom and yields (path, packaging)."""
    pom = read_pom(module_dir)
    packaging = pom.findtext("m:packaging", default="jar", namespaces=POM_NS)
    yield module_dir, packaging
    for module in pom.findall("m:modules/m:module", POM_NS):
        child = os.path.normpath(os.path.join(module_dir, module.text.strip()))
        yield from reactor_modules(child)


def shard_of(module):
    for path, shard in SPLIT.items():
        if module == path or module.startswith(path + "/"):
            return shard
    directory = module.split("/", 1)[0]
    return MERGED.get(directory, directory)


def layer_rank(shard):
    return (LAYERS.index(shard), "") if shard in LAYERS else (len(LAYERS), shard)


def selected_modules(impacted_file):
    testable = [path for path, packaging in reactor_modules() if packaging != "pom"]
    if impacted_file is None or not os.path.exists(impacted_file):
        return testable
    with open(impacted_file, encoding="utf-8") as lines:
        impacted = {os.path.normpath(line.strip()) for line in lines if line.strip()}
    return [module for module in testable if module in impacted]


def main():
    impacted_file = sys.argv[1] if len(sys.argv) > 1 else None
    shards = {}
    for module in selected_modules(impacted_file):
        shards.setdefault(shard_of(module), []).append(module)

    include = [
        {"shard": shard, "modules": ",".join(sorted(shards[shard]))}
        for shard in sorted(shards, key=layer_rank)
    ]
    for entry in include:
        print(f"{entry['shard']}: {entry['modules']}", file=sys.stderr)
    print(json.dumps({"include": include}))


if __name__ == "__main__":
    main()
