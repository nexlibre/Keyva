#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Installation/activation smoke check; does not claim voice or Gemini verification."""
import pathlib, subprocess, sys, time, re, xml.etree.ElementTree as ET
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
print(adb('shell','am','start','-n','org.nexlibre.keyva/helium314.keyboard.latin.keyva.SmokeActivity'))
time.sleep(4)
def hierarchy(name):
    adb('shell','uiautomator','dump','/sdcard/keyva.xml')
    text=adb('shell','cat','/sdcard/keyva.xml');(out/name).write_text(text)
    return ET.fromstring(text)
def tap(node):
    x1,y1,x2,y2=map(int,re.findall(r'\d+',node.attrib['bounds']))
    adb('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2))
tree=hierarchy('typing.xml')
assert any(n.attrib.get('text')=='Voz' for n in tree.iter('node')), 'Keyva toolbar not visible'
# Actual IME key touch: custom keyboard has a view rectangle even without virtual key nodes.
keyboard=next((n for n in tree.iter('node') if 'MainKeyboardView' in n.attrib.get('class','')),None)
if keyboard is None: raise AssertionError('Keyboard view not visible')
x1,y1,x2,y2=map(int,re.findall(r'\d+',keyboard.attrib['bounds']))
adb('shell','input','tap',str(x1+(x2-x1)//4),str(y1+(y2-y1)//8))
time.sleep(1)
tree=hierarchy('typed.xml')
field=next(n for n in tree.iter('node') if n.attrib.get('content-desc')=='test_message')
assert field.attrib.get('text') not in ('','Test message'), 'Key touch did not produce text'
# Insert deterministic fixture and open explicit selection preview; no network or credential used.
adb('shell','input','text','hola')
tree=hierarchy('before-select.xml');tap(next(n for n in tree.iter('node') if n.attrib.get('text')=='Select test message'))
time.sleep(1)
tree=hierarchy('selected.xml');tap(next(n for n in tree.iter('node') if n.attrib.get('text')=='IA'))
time.sleep(1)
tree=hierarchy('preview.xml');assert any('Revisar texto' in n.attrib.get('text','') for n in tree.iter('node')), 'Preview did not open'
adb('shell','input','keyevent','4');time.sleep(1)
logs=adb('logcat' ,'-d','-s','AndroidRuntime:E');(out/'runtime.txt').write_text(logs)
assert 'FATAL EXCEPTION' not in logs, logs
subprocess.run(['adb','exec-out','screencap','-p'],stdout=(out/'settings.png').open('wb'),check=True)
print('PASS: installed, IME default, settings launched, keyboard rendered, real key touch inserted text, explicit selection preview opened. Voice/Gemini and broader regression remain pending.')
