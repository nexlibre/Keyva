#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
import subprocess,sys,time
def adb(*a):return subprocess.check_output(['adb',*a],text=True)
print(adb('install',sys.argv[1]));print(adb('shell','am','start','-n','org.nexlibre.keyva/helium314.keyboard.settings.SettingsActivity'));time.sleep(4)
component='org.nexlibre.keyva/helium314.keyboard.latin.LatinIME'
for _ in range(30):
 if component in adb('shell','ime','list','-a','-s'):break
 time.sleep(1)
else:raise AssertionError('IME did not register')
print(adb('shell','ime','enable',component));print(adb('shell','ime','set',component));time.sleep(2)
assert adb('shell','settings','get','secure','default_input_method').strip()==component
assert 'FATAL EXCEPTION' not in adb('logcat','-d','-s','AndroidRuntime:E')
print('Signed release installed, settings started, and default IME activation verified')
