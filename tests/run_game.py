"""Real isolated external Main/GPU probe, with source installation hashes and write guard."""
from pathlib import Path
import argparse, subprocess, os, shutil, zipfile, json, hashlib, re, sys
p=Path(__file__).resolve().parents[1]
a=argparse.ArgumentParser(description=__doc__)
for name in ['game','framework','java-home']:a.add_argument('--'+name,type=Path,required=True)
a.add_argument('--tag',required=True)
a.add_argument('--language',choices=['chi','en'],default='chi')
args=a.parse_args()
if not re.fullmatch(r'[A-Za-z0-9_-]+',args.tag):a.error('Invalid tag')
run=p/'build/game-tests'/args.tag
if run.exists():a.error('Fresh tag required')
game=args.game.resolve();fw=args.framework.resolve();jdk=args.java_home.resolve()/'bin'
sys.path.insert(0,str(fw/'tools'))
from test_external_install import snapshot
loaderSource=fw/'build/preflight/loader-libs';api=fw/'build/libs/Acbric-1.0-SNAPSHOT-api-mod.jar'
product=p/'build/libs/Electric-Age-0.1.0-dev.4.jar'
for d in ['cwd','appdata','localappdata','home','tmp','classes']:(run/d).mkdir(parents=True,exist_ok=True)
loader=run/'loader-libs';shutil.copytree(loaderSource,loader)
instance=run/'instance'
env=dict(os.environ,APPDATA=str(run/'appdata'),LOCALAPPDATA=str(run/'localappdata'),USERPROFILE=str(run/'home'),TEMP=str(run/'tmp'),TMP=str(run/'tmp'))
base=[str(jdk/'java.exe'),'-Xmx2G','-Dfile.encoding=UTF-8','-Dstdout.encoding=UTF-8','-Dstderr.encoding=UTF-8',f'-Djava.io.tmpdir={run/"tmp"}',f'-Duser.home={run/"home"}']
startup=subprocess.STARTUPINFO();startup.dwFlags|=subprocess.STARTF_USESHOWWINDOW;startup.wShowWindow=subprocess.SW_HIDE
before=snapshot(game);summary={'status':'FAILED','jarSha256':hashlib.sha256(product.read_bytes()).hexdigest(),'loaderSha256':hashlib.sha256((loader/'Acbric-1.0-SNAPSHOT.jar').read_bytes()).hexdigest()}
try:
 with (run/'preflight.log').open('w',encoding='utf-8') as out:
  subprocess.run(base+['-cp',str(loader/'*'),'net.fabricacs.acbric.ExternalPreflight','--game-dir',str(game),'--instance-dir',str(instance)],cwd=run/'cwd',env=env,stdout=out,stderr=subprocess.STDOUT,check=True,timeout=60,startupinfo=startup)
 (instance/'config/launch-settings.json').write_text(json.dumps({'useCustomWindow':True,'customWindowW':1280,'customWindowH':800,'customWindowFullscreen':False,'customWindowFullscreenWindow':False,'customWindowBorderless':False}),encoding='utf-8')
 (instance/'userdata/prefs.json').write_text(json.dumps({'mod_enabled_electric_age':True,'language':args.language,'phoneHomeAsked':True,'phoneHomeWithErrors':False}),encoding='utf-8')
 (instance/'mods').mkdir(exist_ok=True)
 shutil.copy2(api,instance/'mods/acbric-api.jar');shutil.copy2(product,instance/'mods/electric-age.jar')
 summary['apiSha256']=hashlib.sha256((instance/'mods/acbric-api.jar').read_bytes()).hexdigest()
 summary['language']=args.language
 with zipfile.ZipFile(instance/'mods/acbric-api.jar') as z:summary['apiVersion']=json.loads(z.read('fabric.mod.json'))['version']
 guard=fw/'build/classes/java/regressionTest/net/fabricacs/regression/SmokeGuard.class'
 with zipfile.ZipFile(run/'guard.jar','w') as z:z.write(guard,'net/fabricacs/regression/SmokeGuard.class')
 cp=';'.join(map(str,[loader/'*',game/'lib/*',game/'asplit-A.zip',game/'asplit-B.zip',instance/'mods/*']))
 subprocess.run([str(jdk/'javac.exe'),'-encoding','UTF-8','-proc:none','-cp',cp,'-d',str(run/'classes'),str(p/'tests/game/ExternalRuntimeProbe.java'),str(p/'tests/game/GameFixture.java')],check=True)
 with zipfile.ZipFile(instance/'mods/electric-probe.jar','w',zipfile.ZIP_DEFLATED) as z:
  for f in (run/'classes').rglob('*.class'):z.write(f,f.relative_to(run/'classes').as_posix())
  z.writestr('fabric.mod.json',json.dumps({'schemaVersion':1,'id':'electric_probe','version':'1','mixins':['electric-probe.mixins.json']}))
  z.writestr('electric-probe.mixins.json',json.dumps({'required':True,'package':'net.fabricacs.regression.fixtures','compatibilityLevel':'JAVA_21','mixins':['GameFixture$Menu','GameFixture$Tick','GameFixture$Battle'],'injectors':{'defaultRequire':1}}))
 cmd=base+[f'-Dacbric.external.install={game}',f'-Dacbric.external.instance={instance}','-Dacbric.internal.externalProbeMain=net.fabricacs.regression.ExternalRuntimeProbe','-Dorg.lwjgl.util.Debug=false',f'-Dacbric.test.root={run}','-Djava.security.manager=net.fabricacs.regression.SmokeGuard','--add-opens=java.base/java.util=ALL-UNNAMED','-cp',str(loader/'*')+';'+str(run/'guard.jar'),'net.fabricmc.loader.impl.launch.knot.KnotClient']
 with (run/'game.log').open('w',encoding='utf-8') as out:r=subprocess.run(cmd,cwd=run/'cwd',env=env,stdout=out,stderr=subprocess.STDOUT,timeout=420,startupinfo=startup)
 log=(run/'game.log').read_text(encoding='utf-8')
 if (instance/'userdata/log.txt').is_file():log+='\n'+(instance/'userdata/log.txt').read_text(encoding='utf-8',errors='replace')
 if r.returncode or not (instance/'electric-checkpoint.json').is_file() or 'SMOKE_WRITE_DENIED' in log:raise RuntimeError(log[-9000:])
 summary.update(json.loads((instance/'electric-checkpoint.json').read_text(encoding='utf-8')))
 summary['status']='PASS_WITH_NATIVE_GL_ERRORS' if summary['glErrors'] else 'PASS'
finally:
 after=snapshot(game);changes={'added':sorted(after.keys()-before.keys()),'removed':sorted(before.keys()-after.keys()),'modified':sorted(k for k in before.keys()&after.keys() if before[k]!=after[k])}
 summary.update(installationFileCount=len(before),installationChanges=changes)
 if any(changes.values()):summary['status']='FAILED_INSTALL_CHANGED'
 (run/'summary.json').write_text(json.dumps(summary,indent=2,ensure_ascii=False),encoding='utf-8')
 if any(changes.values()):raise AssertionError(changes)
print(json.dumps(summary,indent=2,ensure_ascii=False))
