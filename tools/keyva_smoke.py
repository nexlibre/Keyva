#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Installation and IME interaction smoke check; does not claim voice/Gemini verification."""
import atexit, pathlib, subprocess, sys, time, re, xml.etree.ElementTree as ET
out=pathlib.Path('smoke-evidence');out.mkdir(exist_ok=True)
def adb(*args): return subprocess.check_output(['adb',*args],text=True)
def diagnostics():
    for name,args in [('ime-final.txt',('shell','dumpsys','input_method')),('runtime.txt',('logcat','-d','-s','AndroidRuntime:E'))]:
        try:(out/name).write_text(adb(*args))
        except Exception:pass
    with (out/'screen.png').open('wb') as f: subprocess.run(['adb','exec-out','screencap','-p'],stdout=f)
atexit.register(diagnostics)
def hierarchy(name):
    adb('shell','uiautomator','dump','/sdcard/keyva.xml')
    text=adb('shell','cat','/sdcard/keyva.xml');(out/name).write_text(text)
    return ET.fromstring(text)
def bounds(node): return list(map(int,re.findall(r'\d+',node.attrib['bounds'])))
def tap(node):
    x1,y1,x2,y2=bounds(node);adb('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2))
def find_text(tree,text):return next(n for n in tree.iter('node') if n.attrib.get('text','').casefold()==text.casefold())
apk=next(pathlib.Path(sys.argv[1]).rglob('*.apk'))
print(adb('install','-r',str(apk)),flush=True)
print(adb('shell','am','start','-n','org.nexlibre.keyva/helium314.keyboard.settings.SettingsActivity'),flush=True)
time.sleep(3)
component='org.nexlibre.keyva/helium314.keyboard.latin.LatinIME'
for attempt in range(30):
    if component in adb('shell','ime','list','-a','-s'):break
    time.sleep(1)
else:raise AssertionError('Package installed but IME registration did not complete')
print(adb('shell','ime','enable',component),flush=True);print(adb('shell','ime','set',component),flush=True)
assert adb('shell','settings','get','secure','default_input_method').strip()==component
print(adb('shell','am','start','-n','org.nexlibre.keyva/helium314.keyboard.settings.SettingsActivity'),flush=True)
time.sleep(3)
print(adb('shell','am','start','-n','org.nexlibre.keyva/helium314.keyboard.latin.keyva.SmokeActivity'),flush=True)
time.sleep(5)
tree=hierarchy('typing.xml')
# Shell UIAutomator may expose only the application window, not the IME window.
# Use the resized app content edge as keyboard top and confirm taps by editor state/dialog.
content=next(n for n in tree.iter('node') if n.attrib.get('resource-id')=='android:id/content')
x1,y1,width,top=bounds(content)
size=list(map(int,re.findall(r'\d+',adb('shell','wm','size'))))[-2:]
density=int(re.findall(r'\d+',adb('shell','wm','density'))[-1])/160
assert top < size[1]*0.85, 'App was not resized for visible keyboard'
# Keyva toolbar 48dp, upstream suggestion strip about 36dp, then first letter row.
adb('shell','input','tap',str(width//4),str(int(top+110*density)))
time.sleep(1)
tree=hierarchy('typed.xml')
field=next(n for n in tree.iter('node') if n.attrib.get('content-desc')=='test_message')
assert field.attrib.get('text') not in ('','Test message'), 'Real keyboard key touch did not produce text'
adb('shell','input','text','hola')
tree=hierarchy('before-select.xml');tap(find_text(tree,'Select test message'))
time.sleep(1)
# Click IA, second of four toolbar controls; actual dialog verifies touchable insets.
adb('shell','input','tap',str(width*3//8),str(int(top+24*density)))
time.sleep(1)
tree=hierarchy('preview.xml');assert any('Revisar texto' in n.attrib.get('text','') for n in tree.iter('node')), 'Selection preview did not open'
adb('shell','input','keyevent','4');time.sleep(1)
logs=adb('logcat','-d','-s','AndroidRuntime:E')
assert 'FATAL EXCEPTION' not in logs, logs
print('PASS: installed, IME default, settings launched, real key inserted text, explicit selection preview opened. Voice/Gemini and broader regression remain pending.',flush=True)
