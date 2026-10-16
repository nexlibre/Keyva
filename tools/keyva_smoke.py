#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Installation/activation smoke check; does not claim voice or Gemini verification."""
import pathlib, subprocess, sys, time
out=pathlib.Path('smoke-evidence');out.mkdir(exist_ok=True)
def adb(*args): return subprocess.check_output(['adb',*args],text=True)
apk=next(pathlib.Path(sys.argv[1]).rglob('*.apk'))
print(adb('install','-r',str(apk)))
component='org.nexlibre.keyva/helium314.keyboard.latin.LatinIME'
print(adb('shell','ime','enable',component));print(adb('shell','ime','set',component))
assert adb('shell','settings','get','secure','default_input_method').strip()==component
print(adb('shell','am','start','-n','org.nexlibre.keyva/helium314.keyboard.settings.SettingsActivity'))
time.sleep(3)
(out/'ime.txt').write_text(adb('shell','dumpsys','input_method'))
logs=adb('logcat','-d','-s','AndroidRuntime:E');(out/'runtime.txt').write_text(logs)
assert 'FATAL EXCEPTION' not in logs, logs
subprocess.run(['adb','exec-out','screencap','-p'],stdout=(out/'settings.png').open('wb'),check=True)
print('PASS: installed, enabled, selected as default, settings launched. Interactive typing/voice/Gemini remain pending.')
