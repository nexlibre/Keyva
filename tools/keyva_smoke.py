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
def launch_case(*extras):
    print(adb('shell','am','start','-f','0x10008000','-n','org.nexlibre.keyva/helium314.keyboard.latin.keyva.SmokeActivity',*extras),flush=True)
    time.sleep(4)
    return hierarchy('host.xml')
def host_text(tree): return next(n for n in tree.iter('node') if n.attrib.get('content-desc')=='test_message').attrib.get('text','')
def keyboard_bounds(tree):
    content=next(n for n in tree.iter('node') if n.attrib.get('resource-id')=='android:id/content')
    x,y,width,top=bounds(content)
    size=list(map(int,re.findall(r'\d+',adb('shell','wm','size'))))[-2:]
    assert top < size[1]*0.85, 'App not resized for keyboard'
    return width,top
def toolbar(index,width,top): adb('shell','input','tap',str(int(width*(index+0.5)/4)),str(int(top+24*density)))
def key_touch(width,top): adb('shell','input','tap',str(width//4),str(int(top+110*density)))
def open_preview(width,top):
    tree=hierarchy('before-select.xml');tap(find_text(tree,'Select test message'));time.sleep(0.5)
    toolbar(1,width,top);time.sleep(0.5)
    tree=hierarchy('preview.xml');assert any('Revisar texto' in n.attrib.get('text','') for n in tree.iter('node')), 'Preview absent'
    return tree
def draft_text(tree):return next(n for n in tree.iter('node') if n.attrib.get('content-desc')=='keyva_draft').attrib.get('text','')
density=int(re.findall(r'\d+',adb('shell','wm','density'))[-1])/160
adb('shell','svc','wifi','disable');adb('shell','svc','data','disable')
tree=launch_case('--ez','storage_test','true')
assert any(n.attrib.get('text')=='Keystore roundtrip: PASS' for n in tree.iter('node')), 'Keystore encryption/read/delete failed'
width,top=keyboard_bounds(tree)
key_touch(width,top);time.sleep(0.5)
tree=hierarchy('typed.xml');assert host_text(tree) not in ('','Test message'), 'Actual key did not type'
adb('shell','input','text','hola');tree=hierarchy('original.xml');original=host_text(tree)
preview=open_preview(width,top);assert draft_text(preview)==original
key_touch(width,top);time.sleep(0.5)
preview=hierarchy('draft-edited.xml');assert draft_text(preview)!=original, 'Keyva keys did not edit draft'
adb('shell','input','keyevent','4');time.sleep(0.5)
assert host_text(hierarchy('cancelled.xml'))==original, 'Cancel modified host text'
preview=open_preview(width,top);key_touch(width,top);time.sleep(0.5)
preview=hierarchy('draft-to-insert.xml');edited=draft_text(preview);assert edited!=original
tap(find_text(preview,'Insertar'));time.sleep(0.5)
assert host_text(hierarchy('inserted.xml'))==edited, 'Insertion mismatch'
toolbar(2,width,top);time.sleep(0.5)
assert host_text(hierarchy('undone.xml'))==original, 'Undo did not restore exact original'
# Password and private editors must refuse both AI and microphone before accessing the field.
for kind in ['password','private']:
    tree=launch_case('--ez',kind,'true','--es','fixture','sensitive-test-only')
    width,top=keyboard_bounds(tree)
    for control in [1,0]:
        toolbar(control,width,top);time.sleep(0.5);tree=hierarchy(kind+'-'+str(control)+'.xml')
        assert not any('Revisar texto' in n.attrib.get('text','') or 'Voz e inteligencia artificial' in n.attrib.get('text','') for n in tree.iter('node')), 'Sensitive editor opened AI or voice settings'
# Denied microphone permission routes to a user-controlled runtime permission screen.
tree=launch_case();width,top=keyboard_bounds(tree)
adb('shell','pm','revoke','org.nexlibre.keyva','android.permission.RECORD_AUDIO')
toolbar(0,width,top);time.sleep(1)
tree=hierarchy('microphone-denied.xml')
assert any('Voz e inteligencia artificial' in n.attrib.get('text','') for n in tree.iter('node')), 'Denied mic did not open settings'
logs=adb('logcat','-d','-s','AndroidRuntime:E');assert 'FATAL EXCEPTION' not in logs,logs
print('PASS: install/default IME, offline key input, encrypted config roundtrip/delete, real-key draft editing, cancel, exact insertion/undo, password/private refusal and denied microphone handling. Actual ASR and Gemini remain pending.',flush=True)
