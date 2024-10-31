<?php
if($_SERVER["REQUEST_METHOD"] == "POST"){
    require_once 'conexion.php';

    $idOrden = $mysql->real_escape_string($_POST["idOrden"]);

    $query = "SELECT COUNT(*) AS count FROM ORDEN_PRODUCTO WHERE id_orden = ?";

    $stmt = $mysql->prepare($query);
    $stmt->bind_param("i", $idOrden);
    $stmt->execute();
    $resultado = $stmt->get_result();
    $row = $resultado->fetch_assoc();

    echo json_encode(array(
        'status'=>'success',
        'hasProducts'=>$row['count'] > 0
    ));
    $stmt->close();
    $mysql->close();
}
?>