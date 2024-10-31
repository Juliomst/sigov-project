<?php
if($_SERVER["REQUEST_METHOD"] == "POST"){
    require_once 'conexion.php';

    $idMesero = $mysql->real_escape_string($_POST["id_mesero"]);
    $noMesa = $mysql->real_escape_string($_POST["noMesa"]);
    $fechaRegistro = $mysql->real_escape_string($_POST["fechaRegistro"]);

    $query = "INSERT INTO ORDENES (fechaRegistro, noMesa, id_mesero) VALUES (?, ?, ?)";

    $stmt = $mysql->prepare($query);
    $stmt->bind_param("sii", $fechaRegistro, $noMesa, $idMesero);

    if($stmt->execute()){
        $idOrden = $mysql->insert_id;
        echo json_encode(array(
            'status'=>'success',
            'message'=>'Orden creada exitosamente',
            'idOrden'=>$idOrden
        ));
    }else{
        echo json_encode(array(
            'status'=>'error',
            'message'=>'Error al crear la orden: ' . $mysql->error
        ));
    }
    $stmt->close();
    $mysql->close();
}