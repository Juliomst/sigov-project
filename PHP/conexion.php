<?php
$mysql = new mysqli("localhost", "root", "1234", "sigovdb");
if($mysql->connect_error){
    die(json_encode(array('status' => 'error', 'message' => 'Error de conexión')));
}
?>