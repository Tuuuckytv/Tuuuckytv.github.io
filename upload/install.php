<?php
if (PHP_VERSION_ID < 80200) { http_response_code(503); exit('The Basement requires PHP 8.2 or later. Change the PHP version in your hosting panel.'); }
require __DIR__.'/private/bootstrap.php';
if(installed()){header('Location: '.base_path().'/login.php');exit;}
start_session();$error='';$drivers=class_exists('PDO')?PDO::getAvailableDrivers():[];
$checks=['PHP 8.2 or later'=>PHP_VERSION_ID>=80200,'64-bit PHP for large ROMs'=>PHP_INT_SIZE>=8,'PDO SQLite or MySQL'=>in_array('sqlite',$drivers,true)||in_array('mysql',$drivers,true),'mbstring extension'=>extension_loaded('mbstring'),'Configuration folder writable'=>is_writable(__DIR__.'/private')];
$defaultStorage=dirname($_SERVER['DOCUMENT_ROOT']??__DIR__).'/basement-data-'.substr(hash('sha256',__DIR__),0,12);
if($_SERVER['REQUEST_METHOD']==='POST'){
 check_csrf();$lock=fopen(__DIR__.'/private/install.lock','c');flock($lock,LOCK_EX);
 try {
  if(installed())throw new RuntimeException('Installation is already complete.');
  if(in_array(false,$checks,true))throw new RuntimeException('Resolve the failed checks below before installing.');
  $expected=require __DIR__.'/private/setup-token.php';if(!hash_equals($expected,hash('sha256',trim($_POST['setup_key']??''))))throw new RuntimeException('The installation key is incorrect. Use the key in START-HERE.txt from your ZIP.');
  $owner=(string)($_POST['owner_password']??'');$guest=(string)($_POST['guest_password']??'');
  if(strlen($owner)<10||strlen($guest)<10||strlen($owner)>200||strlen($guest)>200)throw new RuntimeException('Use passwords between 10 and 200 characters.');
  if($owner===$guest)throw new RuntimeException('Use different owner and guest passwords.');
  $storage=rtrim(trim($_POST['storage']??$defaultStorage),'/');if($storage===''||$storage[0]!=='/'||str_contains($storage,"\0"))throw new RuntimeException('Use an absolute folder path for private storage.');
  if(!is_dir($storage)&&!mkdir($storage,0700,true))throw new RuntimeException('Could not create the private storage folder.');
  $storage=realpath($storage);$web=realpath($_SERVER['DOCUMENT_ROOT']??__DIR__);if(!$storage||!$web||$storage===$web||str_starts_with($storage,$web.'/'))throw new RuntimeException('Private storage must be outside public_html / the public document root.');
  foreach(['files','incoming'] as $d)if(!is_dir($storage.'/'.$d)&&!mkdir($storage.'/'.$d,0700))throw new RuntimeException('Could not create storage directories.');
  if(!is_writable($storage)||!is_writable($storage.'/files'))throw new RuntimeException('The private storage folder is not writable.');
  $driver=$_POST['driver']??'sqlite';if(!in_array($driver,$drivers,true)||!in_array($driver,['sqlite','mysql'],true))throw new RuntimeException('Choose an available database driver.');
  $database=['driver'=>$driver];
  if($driver==='mysql'){$host=trim($_POST['db_host']??'localhost');$name=trim($_POST['db_name']??'');$user=trim($_POST['db_user']??'');$password=(string)($_POST['db_password']??'');if(!preg_match('/^[a-zA-Z0-9_.:-]+$/D',$host)||!preg_match('/^[a-zA-Z0-9_]+$/D',$name))throw new RuntimeException('Check the database host and database name.');$database+=['host'=>$host,'name'=>$name,'user'=>$user,'password'=>$password];$pdo=new PDO('mysql:host='.$host.';dbname='.$name.';charset=utf8mb4',$user,$password);}else{$pdo=new PDO('sqlite:'.$storage.'/basement.sqlite');}
  $pdo->setAttribute(PDO::ATTR_ERRMODE,PDO::ERRMODE_EXCEPTION);require __DIR__.'/private/schema.php';install_schema($pdo);
  save_config(['version'=>BASEMENT_VERSION,'storage'=>$storage,'database'=>$database,'owner_hash'=>password_hash($owner,PASSWORD_DEFAULT),'guest_hash'=>password_hash($guest,PASSWORD_DEFAULT),'secret'=>bin2hex(random_bytes(32)),'revision'=>1,'ice_servers'=>[['urls'=>'stun:stun.l.google.com:19302']]]);
  session_regenerate_id(true);$_SESSION['access']='owner';$_SESSION['revision']=1;$_SESSION['identity']=bin2hex(random_bytes(32));flock($lock,LOCK_UN);fclose($lock);header('Location: '.base_path().'/');exit;
 }catch(Throwable $e){error_log('Basement install: '.$e->getMessage());$error=$e instanceof PDOException?'Database connection or setup failed. Check the database details and PHP extensions.':$e->getMessage();}
 flock($lock,LOCK_UN);fclose($lock);
}
page_top('Install your basement');echo '<p>Set up this private hangout on your own hosting. No build commands are needed.</p>';
foreach($checks as $label=>$ok)echo '<div class="check">'.($ok?'✓ ':'✕ ').html($label).'</div>';
if($error)echo '<p class="error">'.html($error).'</p>';
?><form method="post"><input type="hidden" name="csrf" value="<?=html(csrf())?>"><label>Installation key</label><input name="setup_key" required autocomplete="off"><small>Found in START-HERE.txt inside your ZIP. This is only needed once.</small><label>Owner password</label><input type="password" name="owner_password" required minlength="10" maxlength="200" autocomplete="new-password"><label>Guest password</label><input type="password" name="guest_password" required minlength="10" maxlength="200" autocomplete="new-password"><small>Give the guest password to friends. Keep your owner password private.</small><label>Private storage folder</label><input name="storage" value="<?=html($defaultStorage)?>" required><small>Must be outside public_html. The suggested folder is specific to this installation.</small><label>Database</label><select name="driver"><?php foreach(['sqlite'=>'SQLite — automatic, recommended for small groups','mysql'=>'MySQL / MariaDB — enter a database from hPanel'] as $v=>$label)if(in_array($v,$drivers,true))echo '<option value="'.$v.'">'.html($label).'</option>';?></select><fieldset><legend>Only for MySQL / MariaDB</legend><label>Database host</label><input name="db_host" value="localhost"><label>Database name</label><input name="db_name"><label>Database username</label><input name="db_user"><label>Database password</label><input type="password" name="db_password" autocomplete="new-password"></fieldset><button type="submit">Install basement</button></form><?php page_end(); ?>
