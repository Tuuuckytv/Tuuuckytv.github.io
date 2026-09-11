<?php
declare(strict_types=1);
const BASEMENT_VERSION = '1.0.0';
const CHUNK_BYTES = 8388608;
function base_path(): string { return rtrim(str_replace('\\','/',dirname($_SERVER['SCRIPT_NAME'] ?? '/index.php')), '/.'); }
function secure_request(): bool { return (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') || (($_SERVER['SERVER_PORT'] ?? '') === '443'); }
function start_session(): void {
 if (session_status() === PHP_SESSION_ACTIVE) return;
 session_name('basement_' . substr(hash('sha256', dirname(__DIR__)), 0, 12));
 session_set_cookie_params(['lifetime'=>0,'path'=>base_path().'/', 'secure'=>secure_request(),'httponly'=>true,'samesite'=>'Lax']);
 ini_set('session.use_strict_mode','1'); session_start();
}
function config_path(): string {return __DIR__.'/config.php';}
function config(): array {static $c; if ($c === null) {$c=is_file(config_path())?require config_path():[];} return $c;}
function save_config(array $c): void {$tmp=config_path().'.'.bin2hex(random_bytes(6)).'.php';if(file_put_contents($tmp,"<?php\nreturn ".var_export($c,true).";\n",LOCK_EX)===false)throw new RuntimeException('Could not write configuration');chmod($tmp,0600);if(!rename($tmp,config_path()))throw new RuntimeException('Could not save configuration');}
function installed(): bool {return is_file(config_path());}
function html(string $s): string {return htmlspecialchars($s,ENT_QUOTES|ENT_SUBSTITUTE,'UTF-8');}
function now_ms(): int {return (int)round(microtime(true)*1000);}
function cut($s,int $max): string {return mb_substr((string)$s,0,$max,'UTF-8');}
function json_out(array $d,int $status=200): never {http_response_code($status);header('Content-Type: application/json; charset=utf-8');header('Cache-Control: no-store');header('X-Content-Type-Options: nosniff');echo json_encode($d,JSON_UNESCAPED_SLASHES|JSON_INVALID_UTF8_SUBSTITUTE);exit;}
function fail(string $s,int $code=400): never {json_out(['error'=>$s],$code);}
function csrf(): string {start_session();return $_SESSION['csrf']??=bin2hex(random_bytes(32));}
function check_csrf(): void {start_session();if(!hash_equals($_SESSION['csrf']??'',(string)($_POST['csrf']??'')) || empty($_SESSION['csrf'])){http_response_code(403);exit('Please reload the page and try again.');}}
function auth(bool $api=false): array {if(!installed()){if($api)fail('Run the installer first.',503);header('Location: '.base_path().'/install.php');exit;} start_session();$c=config();if(empty($_SESSION['access'])||($_SESSION['revision']??0)!==($c['revision']??1)){if($api){session_write_close();fail('Your access session ended. Refresh and sign in again.',401);}header('Location: '.base_path().'/login.php');exit;}$_SESSION['identity']??=bin2hex(random_bytes(32));$a=['id'=>hash('sha256',$_SESSION['identity']),'role'=>$_SESSION['access']];session_write_close();return $a;}
function api_context(): array {$a=auth(true);$key=$_SERVER['HTTP_X_ROOM_KEY']??'';if(!preg_match('/^[a-f0-9]{64}$/D',$key))fail('Open a valid room link.',400);if(($_SERVER['HTTP_SEC_FETCH_SITE']??'')==='cross-site')fail('Invalid request origin.',403);$origin=$_SERVER['HTTP_ORIGIN']??'';if($origin!==''){$host=strtolower(parse_url($origin,PHP_URL_HOST)??'');$port=parse_url($origin,PHP_URL_PORT);$actual=strtolower($_SERVER['HTTP_HOST']??'');if(($host.($port?':'.$port:''))!==$actual)fail('Invalid request origin.',403);}return [$a['id'],hash('sha256',$key)];}
function body_json(int $max=20000): array {$h=fopen('php://input','rb');$raw=stream_get_contents($h,$max+1);fclose($h);if(strlen($raw)>$max)fail('Too much data.',413);$d=json_decode($raw,true);if(!is_array($d))fail('Invalid request.');return $d;}
function db(): PDO {static $db;if($db)return $db;$c=config();if(($c['database']['driver']??'sqlite')==='mysql'){$d=$c['database'];$db=new PDO('mysql:host='.$d['host'].';dbname='.$d['name'].';charset=utf8mb4',$d['user'],$d['password']);}else{$db=new PDO('sqlite:'.$c['storage'].'/basement.sqlite');$db->exec('PRAGMA journal_mode=WAL');$db->exec('PRAGMA busy_timeout=5000');}$db->setAttribute(PDO::ATTR_ERRMODE,PDO::ERRMODE_EXCEPTION);$db->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE,PDO::FETCH_ASSOC);return $db;}
function query(string $sql,array $values=[]): PDOStatement {$q=db()->prepare($sql);$q->execute($values);return $q;}
function row(string $sql,array $v=[]): ?array {$r=query($sql,$v)->fetch();return $r===false?null:$r;}
function rows(string $sql,array $v=[]): array {return query($sql,$v)->fetchAll();}
function put_row(string $table,array $keys,array $values): void {$all=$keys+$values;$cols=array_keys($all);$sql='INSERT INTO '.$table.' ('.implode(',',$cols).') VALUES ('.implode(',',array_fill(0,count($cols),'?')).')';$updates=array_map(fn($k)=>$k.'='.(db()->getAttribute(PDO::ATTR_DRIVER_NAME)==='mysql'?'VALUES('.$k.')':'excluded.'.$k),array_keys($values));$sql.=db()->getAttribute(PDO::ATTR_DRIVER_NAME)==='mysql'?' ON DUPLICATE KEY UPDATE '.implode(',',$updates):' ON CONFLICT('.implode(',',array_keys($keys)).') DO UPDATE SET '.implode(',',$updates);query($sql,array_values($all));}
function uuid(): string {$h=bin2hex(random_bytes(16));return substr($h,0,8).'-'.substr($h,8,4).'-4'.substr($h,13,3).'-a'.substr($h,17,3).'-'.substr($h,20);}
function valid_id(string $id): bool {return (bool)preg_match('/^[a-f0-9-]{36}$/D',$id);}
function storage_file(string $id,bool $part=false): string {if(!valid_id($id))fail('Invalid file ID.');return config()['storage'].'/'.($part?'incoming/':'files/').$id.($part?'.part':'.blob');}
function colour($v): bool {return is_string($v)&&(bool)preg_match('/^#[a-f0-9]{6}$/iD',$v);}
function file_limit(string $kind): int {return $kind==='rom'?17179869184:(in_array($kind,['art','book','bios'],true)?33554432:1073741824);}
function classify(string $filename,string $kind): string {$ext=strtolower(pathinfo($filename,PATHINFO_EXTENSION));$allowed=['media'=>['mp4','webm','mp3','ogg','wav','m4a'],'book'=>['pdf','txt'],'art'=>['jpg','jpeg','png','webp','gif'],'rom'=>['nes','sfc','smc','gb','gbc','gba','md','gen','bin','zip','7z','n64','z64','v64','iso','chd','pbp'],'bios'=>['bin','rom','zip']];if(!in_array($ext,$allowed[$kind]??[],true))fail('This file type is not supported here.');$mime=['mp4'=>'video/mp4','webm'=>'video/webm','mp3'=>'audio/mpeg','ogg'=>'audio/ogg','wav'=>'audio/wav','m4a'=>'audio/mp4','pdf'=>'application/pdf','txt'=>'text/plain','jpg'=>'image/jpeg','jpeg'=>'image/jpeg','png'=>'image/png','webp'=>'image/webp','gif'=>'image/gif'];return $mime[$ext]??'application/octet-stream';}
function page_top(string $title): void {header('Content-Type: text/html; charset=utf-8');header('X-Content-Type-Options: nosniff');header('X-Frame-Options: SAMEORIGIN');echo '<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>'.html($title).' · The Basement</title><style>*{box-sizing:border-box}body{margin:0;background:#211c2b;color:#e7dbea;font:16px/1.6 monospace;padding:40px 20px}main{max-width:650px;margin:auto;background:#302638;padding:30px;border:2px solid #9779a7;box-shadow:7px 7px #100c16}h1{font-size:27px}label{display:block;margin:18px 0 6px}input,select{width:100%;padding:11px;background:#1d1725;color:#eee;border:1px solid #836e95;font:16px monospace}button,.button{display:inline-block;margin-top:20px;padding:12px 20px;background:#bc9ed0;color:#24172e;font:bold 16px monospace;border:2px solid #e1c2f0;cursor:pointer}small{color:#bda8c9}.error{padding:12px;background:#663943;color:#ffd9df}a{color:#dabaf0}.check{padding:7px 0;border-bottom:1px solid #5d4968}code{overflow-wrap:anywhere}fieldset{border:1px solid #765988;margin-top:20px}li{margin:8px 0}</style><main><h1>'.html($title).'</h1>';}
function page_end(): void {echo '</main></html>';}
