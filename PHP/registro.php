<?php
if($_SERVER["REQUEST_METHOD"] == "POST"){
    require_once 'conexion.php';

    $nombres = $mysql->real_escape_string($_POST["nombres"]);
    $apellido = $mysql->real_escape_string($_POST["apellido"]);
    $nickname = $mysql->real_escape_string($_POST["nickname"]);
    $contrasena = $mysql->real_escape_string($_POST["contrasena"]);

    $checkQuery = "SELECT id_mesero FROM MESEROS WHERE nickname = ?";
    $checkStmt = $mysql->prepare($checkQuery);
    $checkStmt->bind_param("s", $nickname);
    $checkStmt->execute();

    if($checkStmt->get_result()->num_rows > 0){
        echo json_encode(array(
            'status'=>'error',
            'message'=>'El nombre de usuario ya existe'
        ));
        exit();
    }

    $query = "INSERT INTO MESEROS (nombres, apellido, nickname, contrasena)
            VALUES (?, ?, ?, ?)";

    $stmt = $mysql->prepare($query);
    $stmt->bind_param("ssss", $nombres, $apellido, $nickname, $contrasena);

    if($stmt->execute()){
        echo json_encode(array(
            'status'=>'success',
            'message'=>'Error al registrar usuario'
        ));
    }
    $stmt->close();
    $mysql->close();
}
?>