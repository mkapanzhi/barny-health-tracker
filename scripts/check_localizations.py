"""Validate complete language catalogs against the English Android resources.

Run: python3 scripts/check_localizations.py
Regional and device-configuration overlays are excluded deliberately.
"""
from pathlib import Path
from collections import Counter
import re
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1] / 'app/src/main/res'
ERRORS = []
FORMAT = re.compile(r'%(?:\d+\$)?[-#+ 0,(<]*\d*(?:\.\d+)?[a-zA-Z]')

def catalog(directory):
    result = {}
    for path in sorted(directory.glob('*.xml')):
        for node in ET.parse(path).getroot():
            if node.tag not in ('string', 'plurals', 'string-array'):
                continue
            name = node.get('name')
            if name in result:
                ERRORS.append(f'{directory.name}: duplicate key {name}')
            result[name] = node
    return result

def text(node):
    return ''.join(node.itertext())

def formats(node):
    return Counter(FORMAT.findall(text(node).replace('%%', '')))

base = catalog(ROOT / 'values')
expected = {k: v for k, v in base.items() if v.get('translatable') != 'false'}
for key, node in expected.items():
    if re.search(r'[\u0400-\u052f]', text(node)):
        ERRORS.append(f'values: Cyrillic in English resource {key}')

languages = sorted(p for p in ROOT.glob('values-*')
                   if re.fullmatch(r'values-(?:[a-z]{2,3}|b\+[A-Za-z0-9+]+)', p.name))
for directory in languages:
    translated = catalog(directory)
    for key in sorted(expected.keys() - translated.keys()):
        ERRORS.append(f'{directory.name}: missing {key}')
    for key in sorted(translated.keys() - base.keys()):
        ERRORS.append(f'{directory.name}: extra {key}')
    for key in sorted(expected.keys() & translated.keys()):
        source, target = expected[key], translated[key]
        if source.tag != target.tag:
            ERRORS.append(f'{directory.name}: resource type mismatch {key}')
        elif source.tag == 'string':
            if source.get('formatted') != 'false' and formats(source) != formats(target):
                ERRORS.append(f'{directory.name}: placeholder mismatch {key}')
        elif source.tag == 'string-array':
            if len(source) != len(target):
                ERRORS.append(f'{directory.name}: array length mismatch {key}')
            for a, b in zip(source, target):
                if formats(a) != formats(b):
                    ERRORS.append(f'{directory.name}: array placeholder mismatch {key}')
        else:
            source_other = source.find("item[@quantity='other']")
            if target.find("item[@quantity='other']") is None:
                ERRORS.append(f'{directory.name}: missing plural other {key}')
            if source_other is not None:
                for item in target:
                    reference = source.find(f"item[@quantity='{item.get('quantity')}']")
                    if reference is None:
                        reference = source_other
                    if formats(reference) != formats(item):
                        ERRORS.append(f'{directory.name}: plural placeholder mismatch {key}')

if ERRORS:
    print('\n'.join(ERRORS))
    sys.exit(1)
print(f'Localization OK: {len(expected)} base keys, {len(languages)} language catalogs')
