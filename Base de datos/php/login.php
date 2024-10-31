<?php
if($_SERVER["REQUEST_METHOD"] == "POST"){
    require_once 'conexion.php';

    $nickname = $mysql->real_escape_string($_POST["nickname"]);
    $contrasena = $mysql->real_escape_string($_POST["contrasena"]);

    //Verificación de la existencia del usuario:
    $checkQuery = "SELECT id_mesero FROM MESEROS WHERE nickname = ?";
    $checkStmt = $mysql->prepare($checkQuery);
    $checkStmt->bind_param("s", $nickname);
    $checkStmt->execute();
    $checkResult = $checkStmt->get_result();

    if($checkResult->num_rows == 0){
        echo json_encode(array(
            'status'=>'error',
            'message'=>'El usuario no existe'
        ));
        exit();
    }

    //Si el usuario existe, validación de contraseña:
    $query = "SELECT id_mesero, nombres, apellido, nickname FROM MESEROS
            WHERE nickname = ? AND contrasena = ?";

    $stmt = $mysql->prepare($query);
    $stmt->bind_param("ss", $nickname, $contrasena);
    $stmt->execute();
    $resultado = $stmt->get_result();

    if($resultado->num_rows > 0){
        $usuario = $resultado->fetch_assoc();
        $response = array(
            'status'=>'success',
            'message'=>'Login exitoso',
            'data' => array(
                'id_mesero'=>$usuario['id_mesero'],
                'nombres'=>$usuario['nombres'],
                'apellido'=>$usuario['apellido']
                )
            );
            echo json_encode($response);
    }else{
        echo json_encode(array(
            'status'=>'error',
            'message'=>'Contraseña incorrecta'
        ));
    }
    $stmt->close();
    $mysql->close();
}
?>