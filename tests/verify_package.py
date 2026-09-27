"""Verify the distributable, numeric derivation, sprite bounds, and dependency exclusion."""
from pathlib import Path
import json,zipfile,struct,hashlib
p=Path(__file__).resolve().parents[1]
jar=p/'build/libs/Electric-Age-0.1.0-dev.4.jar'
checks=0
def check(condition,label):
 global checks
 if not condition:raise AssertionError(label)
 checks+=1
with zipfile.ZipFile(jar) as z:
 names=z.namelist();meta=json.loads(z.read('fabric.mod.json'))
 check(meta['id']=='electric_age' and meta['version']=='0.1.0-dev.4','identity')
 check(meta['depends']['java']=='>=21','Java baseline')
 for name in names:
  check(not name.lower().endswith(('.jar','.zip','.aseprite','.lua','.py','.ps1')),f'production-only package: {name}')
  if name.endswith('.class'):
   check(name.startswith('net/poosh/electricage/'),'no game/framework/fixture classes')
   check(struct.unpack('>H',z.read(name)[6:8])[0]==65,'Java 21 bytecode')
 mixins=json.loads(z.read('electric_age.mixins.json'))
 check(mixins['required'] and mixins['injectors']['defaultRequire']==1,'fail-closed integration hooks')
 for name in mixins['mixins']:check(f'net/poosh/electricage/mixin/{name}.class' in names,f'packaged hook {name}')
 definitions=json.loads(z.read('acbric_vanilla/ModuleType/electric_age.json'))
 check(len(definitions)==8 and len({d['name'] for d in definitions})==8,'six devices plus two mirrored variants')
 power={line.split('=')[0]:float(line.split('=')[1]) for line in z.read('electric_age/balance.properties').decode('utf-8').splitlines() if '=' in line and not line.startswith('#')}
 lightning=next(d for d in definitions if d['name']=='EA_LIGHTNING')
 check(lightning['blastDmg']==power['lightning.energy'],'panel damage from energy')
 check(lightning['blastSplashRadius']==0,'explicit point blast bypasses native legacy damage conversion')
 check(lightning['maxRange']==power['lightning.range'],'target range from balance')
 check(lightning['armourType']=='EA_UNARMORED' and lightning['topOnly'],'unarmoured deck mount')
 armour=json.loads(z.read('acbric_vanilla/ArmourType/electric_age.json'))[0]
 check(all(armour[k]==0 for k in ['hp','cost','weight','blastDmgAbsorb','penDmgAbsorb']),'no invisible armour protection or cost')
 for d in definitions:
  if 'flippedFrom' not in d:check(d['required']=='GATLING_GUNS','native machining reward')
 for lang in ['chi','en']:
  text=z.read(f'acbric_vanilla/strings/{lang}.properties').decode('utf-8')
  for d in definitions:check('mod_'+d['name']+'=' in text,f'{lang} device translation')
  check('24 / 40 / 60' in text and '2000 kJ' in text,'derived power/capacity descriptions')
 bundles=json.loads(z.read('acbric_vanilla/SpritesheetBundle/electric_age.json'))
 for bundle in bundles:
  check(bundle.get('bump') and bundle.get('fragments'),'native lighting and damage-generation inputs declared')
  base=z.read('acbric_vanilla/images/'+bundle['name']+'.png')
  for role in ['bump','fragments']:
   png=z.read('acbric_vanilla/images/'+bundle[role]+'.png')
   check(png[16:24]==base[16:24],role+' dimensions align with colour atlas')
 for file in [b[k] for b in bundles for k in ['name','bump','fragments']]:
  png=z.read('acbric_vanilla/images/'+file+'.png');w,h=struct.unpack('>II',png[16:24])
  check(png[:8]==b'\x89PNG\r\n\x1a\n' and w==h and w&(w-1)==0,'square power-of-two native shader image')
 atlas=json.loads(z.read('electric_age/atlas.json'))
 for key,item in atlas.items():
  check(item['x']+4*item['w']<=2048 and item['y']+item['h']*item['states']<=2048,f'{key} states inside atlas')
  check(item['states']==(36 if key=='battery' else 8),f'{key} state count')
summary={'checks':checks,'jarSha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'bytes':jar.stat().st_size}
(p/'build/package-verification.json').write_text(json.dumps(summary,indent=2),encoding='utf-8')
print(json.dumps(summary,indent=2))
