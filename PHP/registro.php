<?php
if($_SERVER["REQUEST_METHOD"] == "POST"){
    require_once 'conexion.php';

    //validar que todos los campos estén presentes y no vacíos:
    $campos_requeridos = ['nombres', 'apellido', 'nickname', 'contrasena'];
    foreach($campos_requeridos as $campo){
        if(!isset($_POST[$campo]) || trim($_POST[$campo]) === ''){
            echo json_encode(array(
                'status'=>'error',
                'message'=>'Todos los campos son obligatorios'
            ));
            exit();
        }
    }

    $nombres = $mysql->real_escape_string(trim($_POST["nombres"]));
    $apellido = $mysql->real_escape_string(trim($_POST["apellido"]));
    $nickname = $mysql->real_escape_string(trim($_POST["nickname"]));
    $contrasena = $mysql->real_escape_string(trim($_POST["contrasena"]));

    //validar la longitud mínima del nickname y contraseña:
    if(strlen($nickname) < 4){
        echo json_encode(array(
            'status'=>'error',
            'message'=>'El nombre de usuario debe tener al menos 4 caracteres'
        ));
        exit();
    }
    if(strlen($contrasena < 4)){
        echo json_encode(array(
            'status'=>'error',
            'message'=>'La contraseña debe tener al menos 4 caracteres'
        ));
        exit();
    }

    //verificar si el nickname ya existe:
    $checkQuery = "SELECT id_mesero FROM MESEROS WHERE nickname = ?";
    $checkStmt = $mysql->prepare($checkQuery);
    $checkStmt->bind_param("s", $nickname);
    $checkStmt->execute();

    if($checkStmt->get_result()->num_rows > 0){
        echo json_encode(array(
            'status'=>'error',
            'message'=>'El nombre de usuario ya está en uso'
        ));
        exit();
    }

    //Si se pasan todas las validaciones, se procede al registro:
    $query = "INSERT INTO MESEROS (nombres, apellido, nickname, contrasena)
            VALUES (?, ?, ?, ?)";

    $stmt = $mysql->prepare($query);
    $stmt->bind_param("ssss", $nombres, $apellido, $nickname, $contrasena);

    if($stmt->execute()){
        echo json_encode(array(
            'status'=>'success',
            'message'=>'Usuario registrado exitosamente'
        ));
    }else{
        echo json_encode(array(
            'status'=>'error',
            'message'=>'Error al registrar usuario'
        ));
    }
    $stmt->close();
    $mysql->close();
}
?>