<?php
if($_SERVER["REQUEST_METHOD"] == "POST"){
    require_once 'conexion.php';

    $categoria = $mysql->real_escape_string($_POST["categoria"]);

    $query = "SELECT id_producto, nombre, precio, descripcion FROM PRODUCTOS WHERE tipoProducto = ?";

    $stmt = $mysql->prepare($query);
    $stmt->bind_param("s", $categoria);

    if($stmt->execute()){
        $resultado = $stmt->get_result();
        $productos = array();

        while($row = $resultado->fetch_assoc()){
            $productos[] = $row;
        }
        echo json_encode($productos);
    }else{
        echo json_encode(array(
            'status'=>'error',
            'message'=>'Error al obtener productos' . $mysql->error
        ));
    }
    $stmt->close();
    $mysql->close();
}
?>