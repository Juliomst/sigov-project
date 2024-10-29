<?php
if($_SERVER["REQUEST_METHOD"] == "POST"){
    require_once 'conexion.php';

    $tipoProducto = $mysql->real_escape_string($_POST["tipoProducto"]);

    $query = "SELECT id_producto, nombre, precio, descripcion
            FROM PRODUCTOS
            WHERE tipoProducto = ?";

    $stmt = $mysql->prepare($query);
    $stmt->bind_param("s", $tipoProducto);
    $stmt->execute();
    $resultado = $stmt->get_result();

    $productos = array();
    while($row = $resultado->fetch_assoc()){
        $productos[] = $row;
    }

    if(count($productos) > 0){
        echo json_encode(array(
            'status'=>'success',
            'data'=>$productos
        ));
    }else{
        echo json_encode(array(
            'status'=>'error',
            'message'=>'No se encontraron productos para esta categoría'
        ));
    }
    $stmt->close();
    $mysql->close();
}
?>