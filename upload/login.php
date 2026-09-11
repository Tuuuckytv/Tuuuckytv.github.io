<?php
require __DIR__.'/private/bootstrap.php';if(!installed()){header('Location: '.base_path().'/install.php');exit;}start_session();$error='';
if($_SERVER['REQUEST_METHOD']==='POST'){
 check_csrf();$c=config();$ip=hash_hmac('sha256',$_SERVER['REMOTE_ADDR']??'unknown',$c['secret']);$now=now_ms();$attempt=row('SELECT * FROM login_attempts WHERE ip=?',[$ip]);
 if($attempt&&$attempt['count']>=8&&$attempt['until_at']>$now){$error='Too many attempts. Try again in 15 minutes.';}else{
  $password=(string)($_POST['password']??'');$role=password_verify($password,$c['owner_hash'])?'owner':(password_verify($password,$c['guest_hash'])?'guest':'');
  if($role){session_regenerate_id(true);$_SESSION['access']=$role;$_SESSION['revision']=$c['revision'];$_SESSION['identity']??=bin2hex(random_bytes(32));query('DELETE FROM login_attempts WHERE ip=?',[$ip]);header('Location: '.base_path().'/');exit;}
  put_row('login_attempts',['ip'=>$ip],['count'=>($attempt&&$attempt['until_at']>$now?(int)$attempt['count']:0)+1,'until_at'=>$now+900000]);$error='That password was not recognised.';
 }
}
page_top('Come on downstairs.');if($error)echo '<p class="error">'.html($error).'</p>';?><p>Enter the access password for this basement.</p><form method="post"><input type="hidden" name="csrf" value="<?=html(csrf())?>"><label>Password</label><input type="password" name="password" required autocomplete="current-password"><button type="submit">Enter basement</button></form><?php page_end(); ?>
