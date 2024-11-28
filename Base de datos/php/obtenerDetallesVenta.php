<?php

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require_once 'conexion.php';

    $idVenta = $mysql->real_escape_string($_POST["idVenta"]);

    $queryVenta = "SELECT v.*, o.noMesa, o.Descripcion
                FROM VENTAS v
                JOIN ORDENES o ON v.id_orden = o.id_orden
                WHERE v.id_venta = ?";

    $stmtVenta = $mysql->prepare($queryVenta);
    $stmtVenta->bind_param("i", $idVenta);
    $stmtVenta->execute();
    $resultadoVenta = $stmtVenta->get_result();

    if ($ventaData = $resultadoVenta->fetch_assoc()) {
        $queryOrden = "SELECT o.* FROM ORDENES o
                    JOIN VENTAS v ON o.id_orden = v.id_orden
                    WHERE v.id_venta = ?";
        $stmtOrden = $mysql->prepare($queryOrden);
        $stmtOrden->bind_param("i", $idVenta);
        $stmtOrden->execute();
        $ordenData = $stmtOrden->get_result()->fetch_assoc();

        $queryProductos = "SELECT p.nombre, p.precio, op.cantidadProducto
                        FROM ORDEN_PRODUCTO op
                        JOIN PRODUCTOS p ON op.id_producto = p.id_producto
                        JOIN ORDENES o ON op.id_orden = o.id_orden
                        JOIN VENTAS v ON o.id_orden = v.id_orden
                        WHERE v.id_venta = ?";
        $stmtProductos = $mysql->prepare($queryProductos);
        $stmtProductos->bind_param("i", $idVenta);
        $stmtProductos->execute();
        $resultadoProductos = $stmtProductos->get_result();

        $productos = array();
        while ($producto = $resultadoProductos->fetch_assoc()) {
            $productos[] = $producto;
        }

        
        $queryPago = "SELECT p.*, t.banca as banco
                                      FROM PAGOS p
                                      LEFT JOIN TARJETAS t ON p.id_pago = t.id_pago
                                      WHERE p.id_venta = ?";

        $stmtPago = $mysql->prepare($queryPago);
        $stmtPago->bind_param("i", $idVenta);
        $stmtPago->execute();
        $pagoData = $stmtPago->get_result()->fetch_assoc();

        $metodoPago = array(
            'metodoPago' => $pagoData['banco'] ? 'tarjeta' : 'efectivo'
        );

        if ($pagoData['banco']) {
            $metodoPago['banco'] = $pagoData['banco'];
        }

        echo json_encode(array(
            'status' => 'success',
            'venta' => $ventaData,
            'orden' => $ordenData,
            'productos' => $productos,
            'pago' => $metodoPago
        ));
    } else {
        echo json_encode(array(
            'status' => 'error',
            'message' => 'Venta no encontrada'
        ));
    }

    $mysql->close();
}
