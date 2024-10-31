<?php
if($_SERVER["REQUEST_METHOD"] == "POST"){
    require_once 'conexion.php';

    $idOrden = $mysql->real_escape_string($_POST["idOrden"]);

    //primero eliminar registros relacionados en ORDEN_PRODUCTOS si existen:
    $query = "DELETE FROM ORDEN_PRODUCTO WHERE id_orden = ?";
    $stmt = $mysql->prepare($query);
    $stmt->bind_param("i", $idOrden);
    $stmt->execute();

    //luego se elimina la orden:
    $query = "DELETE FROM ORDENES WHERE id_orden = ?";
    $stmt = $mysql->prepare($query);
    $stmt->bind_param("i", $idOrden);

    if($stmt->execute()){
        echo json_encode(array(
            'status'=>'success',
            'message'=>'Orden eliminada exitosamente'
        ));
    }else{
        echo json_encode(array(
            'status'=>'error',
            'message'=>'Error al eliminar la orden: ' . $mysql->error
        ));
    }
    $stmt->close();
    $mysql->close();
}
?>