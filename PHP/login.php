<?php
if($_SERVER["REQUEST_METHOD"] == "POST"){
    require_once 'conexion.php';

    $nickname = $mysql->real_escape_string($_POST["nickname"]);
    $contrasena = $mysql->real_escape_string($_POST["contrasena"]);

    $query = "SELECT id_mesero, nombres, apellido FROM MESEROS
            WHERE nickname = ? AND contrasena = ?";

    $stmt = $mysql->prepare($query);
    $stmt->bind_param("ss", $nickname, $contrasena);
    $stmt->execute();
    $resultado = $stmt->get_result();

    if($resultado->num_rows > 0){
        $usuario = $resultado->fetch_assoc();
        echo json_encode(array(
            'status'=>'success',
            'data'=> $usuario
        ));
    }else{
        echo json_encode(array(
            'status'=>'error',
            'message'=>'Usuario o contraseña incorrectos'
        ));
    }
    $stmt->close();
    $mysql->close();
}
?>