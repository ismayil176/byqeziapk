from pathlib import Path
import runpy
runpy.run_path(str(Path(__file__).resolve().with_name('validate_home_round1.py')), run_name='__main__')
