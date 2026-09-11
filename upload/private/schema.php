<?php
function install_schema(PDO $db): void {
 $mysql=$db->getAttribute(PDO::ATTR_DRIVER_NAME)==='mysql';$id=$mysql?'VARCHAR(64)':'TEXT';$small=$mysql?'VARCHAR(180)':'TEXT';$big=$mysql?'LONGTEXT':'TEXT';$auto=$mysql?'BIGINT PRIMARY KEY AUTO_INCREMENT':'INTEGER PRIMARY KEY AUTOINCREMENT';
 $tables=[
 "settings (space $id PRIMARY KEY,value $big NOT NULL)",
 "messages (id $auto,space $id NOT NULL,room $small NOT NULL,sender $id NOT NULL,name $small NOT NULL,body TEXT NOT NULL,at BIGINT NOT NULL)",
 "peers (id $id PRIMARY KEY,space $id NOT NULL,name $small NOT NULL,voice $small NOT NULL,at BIGINT NOT NULL)",
 "signals (id $auto,space $id NOT NULL,sender $id NOT NULL,target $id NOT NULL,body TEXT NOT NULL,at BIGINT NOT NULL)",
 "assets (id $id PRIMARY KEY,space $id NOT NULL,kind $small NOT NULL,filename $small NOT NULL,mime $small NOT NULL,size BIGINT NOT NULL,core $small NOT NULL,created BIGINT NOT NULL)",
 "uploads (id $id PRIMARY KEY,space $id NOT NULL,meta TEXT NOT NULL,uploaded BIGINT NOT NULL,created BIGINT NOT NULL)",
 "room_decor (space $id PRIMARY KEY,value $big NOT NULL)",
 "room_players (id $id PRIMARY KEY,space $id NOT NULL,name $small NOT NULL,x DOUBLE NOT NULL,z DOUBLE NOT NULL,r DOUBLE NOT NULL,seat $id NULL,look TEXT NOT NULL,at BIGINT NOT NULL)",
 "login_attempts (ip $id PRIMARY KEY,count INTEGER NOT NULL,until_at BIGINT NOT NULL)"
 ];
 foreach($tables as $t)$db->exec('CREATE TABLE IF NOT EXISTS '.$t.($mysql?' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin':''));
 foreach(['messages_space_room_id'=>'messages(space,room,id)','peers_space_at'=>'peers(space,at)','signals_target_id'=>'signals(space,target,id)','assets_space_created'=>'assets(space,created)','players_space_at'=>'room_players(space,at)'] as $name=>$target){try{$db->exec('CREATE INDEX '.$name.' ON '.$target);}catch(PDOException $e){if(!str_contains(strtolower($e->getMessage()),'already exists')&&!str_contains(strtolower($e->getMessage()),'duplicate key name'))throw $e;}}
 try{$db->exec('CREATE UNIQUE INDEX players_unique_seat ON room_players(space,seat)');}catch(PDOException $e){if(!str_contains(strtolower($e->getMessage()),'already exists')&&!str_contains(strtolower($e->getMessage()),'duplicate key name'))throw $e;}
}
