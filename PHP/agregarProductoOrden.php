<?php
if($_SERVER["REQUEST_METHOD"] == "POST"){
    require_once 'conexion.php';

    $idOrden = $mysql->real_escape_string($_POST["id_orden"]);
    $idProducto = $mysql->real_escape_string($_POST["id_producto"]);
    $cantidadProducto = $mysql->real_escape_string($_POST["cantidadProducto"]);
    $descripcion = $mysql->real_escape_string($_POST["descripcion"]);

    if(!empty($descripcion)){
        $updateOrdenQuery = "UPDATE ORDENES SET descripcion = CONCAT(IFNULL(descripcion, ''), '\n', ?) WHERE id_orden = ?";
        $stmtOrden = $mysql->prepare($updateOrdenQuery);
        $stmtOrden->bind_param("si", $descripcion, $idOrden);
        $stmtOrden->execute();
    }

    $query = "INSERT INTO ORDEN_PRODUCTO (id_orden, id_producto, cantidadProducto) VALUES (?, ?, ?)";

    $stmt = $mysql->prepare($query);
    $stmt->bind_param("iii", $idOrden, $idProducto, $cantidadProducto);

    if($stmt->execute()){
        echo json_encode(array(
            'status'=>'success',
            'message'=>'Producto agregado exitosamente'
        ));
    }else{
        echo json_encode(array(
            'status' => 'error',
            'message'=>'Error al agregar el producto: ' . $mysql->error
        ));
    }
    $stmt->close();
    $mysql->close();
}