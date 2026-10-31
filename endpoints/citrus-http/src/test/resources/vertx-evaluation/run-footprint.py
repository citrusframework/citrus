#!/usr/bin/env python3
"""Bounded, fresh-JDK-21-process HTTP/1.1 evaluation campaign (standard library only)."""
import json
import os
from pathlib import Path
import platform
import statistics
import subprocess
import sys
import time
from datetime import datetime, timezone


def execute(command, cwd=None, timeout=30):
    return subprocess.run(command, cwd=cwd, capture_output=True, text=True, timeout=timeout, check=True)


def run_probe(command, module, stem, timeout=120):
    launched_ms = time.time_ns() / 1_000_000
    with stem.with_suffix(".stdout.log").open("w") as out, stem.with_suffix(".stderr.log").open("w") as err:
        subprocess.run(command, cwd=module, stdout=out, stderr=err, timeout=timeout, check=True)
    return launched_ms


def main():
    module, classpath_file, destination = map(lambda value: Path(value).resolve(), sys.argv[1:4])
    runs = int(sys.argv[4]) if len(sys.argv) > 4 else 5
    requests = int(sys.argv[5]) if len(sys.argv) > 5 else 1000
    if runs <= 0 or requests <= 0:
        raise ValueError("runs and requests must be positive")
    java_home = os.environ.get("VERTX_EVAL_JAVA_HOME") or os.environ.get("JAVA_HOME")
    if not java_home:
        raise RuntimeError("Set JAVA_HOME or VERTX_EVAL_JAVA_HOME to JDK 21")
    java = str(Path(java_home) / "bin/java")
    version = execute([java, "-version"])
    if 'version "21.' not in version.stderr:
        raise RuntimeError("JDK 21 required: " + version.stderr)
    destination.mkdir(parents=True, exist_ok=True)
    flags = ["-Xms128m", "-Xmx256m", "-XX:+UseG1GC"]
    dependencies = classpath_file.read_text().strip().split(os.pathsep)
    classpath = os.pathsep.join([str(module / "target/test-classes"), str(module / "target/classes"), *dependencies])
    inventory = []
    for item in dependencies:
        path = Path(item)
        if path.suffix == ".jar":
            inventory.append({"path": str(path), "file": path.name, "bytes": path.stat().st_size,
                              "artifact": ":".join([".".join(path.parts[path.parts.index("repository") + 1:-3]),
                                                    path.parent.parent.name, path.parent.name]),
                              "scope": "shared evaluation test classpath; not a production dependency delta"})
    (destination / "classpath-inventory.json").write_text(json.dumps(inventory, indent=2) + "\n")
    environment = {"date": datetime.now(timezone.utc).isoformat(), "java": version.stderr,
                   "citrusCommit": execute(["git", "rev-parse", "HEAD"], cwd=module).stdout.strip(),
                   "gitStatus": execute(["git", "status", "--short"], cwd=module).stdout,
                   "javaHome": java_home, "jvmFlags": flags, "platform": platform.platform(),
                   "machine": platform.machine(), "logicalCpus": os.cpu_count(), "runsPerEnginePerFixture": runs,
                   "clients": 8, "warmupPerProcess": 100, "requestsPerProcess": requests,
                   "childTimeoutSeconds": 120, "jarCount": len(inventory),
                   "jarBytes": sum(item["bytes"] for item in inventory)}
    if platform.system() == "Darwin":
        environment["hardware"] = execute(["sysctl", "hw.model", "hw.memsize", "machdep.cpu.brand_string"]).stdout
    (destination / "environment.json").write_text(json.dumps(environment, indent=2) + "\n")
    results = []
    # Interleave engines to reduce order bias. Never discard a failed or timed-out child.
    with (destination / "footprint.jsonl").open("w") as samples:
        for run in range(1, runs + 1):
            for fixture in ["text", "binary"]:
                for engine in (["jetty", "vertx"] if run % 2 else ["vertx", "jetty"]):
                    stem = destination / f"{engine}-{fixture}-{run}"
                    command = [java, *flags, "-cp", classpath, "org.citrusframework.http.vertx.FootprintProbe",
                               engine, "0", str(requests), fixture]
                    try:
                        launched_ms = run_probe(command, module, stem)
                    except (subprocess.TimeoutExpired, subprocess.CalledProcessError) as failure:
                        samples.write(json.dumps({"engine": engine, "fixture": fixture, "run": run,
                                                  "failure": type(failure).__name__,
                                                  "exitCode": getattr(failure, "returncode", None)}) + "\n")
                        samples.flush()
                        raise
                    records = [json.loads(line) for line in stem.with_suffix(".stdout.log").read_text().splitlines() if line.startswith('{"engine"')]
                    if len(records) != 1:
                        raise RuntimeError(f"Expected exactly one probe result: {stem}")
                    result = records[0]
                    if result["protocol"] != "HTTP/1.1" or len(result["rawNanos"]) != requests:
                        raise RuntimeError(f"Invalid workload result: {stem}")
                    result["run"] = run
                    result["processStartToListenMs"] = result["boundEpochMs"] - launched_ms
                    samples.write(json.dumps(result) + "\n"); samples.flush()
                    results.append(result)
    summary = []
    for engine in ["jetty", "vertx"]:
        for fixture in ["text", "binary"]:
            group = [r for r in results if r["engine"] == engine and r["fixture"] == fixture]
            item = {"engine": engine, "fixture": fixture, "runs": len(group)}
            for metric in ["processStartToListenMs", "startupMs", "idleThreads", "threads", "heapUsedBytes", "p50Ms", "p95Ms"]:
                values = [r[metric] for r in group]
                item[metric] = {"median": statistics.median(values), "min": min(values), "max": max(values)}
            summary.append(item)
    (destination / "summary.json").write_text(json.dumps(summary, indent=2) + "\n")
    print(f"Campaign complete: {len(results)} valid fresh JVMs, {requests} samples each")


if __name__ == "__main__":
    main()
