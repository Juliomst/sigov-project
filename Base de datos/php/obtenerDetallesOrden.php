<?php
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require_once 'conexion.php';
    
    $idOrden = $mysql->real_escape_string($_POST["idOrden"]);
    
    // Obtiene la información de la orden
    $queryOrden = "SELECT noMesa, fechaRegistro, descripcion FROM ORDENES WHERE id_orden = ?";
    $stmtOrden = $mysql->prepare($queryOrden);
    $stmtOrden->bind_param("i", $idOrden);
    $stmtOrden->execute();
    $resultadoOrden = $stmtOrden->get_result();
    
    if ($ordenData = $resultadoOrden->fetch_assoc()) {
        // Obtiene los productos de la orden
        $queryProductos = "SELECT p.nombre, p.precio, op.cantidadProducto 
                          FROM ORDEN_PRODUCTO op 
                          JOIN PRODUCTOS p ON op.id_producto = p.id_producto 
                          WHERE op.id_orden = ?";
        
        $stmtProductos = $mysql->prepare($queryProductos);
        $stmtProductos->bind_param("i", $idOrden);
        $stmtProductos->execute();
        $resultadoProductos = $stmtProductos->get_result();
        
        $productos = array();
        while ($producto = $resultadoProductos->fetch_assoc()) {
            $productos[] = $producto;
        }
        
        echo json_encode(array(
            'status' => 'success',
            'orden' => $ordenData,
            'productos' => $productos
        ));
    } else {
        echo json_encode(array(
            'status' => 'error',
            'message' => 'Orden no encontrada'
        ));
    }
    
    $mysql->close();
}
?>