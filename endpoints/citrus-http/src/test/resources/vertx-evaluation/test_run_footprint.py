"""Exercise real child timeout/failure handling without launching a benchmark campaign."""
import importlib.util
from pathlib import Path
import subprocess
import sys
import tempfile
import time
import unittest

sys.dont_write_bytecode = True

spec = importlib.util.spec_from_file_location("footprint", Path(__file__).with_name("run-footprint.py"))
footprint = importlib.util.module_from_spec(spec)
spec.loader.exec_module(footprint)


class ChildProcessTest(unittest.TestCase):
    def test_nonzero_exit_preserves_diagnostics_and_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            stem = Path(directory) / "failed"
            with self.assertRaises(subprocess.CalledProcessError) as caught:
                footprint.run_probe([sys.executable, "-c", "import sys; print('reason', file=sys.stderr); sys.exit(7)"],
                                    directory, stem)
            self.assertEqual(caught.exception.returncode, 7)
            self.assertIn("reason", stem.with_suffix(".stderr.log").read_text())

    def test_timeout_is_bounded_and_preserves_partial_output(self):
        with tempfile.TemporaryDirectory() as directory:
            stem = Path(directory) / "timeout"
            started = time.monotonic()
            with self.assertRaises(subprocess.TimeoutExpired):
                footprint.run_probe([sys.executable, "-c", "import time; print('started', flush=True); time.sleep(30)"],
                                    directory, stem, timeout=0.5)
            self.assertLess(time.monotonic() - started, 3)
            self.assertIn("started", stem.with_suffix(".stdout.log").read_text())


if __name__ == "__main__":
    unittest.main()
