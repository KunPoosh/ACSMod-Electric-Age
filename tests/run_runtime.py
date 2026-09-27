"""隔离复制游戏依赖后在真实 Fabric/Knot 内测试；不写入游戏输入或玩家目录。"""
from pathlib import Path
import argparse,subprocess,os,shutil,json,zipfile,re,hashlib
p=Path(__file__).resolve().parents[1]
a=argparse.ArgumentParser(description=__doc__)
a.add_argument('--tag',required=True)
a.add_argument('--game',type=Path,required=True)
a.add_argument('--framework',type=Path,required=True)
a.add_argument('--java-home',type=Path,required=True)
args=a.parse_args()
if not re.fullmatch(r'[A-Za-z0-9_-]+',args.tag):a.error('Invalid run tag')
run=p/'build/runtime-tests'/args.tag
if run.exists():a.error('Use a fresh run tag')
game=args.game.resolve();framework=args.framework.resolve();jdk=args.java_home.resolve()/'bin'
jar=p/'build/libs/Electric-Age-0.1.0-dev.4.jar'
api=framework/'build/external-dist/Acbric/core/acbric-api.jar'
loader=framework/'build/external-dist/Acbric/loader-libs'
provider=framework/'build/libs/Acbric-1.0-SNAPSHOT.jar'
inputs=[jar,api,provider,jdk/'java.exe',jdk/'javac.exe',game/'asplit-A.zip',game/'asplit-B.zip']
for f in inputs:
 if not f.is_file():a.error(f'Missing input: {f}')
if run.resolve().is_relative_to(game):a.error('Output overlaps game')
for d in ['libs','loader-libs','game/mods','game/data','userdata','home','appdata','classes']:(run/d).mkdir(parents=True,exist_ok=True)
for f in list((game/'lib').glob('*.jar'))+[game/'asplit-A.zip',game/'asplit-B.zip']:shutil.copy2(f,run/'libs'/f.name)
for f in loader.glob('*.jar'):shutil.copy2(f,run/'loader-libs'/f.name)
shutil.copy2(provider,run/'loader-libs'/provider.name)
shutil.copy2(api,run/'game/mods/acbric-api.jar');shutil.copy2(jar,run/'game/mods/electric-age.jar')
for name in ['fontmetrics','lang']:shutil.copytree(game/'data'/name,run/'game/data'/name)
cp=';'.join(str(run/n) for n in ['libs/*','loader-libs/*','libs/asplit-A.zip','libs/asplit-B.zip','game/mods/*'])
subprocess.run([str(jdk/'javac.exe'),'-encoding','UTF-8','-proc:none','-cp',cp,'-d',str(run/'classes'),str(p/'tests/RuntimeProbe.java')],check=True)
with zipfile.ZipFile(run/'libs/electric-probe.jar','w',zipfile.ZIP_DEFLATED) as z:
 for f in (run/'classes').rglob('*.class'):z.write(f,f.relative_to(run/'classes').as_posix())
(run/'game/Airships.json').write_text(json.dumps({'mainClass':'regression.RuntimeProbe','classPath':['asplit-A.zip','asplit-B.zip']}),encoding='utf-8')
(run/'game/launch_settings.json').write_text(json.dumps({'customDataDirectoryLocation':str(run/'userdata')}),encoding='utf-8')
cmd=[str(jdk/'java.exe'),'-Dsteam=false','-Ddev=true','-Djava.awt.headless=true','-Dfile.encoding=UTF-8','-Dstdout.encoding=UTF-8','-Dstderr.encoding=UTF-8',f'-Duser.home={run/"home"}','--add-opens=java.base/java.util=ALL-UNNAMED','-cp',f'{run/"libs"}/*;{run/"loader-libs"}/*','net.fabricmc.loader.impl.launch.knot.KnotClient']
with (run/'runtime.log').open('w',encoding='utf-8') as out:r=subprocess.run(cmd,cwd=run/'game',env=dict(os.environ,APPDATA=str(run/'appdata')),stdout=out,stderr=subprocess.STDOUT,timeout=60)
log=(run/'runtime.log').read_text(encoding='utf-8');match=re.search(r'ELECTRIC RUNTIME PASS: (\d+) checks',log)
if r.returncode or match is None:raise RuntimeError(f'{run/"runtime.log"}\n{log[-9000:]}')
summary={'checks':int(match.group(1)),'scope':'isolated real Knot, synthetic modules; no GPU/full combat/campaign/multiplayer','inputs':{str(f):hashlib.sha256(f.read_bytes()).hexdigest() for f in inputs if f.suffix in ('.jar','.zip')}}
(run/'summary.json').write_text(json.dumps(summary,indent=2),encoding='utf-8')
print(json.dumps(summary,indent=2))
